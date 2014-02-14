package au.com.mineauz.dynmazes;

public interface GridBased<T extends INode>
{
	public int getWidth();
	public int getLength();
	
	public T getNodeAt(int x, int y);
}
