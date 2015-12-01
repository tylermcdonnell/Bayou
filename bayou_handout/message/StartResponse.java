package message;

import server.ServerID;
import server.VersionVector;

/**
 * Returns Version Vector and Commit Sequence Number to process
 * initiating an anti-entropy exchange.
 * @author tsm
 *
 */
public class StartResponse extends Message {

	private static final long serialVersionUID = 1L;
	
	private ServerID sender;
	
	private VersionVector V;
	
	private int CSN;
	
	public StartResponse(ServerID sender, VersionVector V, int CSN)
	{
		this.sender  	= sender;
		this.V 			= V;
		this.CSN   		= CSN;
	}
	
	@Override
	public String toString() 
	{
		return String.format("StartResponse : " + 
							 "Sender <%s> " + 
							 "Version Vector <%s> " + 
							 "Commit Sequence Number <%d>",
							 this.sender, this.V.toString(), this.CSN);
							
	}
}
