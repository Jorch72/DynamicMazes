package au.com.mineauz.dynmazes.commands;

import au.com.mineauz.dynmazes.commands.design.DesignCommandRoot;
import au.com.mineauz.dynmazes.commands.maze.CancelCommand;
import au.com.mineauz.dynmazes.commands.maze.ConfirmCommand;
import au.com.mineauz.dynmazes.commands.maze.DeleteMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.GenerateMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.InfoMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.ListMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.NewMazeCommand;
import au.com.mineauz.dynmazes.commands.maze.SetMazeCommand;

public class MazeCommand extends RootCommandDispatcher
{
	public MazeCommand()
	{
		super("All DynamicMazes commands");
		
		registerCommand(new DesignCommandRoot());
		registerCommand(new NewMazeCommand());
		registerCommand(new GenerateMazeCommand());
		registerCommand(new DeleteMazeCommand());
		registerCommand(new SetMazeCommand());
		registerCommand(new ListMazeCommand());
		registerCommand(new InfoMazeCommand());
		
		registerCommand(new ConfirmCommand());
		registerCommand(new CancelCommand());
	}
}
