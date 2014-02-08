package au.com.mineauz.dynmazes;

import java.util.ArrayDeque;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FillTest
{
	private HashSet<Long> mVisited = new HashSet<Long>();
	private World mWorld;
	private ArrayDeque<Long> mWaiting;
	
	private int mInitialX;
	private int mInitialZ;
	
	public FillTest()
	{
		
	}
	
	public void fill(Location from)
	{
		mWorld = from.getWorld();
		
		mInitialX = from.getBlockX();
		mInitialZ = from.getBlockZ();
		
		if(getBlock(from.getBlockX(), from.getBlockZ()).getType() == Material.BRICK)
			return;
		
		mWaiting = new ArrayDeque<Long>();
		
		process(locationToHash(from.getBlockX(), from.getBlockZ()));
		
		int count = 1;
		while(!mWaiting.isEmpty())
		{
			long loc = mWaiting.poll();
			
			process(loc);
			++count;
		}
		
		Bukkit.broadcastMessage("Count: " + count);
	}
	
	private void addWaiting(long location)
	{
		if(!mVisited.contains(location))
		{
			mWaiting.add(location);
			mVisited.add(location);
		}
	}
	
	private void process(long location)
	{
		int x = (int)location;
		int z = (int)(location >> 32);
		
		Block block = mWorld.getBlockAt(x, 128, z);
		if(x % 2 == 0 && z % 2 == 0)
			block.setType(Material.WOOL);
		
		
		int sides = getSides(x,z);
		for(int i = 0; i < 4; ++i)
		{
			if((sides & (1 << i)) != 0)
			{
				switch(i)
				{
				case 0:
					addWaiting(locationToHash(x + 1, z));
					break;
				case 1:
					addWaiting(locationToHash(x, z + 1));
					break;
				case 2:
					addWaiting(locationToHash(x - 1, z));
					break;
				case 3:
					addWaiting(locationToHash(x, z - 1));
					break;
				}
			}
		}
	}
	
	
	
	private Block getBlock(int x, int z)
	{
		Block block = mWorld.getHighestBlockAt(x, z);
		while(!block.getType().isSolid() && block.getY() > 0)
			block = block.getRelative(BlockFace.DOWN);
		
		return block;
	}
	private long locationToHash(int x, int z)
	{
		return (long)x | (long)z << 32;
	}
	
	private int getSides(int x, int z)
	{
		int sides = 0;
		
		if(x < mInitialX + 200 && getBlock(x + 1,z).getType() != Material.BRICK)
			sides |= 1;
		if(z < mInitialZ + 200 && getBlock(x,z + 1).getType() != Material.BRICK)
			sides |= 2;
		if(x > mInitialX - 200 && getBlock(x - 1,z).getType() != Material.BRICK)
			sides |= 4;
		if(z > mInitialZ - 200 && getBlock(x,z - 1).getType() != Material.BRICK)
			sides |= 8;
		
		return sides;
	}
	
	
	
}
