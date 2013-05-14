package com.evosysdev.bukkit.taylorjb.polyzones.zone;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone.Flag;
import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone.State;

public class ZoneManager
{
    private Map<String, Zone> zones; // map of zones we're managing
    private final String path; // path to zones/saving

    /**
     * Initialize our manager
     * 
     * @param path
     *            path to zones/saving
     * @throws IOException @see {@link #loadZones()} 
     * @throws InvalidConfigurationException @see {@link #loadZones()} 
     */
    public ZoneManager(String path) throws InvalidConfigurationException, IOException
    {
        zones = new HashMap<String, Zone>();
        this.path = path;
        loadZones();
    }

    /**
     * Add a zone to the manager
     * 
     * @param zone
     *            zone to be added
     * @throws IllegalArgumentException
     *             when another zone with that name already exists
     */
    public void addZone(Zone zone) throws IllegalArgumentException
    {
        if (zones.containsKey(zone.getName())) throw new IllegalArgumentException("A zone with that name already exists!");

        zones.put(zone.getName(), zone);
        saveZones(); // save our zone to the zone list file
    }

    /**
     * Delete a zone from the manager
     * 
     * @param zone
     *            zone to be deleted
     */
    public void deleteZone(Zone zone)
    {
        // if we're deleting a null zone, exit out
        if (zone == null) return;

        // go through zones to find all immediate children update parent
        for (Zone z : zones.values())
            if (z.getParent() == zone) z.setParent(zone.getParent());

        zones.remove(zone.getName()); // remove from the list
        (new File(path + "/zones/" + zone.getName().toLowerCase() + ".pz")).delete(); // delete file if one exists
    }

    /**
     * Find the zone with given name
     * 
     * @param name
     *            name of the zone we want to find
     * @return zone with given name or null if doesn't exist
     */
    public Zone getZone(String name)
    {
        return zones.get(name);
    }

    /**
     * Find the most specific zone this location is in
     * 
     * @param loc
     *            location we're looking for zone info for
     * @return most specific zone for location loc
     */
    public Zone getContainedInZone(Location loc)
    {
        return getContainedInZone(loc, zones.values(), null);
    }

    /**
     * Find the most specific zone this location is in
     * 
     * @param loc
     *            location we're looking for zone info for
     * @param zones
     *            the zone collection we're concerned with
     * @param current
     *            current zone location is a part of
     * @return most specific zone for location loc
     */
    private Zone getContainedInZone(Location loc, Collection<Zone> zones, Zone current)
    {
        for (Zone z : zones)
            if (z.inZone(loc.getBlockX(), loc.getBlockZ(), loc.getBlockY(), loc.getWorld().getName())) return getContainedInZone(loc,
                    getImmediateChildren(z), z);

        return current;
    }

    /**
     * Find a zone's immediate children
     * 
     * @param parent
     *            parent zone we're looking for children of
     * @return parent's immediate children
     */
    private Collection<Zone> getImmediateChildren(Zone parent)
    {
        Set<Zone> children = new HashSet<Zone>();

        for (Zone z : zones.values())
            if (z.getParent() == parent) children.add(z);

        return children;
    }

    /**
     * Serialize zone to file
     * 
     * @param zone
     *            zone to sale
     * @return success
     * @throws IOException if IOException when saving config
     */
    public void saveZone(Zone zone) throws IOException
    {
        System.out.println("Saving zone " + zone.getName());
        
        FileConfiguration save = new YamlConfiguration();
        save.set("zone.name", zone.getName());
        save.set("zone.parent", ((zone.getParent() == null) ? null : zone.getParent().getName()));
        save.set("zone.world", zone.getWorld());
        save.set("zone.greeting", zone.getGreeting());
        save.set("zone.farewell", zone.getFarewell());
        save.set("zone.ceiling", zone.getCeiling());
        save.set("zone.floor", zone.getFloor());
        save.set("zone.polygon.points", zone.getNumPoints());

        // make sure we actually have points to save
        if (zone.getNumPoints() > 0)
        {
            int count = 0;
            for (Point p : zone.getPoints())
            {
                save.set("zone.polygon.point" + (count) + ".x", p.getX());
                save.set("zone.polygon.point" + (count++) + ".y", p.getY());
            }
        }

        for (Flag f : Flag.values())
            save.set("zone.flags." + f.toString(), zone.getFlagState(f).toString());
        
        save.save(new File(path + "/zones/" + zone.getName().toLowerCase() + ".pz"));
    }

    /**
     * Save the zones data
     */
    public boolean saveZones()
    {
        try
        {
            (new File(path)).mkdir();
            File f = new File(path + "/zones.dat");
            f.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));

            // add all zones to the list
            for (String zone : zones.keySet())
            {
                writer.append(zone + ","); // add zone to the file
            }

            writer.close();
            return true;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

        return false;
    }

    /**
     * Load zones into the manager
     * @throws @see {@link #loadZone(String)}
     */
    public void loadZones() throws InvalidConfigurationException, IOException
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(path + "/zones.dat"));
            String[] zoneNames = reader.readLine().split(",");

            // go through all zones in the list and load their zone files
            for (String zoneName : zoneNames)
            {
                zones.put(zoneName, loadZone(zoneName));
            }
            
            reader.close(); // close reader
        }
        catch (FileNotFoundException fnfe)
        { // the zones file not found, lets create our everywhere zone
            System.out.println("Zones data not found, generating default...");
            Map<Flag, State> flags = new HashMap<Flag, State>();
            flags.put(Zone.Flag.HEALING, State.OFF);
            flags.put(Zone.Flag.MOBS, State.ON);
            flags.put(Zone.Flag.PVP, State.OFF);
            flags.put(Zone.Flag.SANCTUARY, State.OFF);
            Zone everywhere = new Zone("everywhere", "everywhere", this, flags);
            addZone(everywhere);
            saveZone(everywhere);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Load a zone by name
     * 
     * @param name
     *            name of zone to be loaded
     * @throws @see {@link FileConfiguration#load(File)}
     */
    private Zone loadZone(String name) throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        FileConfiguration load = new YamlConfiguration();
        load.load(new File(path + "/zones/" + name.toLowerCase() + ".pz"));

        // load necessary things to create zone
        String world = load.getString("zone.world");
        String parent = load.getString("zone.parent");

        // make the zone instance
        Zone zone = new Zone(name, world, this);
        if (!name.equalsIgnoreCase("everywhere")) zone = new Zone(name, world, getZone(parent), this);

        zone.setCeil(load.getInt("zone.ceiling", 128));
        zone.setFloor(load.getInt("zone.floor", 0));
        zone.setGreeting(load.getString("zone.greeting"));
        zone.setFarewell(load.getString("zone.farewell"));

        // load and set points
        for (int i = 0; i < load.getInt("zone.polygon.points", 0); i++)
            zone.addPoint(load.getInt("zone.polygon.point" + i + ".x", 0), load.getInt("zone.polygon.point" + i + ".y", 0));

        // load and set the flags
        for (Flag f : Flag.values())
            zone.setFlag(f, State.valueOf(load.getString("zone.flags." + f.toString())));

        return zone;
    }

    /**
     * @return String representation of our zone manager
     */
    public String toString()
    {
        return zones.keySet().toString();
    }
}
