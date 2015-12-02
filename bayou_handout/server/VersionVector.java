package server;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

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
	 * @param s				Server ID.
	 * @param timestamp		Initial timestamp.
	 */
	public void add(ServerID s, int timestamp)
	{
		this.vector.put(s, 0);
	}
	
	/**
	 * Updates the most recent accept stamp for a server.
	 * 
	 * @param s				Server to update.
	 * @param timestamp		Most recent accept stamp.
	 */
	public void update(ServerID s, int timestamp)
	{
		this.vector.put(s, timestamp);
	}
	
	/**
	 * Removes the entry associated with a server from the vector entirely. This
	 * should be called upon retirement.
	 * @param s
	 */
	public void remove(ServerID s)
	{
		this.vector.remove(s);
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
	
	public static boolean test()
	{
		VersionVector v1 = new VersionVector();
		
		ServerID first = new ServerID();
		ServerID second = new ServerID(first, 1);
		ServerID third = new ServerID(first, 2);
		ServerID fourth = new ServerID(third, 3);
		
		v1.update(first, 0);
		v1.update(second, 1);;
		v1.update(third, 2);
		v1.update(fourth, 3);
		
		System.out.println(String.format("Retrieved: %d Expected: %d", v1.getAcceptStamp(first), 0));
		System.out.println(String.format("Retrieved: %d Expected: %d", v1.getAcceptStamp(second), 1));
		System.out.println(String.format("Retrieved: %d Expected: %d", v1.getAcceptStamp(third), 2));
		System.out.println(String.format("Retrieved: %d Expected: %d", v1.getAcceptStamp(fourth), 3));
		
		v1.remove(fourth);
		
		System.out.println(String.format("Retrieved: %d Expected: %d", v1.getAcceptStamp(fourth), Integer.MIN_VALUE));
		
		v1.update(third, 4);
		
		System.out.println(String.format("Retrieved: %d Expected %d", v1.getAcceptStamp(fourth), Integer.MAX_VALUE));
		
		v1.update(second, 4);
		
		System.out.println(String.format("Retrieved: %d Expected %d", v1.getAcceptStamp(fourth), Integer.MAX_VALUE));
		
		v1.update(first, 1);
		
		System.out.println(String.format("Retrieved: %d Expected %d", v1.getAcceptStamp(fourth), Integer.MAX_VALUE));
		
		return false;
	}
}
