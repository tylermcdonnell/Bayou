package message;

import server.ServerID;

public class Put extends Write 
{
	private static final long serialVersionUID = 1L;
	
	private String songName;
	
	private String url;
	
	public Put(ServerID server, int stamp, String songName, String url)
	{
		super(server, stamp);
		
		this.songName 	= songName;
		this.url 		= url;
	}
	
	/**
	 * For debugging purposes.
	 */
	public String toStringVerbose()
	{
		return String.format("PUT : " +
				 "Server <%s> " + 
				 "CSN <%d> " + 
				 "Stamp <%d>" +
				 "Song Name <%s>" +
				 "URL <%s>",
				 this.server, this.CSN, this.stamp, this.songName, this.url);
	}
	
	@Override
	public String toString()
	{
		return String.format("PUT:(\"%s, %s\")", this.songName, this.url);
	}
}
