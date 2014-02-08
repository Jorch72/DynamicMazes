package au.com.mineauz.dynmazes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import au.com.mineauz.dynmazes.grid.GridMaze;

public class DynamicMazePlugin extends JavaPlugin
{
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		if(command.getName().equals("maze"))
		{
//			MazeGenerator gen = new GridMaze(((Player)sender).getLocation(), 20, 20);
//			gen.generate();
			FillTest test = new FillTest();
			test.fill(((Player)sender).getLocation());
			return true;
		}
		else if(command.getName().equals("mazeTemplate"))
		{
			if(!(sender instanceof Player))
				return false;
			
			if(args.length != 2)
				return false;
			
			int size, height;
			
			try
			{
				size = Integer.parseInt(args[0]);
				height = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e)
			{
				return false;
			}
			
			Block source = ((Player)sender).getLocation().getBlock();
			BlockFace face = Util.toFacingSimplest(((Player)sender).getEyeLocation().getYaw());

			for(int i = 0; i < 5; ++i)
			{
				for(int x = 0; x < size; ++x)
				{
					for(int z = 0; z < size; ++z)
					{
						for(int y = 0; y < height; ++y)
						{
							if (x != 0 && x != size - 1 && z != 0 && z != size - 1 && y != 0 && y != height - 1)
								continue;
							
							Block b = source.getRelative(x + face.getModX() * (i * (size + 1)), y, z + face.getModZ() * (i * (size + 1)));
							b.setType(Material.STONE);
						}
					}
				}
			}
			
			
			return true;
		}
		
		return false;
	}
}
