package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import message.Delete;
import message.Put;
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
		//System.out.println("WRITE: " + w.toString());
		if (!this.writes.contains(w))
		{
			this.writes.add(w);
		}
		Collections.sort(this.writes);
	}
	
	public void removeDuplicates()
	{
		Set<Write> hs = new HashSet<Write>();
		hs.addAll(this.writes);
		this.writes.clear();
		this.writes.addAll(hs);
		Collections.sort(this.writes);
	}
	
	/**
	 * @return		
	 * 		A soft copy of ordered committed writes in this log.
	 */
	public List<Write> getCommittedWrites()
	{		
		removeDuplicates();
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
		removeDuplicates();
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
		removeDuplicates();
		return new ArrayList<Write>(this.writes);
	}
	
	public void print()
	{
		for (Write w : getCommittedWrites())
		{
			if (w.action() instanceof Put || w.action() instanceof Delete)
			{
				System.out.println(String.format("%s:TRUE", w.action().toString()));	
			}
		}
		for (Write w : getTentativeWrites())
		{
			if (w.action() instanceof Put || w.action() instanceof Delete)
			{
				System.out.println(String.format("%s:FALSE", w.action().toString()));	
			}
		}
	}
	
	public void printAll()
	{
		removeDuplicates();
		for (Write w : getWrites())
		{
			System.out.println(w.toString());
		}
	}
	
	public static boolean test()
	{
		WriteLog log = new WriteLog();
		
		ServerID primary = new ServerID();
		ServerID secondary = new ServerID(primary, 1);
		
		Write w1 = new Write(primary, Integer.MAX_VALUE, 1, new Delete(""));
		w1.setCSN(1);
		Write w2 = new Write(primary, Integer.MAX_VALUE, 2, new Delete(""));
		w2.setCSN(2);
		Write w3 = new Write(primary, Integer.MAX_VALUE, 3, new Delete(""));
		Write w4 = new Write(secondary, Integer.MAX_VALUE, 3, new Delete(""));
		
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
