package au.com.mineauz.dynmazes;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.NotifiableTask;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class FillTask<T extends INode> extends NotifiableTask implements Runnable
{
	private Iterator<BlockVector> mIt;
	
	private BukkitTask mTask;
	
	private long mIntervalLimit;
	
	private World mWorld;
	private BlockVector mMin;
	private BlockVector mMax;
	private StoredBlock mType;
	
	public FillTask(BlockVector min, BlockVector max, World world, StoredBlock type)
	{
		this(min, max, world, type, null);
	}
	
	public FillTask(BlockVector min, BlockVector max, World world, StoredBlock type, Callback callback)
	{
		super(callback);
		
		mMin = min;
		mMax = max;
		mWorld = world;
		
		mType = type;

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
		
		while(mIt.hasNext())
		{
			if(System.nanoTime() - time >= mIntervalLimit)
				return;
			
			BlockVector node = mIt.next();
			mType.apply(mWorld.getBlockAt(node.getBlockX(), node.getBlockY(), node.getBlockZ()));
		}
		
		mTask.cancel();
		setCompleted();
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
