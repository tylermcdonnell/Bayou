package client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import message.GetResponse;
import message.Message;
import message.ReadRequest;
import message.WriteRequest;
import message.WriteResponse;
import server.VersionVector;
import socketFramework.NetController;

/**
 * Abstraction for Bayou client.
 * @author tsm
 *
 */
public class Client implements Runnable {
	
	// The server I talk to for requests.
	private volatile AtomicInteger serverId;
	
	// A queue the Master can issue this client commands on.
	LinkedList<Message> clientReceiveQueue;
	
	// This client's ID.
	private int myClientId;
	
	// This client's network communication.
	private NetController network;
	
	// This client's Read Version Vector.
	// Used for session guarantees.
	private VersionVector R;
	
	// This client's Write Version Vector.
	// Used for session guarantees.
	private VersionVector W;
	
	private volatile AtomicBoolean busy;
	
	public Client(int myClientId, int myServerId, NetController network)
	{
		this.clientReceiveQueue = new LinkedList<Message>();
		
		this.serverId = new AtomicInteger(myServerId);
		this.myClientId = myClientId;
		
		this.R = new VersionVector();
		this.W = new VersionVector();
		
		this.busy = new AtomicBoolean(false);
		
		this.network = network;
	}
	
	public synchronized void connect(int serverId)
	{
		this.serverId.set(serverId);
	}
	@Override
	public void run()
	{
		// Testing.
		//System.out.println("Client " + this.myClientId + " is up!");
		
		while (true)
		{
			//******************************************************************
			//* MASTER MESSAGES
			//******************************************************************
			
			// This will contain the messages received in this iteration
			// from the master.
			ArrayList<Message> masterMessages = getMasterRequests();
			
			// Process messages from master.
			for (Message m : masterMessages)
			{
				if (m instanceof WriteRequest)
				{
					WriteRequest wr = (WriteRequest)m;
					
					//System.out.println("Client " + this.myClientId + " received from master: " + wr.toString() + " " + this.serverId.get());
					
					// Session guarantees.
					wr.setR(this.R);
					wr.setW(this.W);
					
					// Dispatch request to server.
					this.network.sendMessageToProcess(this.serverId.get(), wr);	
				}
				
				if (m instanceof ReadRequest)
				{
					ReadRequest rr = (ReadRequest)m;
					
					//System.out.println("Client " + this.myClientId + " received from master: " + rr.toString());
					
					// Session guarantees.
					rr.setR(this.R);
					rr.setW(this.W);
					
					// Dispatch request to server.
					this.network.sendMessageToProcess(this.serverId.get(), rr);	
				}
			}
			
			//******************************************************************
			//* SERVER MESSAGES
			//******************************************************************
			for (Map.Entry<Integer, Message> e : this.network.getReceivedMessages())
			{
				int s 		= e.getKey();
				Message m 	= e.getValue();
				
				if (m instanceof GetResponse)
				{
					GetResponse r = (GetResponse)m;
					
					if (r.success)
					{
						// Update Read Vector
						this.R = r.V;
					}
					
					System.out.println(r.toString());
					
					// Signal complete to Master.
					synchronized(this.busy)
					{
						this.busy.set(false);;
						this.busy.notifyAll();
					}
				}
				
				if (m instanceof WriteResponse)
				{
					WriteResponse r = (WriteResponse)m;
					
					if (r.success)
					{
						// Update Write Vector
						this.W = r.V;
					}
					
					// Signal complete to Master.
					synchronized(this.busy)
					{
						this.busy.set(false);;
						this.busy.notifyAll();
					}
				}
			}
			
		}
	}
	
	/**
	 * Send a client a command to fulfill.  The master class can use this.
	 * 
	 * @param m
	 * 		The command for the client.
	 */
	public synchronized void giveClientCommand(Message m)
	{
		this.busy.set(true);
		
		synchronized(this.clientReceiveQueue)
		{
			this.clientReceiveQueue.add(m);
		}
	}
	
	public synchronized void waitClient()
	{
		synchronized(this.busy)
		{
			try
			{
				if (this.busy.get() == true)
				{
					this.busy.wait();
				}
			} 
			catch (InterruptedException exc)
			{
				// Nothing.
			}
		}
	}
	
	
	/**
	 * Returns write requests sent from the master at the time this method is
	 * called.
	 * 
	 * @return write requests sent from the master at the time this method is
	 * called.
	 */
	private ArrayList<Message> getMasterRequests()
	{		
		// Continuously listen for client commands from the master.
		synchronized(this.clientReceiveQueue)
		{	
			ArrayList<Message> get = new ArrayList<Message>(this.clientReceiveQueue);
			this.clientReceiveQueue.clear();
			return get;
		}
	}
}
