package au.com.mineauz.dynmazes.misc;

public abstract class NotifiableTask implements Runnable
{
	private final Callback mCallback;
	
	public NotifiableTask(Callback callback)
	{
		mCallback = callback;
	}
	
	public abstract void run();
	
	protected void setCompleted()
	{
		if(mCallback != null)
			mCallback.onComplete();
	}
	
	protected void setFailed(Throwable exception)
	{
		if(mCallback != null)
			mCallback.onFailure(exception);
		else
			exception.printStackTrace();
	}
}
