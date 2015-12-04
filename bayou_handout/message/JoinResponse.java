package message;

import server.ServerID;

/**
 * A response to a Join message that specifies the ID of the
 * server which wishes to join the Bayou server pool.
 * @author tyler
 *
 */
public class JoinResponse extends Message
{
	private static final long serialVersionUID = 1L;
	
	public ServerID ID;
	
	public JoinResponse(ServerID ID)
	{
		this.ID = ID;
	}
	
	@Override
	public String toString()
	{
		return String.format("JoinResponse: Server <%s>", this.ID);
	}
}
