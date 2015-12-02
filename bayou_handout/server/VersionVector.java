package server;

import java.util.Hashtable;

/**
 * Abstraction for the <ServerID, TimeStamp> vector used for Bayou exchanges.
 * @author tyler
 *
 */
public class VersionVector {

	private Hashtable<ServerID, Integer> vector;
	
	public VersionVector()
	{
		vector = new Hashtable<ServerID, Integer>();
	}
	
	/**
	 * Adds a server to the version vector and initializes timestamp to 0.
	 * @param s				Server ID
	 */
	public void add(ServerID s)
	{
		add(s, 0);
	}
	
	/**
	 * Adds a server to the version vector and initializes timestamp.
	 * @param s				Server ID
	 * @param timestamp		Initial timestamp
	 */
	public void add(ServerID s, int timestamp)
	{
		this.vector.put(s, 0);
	}
	
	/**
	 * Returns the largest accept-stamp of any write known to this Version Vector
	 * that was originally accepted from by server s.
	 * 
	 * @param s				Server ID which we would like to know accept-stamp for.
	 */
	public Integer getAcceptStamp(ServerID s)
	{
		if (this.vector.containsKey(s))
		{
			return this.vector.get(s);
		}
		else if (s.isInitialPrimary())
		{
			return Integer.MAX_VALUE;
		}
		else if (getAcceptStamp(s.getParent()) >= s.getStamp())
		{
			return Integer.MAX_VALUE;
		}
		else
		{
			return Integer.MIN_VALUE;
		}
	}
}
