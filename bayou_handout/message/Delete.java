package message;

public class Delete extends WriteRequest
{
	private static final long serialVersionUID = 1L;
	
	public String songName;
	
	public Delete(String songName)
	{
		this.songName = songName;
	}
	
	@Override
	public String toString()
	{
		return String.format("DELETE:(\"%s\")", this.songName);
	}
}
