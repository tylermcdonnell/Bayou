package message;

/**
 * Acknowledges receipt of a retirement Write.
 * @author tyler
 *
 */
public class RetireAck extends Message
{
	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString()
	{
		return "RetireAck";
	}
}
