package client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import message.WriteRequest;

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
	
	public Client(int myClientId, int myServerId)
	{
		this.clientReceiveQueue = new LinkedList<WriteRequest>();
		this.myServerId = myServerId;
		this.myClientId = myClientId;
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
			for (int i = 0; i < masterMessages.size(); i++)
			{
				System.out.println("Client " + this.myClientId + " received from master: " + masterMessages.get(i));
				
				
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
