package au.com.mineauz.dynmazes.misc.nms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.desht.dhutils.block.CraftMassBlockUpdate;
import me.desht.dhutils.block.MassBlockUpdate.RelightingStrategy;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import com.google.common.collect.ArrayListMultimap;

import au.com.mineauz.dynmazes.DynamicMazePlugin;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.DependencySortThread;
import au.com.mineauz.dynmazes.misc.MassBlockUpdater;
import au.com.mineauz.dynmazes.misc.TimeDividedTask;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class SpigotMassBlockUpdater extends MassBlockUpdater
{
	private ArrayListMultimap<World, StoredBlock> mBlocks;
	private HashMap<World, Integer> mMinimumY;
	
	// For execution
	private Iterator<World> mWorldIterator;
	
	public SpigotMassBlockUpdater()
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
		mWorldIterator = mBlocks.keySet().iterator();
		nextWorld();
	}
	
	private void nextWorld()
	{
		if (mWorldIterator.hasNext())
			processWorld(mWorldIterator.next());
		else
			setCompleted();
	}
	
	private void processWorld(final World world)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				List<StoredBlock> blocks = mBlocks.get(world);
				if (blocks.isEmpty())
					return;
				int minY = (mMinimumY.containsKey(world) ? mMinimumY.get(world)-1 : 0);
				
				// Sort the blocks
				blocks = new DependencySortThread(blocks, minY).call();
				System.out.println("Blocks: " + blocks.size());
				// Place the blocks
				new BlockPlacer(blocks, world, 10, TimeUnit.MILLISECONDS, new Callback()
				{
					@Override
					public void onFailure( Throwable exception )
					{
						setFailed(exception);
					}
					
					@Override
					public void onComplete()
					{
						nextWorld();
					}
				}).start(DynamicMazePlugin.getInstance());
			}
		}.runTaskAsynchronously(DynamicMazePlugin.getInstance());
	}
	
	private static class BlockPlacer extends TimeDividedTask<StoredBlock>
	{
		private CraftMassBlockUpdate mMBU;
		
		public BlockPlacer( Iterable<StoredBlock> collection, World world, long maxTime, TimeUnit unit, Callback callback )
		{
			super(collection, maxTime, unit, callback);
			
			mMBU = new CraftMassBlockUpdate(DynamicMazePlugin.getInstance(), world);
			mMBU.setRelightingStrategy(RelightingStrategy.DEFERRED);
		}

		@SuppressWarnings( "deprecation" )
		@Override
		protected void process( StoredBlock block )
		{
			mMBU.setBlock(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ(), block.getType().getId(), block.getData().getData());
		}
		
		@Override
		protected void done()
		{
			mMBU.notifyClients();
		}
	}
}
