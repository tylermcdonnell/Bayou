package message;

import server.VersionVector;

public class WriteRequest extends Message
{
	private static final long serialVersionUID = 1L;
	
	// Sessions reads Version Vector.
	// Used to provide Monotonic Reads and Writes Follow Reads session guarantees.
	protected VersionVector R;
	
	// Sessions writes Version Vector.
	// Used to provide Read Your Writes and Monotonic Writes session guarantees.
	protected VersionVector W;
	
	public WriteRequest()
	{
		this.R = new VersionVector();
		this.W = new VersionVector();
	}
	
	public VersionVector R()
	{
		return R;
	}
	
	public void setR(VersionVector R)
	{
		this.R = R; 
	}
	
	public VersionVector W()
	{
		return W;
	}
	
	public void setW(VersionVector W)
	{
		this.W = W;
	}
	
	@Override
	public String toString()
	{
		return String.format("WriteRequest");
	}
}
