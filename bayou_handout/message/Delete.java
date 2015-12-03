package message;

import server.ServerID;

public class Delete extends Write
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	
	public Delete(ServerID server, int stamp, String songName)
	{
		super(server, stamp);
		this.songName = songName;
	}
	
	/**
	 * For debugging purposes.
	 */
	public String toStringVerbose()
	{
		return String.format("DELETE : " +
				 "Server <%s> " + 
				 "CSN <%d> " + 
				 "Stamp <%d>" +
				 "Song Name <%s>",
				 this.server, this.CSN, this.stamp, this.songName);
	}
	
	@Override
	public String toString()
	{
		return String.format("DELETE:(\"%s\")", this.songName);
	}
}
