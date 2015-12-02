package server;

/**
 * Abstraction for a Bayou server.
 * @author tsm
 *
 */
public class Server implements Runnable {

	// Server's ID.
	private ServerID ID;
	
	// Write Log.
	
	// Version Vector
	private VersionVector V;
	
	// Commit Sequence Number
	private int CSN;
	
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
		this.V 		= new VersionVector();
		this.CSN 	= 0;
	}
	
	@Override
	public void run()
	{
		// Primary server logic.
	}
	
	public void antiEntropy(VersionVector RV, int RCSN)
	{
		// Propagate committed writes.
		if (RCSN < this.CSN)
		{
			
		}
		
		// Propagate tentative writes.
	}
}
