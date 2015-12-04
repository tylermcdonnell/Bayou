package message;

import server.ServerID;
import server.VersionVector;

/**
 * Responds to a message requesting an anti-entropy exchange by returning
 * the process's Version Vector and Commit Sequence Number.
 * @author tsm
 *
 */
public class AcceptAntiEntropy extends Message {

	private static final long serialVersionUID = 1L;
	
	public ServerID server;
	
	public VersionVector V;
	 
	public int CSN;
	
	public AcceptAntiEntropy(ServerID server, VersionVector V, int CSN)
	{
		this.server = server;
		this.V 		= V;
		this.CSN	= CSN;
	}
	
	@Override
	public String toString() 
	{
		return String.format("AcceptAntiEntropy : " +
							 "Sender <%s> " +
							 "Vector <%s> " + 
							 "CSN <%d>", this.server, this.V, this.CSN);
	}
}
