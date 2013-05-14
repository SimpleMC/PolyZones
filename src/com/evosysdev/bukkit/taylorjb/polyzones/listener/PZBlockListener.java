package com.evosysdev.bukkit.taylorjb.polyzones.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.evosysdev.bukkit.taylorjb.polyzones.PolyZones;

public class PZBlockListener extends PZListener
{
    /**
     * Initialize the block listener
     * 
     * @param plugin
     *            the plugin we are a listener for
     */
    public PZBlockListener(PolyZones plugin)
    {
        super(plugin);
    }

    /**
     * Check block placement to make sure player can do that in current zone
     * 
     * @param event
     *            block place event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event)
    {

    }

    /**
     * Check on block damage that the player doing the damage can actually damage blocks
     * 
     * @param even
     *            block break event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent event)
    {

    }
}
