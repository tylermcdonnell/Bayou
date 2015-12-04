package message;

import server.ServerID;

/**
 * Requests the Version Vector and Commit Sequence Number a receiving
 * process in the anti-entropy protocol.
 * @author tsm
 *
 */
public class StartAntiEntropy extends Message {

	private static final long serialVersionUID = 1L;
	
	public ServerID server;
	
	public StartAntiEntropy(ServerID server)
	{
		this.server = server;
	}
	
	@Override
	public String toString() 
	{
		return String.format("StartAntiEntropy : Server <%s> ", this.server);
	}
}
