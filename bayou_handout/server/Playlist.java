package server;

import java.util.Hashtable;

import message.Delete;
import message.Put;
import message.Write;

/**
 * Implements a simple Key-Value Store on top of the write log:
 * 
 * 		<songName, URL>
 * 
 * @author tyler
 *
 */
public class Playlist 
{
	private WriteLog log;
	
	public Playlist(WriteLog log)
	{
		this.log = log;
	}
	
	/**
	 * Returns the URL for a song name. 
	 * 
	 * Note: Very inefficient. This creates the database from scratch on demand.
	 * 
	 * @param songName
	 * 		Name of the song being queried.
	 * 
	 * @return
	 * 		URL associated with the song or ERR_KEY if there is no song in the database.
	 */
	public String get(String songName)
	{
		try
		{
			Hashtable<String, String> h = new Hashtable<String, String>();
			for (Write w : this.log.getWrites())
			{
				if (w.action() instanceof Put)
				{
					Put p = (Put)w.action();
					h.put(p.songName, p.url);
				}
				if (w.action() instanceof Delete)
				{
					Delete d = (Delete)w.action();
					h.remove(d.songName);
				}
			}
			String url = h.get(songName);
			if (url == null)
			{
				return "ERR_KEY";
			}
			else
			{
				return url;
			}
		}
		catch (Exception exc)
		{
			return "ERR_KEY";
		}
	}
}
