package server;

import java.util.ArrayList;

/**
 * Abstraction for a Bayou server.
 * @author tyler
 *
 */
public class Server implements Runnable {

	// Server's ID.
	
	// Write Log.
	
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
	 */
	public Server(boolean isInitialPrimary)
	{
		
	}
	
	@Override
	public void run()
	{
		// Primary server logic.
	}
	
	
}
