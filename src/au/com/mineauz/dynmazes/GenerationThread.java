package au.com.mineauz.dynmazes;

public class GenerationThread<T extends INode> extends Thread
{
	private Maze<T> mMaze;
	
	public GenerationThread(Maze<T> maze)
	{
		mMaze = maze;
	}
	
	@Override
	public void run()
	{
		DynamicMazePlugin.getInstance().getLogger().info("Generation starting for " + mMaze.getName());
		
		mMaze.buildNodes();
		
		T root = mMaze.findStart();
		mMaze.getAlgorithm().generate(root);
		
		mMaze.processMaze(root);
		mMaze.setGenerationComplete();
		
		mMaze.draw();
		
		DynamicMazePlugin.getInstance().getLogger().info("Generation finished for " + mMaze.getName());
	}
}
