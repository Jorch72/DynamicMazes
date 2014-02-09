package au.com.mineauz.dynmazes;

import org.bukkit.plugin.java.JavaPlugin;
import au.com.mineauz.dynmazes.commands.MazeCommand;

public class DynamicMazePlugin extends JavaPlugin
{
	private static DynamicMazePlugin mInstance;
	
	public static DynamicMazePlugin getInstance()
	{
		return mInstance;
	}
	
	@Override
	public void onEnable()
	{
		mInstance = this;
		MazeCommand command = new MazeCommand();
		command.registerAs(getCommand("dynmaze"));
	}
}
