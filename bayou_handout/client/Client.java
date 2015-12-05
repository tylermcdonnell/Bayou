package client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import message.GetResponse;
import message.Message;
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
	private int myServerId;
	
	// A queue the Master can issue this client commands on.
	LinkedList<WriteRequest> clientReceiveQueue;
	
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
	
	// Net Controller ID of the server this client is currently 
	// communicating with.
	private int currentServer;
	
	public Client(int myClientId, int myServerId, NetController network)
	{
		this.clientReceiveQueue = new LinkedList<WriteRequest>();
		this.myServerId = myServerId;
		this.myClientId = myClientId;
		
		this.R = new VersionVector();
		this.W = new VersionVector();
	}
	
	
	@Override
	public void run()
	{
		System.out.println("Client " + this.myClientId + " is up!");
		
		while (true)
		{
			//******************************************************************
			//* MASTER MESSAGES
			//******************************************************************
			
			// This will contain the messages received in this iteration
			// from the master.
			ArrayList<WriteRequest> masterMessages = getMasterWriteRequests();
			
			// Process messages from master.
			for (WriteRequest wr : masterMessages)
			{
				System.out.println("Client " + this.myClientId + " received from master: " + wr.toString());
				
				// Session guarantees.
				wr.setR(this.R);
				wr.setW(this.W);
				
				// Dispatch request to server.
				this.network.sendMessageToProcess(currentServer, wr);
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
					
					// TODO: Signal complete to Master.
				}
				
				if (m instanceof WriteResponse)
				{
					WriteResponse r = (WriteResponse)m;
					
					if (r.success)
					{
						// Update Write Vector
						this.W = r.V;
					}
					
					// TODO: Signal complete to Master.
				}
			}
			
		}
	}
	
	/**
	 * Send a client a command to fulfill.  The master class can use this.
	 * 
	 * @param object, the Object describing this command.
	 */
	public synchronized void giveClientCommand(Object object)
	{
		WriteRequest writeRequest = null;
		
		if (object instanceof WriteRequest)
		{
			writeRequest = (WriteRequest)object;
		}
		
		synchronized(this.clientReceiveQueue)
		{
			this.clientReceiveQueue.add(writeRequest);
		}
	}
	
	
	/**
	 * Returns write requests sent from the master at the time this method is
	 * called.
	 * 
	 * @return write requests sent from the master at the time this method is
	 * called.
	 */
	private ArrayList<WriteRequest> getMasterWriteRequests()
	{
		ArrayList<WriteRequest> writeRequestsFromMaster = new ArrayList<WriteRequest>();
		
		// Continuously listen for client commands from the master.
		synchronized(this.clientReceiveQueue)
		{	
			for (Iterator<WriteRequest> i = this.clientReceiveQueue.iterator(); i.hasNext();)
			{
				WriteRequest msg = i.next();
				i.remove();
				
				// Process this message outside of the iterator loop.
				writeRequestsFromMaster.add(msg);
			}
		}
		
		return writeRequestsFromMaster;
	}
}
