package au.com.mineauz.dynmazes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.DependencySortThread;
import au.com.mineauz.dynmazes.misc.NotifiableTask;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class ClearingTask extends NotifiableTask implements Runnable
{
	private Iterator<BlockVector> mIt;
	private ListIterator<StoredBlock> mIt2;
	
	private BukkitTask mTask;
	
	// 0 = Scan
	// 1 = Sort (wait for results)
	// 2 = Remove
	private int mStage = 0;
	
	private long mIntervalLimit;
	
	private World mWorld;
	private BlockVector mMin;
	private BlockVector mMax;
	
	private List<StoredBlock> mBlocks;
	private Future<List<StoredBlock>> mFuture;
	
	public ClearingTask(BlockVector min, BlockVector max, World world)
	{
		this(min, max, world, null);
	}
	
	public ClearingTask(BlockVector min, BlockVector max, World world, Callback callback)
	{
		super(callback);
		
		mMin = min;
		mMax = max;
		mWorld = world;
		
		mBlocks = new ArrayList<StoredBlock>((mMax.getBlockX() - mMin.getBlockX()) * (mMax.getBlockY() - mMin.getBlockY()) * (mMax.getBlockZ() - mMin.getBlockZ()));
		
		mStage = 0;
		mIt = new BlockIterator();
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
				
				BlockVector node = mIt.next();
				StoredBlock block = new StoredBlock(mWorld.getBlockAt(node.getBlockX(), node.getBlockY(), node.getBlockZ()).getState());
				block.setLocation(node);
				mBlocks.add(block);
			}
			
			mStage = 1;
			DependencySortThread thread = new DependencySortThread(mBlocks);
			thread.start();
			mFuture = thread.getFuture();
		}
		else if(mStage == 1)
		{
			if(mFuture.isDone())
			{
				mStage = 2;
				try
				{
					mBlocks = mFuture.get();
				}
				catch ( InterruptedException e )
				{
					mTask.cancel();
					setFailed(e);
					return;
				}
				catch ( ExecutionException e )
				{
					mTask.cancel();
					setFailed(e);
					return;
				}
				mIt2 = mBlocks.listIterator(mBlocks.size());
			}
		}
		else
		{
			while(mIt2.hasPrevious())
			{
				if(System.nanoTime() - time >= mIntervalLimit)
					return;
				
				StoredBlock block = mIt2.previous();
				mWorld.getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()).setType(Material.AIR);
			}
			
			mTask.cancel();
			setCompleted();
		}
	}
	
	private class BlockIterator implements Iterator<BlockVector>
	{
		private BlockVector mCurrent;
		
		public BlockIterator()
		{
			mCurrent = mMin.clone();
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public BlockVector next()
		{
			if(!hasNext())
				throw new NoSuchElementException();
			
			BlockVector vec = mCurrent.clone();
			
			mCurrent.setX(mCurrent.getBlockX() + 1);
			
			if(mCurrent.getX() >= mMax.getBlockX())
			{
				mCurrent.setX(mMin.getBlockX());
				mCurrent.setZ(mCurrent.getBlockZ() + 1);
				
				if(mCurrent.getZ() >= mMax.getBlockZ())
				{
					mCurrent.setZ(mMin.getBlockZ());
					mCurrent.setY(mCurrent.getBlockY() + 1);
				}
			}
			
			return vec;
		}
		
		@Override
		public boolean hasNext()
		{
			return mCurrent.getBlockY() < mMax.getBlockY();
		}
	}
}
