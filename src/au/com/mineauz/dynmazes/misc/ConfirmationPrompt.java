package au.com.mineauz.dynmazes.misc;

import java.util.WeakHashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ConfirmationPrompt
{
	private static WeakHashMap<Player, ConfirmationPrompt> mActivePrompts = new WeakHashMap<Player, ConfirmationPrompt>();
	
	private Player mPlayer;
	private Callback mCallback;
	private String mPrompt;
	
	public ConfirmationPrompt()
	{
		
	}
	
	public ConfirmationPrompt setPlayer(Player player)
	{
		mPlayer = player;
		return this;
	}
	
	public ConfirmationPrompt setCallback(Callback callback)
	{
		mCallback = callback;
		return this;
	}
	
	public ConfirmationPrompt setText(String promptText)
	{
		mPrompt = promptText;
		return this;
	}
	
	public void launch()
	{
		Validate.notNull(mPrompt);
		Validate.notNull(mPlayer);
		Validate.notNull(mCallback);
		
		mPlayer.sendMessage(mPrompt);
		mPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/dynmaze confirm &fto accept or \n&c/dynmaze cancel &fto reject."));
		mActivePrompts.put(mPlayer, this);
	}
	
	public static boolean accept(Player player)
	{
		ConfirmationPrompt prompt = mActivePrompts.remove(player);
		if(prompt == null)
			return false;
		
		prompt.mCallback.onComplete();
		return true;
	}
	
	public static boolean reject(Player player)
	{
		ConfirmationPrompt prompt = mActivePrompts.remove(player);
		if(prompt == null)
			return false;
		
		prompt.mCallback.onFailure(null);
		return true;
	}
}
