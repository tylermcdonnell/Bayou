package server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
	 * Updates V with the max of every element this Version Vector.
	 * @param V
	 * 		Target vector.
	 */
	public void max(VersionVector V)
	{
		for (ServerID ID : this.vector.keySet())
		{
			int thatValue = V.get(ID);
			
			V.update(ID, Math.max(this.vector.get(ID), thatValue));
		}
	}
	
	/**
	 * @return
	 * 		All ServerIDs for which this vector has a recorded accept stamp.
	 */
	public List<ServerID> getIDs()
	{
		return new ArrayList<ServerID>(this.vector.keySet());
	}
	
	/**
	 * Returns the accempt stamp for the provided server or null if there is none.
	 * @param s
	 * 		Server you wish to retrieve accept stamp for.
	 */
	public int get(ServerID s)
	{
		return this.vector.get(s);
	}
	
	/**
	 * Returns the largest accept-stamp of any write known to this Version Vector
	 * that was originally accepted from by server s.
	 * 
	 * @param s				Server ID which we would like to know accept-stamp for.
	 */
	public int getAcceptStamp(ServerID s)
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
	
	/**
	 * Returns true if this VersionVector "dominates" the specified vector.
	 * W1 "dominates" W2 iff for all entries i in W2, W1[i] >= W2[i].
	 * 
	 * @param V 	
	 * 		VersionVector to compare.
	 * @return
	 * 		True if this vector dominates the specified vector. Else, false.
	 */
	public boolean dominates(VersionVector V)
	{
		for (ServerID id : V.getIDs())
		{
			Integer thisStamp = this.vector.get(id);
			Integer thatStamp = V.get(id);
			if (thisStamp == null ||
				thatStamp == null ||
				(int)thisStamp < (int)thatStamp)
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean dominatesTest()
	{
		VersionVector v1 = new VersionVector();
		VersionVector v2 = new VersionVector();
		
		ServerID s1 = new ServerID();
		ServerID s2 = new ServerID(s1, 1);
		ServerID s3 = new ServerID(s1, 2);
		ServerID s4 = new ServerID(s1, 3);
		ServerID s5 = new ServerID(s1, 4);
		
		v1.update(s1, 3);
		v1.update(s2, 3);
		v1.update(s3, 3);
		
		v2.update(s1, 3);
		v2.update(s2, 3);
		v2.update(s3, 3);
		
		System.out.println(String.format("Expected: <true> Actual: <%b>", v1.dominates(v2)));
		
		v2.update(s3, 4);
		
		System.out.println(String.format("Expected: <false> Actual: <%b>", v1.dominates(v2)));
		
		v1.update(s3, 4);
		v2.update(s4, 1);
		
		System.out.println(String.format("Expected: <false> Actual: <%b>", v1.dominates(v2)));
		
		v1.update(s4,  1);
		v1.update(s5,  1);
		
		System.out.println(String.format("Expected: <true> Actual: <%b>", v1.dominates(v2)));
		
		return true;
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
