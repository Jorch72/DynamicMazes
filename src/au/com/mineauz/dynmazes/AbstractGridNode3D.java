package au.com.mineauz.dynmazes;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.BlockFace;

public abstract class AbstractGridNode3D extends AbstractNode
{
	private static BlockFace[] sides = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
	
	protected int x;
	protected int y;
	protected int z;
	
	protected CubeBased<?> maze;
	
	public AbstractGridNode3D(CubeBased<?> maze, int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.maze = maze;
	}
	
	@Override
	public INode[] getNeighbours()
	{
		if(x == 0)
		{
			if(y == 0)
			{
				if(z == 0)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1)};
				if(z == maze.getLength() - 1)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z - 1)};
				
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
			}
			if(y == maze.getHeight() - 1)
			{
				if(z == 0)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z + 1)};
				if(z == maze.getLength() - 1)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z - 1)};
				
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
			}
			
			if(z == 0)
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1)};
			if(z == maze.getLength() - 1)
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z - 1)};
			
			return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
		}
		else if(x == maze.getWidth() - 1)
		{
			if(y == 0)
			{
				if(z == 0)
					return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1)};
				if(z == maze.getLength() - 1)
					return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z - 1)};
				
				return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
			}
			if(y == maze.getHeight() - 1)
			{
				if(z == 0)
					return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z + 1)};
				if(z == maze.getLength() - 1)
					return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z - 1)};
				
				return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
			}
			
			if(z == 0)
				return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1)};
			if(z == maze.getLength() - 1)
				return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z - 1)};
			
			return new INode[] {maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
		}
		else
		{
			if(y == 0)
			{
				if(z == 0)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1)};
				if(z == maze.getLength() - 1)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z - 1)};
				
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
			}
			if(y == maze.getHeight() - 1)
			{
				if(z == 0)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z + 1)};
				if(z == maze.getLength() - 1)
					return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z - 1)};
				
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
			}
			
			if(z == 0)
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1)};
			if(z == maze.getLength() - 1)
				return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z - 1)};
			
			return new INode[] {maze.getNodeAt(x + 1, y, z), maze.getNodeAt(x - 1, y, z), maze.getNodeAt(x, y - 1, z), maze.getNodeAt(x, y + 1, z), maze.getNodeAt(x, y, z + 1), maze.getNodeAt(x, y, z - 1)};
		}
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getZ()
	{
		return z;
	}
	
	public BlockFace toNode(AbstractGridNode3D node)
	{
		int xx = node.x - x;
		int yy = node.y - y;
		int zz = node.z - z;
		
		for(BlockFace dir : sides)
		{
			if(dir.getModX() == xx && dir.getModY() == yy && dir.getModZ() == zz)
				return dir;
		}
		
		return BlockFace.SELF;
		
	}
	
	public Set<BlockFace> getConnections()
	{
		HashSet<BlockFace> others = new HashSet<BlockFace>(getChildren().size() + 1);
		
		for(INode child : getChildren())
			others.add(toNode((AbstractGridNode3D)child));
		
		for(INode parent : getParents())
			others.add(toNode((AbstractGridNode3D)parent));
		
		return others;
	}

	@Override
	public int hashCode()
	{
		return x & (0xFFF) | (z & 0xFFF) << 12 | (y & 0xFF) << 24;
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof AbstractGridNode3D))
			return false;
		
		AbstractGridNode3D other = (AbstractGridNode3D)obj;
		
		return x == other.x && y == other.y && z == other.z;
	}
	
	@Override
	public String toString()
	{
		return String.format("{%d,%d,%d}", x, y, z);
	}
}
