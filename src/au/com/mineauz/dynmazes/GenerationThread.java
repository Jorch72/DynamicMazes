package au.com.mineauz.dynmazes;

import java.util.Collection;

import org.bukkit.Bukkit;

import au.com.mineauz.dynmazes.events.AsyncMazeGenerateEvent;
import au.com.mineauz.dynmazes.events.MazePostGenerateEvent;
import au.com.mineauz.dynmazes.misc.Callback;

public class GenerationThread<T extends INode> extends Thread
{
	private Maze<T> mMaze;
	private Callback mCallback;
	
	public GenerationThread(Maze<T> maze, Callback callback)
	{
		mMaze = maze;
		mCallback = callback;
	}
	
	@SuppressWarnings( "unchecked" )
	@Override
	public void run()
	{
		DynamicMazePlugin.getInstance().getLogger().info("Generation starting for " + mMaze.getName());
		
		mMaze.buildNodes();
		
		final T root = mMaze.findStart();
		mMaze.getAlgorithm().generate(root);
		
		mMaze.processMaze(root);
		Bukkit.getPluginManager().callEvent(new AsyncMazeGenerateEvent(mMaze, (Collection<INode>)mMaze.allNodes, root));
		
		mMaze.setGenerationComplete();
		
		Bukkit.getScheduler().runTask(DynamicMazePlugin.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				Bukkit.getPluginManager().callEvent(new MazePostGenerateEvent(mMaze, (Collection<INode>)mMaze.allNodes, root));
				mMaze.draw(mCallback);
			}
		});
		
		DynamicMazePlugin.getInstance().getLogger().info("Generation finished for " + mMaze.getName());
	}
}
