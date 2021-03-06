import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import client.Client;
import message.Delete;
import message.Get;
import message.Message;
import message.Put;
import message.WriteRequest;
import server.Server;
import server.VersionVector;
import socketFramework.Config;
import socketFramework.NetController;

public class Master
{
	// Who the current primary server is (their ID).
	public static int primaryServerId = -1;
	
	// A list of the process objects underlying the client thread handles.
	// Note that a client with ID = x will be stored at index x.
	public static ArrayList<Client> clientProcesses = new ArrayList<Client>();
	
	// A list of the process objects underlying the client thread handles.
	// Note that a server with ID = x will be stored at index x.
	public static ArrayList<Server> serverProcesses = new ArrayList<Server>();
	
	// Keep track of client and server threads for clean up.
	public static ArrayList<Thread> serverThreads = new ArrayList<Thread>();
	public static ArrayList<Thread> clientThreads = new ArrayList<Thread>();
	
	
	// The maximum number of nodes this system can handle.
	// Note: this is how many ports the system will use. See NetController
	// code for more information.
	private final static int MAX_NUM_NODES_IN_SYSTEM = 1000;
	
	// A list of all NetControllers. Each client or server receives a NetController
	// which is the element at the index equal to the ID given in the joinServer or
	// joinClient command used to create it.
	//
	// NOTE: assumes that servers and clients are given the next possible ID
	// available, and the very first NetController created has ID = 0.
	public static ArrayList<NetController> netControllers = new ArrayList<NetController>();
	
