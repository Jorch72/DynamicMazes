package au.com.mineauz.dynmazes.misc;

public abstract class Notifiable
{
	private final Callback mCallback;
	
	public Notifiable(Callback callback)
	{
		mCallback = callback;
	}
	
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
