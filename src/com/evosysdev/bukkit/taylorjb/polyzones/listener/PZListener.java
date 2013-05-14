package com.evosysdev.bukkit.taylorjb.polyzones.listener;

import org.bukkit.event.Listener;

import com.evosysdev.bukkit.taylorjb.polyzones.PolyZones;

public abstract class PZListener implements Listener
{
    protected PolyZones plugin; // plugin using the listener

    /**
     * Initialize the listener
     * 
     * @param plugin
     *            plugin using the listener
     */
    public PZListener(PolyZones plugin)
    {
        this.plugin = plugin;
    }
}
