import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import message.Get;
import message.Message;
import socketFramework.Config;
import socketFramework.NetController;

public class Master
{
	
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

	public static void main(String [] args)
	{
		Scanner scan = new Scanner(System.in);

		while (scan.hasNextLine())
		{
			String [] inputLine = scan.nextLine().split(" ");
			int clientId, serverId, id1, id2;
			String songName, URL;
      
			switch (inputLine[0])
			{
				case "joinServer":
        		
					serverId = Integer.parseInt(inputLine[1]);
            
	            /*
	             * Start up a new server with this id and connect it to all servers
	             */
	            
	            // Create a NetController for this server.
	            createNetController(serverId, MAX_NUM_NODES_IN_SYSTEM);
	            
	            	break;
	            
		        case "retireServer":
		        	
		            serverId = Integer.parseInt(inputLine[1]);
		            /*
		             * Retire the server with the id specified. This should block until
		             * the server can tell another server of its retirement
		             */
		            
		            
		            break;
		            
		        case "joinClient":
		        	
		            clientId = Integer.parseInt(inputLine[1]);
		            serverId = Integer.parseInt(inputLine[2]);
		            
		            /*
		             * Start a new client with the id specified and connect it to 
		             * the server
		             */
		            
		            // Create a NetController for this client.
		            createNetController(serverId, MAX_NUM_NODES_IN_SYSTEM);
		            
		            
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
        	
        	
		        	break;
            
		        case "start":
		        	/*
		        	 * Resume the system and allow any Anti-Entropy messages to
		        	 * propagate through the system
		        	 */
        	
        	
		        	break;
            
		        case "stabilize":
		        	/*
		             * Block until there are enough Anti-Entropy messages for all values to 
		             * propagate through the currently connected servers. In general, the 
		             * time that this function blocks for should increase linearly with the 
		             * number of servers in the system.
		             */
		
		
		        	break;
            
		        case "printLog":
		        	
		            serverId = Integer.parseInt(inputLine[1]);
		            /*
		             * Print out a server's operation log in the format specified in the
		             * hand out.
		             */
		            
		            
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
				    
				    
			            break;
            
		        case "delete":
		        	
		            clientId = Integer.parseInt(inputLine[1]);
		            songName = inputLine[2];
		            
		            /*
		             * Instruct the client to delete the given songName from the playlist. 
		             * This command should block until the client communicates with one server.
		             */
		            
		            
		            break;
            
	            // MIKE: added for testing.
	        	case "commTest":
	            
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
	            
	        		break;
	        		
	        	// MIKE: added for API, this is the "clean up" command.
	        	case "c":
	        	
	        		// TODO
	        		// Clean up all NetControllers and state -- create a fresh
	        		// slate.
	        	
	        		break;
	        	
	        		
	        	// MIKE: added default so it fails.
	        	default:
	        		
	        		System.out.println("Unrecognized command. Terminating.");
	        		System.exit(-1);
	        		
	        		break;
            
      		} // End switch.
      
		} // End while has next line of input.
    
	} // End main.
  
	
	
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
		
		nc1.sendMessageToProcess(2, new Get("Test"));
		nc2.sendMessageToProcess(1, new Get("Test"));
		
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
  
}
