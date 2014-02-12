package au.com.mineauz.dynmazes;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.mineauz.dynmazes.algorithm.DepthFirstAlgorithm;
import au.com.mineauz.dynmazes.algorithm.GrowingTreeAlgorithm;
import au.com.mineauz.dynmazes.algorithm.PrimsAlgorithm;
import au.com.mineauz.dynmazes.commands.MazeCommand;
import au.com.mineauz.dynmazes.styles.StyleManager;
import au.com.mineauz.dynmazes.types.GridMaze;
import au.com.mineauz.dynmazes.types.ModuleMaze;

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
		
		StyleManager.initialize(getDataFolder());
		MazeManager.initialize(new File(getDataFolder(), "mazes"));
		
		MazeManager.registerType("Module", ModuleMaze.class);
		MazeManager.registerType("Grid", GridMaze.class);
		
		MazeManager.registerAlgorithm("DepthFirst", DepthFirstAlgorithm.class);
		MazeManager.registerAlgorithm("Prims", PrimsAlgorithm.class);
		MazeManager.registerAlgorithm("GrowingTree", GrowingTreeAlgorithm.class);
		
		MazeCommand command = new MazeCommand();
		command.registerAs(getCommand("dynmaze"));
		
		MazeManager.loadMazes();
	}
	
	@Override
	public void onDisable()
	{
		MazeManager.saveMazes();
	}
}
