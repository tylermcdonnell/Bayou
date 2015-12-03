package message;

import server.ServerID;

public class Get extends Write 
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	
	public Get(ServerID server, int stamp, String songName)
	{
		super(server, stamp);
	}
	
	/**
	 * For debugging purposes.
	 */
	public String toStringVerbose()
	{
		return String.format("GET : " +
				 "Server <%s> " + 
				 "CSN <%d> " + 
				 "Stamp <%d>" +
				 "Song Name <%s>",
				 this.server, this.CSN, this.stamp, this.songName);
	}
	
	@Override
	public String toString()
	{
		return String.format("GET:(\"%s\")", this.songName);
	}
}
