package message;

import server.VersionVector;

public class GetResponse extends Message
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	public String url;
	
	public VersionVector V;
	
	public boolean success;
	
	public GetResponse(String songName, String url, boolean success)
	{
		this.songName	= songName;
		this.url		= url;
		this.success 	= success;
		
		this.V			= new VersionVector();
	}
	
	@Override
	public String toString()
	{
		return String.format("%s:%s", this.songName, this.url);
	}
}
