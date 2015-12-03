package client;

/**
 * Abstraction for Bayou client.
 * @author tsm
 *
 */
public class Client implements Runnable {
	
	// The server I talk to for requests.
	private int myServerId;
	
	public Client(int myServerId)
	{
		this.myServerId = myServerId;
	}
	
	@Override
	public void run()
	{
		
	}
	
	public int getMySeverId()
	{
		return this.myServerId;
	}
}