	// A list of the NetController IDs for servers that are alive.
	public static ArrayList<Integer> aliveServerNetControllerIDs = new ArrayList<Integer>();

	
	public static void main(String[] args)
	{
		Scanner scan = new Scanner(System.in);
		
		//Logger log = Logger.getInstance();
		//Thread logThread = new Thread(log);
		//logThread.start();
		
		while (scan.hasNextLine())
		{
			String[] inputLine = scan.nextLine().split(" ");
			execute(inputLine);
		}
	}
	
	
	private static void execute(String [] inputLine)
	{

		int clientId, serverId, id1, id2;
		String songName, URL;
  
		//System.out.println("Master Command: " + inputLine[0]);
		
		//System.out.println(inputLine[0]);
		
		switch (inputLine[0])
		{
			case "joinServer":
    		
				serverId = Integer.parseInt(inputLine[1]);
        
				/*
				 * Start up a new server with this id and connect it to all servers
				 */
				joinServer(serverId);
            	break;
            
	        case "retireServer":
	        	
	            serverId = Integer.parseInt(inputLine[1]);
	            /*
	             * Retire the server with the id specified. This should block until
	             * the server can tell another server of its retirement
	             */
	            
	            Master.serverProcesses.get(serverId).retire();
	            
	            // TODO: remove this server's ID from the list of alive server IDs,
	            // but ONLY ONCE the retired server has talked to one other server
	            // (I think that's supposed to be blocking anyways, else the server
	            // which is retiring can simply call a Master (or NetController) method 
	            // to remove it from the list.
	            
	            // .... serverNetControllerIDs.remove(retiredServerId);
	            // If we are retiring the primary, update the primary server Id.
	            if (serverId == Master.primaryServerId)
	            {
	            	Master.primaryServerId = serverId;
	            }
	            // Master.primaryServerId = ...
	            break;
	            
	        case "joinClient":
	        	
	            clientId = Integer.parseInt(inputLine[1]);
	            serverId = Integer.parseInt(inputLine[2]);
	            
	            /*
	             * Start a new client with the id specified and connect it to 
	             * the server
	             */
	            joinClient(clientId, serverId);
	            break;
	            
	        case "breakConnection":
	        	
	        	id1 = Integer.parseInt(inputLine[1]);
	        	id2 = Integer.parseInt(inputLine[2]);
	            
	        	/*
	             * Break the connection between a client and a server or between
	             * two servers
	             */
	        	breakConnection(id1, id2);
	            break;
            
	        case "restoreConnection":
		
	        	id1 = Integer.parseInt(inputLine[1]);
	        	id2 = Integer.parseInt(inputLine[2]);
		        	/*
		        	 * Restore the connection between a client and a server or between
		        	 * two servers
		        	 */
		        	restoreConnection(id1, id2);
		        	break;
            
	        case "pause":
	        	/*
	        	 * Pause the system and don't allow any Anti-Entropy messages to
	        	 * propagate through the system
	        	 */
	        	for (Server s : Master.serverProcesses)
	        	{
	        		if (s != null)
	        		{
	        			s.pause();
	        		}
	        	}	
	        	break;
        
	        case "start":
	        	/*
	        	 * Resume the system and allow any Anti-Entropy messages to
	        	 * propagate through the system
	        	 */
	        	for (Server s : Master.serverProcesses)
	        	{
	        		if (s != null)
	        		{
	        			s.start();
	        		}
	        	}
	        	break;
        
	        case "stabilize":
	        	/*
	             * Block until there are enough Anti-Entropy messages for all values to 
	             * propagate through the currently connected servers. In general, the 
	             * time that this function blocks for should increase linearly with the 
	             * number of servers in the system.
	             */
	        	long time = 10 * Master.aliveServerNetControllerIDs.size() * Server.ANTI_ENTROPY_PERIOD;
	        	// System.out.println("Waiting " + time);
	        	try
	        	{
	        		Thread.sleep(time);
	        	}
	        	catch (InterruptedException exc)
	        	{
	        		// Shouldn't happen.
	        	}
	        	
	        	
	        	//System.out.println("Stabilize DONE");
	
	        	break;
        
	        case "printLog":
	            serverId = Integer.parseInt(inputLine[1]);
	            /*
	             * Print out a server's operation log in the format specified in the
	             * hand out.
	             */
	            Master.serverProcesses.get(serverId).printLog();
	            
	            break;
	            
	        case "printAll":
	        	serverId = Integer.parseInt(inputLine[1]);
	        	
	        	Master.serverProcesses.get(serverId).printAll();
	        	break;
	        	
        
	        case "put":
	            clientId = Integer.parseInt(inputLine[1]);
	            songName = inputLine[2];
	            URL = inputLine[3];
	            
	            /*
	             * Instruct the client specified to associate the given URL with the given
	             * songName. This command should block until the client communicates with
	             * one server.
	             */
	            validateClientId(clientId);
	            Put putRequest = new Put(songName, URL);
	            Master.clientProcesses.get(clientId).giveClientCommand(putRequest);
	            Master.clientProcesses.get(clientId).waitClient();
	            
	            break;
        
			case "get":
	            clientId = Integer.parseInt(inputLine[1]);
	            songName = inputLine[2];
	            
	            /*
	             * Instruct the client specified to attempt to get the URL associated with
	             * the given songName. The value should then be printed to standard out of 
	             * the master script in the format specified in the handout. This command 
	             * should block until the client communicates with one server.
	             */
	            validateClientId(clientId);
	            Get getRequest = new Get(songName);
	            Master.clientProcesses.get(clientId).giveClientCommand(getRequest);
	            Master.clientProcesses.get(clientId).waitClient();
	            
	            break;
        
	        case "delete":
	        	
	            clientId = Integer.parseInt(inputLine[1]);
	            songName = inputLine[2];
	            
	            /*
	             * Instruct the client to delete the given songName from the playlist. 
	             * This command should block until the client communicates with one server.
	             */
	            validateClientId(clientId);
	            Delete deleteRequest = new Delete(songName);
	            Master.clientProcesses.get(clientId).giveClientCommand(deleteRequest);
	            Master.clientProcesses.get(clientId).waitClient();
	            
	            break;
	            
	         // MIKE: added for API, this is the "clean up" command.
        	case "clean":
        		clean();
        		break;
        
            // MIKE: added for testing.
        	case "commTest":
        		
        		commTest();
        		break;
        		
        	// MIKE: added for testing alive server list.
        	case "createServerTest":
        		
        		createServerTest();
        		break;
        	
        	case "s":
    			String filename = inputLine[1];
    			runScript(filename);
    			break;
        		
        	// MIKE: added default so it fails.
        	default:
        		
        		System.out.println("Unrecognized command. Terminating.");
        		System.exit(-1);
        		
        		break;
        
  		} // End switch.
	} // End main.
	
	
	private static void joinServer(int serverId)
	{
		//System.out.println("Adding " + serverId);
		// Create a NetController for this server.
        NetController nc = createNetController(serverId, Master.MAX_NUM_NODES_IN_SYSTEM);
        
        // Add its index to the list of alive server IDs.
        Master.aliveServerNetControllerIDs.add(serverId);
        
        // Add this index to all NetControllers.
        for (int i = 0; i < Master.netControllers.size(); i++)
        {
        	if (Master.netControllers.get(i) != null)
        	{
        		Master.netControllers.get(i).setAlive(Master.aliveServerNetControllerIDs);
        	}
        }
        
        Server newServer = null;
        
        if (Master.serverProcesses.size() == 0)
        {
        	// This server is not initial primary.
        	Master.primaryServerId = serverId;
        	newServer = new Server(true, nc, serverId);
        }
        else
        {
        	// This server is not initial primary.
        	newServer = new Server(false, nc, Master.primaryServerId);
        }
        
        // If the element at index serverId is not created yet.
     	int highestIndex = Master.serverProcesses.size() - 1;
     	if (highestIndex < serverId)
     	{
     		int difference = serverId - highestIndex;
     		
     		for (int i = 0; i < (difference - 1); i++)
     		{
     			Master.serverProcesses.add(null);
     		}
     		
     		Master.serverProcesses.add(newServer);
     	}
     	else
     	{
     		Master.serverProcesses.set(serverId, newServer);
     	}
     	
        // Create server thread.
        Thread newServerThread = new Thread(newServer);
        newServerThread.start();
        
        // Remember all threads created for clean up.
        Master.serverThreads.add(newServerThread);
        
        newServer.waitUntilJoined();
	}
	
	
	private static void joinClient(int clientId, int serverId)
	{
		// MIKE: changed to <= from <.
		if (Master.clientProcesses.size() <= clientId ||
			Master.clientProcesses.get(clientId) == null)
		{
			// Create a NetController for this client.
			NetController nc = createNetController(clientId, MAX_NUM_NODES_IN_SYSTEM);
	        
	        // Pass in the server ID to this client, they can use that as
	        // the argument to send() when they talk to their server.
	        Client newClient = new Client(clientId, serverId, nc);
	        
	        // If the element at index clientId is not created yet.
	     	int highestIndex = Master.clientProcesses.size() - 1;
	     	if (highestIndex < clientId)
	     	{
	     		int difference = clientId - highestIndex;
	     		
	     		for (int i = 0; i < (difference - 1); i++)
	     		{
	     			Master.clientProcesses.add(null);
	     		}
	     		
	     		Master.clientProcesses.add(newClient);
	     	}
	     	else
	     	{
	     		Master.clientProcesses.set(clientId, newClient);
	     	}
	     	
	        // Create client thread.
	        Thread newClientThread = new Thread(newClient);
	        newClientThread.start();
	        
	        // Remember all threads created for clean up.
	        Master.clientThreads.add(newClientThread);
		}
		
		Master.clientProcesses.get(clientId).connect(serverId);
	}
	
