package message;

public class ReadRequest extends Message
{
	private static final long serialVersionUID = 1L;
	
	public ReadRequest()
	{

	}
	
	@Override
	public String toString()
	{
		return String.format("ReadRequest");
	}
}
