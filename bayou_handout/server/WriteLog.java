package server;

import java.util.ArrayList;
import java.util.List;

import message.Write;

/**
 * Bayou Write Log. This log manages both committed and tentative writes.
 * @author tsm
 *
 */
public class WriteLog {
	
	private List<Write> committed;
	private List<Write> tentative;
	
	public WriteLog()
	{
		this.committed = new ArrayList<Write>();
		this.tentative = new ArrayList<Write>();
	}
	
	/**
	 * Commits a write, specified by the ServerID and accept stamp.
	 * If the specified write is already committed, does nothing. If
	 * there is no write in the log with the specified ServerID and
	 * accept stamp, does nothing. However, this should never happen.
	 * @param server		
	 * 			ID of server which first accepted write.
	 * @param stamp			
	 * 			Accept stamp given by server which first accepted.
	 */
	public void commit(ServerID server, Integer stamp)
	{
		// First, check to make sure that it isn't already in 
		// the committed writes. It might be, if we are the
		// subject of more than one concurrent AE exchange.
		
		// Try to move from the list of tentative writes to
		// committed writes.
		
		// If for some reason we don't know about it, there's
		// been an error.
	}
	
	/**
	 * Adds a new write to the log. If the write already exists
	 * as defined by its ServerID and accept stamp, does nothing.
	 * @param w
	 * 			Write to add to log.
	 */
	public void add(Write w)
	{
		
	}
	
	/**
	 * @return		
	 * 		A soft copy of committed writes in this log.
	 */
	public List<Write> getCommittedWrites()
	{
		return new ArrayList<Write>(this.committed);
	}
	
	/**
	 * @return		
	 * 		A soft copy of tentative writes in thei log.
	 */
	public List<Write> getTentativeWrites()
	{
		return new ArrayList<Write>(this.tentative);
	}
	
	/**
	 * @return		
	 * 		A soft copy of ALL writes (committed + tentative) in
	 * 		defined total order. (See class notes).
	 */
	public List<Write> getWrites()
	{
		List<Write> all = new ArrayList<Write>();
		all.addAll(this.committed);
		all.addAll(this.tentative);
		return all;
	}
}
