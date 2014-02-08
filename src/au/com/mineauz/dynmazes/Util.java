package au.com.mineauz.dynmazes;

import org.bukkit.block.BlockFace;

public class Util
{
	private static BlockFace[] mDirOrder = {BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST_SOUTH_WEST, BlockFace.WEST, BlockFace.WEST_NORTH_WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_EAST, BlockFace.EAST_NORTH_EAST, BlockFace.EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_SOUTH_EAST};
	private static BlockFace[] mDirOrderSimple = {BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST};
	private static BlockFace[] mDirOrderSimpler = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
	
	public static BlockFace toFacing(float yaw)
	{
		if(yaw < 0)
			yaw += 360;
		
		float current = -11.25f;
		for(BlockFace dir : mDirOrder)
		{
			if(yaw >= current && yaw < current + 22.5f)
				return dir;
			
			current += 22.5f;
		}
		
		return BlockFace.SOUTH;
	}
	
	public static BlockFace toFacingSimple(float yaw)
	{
		if(yaw < 0)
			yaw += 360;
		
		float current = -22.5f;
		for(BlockFace dir : mDirOrderSimple)
		{
			if(yaw >= current && yaw < current + 45f)
				return dir;
			
			current += 45f;
		}
		
		return BlockFace.SOUTH;
	}
	
	public static BlockFace toFacingSimplest(float yaw)
	{
		if(yaw < 0)
			yaw += 360;
		
		float current = -45f;
		for(BlockFace dir : mDirOrderSimpler)
		{
			if(yaw >= current && yaw < current + 90f)
				return dir;
			
			current += 90f;
		}
		
		return BlockFace.SOUTH;
	}
}
