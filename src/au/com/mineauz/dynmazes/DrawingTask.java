package au.com.mineauz.dynmazes;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class DrawingTask<T extends INode> implements Runnable
{
	private Maze<T> mMaze;
	private Iterator<T> mIt;
	private BukkitTask mTask;
	
	private long mIntervalLimit;
	
	public DrawingTask(Maze<T> maze, Collection<T> allNodes)
	{
		mMaze = maze;
		mIt = allNodes.iterator();
	}
	
	public void start()
	{
		mTask = Bukkit.getScheduler().runTaskTimer(DynamicMazePlugin.getInstance(), this, 0, 1);
		mIntervalLimit = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void run()
	{
		long time = System.nanoTime();
		
		while(mIt.hasNext())
		{
			if(System.nanoTime() - time >= mIntervalLimit)
				return;
			
			T node = mIt.next();
			mMaze.placeNode(node);
		}
		
		mMaze.setDrawComplete();
		
		mTask.cancel();
	}
}
