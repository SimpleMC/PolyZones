package com.evosysdev.bukkit.taylorjb.polyzones;

import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.evosysdev.bukkit.taylorjb.polyzones.listener.PZBlockListener;
import com.evosysdev.bukkit.taylorjb.polyzones.listener.PZEntityListener;
import com.evosysdev.bukkit.taylorjb.polyzones.listener.PZPlayerListener;
import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone;
import com.evosysdev.bukkit.taylorjb.polyzones.zone.ZoneManager;
import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone.Flag;
import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone.State;

/**
 * Base class for the PolyZones Minecraft plugin using the Bukkit API
 * 
 * @author taylorjb
 *
 */
public class PolyZones extends JavaPlugin
{
    private ZoneManager zoneManager; // Zone manager

    /**
     * Set up Permissions and set up listeners
     */
    public void onEnable()
    {
        // create listener instances
        new PZPlayerListener(this);
        new PZBlockListener(this);
        new PZEntityListener(this);
        
        try
        {
            Wand.wandId = getConfig().getInt("wand.id", 280);
        }
        catch (NullPointerException npe)
        { // should happen on first-run when config doesn't exist
            getConfig().set("wand.id", 280);
            saveConfig();

            Wand.wandId = getConfig().getInt("wand.id", 280);
        }

        try
        {
            zoneManager = new ZoneManager(getDataFolder().getPath());
        }
        catch (IOException ioe)
        {
            getLogger().log(Level.SEVERE, "Error loading zones! Cannot continue", ioe);
            this.setEnabled(false);
            return;
        }
        catch (InvalidConfigurationException ice)
        {
            getLogger().log(Level.SEVERE, "Error loading zones! Cannot continue", ice);
            this.setEnabled(false);
            return;
        }
        
        // inform enable
        System.out.println(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
    }

    /**
     * plugin disabled
     */
    public void onDisable()
    {
        System.out.println("PolyZones disabled!");
    }

    @Override
    /**
     * When a command is typed by a <b>player</b> we want to handle it
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // unlike some plugins, we need the sender to be a player
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("polyzone") && player.hasPermission("polyzone"))
            {
                if (args.length > 0)
                {
                    // player's current wand
                    Wand playerWand = Wand.getWand(player);

                    if (playerWand != null)
                    {
                        sender.sendMessage(ChatColor.RED + "You are currently editing a zone with your wand! Exit edit mode do non-wand functions.");
                        return true;
                    }

                    if ((args[0].equalsIgnoreCase("mk") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("create"))
                            && player.hasPermission("polyzone.create"))
                    {
                        if (args.length > 1)
                        {
                            try
                            {
                                String name = args[1];
                                Zone parent = (args.length >= 3) ? zoneManager.getZone(args[2]) : zoneManager.getZone("everywhere");

                                // check that sender has permission to admin parent zone(can make child zones)
                                if (player.hasPermission("polyzone." + parent + ".admin"))
                                {
                                    // add a wand for this player
                                    Wand.addWand(player, new Wand(new Zone(name, player.getWorld().getName(), parent, zoneManager), this, player));
                                    sender.sendMessage(ChatColor.BLUE + "Zone " + name + " created with parent " + parent.getName()
                                            + "! Your wand is now in edit mode.");
                                    return true;
                                }
                            }
                            catch (IllegalArgumentException iae)
                            {
                                sender.sendMessage(ChatColor.RED + iae.getMessage());
                            }
                        }
                    }
                    else if ((args[0].equalsIgnoreCase("rm") || args[0].equalsIgnoreCase("del")) && player.hasPermission("polyzone.delete"))
                    { // remove a zone from our manager
                        if (args.length > 1)
                        {
                            String name = args[1];
                            zoneManager.deleteZone(zoneManager.getZone(name));
                            return true;
                        }
                    }
                    else if ((args[0].equalsIgnoreCase("ls") || args[0].equalsIgnoreCase("list")) && player.hasPermission("polyzone.list"))
                    { // list zones managed by our manager
                        player.sendMessage(zoneManager.toString());
                        return true;
                    }
                    else
                    { // not a normal pz sub-command, try reading a zone from it
                        Zone zone = zoneManager.getZone(args[0]);

                        // able to read the zone and have following arguments
                        if (zone != null && args.length > 1)
                        {
                            // allow player or group a zone permission
                            if (args[1].equalsIgnoreCase("allow") && player.hasPermission("polyzone." + zone.getName() + ".admin"))
                            {

                            }
                            else if (args[1].equalsIgnoreCase("revoke") && player.hasPermission("polyzone." + zone.getName() + ".admin"))
                            { // revoke player or group an allowed permission

                            }
                            else
                            { // not a permissions command, try to load a flag
                                if (args.length > 2)
                                {
                                    Flag flag = Flag.valueOf(args[1]);
                                    State state = State.valueOf(args[2]);
                                    
                                    // make sure neither flag or state are null before setting it
                                    if (flag != null && state != null)
                                    {
                                        zone.setFlag(flag, state);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else if (command.getName().equalsIgnoreCase("wand") && player.hasPermission("polyzone.edit"))
            {
                if (args.length > 0)
                {
                    Wand playerWand = Wand.getWand(player);
                    if (playerWand != null)
                    {
                        if (args[0].equalsIgnoreCase("save"))
                        {
                            try
                            {
                                playerWand.save(player);
                            }
                            catch (IOException ioe)
                            {
                                getLogger().log(Level.SEVERE, "Could not save zone!", ioe);
                                return true;
                            }
                            
                            sender.sendMessage(ChatColor.BLUE + "Zone " + playerWand.getZone().getName() + " saved!");
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("cancel"))
                        {
                            playerWand.cancel(player);
                            sender.sendMessage(ChatColor.RED + "Zone " + playerWand.getZone().getName() + " editing cancelled.");
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("reset"))
                        {
                            playerWand.reset();
                            sender.sendMessage(ChatColor.RED + "Zone " + playerWand.getZone().getName() + " pylons reset.");
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("setceil"))
                        {
                            // default to player location
                            int ceil = player.getLocation().getBlockY();

                            if (args.length > 1)
                            {
                                try
                                {
                                    ceil = Integer.parseInt(args[1]); // if they provide a ceil, use that
                                }
                                catch (NumberFormatException nfe)
                                {
                                    sender.sendMessage(ChatColor.RED + "Error reading numerical input!");
                                    return false;
                                }
                            }

                            try
                            {
                                playerWand.getZone().setCeil(ceil);
                                sender.sendMessage(ChatColor.BLUE + "Zone " + playerWand.getZone().getName() + " ceiling set to " + ceil + "!");
                            }
                            catch (IllegalArgumentException iae)
                            {
                                sender.sendMessage(ChatColor.RED + iae.getMessage());
                                return false;
                            }
                            return true;
                        }
                        else if (args[0].equalsIgnoreCase("setfloor"))
                        {
                            // default to player loc
                            int floor = player.getLocation().getBlockY();

                            if (args.length > 1)
                            {
                                try
                                {
                                    floor = Integer.parseInt(args[1]); // use supplied floor if given
                                }
                                catch (NumberFormatException nfe)
                                {
                                    sender.sendMessage(ChatColor.RED + "Error reading numerical input!");
                                    return false;
                                }
                            }

                            try
                            {
                                playerWand.getZone().setFloor(floor);
                                sender.sendMessage(ChatColor.BLUE + "Zone " + playerWand.getZone().getName() + " floor set to " + floor + "!");
                            }
                            catch (IllegalArgumentException iae)
                            {
                                sender.sendMessage(ChatColor.RED + iae.getMessage());
                                return false;
                            }

                            return true;
                        }
                    }
                    else
                    {
                        if (args[0].equalsIgnoreCase("edit"))
                        {
                            if (args.length > 1)
                            {
                                Zone editing = zoneManager.getZone(args[1]);

                                // don't edit a null zone or the everywhere zone
                                if (editing != null && editing != zoneManager.getZone("everywhere"))
                                {
                                    Wand.addWand(player, new Wand(editing, this, player));
                                    sender.sendMessage(ChatColor.BLUE + "You are now editing zone \"" + editing.getName() + "\"!");
                                    return true;
                                }
                            }
                            else
                            {
                                Zone editing = zoneManager.getContainedInZone(player.getLocation());

                                // editing the everywhere zone would be bad as is isn't actually a polygonal zone
                                if (editing != zoneManager.getZone("everywhere"))
                                {
                                    Wand.addWand(player, new Wand(editing, this, player));
                                    sender.sendMessage(ChatColor.BLUE + "You are now editing zone \"" + editing.getName() + "\"!");
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @return our zone manager
     */
    public ZoneManager getZoneManager()
    {
        return zoneManager;
    }
}
