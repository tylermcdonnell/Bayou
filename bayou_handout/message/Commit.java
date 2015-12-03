package message;

import server.ServerID;

public class Commit extends Message
{
	private static final long serialVersionUID = 1L;
	
	private ServerID server;
	
	private int stamp;
	
	private int CSN;
	
	public Commit(ServerID server, int stamp, int CSN)
	{
		this.server	= server;
		this.stamp 	= stamp;
		this.CSN	= CSN;
	}
	
	@Override
	public String toString()
	{
		return String.format("COMMIT : " +
				 "Server <%s> " + 
				 "CSN <%d> " + 
				 "Stamp <%d>",
				 this.server, this.CSN, this.stamp);
	}
}
