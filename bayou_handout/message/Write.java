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
	private Integer CSN;
	
	// The ServerID which first accepted this write.
	private ServerID server; 
	
	// The accept stamp of the server which first accepted this write.
	private Integer stamp;
	
	/**
	 * Constructor. Initializes CSN to Infinity.
	 * 
	 * @param server		Server which first accepted this write.
	 * @param stamp			Stamp served which accepted this write gave to it.
	 */
	public Write(ServerID server, Integer stamp)
	{
		this.CSN 	= Integer.MAX_VALUE;
		this.server	= server;
		this.stamp 	= stamp;
	}
	
	public Integer CSN()
	{
		return this.CSN;
	}
	
	public void setCSN(Integer CSN)
	{
		this.CSN = CSN;
	}
	
	public ServerID server()
	{
		return this.server;
	}
	
	public Integer stamp()
	{
		return this.stamp;
	}
	
	@Override
	public String toString() 
	{
		return String.format("Write : " +
							 "Server <%s> " + 
							 "CSN <%d> " + 
							 "Stamp <%d>",
							 this.server, this.CSN, this.stamp);
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
