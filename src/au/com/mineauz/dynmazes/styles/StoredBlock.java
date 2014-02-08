package au.com.mineauz.dynmazes.styles;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;

@SuppressWarnings( "deprecation" )
public class StoredBlock
{
	private Material mType;
	private int mData;
	
	private Map<String, Object> mExtra;
	
	public StoredBlock()
	{
		
	}
	
	public StoredBlock(BlockState state)
	{
		mType = state.getType();
		mData = state.getData().getData();
		
		mExtra = new HashMap<String, Object>();

		if(state instanceof BrewingStand)
			mExtra.put("brewTime", ((BrewingStand) state).getBrewingTime());
		
		if(state instanceof CommandBlock)
		{
			mExtra.put("name", ((CommandBlock) state).getName());
			mExtra.put("cmd", ((CommandBlock) state).getCommand());
		}
		
		if(state instanceof CreatureSpawner)
		{
			mExtra.put("type", ((CreatureSpawner) state).getSpawnedType().name());
			mExtra.put("delay", ((CreatureSpawner) state).getDelay());
		}
		
		if(state instanceof Furnace)
		{
			mExtra.put("cook", ((Furnace) state).getCookTime());
			mExtra.put("burn", ((Furnace) state).getBurnTime());
		}
		
		if(state instanceof Jukebox)
		{
			if(((Jukebox) state).isPlaying())
				mExtra.put("jukebox", ((Jukebox) state).getPlaying().name());
		}
		
		if(state instanceof NoteBlock)
			mExtra.put("note", (short)((NoteBlock) state).getRawNote());
		
		if(state instanceof Sign)
		{
			mExtra.put("line1", ((Sign) state).getLine(0));
			mExtra.put("line2", ((Sign) state).getLine(1));
			mExtra.put("line3", ((Sign) state).getLine(2));
			mExtra.put("line4", ((Sign) state).getLine(3));
		}
		
		if(state instanceof Skull)
		{
			mExtra.put("rot", ((Skull) state).getRotation().name());
			if(((Skull) state).hasOwner())
				mExtra.put("owner", ((Skull) state).getOwner());
			mExtra.put("type", ((Skull) state).getSkullType().name());
		}
		
		if(mExtra.isEmpty())
			mExtra = null;
	}
	
	public void apply(Block block)
	{
		block.setType(mType);
		block.setData((byte)mData, false);
		
		BlockState state = block.getState();
		
		if(state instanceof BrewingStand)
			((BrewingStand) state).setBrewingTime((Integer)mExtra.get("brewTime"));
		
		if(state instanceof CommandBlock)
		{
			((CommandBlock) state).setName((String)mExtra.get("name"));
			((CommandBlock) state).setCommand((String)mExtra.get("cmd"));
		}
		
		if(state instanceof CreatureSpawner)
		{
			((CreatureSpawner) state).setSpawnedType(EntityType.valueOf((String)mExtra.get("type")));
			((CreatureSpawner) state).setDelay((Integer)mExtra.get("delay"));
		}
		
		if(state instanceof Furnace)
		{
			((Furnace) state).setCookTime((Short)mExtra.get("cook"));
			((Furnace) state).setBurnTime((Short)mExtra.get("burn"));
		}
		
		if(state instanceof Jukebox)
		{
			if(mExtra.containsKey("jukebox"))
				((Jukebox) state).setPlaying(Material.valueOf((String)mExtra.get("jukebox")));
		}
		
		if(state instanceof NoteBlock)
			((NoteBlock) state).setRawNote((byte)(short)(Short)mExtra.get("note"));
		
		if(state instanceof Sign)
		{
			((Sign) state).setLine(0, (String)mExtra.get("line1"));
			((Sign) state).setLine(1, (String)mExtra.get("line2"));
			((Sign) state).setLine(2, (String)mExtra.get("line3"));
			((Sign) state).setLine(3, (String)mExtra.get("line4"));
		}
		
		if(state instanceof Skull)
		{
			((Skull) state).setRotation(BlockFace.valueOf((String)mExtra.get("rot")));
			if(mExtra.containsKey("owner"))
				((Skull) state).setOwner((String)mExtra.get("owner"));

			((Skull) state).setSkullType(SkullType.valueOf((String)mExtra.get("type")));
		}
	}
	
	public void write(DataOutput output) throws IOException
	{
		output.writeUTF(mType.name());
		output.writeByte(mData);
		
		if(mExtra != null)
		{
			output.writeByte(mExtra.size());
			
			for(Entry<String, Object> entry : mExtra.entrySet())
			{
				output.writeUTF(entry.getKey());
				
				if(entry.getValue() instanceof String)
				{
					output.writeByte(0);
					output.writeUTF((String)entry.getValue());
				}
				else if(entry.getValue() instanceof Short)
				{
					output.writeByte(1);
					output.writeShort((Short)entry.getValue());
				}
				else if(entry.getValue() instanceof Integer)
				{
					output.writeByte(2);
					output.writeInt((Integer)entry.getValue());
				}
				else
					throw new IllegalArgumentException("Bad type " + entry.getValue().getClass().getName());
			}
		}
		else
			output.writeByte(0);
	}
	
	public void read(DataInput input) throws IOException
	{
		mType = Material.getMaterial(input.readUTF());
		mData = input.readByte() & 15;
		
		int count = input.readByte();
		
		if(count > 0)
		{
			mExtra = new HashMap<String, Object>();
			for(int i = 0; i < count; ++i)
			{
				String name = input.readUTF();
				int type = input.readByte();
				switch(type)
				{
				case 0: // String
					mExtra.put(name, input.readUTF());
					break;
				case 1: // Short
					mExtra.put(name, input.readShort());
					break;
				case 2: // Int
					mExtra.put(name, input.readInt());
					break;
				default:
					throw new IllegalArgumentException("Bad type " + type);
				}
			}
		}
		else
			mExtra = null;
	}
}
