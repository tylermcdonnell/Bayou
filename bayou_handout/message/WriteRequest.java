package message;

public class WriteRequest extends Message
{
	private static final long serialVersionUID = 1L;
	
	public WriteRequest()
	{

	}
	
	@Override
	public String toString()
	{
		return String.format("WriteRequest");
	}
}
