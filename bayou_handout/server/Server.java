package server;

import message.Commit;
import message.Write;
import socketFramework.NetController;

/**
 * Abstraction for a Bayou server.
 * @author tsm
 *
 */
public class Server implements Runnable {

	// Server's ID.
	private ServerID ID;
	
	// Write Log.
	private WriteLog DB;
	
	// Music Playlist.
	private Playlist playlist;
	
	// Net Controller used to communicate with other processes.
	private NetController network;
	
	// Version Vector.
	private VersionVector V;
	
	// Highest Commit Sequence Number this server knows about.
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
	public Server(boolean isInitialPrimary, NetController network)
	{
		this.V 			= new VersionVector();
		this.CSN 		= 0;
		this.DB 		= new WriteLog();
		this.playlist 	= new Playlist(this.DB);
		this.network	= network;
		
		if (isInitialPrimary)
		{
			this.ID = new ServerID();
		}
		else 
		{
			// TODO: Initiate join process
		}
	}
	
	@Override
	public void run()
	{
		// Primary server logic.
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
