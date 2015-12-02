package server;

import java.util.ArrayList;
import java.util.List;

public class ServerID implements Comparable {
	
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
	
	/** 
	 * Servers can be uniquely identified by their list of accept stamps. Note that all
	 * servers in a system must necessarily arise from the initial primary. This constructor
	 * is provided as convenience, since we can not rely on references to parents due to
	 * serialization.
	 * 
	 * @param stamps			The list of stamps which uniquely identifies a ServerID.
	 */
	private ServerID(List<Integer> stamps)
	{
		this.stamps = stamps;
	}
	
	/**
	 * Returns the ServerID of the parent. Note that this is a reference to a fresh copy
	 * and not the original since those might otherwise get corrupted by serialization.
	 * 
	 * @return					Returns the ServerID of the parent of this ServerID.
	 */
	public ServerID getParent()
	{
		return new ServerID(this.stamps.subList(0, this.stamps.size() - 1));
	}

	/**
	 * Returns True if this server is the initial primary server for the Bayou system.
	 * This should be simple to observe, since all servers other than the initial 
	 * primary are recursively defined. Thus, the initial primary is the only server
	 * in the system with a single-level ID.
	 * 
	 * @return					True if this Server is initial primary server. Else, false.
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
	
	/**
	 * Returns the accept stamp associated with this ServerID. Note that the ServerID
	 * is by definition the concatenation of this stamp and the ServerID of its parent.
	 * @return 					This server's accept stamp with parent.
	 */
	public Integer getStamp()
	{
		return this.stamps.get(this.stamps.size() - 1);
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
	
	@Override
	public int compareTo(Object compareTo) 
	{
		if (compareTo instanceof ServerID)
		{
			return this.toString().compareTo(compareTo.toString());
		}
		else
		{
			return -1;
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ServerID)
		{
			return this.toString().equals(o.toString());
		}
		else
		{
			return false;
		}
	}
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (this.toString().hashCode());
        return result;
    }
}
