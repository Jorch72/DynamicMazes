package au.com.mineauz.dynmazes;

import java.util.Set;

import org.bukkit.util.BlockVector;

public interface INode
{
	public BlockVector toLocation();
	public INode[] getNeighbours();
	
	public Set<INode> getParents();
	public void addParent(INode node);
	
	public void addChild(INode node);
	public Set<INode> getChildren();
	
	/**
	 * Returns the minimum distance from this node to the root node 
	 * traversing the hierarchy
	 */
	public int getDepth();
}
