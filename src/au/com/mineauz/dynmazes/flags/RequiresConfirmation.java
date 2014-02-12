package au.com.mineauz.dynmazes.flags;

public interface RequiresConfirmation<T>
{
	public String getConfirmationPrompt(T value);
}
