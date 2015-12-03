package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import message.Get;
import message.Write;

/**
 * Bayou Write Log. This log manages both committed and tentative writes.
 * @author tsm
 *
 */
public class WriteLog {
	
	private List<Write> writes;
	
	public WriteLog()
	{
		this.writes = new ArrayList<Write>();
	}
	
	/**
	 * Commits a write, specified by the ServerID and accept stamp.
	 * If the specified write is already committed, does nothing. If
	 * there is no write in the log with the specified ServerID and
	 * accept stamp, does nothing. However, this should never happen.
	 * 
	 * Invariant: writes are sorted by total ordering. (see compareTo)
	 * 
	 * @param server		
	 * 			ID of server which first accepted write.
	 * @param stamp			
	 * 			Accept stamp given by server which first accepted.
	 * @param CSN
	 * 			Commit sequence number for this commit.
	 */
	public void commit(ServerID server, int stamp, int CSN)
	{
		for (Write w : this.writes)
		{
			if (w.server().equals(server) && w.stamp() == stamp)
			{
				w.setCSN(CSN);
			}
		}
		Collections.sort(this.writes);
	}
	
	/**
	 * Adds a new write to the log. If the write already exists
	 * as defined by its ServerID and accept stamp, does nothing.
	 * 
	 * Invariant: writes are sorted by total ordering. (see compareTo)
	 * 
	 * @param w
	 * 			Write to add to log.
	 */
	public void add(Write w)
	{
		if (!this.writes.contains(w))
		{
			this.writes.add(w);
		}
		Collections.sort(this.writes);
	}
	
	/**
	 * @return		
	 * 		A soft copy of ordered committed writes in this log.
	 */
	public List<Write> getCommittedWrites()
	{
		List<Write> committed = new ArrayList<Write>();
		for(Write w : this.writes)
		{
			if (w.CSN() != Integer.MAX_VALUE)
			{
				committed.add(w);
			}
		}
		Collections.sort(committed);
		return committed;
	}
	
	/**
	 * @return		
	 * 		A soft copy of ordered tentative writes in the log.
	 */
	public List<Write> getTentativeWrites()
	{
		List<Write> tentative = new ArrayList<Write>();
		for(Write w : this.writes)
		{
			if (w.CSN() == Integer.MAX_VALUE)
			{
				tentative.add(w);
			}
		}
		Collections.sort(tentative);
		return tentative;
	}
	
	/**
	 * @return		
	 * 		A soft copy of ordered writes (committed + tentative) in
	 * 		defined total order. (See class notes).
	 */
	public List<Write> getWrites()
	{
		return new ArrayList<Write>(this.writes);
	}
	
	public static boolean test()
	{
		WriteLog log = new WriteLog();
		
		ServerID primary = new ServerID();
		ServerID secondary = new ServerID(primary, 1);
		
		Write w1 = new Write(primary, 1, new Get(""));
		w1.setCSN(1);
		Write w2 = new Write(primary, 2, new Get(""));
		w2.setCSN(2);
		Write w3 = new Write(primary, 3, new Get(""));
		Write w4 = new Write(secondary, 3, new Get(""));
		
		log.add(w4);
		log.add(w3);
		log.add(w2);
		log.add(w1);

		System.out.println(String.format("Expected: \n<%s>\n<%s>", w1.toString(), w2.toString()));
		System.out.println("Actual: ");
		for (Write w : log.getCommittedWrites())
		{
			System.out.println(String.format("<%s>", w.toString()));
		}
		
		System.out.println(String.format("Expected: \n<%s>\n<%s>", w4.toString(), w3.toString()));
		System.out.println("Actual: ");
		for (Write w : log.getTentativeWrites())
		{
			System.out.println(String.format("<%s>", w.toString()));
		}
		
		System.out.println(String.format("Expected: \n<%s>\n<%s>\n<%s>\n<%s>", w1.toString(), w2.toString(), w4.toString(), w3.toString()));
		System.out.println("Actual: ");
		for (Write w : log.getWrites())
		{
			System.out.println(String.format("<%s>", w.toString()));
		}
		return true;
	}
}
