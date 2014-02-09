package au.com.mineauz.dynmazes.commands;

import au.com.mineauz.dynmazes.commands.design.DesignCommandRoot;

public class MazeCommand extends RootCommandDispatcher
{
	public MazeCommand()
	{
		super("dynmaze", "All DynamicMazes commands");
		
		registerCommand(new DesignCommandRoot());
	}
}
