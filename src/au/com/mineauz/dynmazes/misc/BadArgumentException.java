package au.com.mineauz.dynmazes.misc;

public class BadArgumentException extends RuntimeException
{
	private static final long serialVersionUID = -5099437186563532852L;
	
	private int mArg;
	
	public BadArgumentException(int argument)
	{
		mArg = argument;
	}
	
	public BadArgumentException(int argument, String reason)
	{
		super(reason);
		mArg = argument;
	}
	
	public int getArgument()
	{
		return mArg;
	}
}
