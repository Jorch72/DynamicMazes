package au.com.mineauz.dynmazes.commands;

import au.com.mineauz.dynmazes.commands.design.DesignCommandRoot;
import au.com.mineauz.dynmazes.commands.maze.CancelCommand;
import au.com.mineauz.dynmazes.commands.maze.ConfirmCommand;
import au.com.mineauz.dynmazes.commands.maze.DeleteMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.GenerateMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.NewMazeCommand;

public class MazeCommand extends RootCommandDispatcher
{
	public MazeCommand()
	{
		super("All DynamicMazes commands");
		
		registerCommand(new DesignCommandRoot());
		registerCommand(new NewMazeCommand());
		registerCommand(new GenerateMazeCommand());
		registerCommand(new DeleteMazeCommand());
		
		registerCommand(new ConfirmCommand());
		registerCommand(new CancelCommand());
	}
}
