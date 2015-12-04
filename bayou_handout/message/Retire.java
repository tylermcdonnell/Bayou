package message;

import server.ServerID;

public class Retire extends WriteRequest
{
	private static final long serialVersionUID = 1L;
	
	public ServerID ID;
	
	public Retire(ServerID ID)
	{
		this.ID = ID;
	}
	
	@Override
	public String toString()
	{
		return String.format("Retire: Server <%s>", this.ID);
	}
}
