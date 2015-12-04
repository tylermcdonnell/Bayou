package message;

public class Put extends WriteRequest 
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	
	public String url;
	
	public Put(String songName, String url)
	{
		this.songName 	= songName;
		this.url 		= url;
	}
	
	@Override
	public String toString()
	{
		return String.format("PUT:(\"%s, %s\")", this.songName, this.url);
	}
}
