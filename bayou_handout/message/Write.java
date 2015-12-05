package message;

import server.ServerID;

/**
 * Parent class for all write commands.
 * @author tsm
 *
 */
public class Write extends Message implements Comparable<Write> {

	private static final long serialVersionUID = 1L;
	
	// Commit Sequence Number once this Write has been committed.
	protected int CSN;
	
	// The ServerID which first accepted this write.
	protected ServerID server; 
	
	// The accept stamp of the server which first accepted this write.
	protected int stamp;
	
	// Actual Write information
	private WriteRequest action;
	
	/**
	 * Constructor. Initializes CSN to Infinity.
	 * 
	 * @param server		Server which first accepted this write.
	 * @param stamp			Stamp served which accepted this write gave to it.
	 */
	public Write(ServerID server, int CSN, int stamp, WriteRequest action)
	{
		this.CSN 	= Integer.MAX_VALUE;
		this.server	= server;
		this.stamp 	= stamp;
		this.action = action;
		this.CSN	= CSN;
	}
	
	public int CSN()
	{
		return this.CSN;
	}
	
	public void setCSN(int CSN)
	{
		this.CSN = CSN;
	}
	
	public ServerID server()
	{
		return this.server;
	}
	
	public int stamp()
	{
		return this.stamp;
	}
	
	public WriteRequest action()
	{
		return this.action;
	}
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (this.toString().hashCode());
        return result;
    }
	
	@Override
	public String toString() 
	{
		return String.format("Write : " +
							 "Server <%s> " + 
							 "CSN <%d> " + 
							 "Stamp <%d>" + 
							 "Action <%s>",
							 this.server, this.CSN, this.stamp, this.action);
	}
	
	@Override
	/**
	 * Total order of writes:
	 * 		1. CSN is most important.
	 * 		2. Accept stamp is used after CSN.
	 * 		3. If CSN and accept order are the same, use server ID to break ties.
	 * This total ordering ensures eventual consistency even for non-stable writes.
	 */
	public int compareTo(Write toCompare)
	{
		if (this.CSN != toCompare.CSN)
		{
			return (this.CSN < toCompare.CSN) ? -1 : 1;
		}
		else if (this.stamp != toCompare.stamp)
		{
			return (this.stamp < toCompare.stamp) ? -1 : 1;
		}
		else
		{
			return this.server.compareTo(toCompare.server);
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Write))
		{
			return false;
		}
		Write w = (Write)o;
		if (this.compareTo(w) == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
