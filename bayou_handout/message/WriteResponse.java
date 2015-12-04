package message;

public class WriteResponse extends Message
{
	private static final long serialVersionUID = 1L;
	
	public boolean success;
	
	public WriteResponse(boolean success)
	{
		this.success = success;
	}
	
	@Override
	public String toString()
	{
		return String.format("WriteResponse:%b", this.success);
	}
}
