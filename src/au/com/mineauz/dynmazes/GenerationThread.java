package au.com.mineauz.dynmazes;

import org.bukkit.Bukkit;

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
	
	@Override
	public void run()
	{
		DynamicMazePlugin.getInstance().getLogger().info("Generation starting for " + mMaze.getName());
		
		mMaze.buildNodes();
		
		T root = mMaze.findStart();
		mMaze.getAlgorithm().generate(root);
		
		mMaze.processMaze(root);
		mMaze.setGenerationComplete();
		
		Bukkit.getScheduler().runTask(DynamicMazePlugin.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				mMaze.draw(mCallback);
			}
		});
		
		
		DynamicMazePlugin.getInstance().getLogger().info("Generation finished for " + mMaze.getName());
	}
}