	/**
	 * Breaks the connection between the two NetControllers whose IDs correspond
	 * to id1 and id2. Note that the connection is broken in both directions.
	 * 
	 * @param id1
	 * @param id2
	 */
	private static void breakConnection(int id1, int id2)
	{
		// Get NetController for id1 and id2.
		NetController netController1 = netControllers.get(id1);
		NetController netController2 = netControllers.get(id2);
		
		// Break the connection from id1 to id2.
		netController1.breakConnection(id2);
		
		// Break the connection from id2 to id1.
		netController2.breakConnection(id1);
	}
	
	
	/**
	 * Restores the connection between the two NetControllers whose IDs correspond
	 * to id1 and id2. Note that the connection is restored in both directions.
	 * 
	 * @param id1
	 * @param id2
	 */
	private static void restoreConnection(int id1, int id2)
	{
		// Get NetController for id1 and id2.
		NetController netController1 = netControllers.get(id1);
		NetController netController2 = netControllers.get(id2);
				
		// Break the connection from id1 to id2.
		netController1.restoreConnection(id2);
				
		// Break the connection from id2 to id1.
		netController2.restoreConnection(id1);
	}
	
  
	/**
	 * Creates a NetController for the given process, described by its process
	 * number.
	 * 
	 * @param processNumber, this process' number (ID).
	 * 
	 * @param numProcesses, the number of processes this process can talk to (the
	 *        maximum number of other NetControllers this system will ever handle,
	 *        including this NetController being created).
	 *        
	 * @return a NetController for the given process, described by its process
	 *         number (ID).
	 */
	private static NetController createNetController(int processNumber, int numProcesses) {

		// **********************************************************************
		// * Setup communication for this process with all other processes.
		// **********************************************************************
		NetController nc = null;

		// Dynamically create a config file for this process.
		// Reuse the same file for all processes.
		String fileName = "config.txt";
		File file = new File(fileName);

		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// NumProcesses field.
		out.println("NumProcesses=" + numProcesses);

		// ProcNum field.
		out.println("ProcNum=" + processNumber);

		// host fields.
		for (int i = 0; i < numProcesses; i++) {
			out.println("host" + i + "=localhost");
		}

		// port fields.
		for (int i = 0; i < numProcesses; i++) {
			out.println("port" + i + "=" + (6100 + i));
		}

		out.flush();
		out.close();

		try {

			Config config = new Config(fileName);
			nc = new NetController(config, numProcesses, processNumber);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Add this to the list of net controllers in the entire system.
		netControllers.add(nc);

		return nc;
	}
	
	public static void testNetControllers()
	{
		NetController nc1 = createNetController(1, Master.MAX_NUM_NODES_IN_SYSTEM);
		NetController nc2 = createNetController(2, Master.MAX_NUM_NODES_IN_SYSTEM);
		
		nc1.sendMessageToProcess(2, new Delete("Test"));
		nc2.sendMessageToProcess(1, new Delete("Test"));
		
		while (true)
		{
			for (Map.Entry<Integer, Message> e : nc1.getReceivedMessages())
			{
				System.out.println(String.format("Received messaged from <%d> <%s>", 
											   	 e.getKey(), e.getValue().toString()));
			}
			for (Map.Entry<Integer, Message> e : nc2.getReceivedMessages())
			{
				System.out.println(String.format("Received messaged from <%d> <%s>", 
					   	 e.getKey(), e.getValue().toString()));
			}
		}
	}
	
	private static void commTest()
	{
		createNetController(0, Master.MAX_NUM_NODES_IN_SYSTEM);
		createNetController(1, Master.MAX_NUM_NODES_IN_SYSTEM);
		createNetController(2, Master.MAX_NUM_NODES_IN_SYSTEM);
		
		NetController nc_0 = netControllers.get(0);
		NetController nc_1 = netControllers.get(1);
		NetController nc_2 = netControllers.get(2);
		
		// Should all work.
		nc_0.sendMsg(1, "HI");
		nc_0.sendMsg(2, "HI");
		nc_1.sendMsg(0, "HI");
		nc_1.sendMsg(2, "HI");
		nc_2.sendMsg(1, "HI");
		nc_2.sendMsg(2, "HI");
		
		// Break from 0 to 1.
		breakConnection(0, 1);
		
		// Should fail.
		nc_0.sendMsg(1, "HI");
		nc_1.sendMsg(0, "HI");
		
		restoreConnection(0, 1);
		
		// Should succeed.
		nc_0.sendMsg(1, "HI");
		nc_1.sendMsg(0, "HI");
	}
	
	private static void createServerTest()
	{
		joinClient(0, 0);
		joinClient(1, 0);
		joinServer(2);
		joinServer(3);
		joinClient(4, 0);
		joinServer(5);
		
		System.out.println("Should be (2, 3, 5): " + Master.aliveServerNetControllerIDs);
		System.out.println("Should be (2, 3, 5): " + Master.netControllers.get(0).getAliveServers());
	}
	
	private static void validateClientId(int clientId)
	{
		if ((clientId >= Master.clientProcesses.size()) || (Master.clientProcesses.get(clientId) == null))
        {
        	System.out.println("Invalid clientId!");
        	System.exit(-1);;
        }
	}
	
	private static void runScript(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("bayou_handout" + File.separator +
					   "tests" + File.separator +
					   filename + ".test"));
			
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("/")) {
					continue;
				}
				
