package message;

import server.ServerID;

/**
 * Parent class for all write commands.
 * @author tsm
 *
 */
public class Write extends Message {

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
		this.server = server;
		this.stamp 	= stamp;
	}
	
	public Integer CSN()
	{
		return this.CSN;
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
}
