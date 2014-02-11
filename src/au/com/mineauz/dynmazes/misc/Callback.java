package au.com.mineauz.dynmazes.misc;

public interface Callback
{
	public void onComplete();
	
	public void onFailure(Throwable exception);
}
