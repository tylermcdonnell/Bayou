package message;

public class Get extends ReadRequest 
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	
	public Get(String songName)
	{
		this.songName = songName;
	}
	
	@Override
	public String toString()
	{
		return String.format("GET:(\"%s\")", this.songName);
	}
}
