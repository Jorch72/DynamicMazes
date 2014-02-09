package au.com.mineauz.dynmazes.styles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BlockVector;

import com.google.common.collect.HashMultimap;

public class Piece
{
	private byte mSize;
	private byte mHeight;
	
	private StoredBlock[] mBlocks;
	
	public Piece(byte size, byte height)
	{
		mSize = size;
		mHeight = height;
		
		mBlocks = new StoredBlock[mSize * mSize * mHeight];
	}
	
	public void setFrom(Location minCorner)
	{
		for(int y = 0; y < mHeight; ++y)
		{
			for(int x = 0; x < mSize; ++x)
			{
				for(int z = 0; z < mSize; ++z)
					mBlocks[x + z * (mSize) + y * (mSize * mSize)] = new StoredBlock(minCorner.getWorld().getBlockAt(minCorner.getBlockX() + x, minCorner.getBlockY() + y, minCorner.getBlockZ() + z).getState());
			}
		}
	}
	
	public void place(Location minCorner)
	{
		HashMultimap<BlockVector, BlockVector> dependencies = HashMultimap.create();
		ArrayList<BlockVector> blocksToAdd = new ArrayList<BlockVector>(dependencies.entries().size());
		
		// Place non dependent blocks and list dependent ones
		for(int y = 0; y < mHeight; ++y)
		{
			for(int x = 0; x < mSize; ++x)
			{
				for(int z = 0; z < mSize; ++z)
				{
					StoredBlock block = mBlocks[x + z * (mSize) + y * (mSize * mSize)];

					if(block.getLocation() == null)
						block.setLocation(new BlockVector(x,y,z));
					
					BlockFace face = block.getDependantFace();
					
					if(face == BlockFace.SELF || (face == BlockFace.DOWN && y == 0))
						blocksToAdd.add(block.getLocation());
					else
						dependencies.put(block.getLocationRelative(face), block.getLocation());
				}
			}
		}

		// Insert dependent blocks after their dependencies
		for(int i = 0; i < blocksToAdd.size(); ++i)
		{
			Set<BlockVector> dependents = dependencies.get(blocksToAdd.get(i));
			if(dependents != null)
			{
				for(BlockVector dependent : dependents)
					blocksToAdd.add(i+1, dependent);
				
				dependencies.removeAll(blocksToAdd.get(i));
			}
		}
		
		// Add any that are dependent on something that doesnt exist (or depedency loops)
		blocksToAdd.addAll(dependencies.values());
		
		for(BlockVector loc : blocksToAdd)
			mBlocks[loc.getBlockX() + loc.getBlockZ() * (mSize) + loc.getBlockY() * (mSize * mSize)].apply(minCorner.getWorld().getBlockAt(minCorner.getBlockX() + loc.getBlockX(), minCorner.getBlockY() + loc.getBlockY(), minCorner.getBlockZ() + loc.getBlockZ()));
	}
	
	public void save(ConfigurationSection parent)
	{
		for(int i = 0; i < mBlocks.length; ++i)
		{
			if(mBlocks[i].isAir())
				continue;
			
			ConfigurationSection block = parent.createSection(String.valueOf(i));
			mBlocks[i].save(block);
		}
	}
	
	public void read(ConfigurationSection parent)
	{
		Arrays.fill(mBlocks, new StoredBlock());
		for(String key : parent.getKeys(false))
		{
			int id = Integer.parseInt(key);
			mBlocks[id] = new StoredBlock();
			mBlocks[id].read(parent.getConfigurationSection(key));
		}
	}
}
