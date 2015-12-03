/**
 * This code may be modified and used for non-commercial 
 * purposes as long as attribution is maintained.
 * 
 * @author: Isaac Levy
 */

/**
* The sendMsg method has been modified by Navid Yaghmazadeh to fix a bug regarding to send a message to a reconnected socket.
*/

package socketFramework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;

import message.Message;

/**
 * Public interface for managing network connections.
 * You should only need to use this and the Config class.
 * @author ilevy
 *
 */
public class NetController {
	
	// MIKE: For each net controller that this net controller can talk to,
	// the value true at the index corresponding to the ID of this array
	// implies that this net controller can talk to the net controller
	// with that index. False means they can't talk.
	private boolean[] canTalkTo;
	
	// MIKE: added ID.
	private final int ID;
	
	private final int MAX_NUM_NODES_IN_SYSTEM;
	private final Config config;
	private final List<IncomingSock> inSockets;
	private final OutgoingSock[] outSockets;
	private final ListenServer listener;
	
	// MIKE: added for heart beats, if necessary.
	//private long lastTimeMessageSent;
	
	// MIKE: added.
	// MIKE: not used for Bayou -- clients and servers have unique IDs and
	//       sends have a client or server ID specified.
	//private final int numClients;
	
	public NetController(Config config, int numProcesses, int ID)
	{
		// MIKE: added.
		this.MAX_NUM_NODES_IN_SYSTEM = numProcesses;
		
		// MIKE: added.
		this.canTalkTo = new boolean[this.MAX_NUM_NODES_IN_SYSTEM];
		
		// MIKE: added.
		this.ID = ID;
		
		// MIKE: initialize array to all true.
		for (int i = 0; i < canTalkTo.length; i++)
		{
			canTalkTo[i] = true;
		}
		
		this.config = config;
		inSockets = Collections.synchronizedList(new ArrayList<IncomingSock>());
		listener = new ListenServer(config, inSockets);
		outSockets = new OutgoingSock[config.numProcesses];
		listener.start();
		
		// MIKE: added.
		//this.numClients = numClients;
		
		// MIKE: added.  This way, if a particular NetController has never
		// sent a message, it can still pass the allClear test.
		//this.lastTimeMessageSent = Long.MIN_VALUE;
	}
	
	// MIKE: not used for Bayou -- clients and servers have unique IDs and
	//       sends have a client or server ID specified.
	
	/*
	public long lastMessageTime()
	{
		return this.lastTimeMessageSent;
	}
	*/
	
	
	/**
	 * Breaks the connection from this net controller to the net controller
	 * with the given ID.
	 * 
	 * @param id, the given ID.
	 */
	public void breakConnection(int id)
	{
		this.canTalkTo[id] = false;
	}
	
	
	/**
	 * Restores the connection from this net controller to the net controller
	 * with the given ID.
	 * 
	 * @param id, the given ID.
	 */
	public void restoreConnection(int id)
	{
		this.canTalkTo[id] = true;
	}
	
	// Establish outgoing connection to a process
	private synchronized void initOutgoingConn(int proc) throws IOException {
		
		// MIKE: Make sure this is only called once per outgoing connection.
		if (outSockets[proc] != null)
			throw new IllegalStateException("proc " + proc + " not null");
		
		// MIKE: config.addresses[proc] can just be localhost.
		// MIKE: pulls the port for this new connection from config.ports array.
		outSockets[proc] = new OutgoingSock(new Socket(config.addresses[proc], config.ports[proc]));
		//config.logger.info(String.format("Server %d: Socket to %d established", 
		//		config.procNum, proc));
	}
	
	/**
	 * Send a msg to another process.  This will establish a socket if one is not created yet.
	 * Will fail if recipient has not set up their own NetController (and its associated serverSocket)
	 * @param process int specified in the config file - 0 based
	 * @param msg Do not use the "&" character.  This is hardcoded as a message separator. 
	 *            Sends as ASCII.  Include the sending server ID in the message
	 * @return bool indicating success
	 */
	public synchronized boolean sendMsg(int process, String msg) {
		
		// MIKE: added for breaking connections (Bayou).
		if (!this.canTalkTo[process])
		{
			System.out.println("NC " + this.ID + " tried to send to " + process + " -- BROKEN CONNECTION.");
			return false;
		}
		
		try {
			if (outSockets[process] == null)
				initOutgoingConn(process);
			outSockets[process].sendMsg(msg);
		} catch (IOException e) { 
			if (outSockets[process] != null) {
				outSockets[process].cleanShutdown();
				outSockets[process] = null;
				try{
					initOutgoingConn(process);
                        		outSockets[process].sendMsg(msg);	
				} catch(IOException e1){
					if (outSockets[process] != null) {
						outSockets[process].cleanShutdown();
	                	outSockets[process] = null;
					}
					config.logger.info(String.format("Server %d: Msg to %d failed.",
                        config.procNum, process));
        		    config.logger.log(Level.FINE, String.format("Server %d: Socket to %d error",
                        config.procNum, process), e);
                    return false;
				}
				return true;
			}
			config.logger.info(String.format("Server %d: Msg to %d failed.", 
				config.procNum, process));
			config.logger.log(Level.FINE, String.format("Server %d: Socket to %d error", 
				config.procNum, process), e);
			return false;
		}
		return true;
	}
	
