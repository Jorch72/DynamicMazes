package au.com.mineauz.dynmazes.misc;

public class Profiler
{
	private String mName;
	private long mCount = 0;
	private long mTime = 0;
	private long mEnterTime = 0;
	
	public Profiler(String name)
	{
		mName = name;
	}
	
	public void enter()
	{
		mEnterTime = System.nanoTime();
	}
	
	public void leave()
	{
		mTime += (System.nanoTime() - mEnterTime);
		mEnterTime = 0;
		++mCount;
	}
	
	public void end()
	{
		System.out.println(String.format("Profiler: %s Count: %d AvgTime: %.3fms TotalTime: %.3fms", mName, mCount, (mTime / (double)mCount) / 1000000D, mTime / 1000000D));
		mCount = 0;
		mTime = 0;
	}
}