				if (line.equals(""))
				{
					continue;
				}

				String[] inputLine = line.split(" ");
				
				execute(inputLine);
			}
			return;
		}
		catch (Exception exc)
		{

		}
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("tests" + File.separator +
		                filename + ".test"));
			
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("/")) {
					continue;
				}
				
				if (line.equals(""))
				{
					continue;
				}

				String[] inputLine = line.split(" ");
				
				execute(inputLine);
			}
		}
		catch (Exception exc)
		{
			System.out.println("Error while running script.");
			exc.printStackTrace();
		}
	}
	
	private static void clean()
	{
		// Close all NetControllers.
		for (int i = 0; i < Master.netControllers.size(); i++)
		{
			Master.netControllers.get(i).shutdown();
		}
		
		// Stop all Threads (except Master).
		for (int i = 0; i < Master.serverThreads.size(); i++)
		{
			Master.serverThreads.get(i).stop();
		}
		
		for (int i = 0; i < Master.clientThreads.size(); i++)
		{
			Master.clientThreads.get(i).stop();
		}
		
		// Destroy all objects (servers & clients).
		for (int i = 0; i < Master.clientProcesses.size(); i++)
		{
			// Master class is only one who has reference.
			Master.clientProcesses.set(i, null);
		}
		
		for (int i = 0; i < Master.serverProcesses.size(); i++)
		{
			// Master class is only one who has reference.
			Master.serverProcesses.set(i, null);
		}
		
		// Clear out all of Master state.
		Master.primaryServerId = -1; 
		Master.clientProcesses = new ArrayList<Client>();
		Master.serverProcesses = new ArrayList<Server>();
		Master.serverThreads = new ArrayList<Thread>();
		Master.clientThreads = new ArrayList<Thread>();
		Master.netControllers = new ArrayList<NetController>();
		Master.aliveServerNetControllerIDs = new ArrayList<Integer>();
	}
  
}