	private static String toString(Serializable o) throws IOException 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(o);
		oos.close();
		return Base64.getEncoder().encodeToString(out.toByteArray());
	}
	
	/**
	 * Sends a message to the specified process.
	 * 
	 * @param process
	 * 		NetController port ID of the process the message
	 * 		should be sent to. Note that this is not the 
	 * 		fully qualified ServerID. Clients and servers
	 * 		share the same ID space.
	 * @param m
	 * 		Message to be sent.
	 * @return
	 */
	public boolean sendMessageToProcess(int process, Message m)
	{
		try 
		{
			Map.Entry<Integer, Message> toSend = new AbstractMap.SimpleEntry<Integer, Message>(this.ID, m);
			return sendMsg(process, toString((Serializable)toSend));
		}
		catch (Exception exc)
		{
			System.out.println(exc.getMessage());
			System.out.println("ERROR: IOException while sending message.");
			return false;
		}
	}
	
	// MIKE: changed for Paxos.
	// MIKE: not used for Bayou -- clients and servers have unique IDs and
	//       sends have a client or server ID specified.
	/*
	public boolean sendMsgToClient(int process, Message msg)
	{
		// MIKE: added.  Clients don't send each other heart beats, so
		// no we can tag every message sent.
		this.lastTimeMessageSent = System.currentTimeMillis();
		
		try
		{
			return sendMsg(this.getClientNetControllerIndex(process), toString((Serializable)msg));
		}
		catch (Exception exc)
		{
			System.out.println(exc.getMessage());
			System.out.println("ERROR: IOException while sending message.");
			return false;
		}
	}
	*/
	
	// Mike: changed for Paxos.
	// MIKE: not used for Bayou -- clients and servers have unique IDs and
	//       sends have a client or server ID specified.
	/*
	public boolean sendMsgToServer(int process, Message msg)
	{
		// MIKE: added.
		// Servers send each other heart beats -- exclude these.
		// MIKE: modified for Bayou project. Include later on if we need.
		//if (!(msg instanceof HeartBeat))
		//{
		//	this.lastTimeMessageSent = System.currentTimeMillis();
		//}
		
		try
		{
			return sendMsg(this.getServerNetControllerIndex(process), toString((Serializable)msg));
		}
		catch (Exception exc)
		{
			System.out.println(exc.getMessage());
			System.out.println("ERROR: IOException while sending message.");
			return false;
		}
	}
	*/
	
	
	/**
	 * Return a list of msgs received on established incoming sockets
	 * @return list of messages sorted by socket, in FIFO order. *not sorted by time received*
	 */
	private synchronized List<String> getReceivedMsgs() {
		List<String> objs = new ArrayList<String>();
		synchronized(inSockets) {
			ListIterator<IncomingSock> iter  = inSockets.listIterator();
			while (iter.hasNext()) {
				IncomingSock curSock = iter.next();
				try {
					objs.addAll(curSock.getMsgs());
				} catch (Exception e) {
					config.logger.log(Level.INFO, 
							"Server " + config.procNum + " received bad data on a socket", e);
					curSock.cleanShutdown();
					iter.remove();
				}
			}
		}
		
		return objs;
	}
	
	private static Object fromString(String s) throws IOException, ClassNotFoundException 
	{
		byte[] data = Base64.getDecoder().decode(s);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
		Object o = in.readObject();
		in.close();
		return o;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map.Entry<Integer, Message>> getReceivedMessages()
	{
		List<String> msgs = getReceivedMsgs();
		List<Map.Entry<Integer, Message>> received = new ArrayList<Map.Entry<Integer, Message>>();
		for (String s : msgs)
		{
			try
			{
				Object r = fromString(s);
				if (r instanceof Map.Entry<?, ?>)
				{
					received.add((Map.Entry<Integer, Message>)r);
				}
			}
			catch(IOException exc)
			{
				System.out.println("ERROR: I/O while receiving message.");
				exc.printStackTrace();
			}
			catch(ClassNotFoundException exc)
			{
				System.out.println("ERROR: Class not found while receiving message.");
			}
		}
		return received;
	}
	
	public List<Message> getReceived() 
	{
		List<String> msgs = getReceivedMsgs();
		List<Message> received = new ArrayList<Message>();
		for(Iterator<String> i = msgs.iterator(); i.hasNext();)
		{
			try
			{
				Object r = fromString(i.next());
				if (r instanceof Message)
				{
					received.add((Message)r);
				}
			}
			catch(IOException exc)
			{
				System.out.println("ERROR: I/O while receiving message.");
				exc.printStackTrace();
			}
			catch(ClassNotFoundException exc)
			{
				System.out.println("ERROR: Class not found while receiving message.");
			}
		}
		return received;
	}
	
	/**
	 * Shuts down threads and sockets.
	 */
	public synchronized void shutdown() {
		listener.cleanShutdown();
        if(inSockets != null) {
		    for (IncomingSock sock : inSockets)
			    if(sock != null)
                    sock.cleanShutdown();
        }
		if(outSockets != null) {
            for (OutgoingSock sock : outSockets)
			    if(sock != null)
                    sock.cleanShutdown();
        }	
	}
	
	
	/**
	 * Returns the NetController index for this server.
	 * 
	 * @param serverNum, this server's index.
	 */
	
	// MIKE: not used for Bayou -- clients and servers have unique IDs and
	//       sends have a client or server ID specified.
	
	/*
	public int getServerNetControllerIndex(int serverNum)
	{
		return this.numClients + serverNum;
	}
	*/
	  
	/**
	 * Returns the NetController index for this client.
	 * 
	 * @param clientNum, this client's index.
	 */
	
	// MIKE: not used for Bayou -- clients and servers have unique IDs and
	//       sends have a client or server ID specified.
	
	/*
	public int getClientNetControllerIndex(int clientNum)
	{
		return clientNum;
	}
	*/
}
