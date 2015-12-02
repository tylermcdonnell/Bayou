package server;

import java.util.ArrayList;
import java.util.List;

public class ServerID {
	
	private final String INITIAL_PRIMARY = "INITIAL";
	
	public List<Integer> stamps;
	
	/**
	 * Constructor used if this server is the initial primary server. The accept stamp
	 * paired with the string name will be "0". 
	 * 
	 * Note: This constructor should be used ONCE per entire Bayou system instantiation.
	 * 
	 * @param name				Desired name of initial primary server.
	 */
	public ServerID()
	{
		this.stamps = new ArrayList<Integer>();
		this.stamps.add(0);
	}
	
	/**
	 * Server IDs in the Bayou protocol are defined recursively. When a new server joins
	 * the network, it issues a join Write to an existing server. It then uses the 
	 * concatenation of the accept stamp issued by that server and that server's ID
	 * as it's unique identifier.
	 * 
	 * @param server			Server ID of server through which this server
	 * 							joined the system. If this server is the first
	 * 							server in the system, use the other constructor.
	 * @param acceptStamp		
	 * @param server
	 */
	public ServerID(ServerID server, Integer acceptStamp)
	{
		this.stamps = new ArrayList<Integer>(server.stamps);
		this.stamps.add(acceptStamp);
	}
	
	private ServerID(List<Integer> stamps)
	{
		this.stamps = stamps;
	}
	
	/**
	 * Returns the ServerID of the parent. Note that this is a reference to a fresh copy
	 * and not the original since those might otherwise get corrupted by serialization.
	 * @return
	 */
	public ServerID getParent()
	{
		return new ServerID(this.stamps.subList(0, this.stamps.size() - 1));
	}
	
	@Override
	public String toString()
	{
		if (isInitialPrimary())
		{
			return String.format("<%s, %d>", this.INITIAL_PRIMARY, this.stamps.get(0));
		}
		else
		{
			return String.format("<%s, %d>", this.getParent().toString(), this.stamps.get(this.stamps.size() - 1));
		}
	}

	/**
	 * Returns True if this server is the initial primary server for the Bayou system.
	 * This should be simple to observe, since all servers other than the initial 
	 * primary are recursively defined. Thus, the initial primary is the only server
	 * in the system with a single-level ID.
	 * @return
	 */
	public boolean isInitialPrimary()
	{
		if (this.stamps.size() == 1) // Initial Primary has actually one stamp - "0"
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
