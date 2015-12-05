package server;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import message.GetResponse;
import message.Join;
import message.JoinResponse;
import message.AcceptAntiEntropy;
import message.Commit;
import message.Delete;
import message.ElectPrimary;
import message.Get;
import message.Message;
import message.Put;
import message.ReadRequest;
import message.Retire;
import message.StartAntiEntropy;
import message.Write;
import message.WriteRequest;
import message.WriteResponse;
import socketFramework.NetController;

/**
 * Abstraction for a Bayou server.
 * @author tsm
 *
 */
public class Server implements Runnable {

	// Server's ID.
	private ServerID ID;
	
	// Lamport logical clock for accept stamps.
	private int logical;
	
	// Write Log.
	private WriteLog DB;
	
	// Music Playlist.
	private Playlist playlist;
	
	// Denotes whether this server is the primary, responsible for assigning commit sequence numbers.
	private boolean isPrimary;
	
	// Net Controller used to communicate with other processes.
	private NetController network;
	
	// Version Vector.
	private VersionVector V;
	
	// Highest Commit Sequence Number this server knows about.
	private int CSN;
	
	// Scheduled time for next anti-entropy exchange.
	private long nextAE;
	
	// Period between anti-entropy exchanges in MS.
	public static final int ANTI_ENTROPY_PERIOD = 400;
	
	// Random number generator.
	private Random random;
	
	// Used for test-case simulation. Pauses all anti-entropy messages. 
	private volatile AtomicBoolean paused;

	
	/**
	 * Constructor.
	 * 
	 * @param isInitialPrimary	True if this server is the initial primary 
	 * 							server. In this case, it will create it's
	 * 							own initial server ID. Otherwise, it will
	 * 							join the system through the Bayou creation 
	 * 							mechanism (i.e., through existing server).
	 * @param netController		Placeholder for net controller. It must
	 * 							allow new server to immediately communicate
	 * 							with all servers in the system. How to?
	 * @param joinThrough		This is the network ID (i.e., not ServerID)
	 * 							of the process this server should attempt
	 * 							to join the system through.
	 */
	public Server(boolean isInitialPrimary, NetController network, int joinThrough)
	{
		this.ID			= null;
		this.V 			= new VersionVector();
		this.CSN 		= 0;
		this.logical	= 0;
		this.DB 		= new WriteLog();
		this.playlist 	= new Playlist(this.DB);
		this.network	= network;
		this.nextAE		= Long.MAX_VALUE;
		this.random 	= new Random();
		this.paused		= new AtomicBoolean(false);
		
		if (isInitialPrimary)
		{
			this.ID = new ServerID();
			this.isPrimary = true;
		}
		else 
		{
			this.isPrimary = false;
			this.network.sendMessageToProcess(joinThrough, new Join());
		}
	}
	
	/**
	 * Pauses this server. No anti-entropy exchanges will be initiated or 
	 * responded to during this period. In fact, the server will send and
	 * receive no messages while paused. Pause is not IMMEDIATE, any messages
	 * received by the server in the current iteration of the loop may leak
	 * through.
	 */
	public synchronized void pause()
	{
		synchronized (this.paused)
		{
			this.paused.set(true);
			this.paused.notify();
		}
	}
	
	public synchronized void start()
	{
		synchronized (this.paused)
		{
			this.paused.set(false);
			this.paused.notify();
		}
	}
	
