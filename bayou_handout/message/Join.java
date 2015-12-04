package message;

/**
 * Bayou Write indicating that a server wishes to join the system and be assigned
 * a ServerID.
 * @author tyler
 *
 */
public class Join extends WriteRequest 
{
	private static final long serialVersionUID = 1L;
	
	public Join()
	{
		
	}
	
	@Override
	public String toString()
	{
		return "Join";
	}
}
