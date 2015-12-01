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
	
	private ServerID sender;
	
	public StartAntiEntropy(String sender)
	{
		
	}
	
	@Override
	public String toString() 
	{
		return String.format("StartAntiEntropy : Sender <%s> ", sender);
	}
}
