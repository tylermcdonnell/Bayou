package message;


/**
 * Heart beat messages for servers to send to each other to keep a rough 
 * idea of attendance (who is dead or not).
 * 
 * @author Mike Feilbach
 */
public class HeartBeat extends Message
{
	private static final long serialVersionUID = 1L;
	
	// Who is sending this heart beat.
	private int senderId;
	
	public HeartBeat(int senderId)
	{
		this.senderId = senderId;
	}
	
	public int getSenderId()
	{
		return this.senderId;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "HeartBeat: <senderId: " + this.senderId + ">";
		return retVal;
	}
}
