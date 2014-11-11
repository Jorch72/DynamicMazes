package au.com.mineauz.dynmazes.misc.nms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import com.google.common.collect.ArrayListMultimap;

import au.com.mineauz.dynmazes.DynamicMazePlugin;
import au.com.mineauz.dynmazes.misc.DependencySortThread;
import au.com.mineauz.dynmazes.misc.MassBlockUpdater;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class FallbackMassBlockUpdater extends MassBlockUpdater implements Runnable
{
	private ArrayListMultimap<World, StoredBlock> mBlocks;
	private HashMap<World, Integer> mMinimumY;
	
	private long mIntervalLimit;
	
	// For execution
	private BukkitTask mTask;
	private Iterator<World> mWorldIterator;
	private Iterator<StoredBlock> mBlockIterator;
	private World mCurrentWorld;
	// For sorting
	private Future<List<StoredBlock>> mSortFuture;
	
	public FallbackMassBlockUpdater()
	{
		mBlocks = ArrayListMultimap.create();
		mMinimumY = new HashMap<World, Integer>();
	}
	
	@Override
	public void setBlock( World world, int x, int y, int z, StoredBlock material )
	{
		material.setLocation(new BlockVector(x, y, z));
		mBlocks.put(world, material);
		
		int minY = y;
		if (mMinimumY.containsKey(world))
			minY = Math.min(minY, mMinimumY.get(world));
		mMinimumY.put(world, minY);
	}

	@Override
	public void execute()
	{
		mTask = Bukkit.getScheduler().runTaskTimer(DynamicMazePlugin.getInstance(), this, 0, 1);
		mIntervalLimit = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MILLISECONDS);
		
		mWorldIterator = mBlocks.keySet().iterator();
	}
	
	@Override
	public void run()
	{
		if (mBlockIterator == null)
		{
			// Sorting has not yet been done
			if (mSortFuture == null)
			{
				// Submit for sorting
				if (mWorldIterator.hasNext())
				{
					mCurrentWorld = mWorldIterator.next();
					List<StoredBlock> blocks = mBlocks.get(mCurrentWorld);
					if (blocks.isEmpty())
						return;
					
					int minY = (mMinimumY.containsKey(mCurrentWorld) ? mMinimumY.get(mCurrentWorld)-1 : 0);
					
					DependencySortThread sorter = new DependencySortThread(blocks, minY);
					mSortFuture = sorter.getFuture();
					Thread thread = new Thread(sorter);
					thread.start();
				}
				// We are completely done
				else
				{
					mTask.cancel();
					setCompleted();
				}
			}
			// Check for completion
			else
			{
				if (mSortFuture.isDone())
				{
					try
					{
						mBlockIterator = mSortFuture.get().iterator();
						mSortFuture = null;
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
		}
		// Sorting is done, now place the blocks
		else
		{
			long time = System.nanoTime();
			
			while(mBlockIterator.hasNext())
			{
				if(System.nanoTime() - time >= mIntervalLimit)
					return;
				
				StoredBlock block = mBlockIterator.next();
				block.apply(mCurrentWorld.getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()));
			}
			
			// This world is done, go around again
			mBlockIterator = null;
		}
	}

}
