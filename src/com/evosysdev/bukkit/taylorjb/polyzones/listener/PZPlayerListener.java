package com.evosysdev.bukkit.taylorjb.polyzones.listener;

import java.awt.Point;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.evosysdev.bukkit.taylorjb.polyzones.PolyZones;
import com.evosysdev.bukkit.taylorjb.polyzones.Wand;
import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone;

public class PZPlayerListener extends PZListener
{
    /**
     * Initialize the player listener
     * 
     * @param plugin
     *            plugin using the listener
     */
    public PZPlayerListener(PolyZones plugin)
    {
        super(plugin);
    }

    /**
     * Ensure the player can move in the zone they are in
     * 
     * @param event
     *            player move event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event)
    {

    }

    /**
     * Check interact event to make sure player can do it
     * 
     * @param event
     *            player interact event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        // TODO ensure permissions
        if (event.getItem() != null && event.getItem().getTypeId() == Wand.wandId)
        {
            Wand playerWand = Wand.getWand(event.getPlayer());

            // if the player is editing a zone
            if (playerWand != null)
            {
                Zone editing = playerWand.getZone(); // zone we're editing

                // left click sets points
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
                {
                    boolean added = playerWand.togglePoint(event.getClickedBlock());
                    Point point = new Point(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
                    event.getPlayer().sendMessage(
                            ChatColor.BLUE + "Point " + point.x + "," + point.y + (added ? " added to zone " : " removed from zone ")
                                    + editing.getName());
                }
                // right click sets vertical bounds
                else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
                {
                    event.getPlayer().sendMessage(ChatColor.GRAY + "Vertical loc: " + event.getClickedBlock().getY());
                }
            }
            else
            // not editing a zone, getInfo
            {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
                {
                    event.getPlayer().sendMessage(
                            "Current zone: " + ChatColor.GRAY
                                    + plugin.getZoneManager().getContainedInZone(event.getClickedBlock().getLocation()).getName());
                }
            }
        }
    }
}
