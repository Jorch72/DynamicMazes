package au.com.mineauz.dynmazes.styles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import org.apache.commons.lang.Validate;

public class StyleManager
{
	private static File mStyleFolder;
	
	private static HashMap<String, Style> mLoadedStyles = new HashMap<String, Style>();
	
	public static boolean initialize(File baseFolder)
	{
		mStyleFolder = new File(baseFolder, "styles");
		if(!mStyleFolder.exists())
			return mStyleFolder.mkdirs();
		
		return true;
	}
	
	public static Style newStyle(int size, int height)
	{
		return new Style(null,(byte)size, (byte)height);
	}
	
	public static void saveStyle(Style style)
	{
		Validate.notNull(style.getName());
		
		File dest = new File(mStyleFolder, style.getName() + ".yml");
		style.save(dest);
		
		mLoadedStyles.put(style.getName(), style);
	}
	
	public static Style getStyle(String name)
	{
		if(!styleExists(name))
			return null;
		
		Style style = mLoadedStyles.get(name);
		if(style != null)
			return style;
		
		style = new Style();
		File file = new File(mStyleFolder, name + ".yml");
		if(!style.read(file))
			return null;
		
		mLoadedStyles.put(style.getName(), style);
		return style;
	}
	
	public static void deleteStyle(String name) throws IOException
	{
		Validate.isTrue(styleExists(name), "Style " + name + " does not exist");
		
		mLoadedStyles.remove(name);
		
		Files.delete(new File(mStyleFolder, name + ".yml").toPath());
	}
	
	public static boolean styleExists(String name)
	{
		if(mLoadedStyles.containsKey(name))
			return true;
		
		return new File(mStyleFolder, name + ".yml").exists();
	}
	
}