	public synchronized void printLog()
	{
		synchronized(this.DB)
		{
			this.DB.print();	
		}
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			// Pause functionality for test case simulation.
			synchronized(this.paused)
			{
				try
				{ 
					while (this.paused.get() == true)
					{
						this.paused.wait();
					}
				}
				catch (InterruptedException e) 
				{
			        // No one should interrupt this thread...
			    }
			}
			
			// Check if it's time to initiate a new anti-entropy exchange. For now,
			// this does not use any type of state machine to guide anti-entropy 
			// exchanges. In other words, one begins every 100 milliseconds, regardless
			// of whether one is already in progress or failed. This can certainly be
			// problematic if the period is so low that the messages flood the system.
			if (System.currentTimeMillis() >= this.nextAE)
			{
				this.nextAE = System.currentTimeMillis() + Server.ANTI_ENTROPY_PERIOD;
				
				// Choose target for anti-entropy exchange and initiate.
				ArrayList<Integer> servers = this.network.getAliveServers();
				if (servers.size() > 1)
				{
					int target;
					do
					{
						target = servers.get(random.nextInt(servers.size()));
					}
					while (target != this.network.getID());
					this.network.sendMessageToProcess(target, new StartAntiEntropy(this.ID));
				}
			}
			
			//******************************************************************
			//* PROCESS NETWORK MESSAGES
			//******************************************************************
			for (Map.Entry<Integer, Message> e : this.network.getReceivedMessages())
			{
				int s 		= e.getKey();
				Message m 	= e.getValue();
				
				//******************************************************************
				//* SERVER ID ASSIGNMENT - Do NOTHING until we are assigned an ID.
				//******************************************************************
				if (m instanceof JoinResponse)
				{
					this.ID = ((JoinResponse)m).ID;
					this.nextAE = System.currentTimeMillis() + Server.ANTI_ENTROPY_PERIOD;
				}
				if (this.ID == null)
				{
					continue;
				}
				
				//******************************************************************
				//* CLIENT REQUESTS
				//******************************************************************
				if (m instanceof WriteRequest)
				{
					WriteRequest wr = (WriteRequest)m;
					
					// Writes Follow Reads and Monotonic Writes guarantees.
					if (this.V.dominates(wr.R()) && this.V.dominates(wr.W()))
					{
						// Accept Write.
						Write w = write((Put)m);
						 
						// Update return vector for future session guarantees.
						VersionVector newVector = wr.W();
						newVector.update(this.ID, w.stamp());
						
						// Respond to client.
						this.network.sendMessageToProcess(s, new WriteResponse(true, newVector));
					}
					else
					{
						// Drop Write.
						this.network.sendMessageToProcess(s, new WriteResponse(false, wr.R()));
					}
				}
				
				if (m instanceof ReadRequest)
				{
					ReadRequest rr = (ReadRequest)m;
					
					// Writes Follow Reads and Monotonic Writes guarantees.
					if (this.V.dominates(rr.R()) && this.V.dominates(rr.W()))
					{
						// Accept Read.
						if (m instanceof Get)
						{
							// Update return vector for future session guarantees.
							VersionVector newVector = rr.R();
							this.V.max(newVector);
							
							// Respond to client.
							GetResponse r = get((Get)m);
							r.V = newVector;
							this.network.sendMessageToProcess(s, r);
						}
					}
					else
					{
						// Report error for Read.
						if (m instanceof Get)
						{
							this.network.sendMessageToProcess(s, new GetResponse(((Get)m).songName, "ERR_DEP", false));
						}
					}
				}
				
				//******************************************************************
				//* ANTI-ENTROPY STATE EXCHANGE
				//******************************************************************
				// TODO: Anti-Entropy initiate message.
				
				if (m instanceof StartAntiEntropy)
				{
					StartAntiEntropy SAE = (StartAntiEntropy)m;
					AcceptAntiEntropy r = new AcceptAntiEntropy(SAE.server, this.V, this.CSN);
					this.network.sendMessageToProcess(s, r);
				}
				
				if (m instanceof ElectPrimary)
				{
					this.isPrimary = true;
					
					// Stabilize all local writes by assigning CSNs.
					for (Write w : this.DB.getTentativeWrites())
					{
						w.setCSN(this.assignCSN());
					}
				}
				
				if (m instanceof Put || m instanceof Delete)
				{
					this.DB.add((Write)m);
				}
				
				if (m instanceof Join)
				{
					// Log Write
					Write w = write((Join)m);
					
					// Assign new Server ID to joining process.
					ServerID newServerID = new ServerID(this.ID, w.stamp());
					JoinResponse r = new JoinResponse(newServerID);
					this.network.sendMessageToProcess(s, r);
				}
				
				if (m instanceof Retire)
				{
					// Log Write
					write((Retire)m);
					
					// Decided not to remove retired servers from the Version Vector.
					// This is only an optimization, and one that apparently comes at
					// the cost of buggy session guarantees.
					//this.V.remove(((Retire)m).ID);
				}
			}
		}
	}	
	
	/**
	 * Assigns a CSN and accept-stamp for a write request and logs it.
	 * 
	 * @param wr	
	 * 		The write request.
	 * @return
	 * 		The resultant CSN-assigned, stamped, logged Write.
	 */
	private Write write(WriteRequest wr)
	{
		Write w = new Write(this.ID, this.assignCSN(), this.stamp(), wr);
		this.DB.add(w);
		return w;
	}
	
	/**
	 * Processes a get request.
	 * @param g
	 * 		The Get request
	 * @return
	 * 		Response ready to be returned to sender.
	 */
	private GetResponse get(Get g)
	{
		return new GetResponse(g.songName, this.playlist.get(g.songName), true);
	}
	
	/**
	 * Assigns a commit sequence number on demand. 
	 * 
	 * @return
	 * 		If this server is the primary, it will return and increment the
	 * 		current commit sequence number. If this server is not the primary,
	 * 		this will return infinity.
	 */
	private int assignCSN()
	{
		if (this.isPrimary)
		{
			return this.CSN++;
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Stamps an incoming write. This will automatically increment the logical
	 * clock of this server.
	 * @return
	 */
	private int stamp()
	{
		return this.logical++;
	}
	
	public void antiEntropy(int serverId, VersionVector RV, int RCSN)
	{
		// Propagate committed writes.
		if (RCSN < this.CSN)
		{
			for (Write w : this.DB.getCommittedWrites())
			{
				if (w.stamp() <= RV.getAcceptStamp(w.server()))
				{
					// R has the write, but does not know it is committed.
					Commit c = new Commit(w.server(), w.stamp(), w.CSN());
					this.network.sendMessageToProcess(serverId, c);
				}
				else
				{
					// R does not have the write. Will commit automatically.
					this.network.sendMessageToProcess(serverId, w);
				}
			}
		}
		
		// Propagate tentative writes.
		for (Write w : this.DB.getTentativeWrites())
		{
			if (RV.getAcceptStamp(w.server()) < w.stamp())
			{
				this.network.sendMessageToProcess(serverId, w);
			}
		}
	}
}
