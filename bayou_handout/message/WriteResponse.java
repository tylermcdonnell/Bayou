package message;

import server.VersionVector;

public class WriteResponse extends Message
{
	private static final long serialVersionUID = 1L;
	
	public boolean success;
	
	public VersionVector V;
	
	public WriteResponse(boolean success, VersionVector V)
	{
		this.V			= new VersionVector();
		this.success 	= success;
	}
	
	@Override
	public String toString()
	{
		return String.format("WriteResponse:%b", this.success);
	}
}
