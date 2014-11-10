package au.com.mineauz.dynmazes.styles;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class Style
{
	private String mName;
	private ListMultimap<PieceType, Piece> mPieces;
	private byte mPieceSize;
	private byte mHeight;
	
	public Style()
	{
		mPieces = ArrayListMultimap.create(); 
	}
	
	public Style(String name, byte size, byte height)
	{
		Validate.isTrue(size >= 4 && size <= 10);
		Validate.isTrue(height > 4);
		
		mPieceSize = size;
		mName = name;
		mHeight = height;
		
		mPieces = ArrayListMultimap.create();
	}
	
	public void setPieces(PieceType type, List<Piece> pieces)
	{
		Validate.notNull(pieces);
		mPieces.removeAll(type);
		mPieces.putAll(type, pieces);
	}
	
	public List<Piece> getPieces(PieceType type)
	{
		return mPieces.get(type);
	}
	
	public String getName()
	{
		return mName;
	}
	
	public int getHeight()
	{
		return mHeight;
	}
	
	public void setHeight(int height)
	{
		Validate.isTrue(height >= 3 && height <= 40);
		mHeight = (byte)height;
		for(Piece piece : mPieces.values())
			piece.setHeight(height);
	}
	
	
	public int getPieceSize()
	{
		return mPieceSize;
	}
	
	public void save(File file)
	{
		try
		{
			YamlConfiguration out = new YamlConfiguration();
			out.set("name", mName);
			out.set("size", mPieceSize);
			out.set("height", mHeight);
			
			ConfigurationSection pieces = out.createSection("pieces");
			
			for(PieceType type : PieceType.values())
			{
				List<Piece> versions = mPieces.get(type);
				for (int i = 0; i < versions.size(); ++i)
				{
					ConfigurationSection piece = pieces.createSection(type.name() + "-" + i);
					versions.get(i).save(piece);
				}
			}
			
			out.save(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean read(File file)
	{
		try
		{
			YamlConfiguration input = new YamlConfiguration();
			input.load(file);
			
			mName = input.getString("name");
			mPieceSize = (byte)input.getInt("size");
			mHeight = (byte)input.getInt("height");
			
			ConfigurationSection pieces = input.getConfigurationSection("pieces");
			
			for(String key : pieces.getKeys(false))
			{
				PieceType type;
				if (key.contains("-"))
					type = PieceType.valueOf(key.split("-")[0]);
				else
					type = PieceType.valueOf(key);
				
				Piece piece = new Piece(mPieceSize, mHeight);
				piece.read(pieces.getConfigurationSection(key));
				mPieces.put(type, piece);
			}
			
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
