package message;

public class GetResponse extends Message
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	public String url;
	
	public GetResponse(String songName, String url)
	{
		this.songName	= songName;
		this.url		= url;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s:%s", this.songName, this.url);
	}
}
