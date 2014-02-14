package au.com.mineauz.dynmazes;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.BlockFace;

public abstract class AbstractGridNode extends AbstractNode
{
	protected int x;
	protected int y;
	
	protected GridBased<?> maze;
	
	public AbstractGridNode(GridBased<?> maze, int x, int y)
	{
		this.x = x;
		this.y = y;
		this.maze = maze;
	}
	
	@Override
	public INode[] getNeighbours()
	{
		if(x == 0)
		{
			if(y == 0)
				return new INode[] {maze.getNodeAt(x + 1, y), maze.getNodeAt(x, y + 1)};
			if(y == maze.getLength() - 1)
				return new INode[] {maze.getNodeAt(x + 1, y), maze.getNodeAt(x, y - 1)};
			
			return new INode[] {maze.getNodeAt(x + 1, y), maze.getNodeAt(x, y - 1), maze.getNodeAt(x, y + 1)};
		}
		else if(x == maze.getWidth() - 1)
		{
			if(y == 0)
				return new INode[] {maze.getNodeAt(x - 1, y), maze.getNodeAt(x, y + 1)};
			if(y == maze.getLength() - 1)
				return new INode[] {maze.getNodeAt(x - 1, y), maze.getNodeAt(x, y - 1)};
			
			return new INode[] {maze.getNodeAt(x - 1, y), maze.getNodeAt(x, y - 1), maze.getNodeAt(x, y + 1)};
		}
		else
		{
			if(y == 0)
				return new INode[] {maze.getNodeAt(x + 1, y), maze.getNodeAt(x - 1, y), maze.getNodeAt(x, y + 1)};
			if(y == maze.getLength() - 1)
				return new INode[] {maze.getNodeAt(x + 1, y), maze.getNodeAt(x - 1, y), maze.getNodeAt(x, y - 1)};
			
			return new INode[] {maze.getNodeAt(x + 1, y), maze.getNodeAt(x - 1, y), maze.getNodeAt(x, y - 1), maze.getNodeAt(x, y + 1)};
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
	
	private BlockFace toNode(AbstractGridNode node)
	{
		int xx = node.x - x;
		int yy = node.y - y;
		
		for(BlockFace dir : Util.dirOrderSimpler)
		{
			if(dir.getModX() == xx && dir.getModZ() == yy)
				return dir;
		}
		
		return BlockFace.SELF;
		
	}
	
	public Set<BlockFace> getConnections()
	{
		HashSet<BlockFace> others = new HashSet<BlockFace>(getChildren().size() + 1);
		
		for(INode child : getChildren())
			others.add(toNode((AbstractGridNode)child));
		
		if(getParent() != null)
			others.add(toNode((AbstractGridNode)getParent()));
		
		return others;
	}

	@Override
	public int hashCode()
	{
		return x | y << 16;
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof AbstractGridNode))
			return false;
		
		AbstractGridNode other = (AbstractGridNode)obj;
		
		return x == other.x && y == other.y;
	}
	
	@Override
	public String toString()
	{
		return String.format("{%d,%d}", x, y);
	}
}
