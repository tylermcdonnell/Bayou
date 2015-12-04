package message;

/**
 * This message indicates that the receiver has been elected primary
 * by the previous primary process as part of its retirement sequence.
 * @author tyler
 *
 */
public class ElectPrimary extends Message
{
	private static final long serialVersionUID = 1L;

	@Override
	public String toString()
	{
		return "ElectPrimary: ";
	}
}
