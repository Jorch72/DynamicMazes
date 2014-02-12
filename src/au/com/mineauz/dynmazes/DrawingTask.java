package au.com.mineauz.dynmazes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.DependencySortThread;
import au.com.mineauz.dynmazes.misc.NotifiableTask;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class DrawingTask<T extends INode> extends NotifiableTask implements Runnable
{
	private Maze<T> mMaze;
	private Iterator<T> mIt;
	private ArrayList<StoredBlock> mBlocks;
	
	private Future<List<StoredBlock>> mFuture;
	
	private Iterator<StoredBlock> mIt2;
	
	private BukkitTask mTask;
	
	// 0 = Gather blocks
	// 1 = Sort (wait for result)
	// 2 = Place blocks
	private int mStage;
	
	
	private long mIntervalLimit;
	
	public DrawingTask(Maze<T> maze, Collection<T> allNodes, Callback callback)
	{
		super(callback);
		
		mMaze = maze;
		mIt = allNodes.iterator();
		mStage = 0;
		
		mBlocks = new ArrayList<StoredBlock>();
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
		
		if(mStage == 0)
		{
			while(mIt.hasNext())
			{
				if(System.nanoTime() - time >= mIntervalLimit)
					return;
				
				T node = mIt.next();
				mMaze.placeNode(node, mBlocks);
			}
			
			DependencySortThread thread = new DependencySortThread(mBlocks);
			mFuture = thread.getFuture();
			
			thread.start();
			mStage = 1;
		}
		else if(mStage == 1)
		{
			if(mFuture.isDone())
			{
				try
				{
					mIt2 = mFuture.get().iterator();
					mStage = 2;
				}
				catch(InterruptedException e)
				{
					mTask.cancel();
					setFailed(e);
					return;
				}
				catch(ExecutionException e)
				{
					mTask.cancel();
					setFailed(e);
					return;
				}
			}
		}
		else if(mStage == 2)
		{
			while(mIt2.hasNext())
			{
				if(System.nanoTime() - time >= mIntervalLimit)
					return;
				
				StoredBlock block = mIt2.next();
				block.apply(mMaze.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()));
			}
			
			mTask.cancel();
			setCompleted();
		}
	}
}
