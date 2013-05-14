package com.evosysdev.bukkit.taylorjb.polyzones.zone;

import java.awt.Point;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Zone represents a polygonal 3d zone
 * 
 * @author TJ
 * 
 */
public class Zone
{
    private Polygon polyZone; // polygonal area of our zone
    private int ceiling, // top height of the zone
            floor; // bottom of the zone
    private Zone parent; // parent zone
    private String name, // zone name
            greeting, // zone greeting
            farewell, // zone farewell
            world; // zone's world
    private ZoneManager manager; // zone's manager

    private Map<Flag, State> flags; // zone flags

    /**
     * Initialize the zone. Only used to create first zone(everywhere), anything after should have a parent
     * 
     * @param name
     *            name for the Zone
     * @param world
     *            world this zone is for
     * @param manager
     *            zone's manager
     * @param flags
     *            flags for this zone
     */
    protected Zone(String name, String world, ZoneManager manager, Map<Flag, State> flags)
    {
        this.name = name;
        this.world = world;
        this.manager = manager;
        this.flags = flags;
        parent = null;
    }

    /**
     * Initialize the zone. Only used to create first zone(everywhere), anything after should have a parent
     * 
     * @param name
     *            name for the Zone
     * @param world
     *            world this zone is for
     * @param manager
     *            zone's manager
     */
    protected Zone(String name, String world, ZoneManager manager)
    {
        this.name = name;
        this.world = world;
        this.manager = manager;
        this.flags = new HashMap<Flag, State>();
        parent = null;
    }

    /**
     * Initialize the zone
     * 
     * @param name
     *            name for the Zone
     * @param parent
     *            parent of this zone
     */
    public Zone(String name, String world, Zone parent, ZoneManager manager) throws IllegalArgumentException
    {
        if (manager.getZone(name) != null) throw new IllegalArgumentException("A zone with that name already exists!");

        if (parent == null) throw new IllegalArgumentException("Invalid parent zone supplied!");

        this.name = name;
        this.world = world;
        this.manager = manager;
        flags = new HashMap<Flag, State>();
        setParent(parent);
        polyZone = new Polygon();

        ceiling = 128;
        floor = 0;
    }

    /**
     * Add a point to our zone
     * 
     * @param p
     *            point to be added
     */
    public void addPoint(Point p)
    {
        // exit if no zone(everywhere zone)
        if (polyZone == null) return;

        polyZone.addPoint(p.x, p.y);
    }

    /**
     * Add a point to our zone
     * 
     * @param x
     *            x coord of the point to be added
     * @param y
     *            y coord of the point to be added
     */
    public void addPoint(int x, int y)
    {
        // exit if no zone(everywhere zone)
        if (polyZone == null) return;

        polyZone.addPoint(x, y);
    }

    /**
     * Reset the polygon selection
     */
    public void reset()
    {
        // exit if no zone(everywhere zone)
        if (polyZone == null) return;

        polyZone.reset();
    }

    /**
     * Set the zone ceiling
     * 
     * @param ceiling
     *            new ceiling of the zone
     */
    public void setCeil(int ceiling) throws IllegalArgumentException
    {
        // if trying to set ceiling below floor, error
        if (ceiling < floor) throw new IllegalArgumentException("Ceiling must be above the floor!");

        this.ceiling = ceiling;
    }

    /**
     * Set the zone floor
     * 
     * @param floor
     *            new floor of the zone
     */
    public void setFloor(int floor) throws IllegalArgumentException
    {
        // if trying to set floor above ceiling, error
        if (floor > ceiling) throw new IllegalArgumentException("Floor must be below the ceiling!");

        this.floor = floor;
    }

    /**
     * Set the zone greeting
     * 
     * @param greeting
     *            new greeting of the zone
     */
    public void setGreeting(String greeting)
    {
        this.greeting = greeting;
    }

    /**
     * Set the zone farewell
     * 
     * @param greeting
     *            new farewell of the zone
     */
    public void setFarewell(String farewell)
    {
        this.farewell = farewell;
    }

    /**
     * Set the parent of this zone
     * 
     * @param parent
     *            new parent of the zone
     */
    public void setParent(Zone parent)
    {
        // TODO: check if parent contains this zone
        this.parent = parent;

        // if we don't have the flags yet, we will inherit parent's by default
        for (Flag f : Flag.values())
        {
            if (!flags.containsKey(f)) flags.put(f, State.INHERIT);
        }
    }

    /**
     * Set a flag's state, along with all children if they are set to inherit
     * 
     * @param flag
     *            flag who's state we will be setting
     * @param state
     *            state to set flag f to
     */
    public void setFlag(Flag flag, State state)
    {
        flags.put(flag, state);
    }

    /**
     * @return zone's parent
     */
    protected Zone getParent()
    {
        return parent;
    }

    /**
     * @return the world this zone is in
     */
    public String getWorld()
    {
        return world;
    }

    /**
     * @return zone name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the zone's greeting message
     */
    public String getGreeting()
    {
        return greeting;
    }

    /**
     * @return list of points in the polyzone
     */
    public List<Point> getPoints()
    {
        List<Point> points = new LinkedList<Point>();

        // go through all the points in the zone and add them
        for (int i = 0; i < polyZone.npoints; i++)
            points.add(new Point(polyZone.xpoints[i], polyZone.ypoints[i]));

        return points;
    }

    /**
     * @return number of points our polygon has
     */
    protected int getNumPoints()
    {
        // no zone(0 points)
        if (polyZone == null) return 0;

        return polyZone.npoints;
    }

    /**
     * @return zone's ceiling
     */
    protected int getCeiling()
    {
        return ceiling;
    }

    /**
     * @return zone's floor
     */
    protected int getFloor()
    {
        return floor;
    }

    /**
     * @return if the zone is an empty zone
     */
    public boolean isEmpty()
    {
        return (polyZone == null || polyZone.npoints == 0);
    }

    /**
     * @return the zone's farewell message
     */
    public String getFarewell()
    {
        return farewell;
    }

    /**
     * Get the state of the given flag
     * 
     * @param flag
     *            flag we're looking for the state of
     * @return state of flag flag
     */
    public State getFlagState(Flag flag)
    {
        // we should give the parent's flag state if we're inheriting
        if (flags.get(flag) == State.INHERIT) return parent.getFlagState(flag);

        return flags.get(flag);
    }

    /**
     * Check if the provided location are contained in this zone
     * 
     * @param x
     *            x coord of the location
     * @param y
     *            y coord of the location
     * @param z
     *            z(height) of the location
     * @return if the location is in the zone
     */
    public boolean inZone(int x, int y, int z, String world)
    {
        // if the x and y(lateral) coords are in the polygon and the height(z) is within the floor and ceil params, we're in the zone provided we're
        // on the right world
        return (getName().equalsIgnoreCase("everywhere") || (polyZone.contains(x, y) && z > floor && z < ceiling && (this.world.equals(world) || this.world
                .equals("everywhere")))) ? true : false;
    }

    /**
     * Available flags for a zone
     * 
     * @author TJ
     * 
     */
    public enum Flag
    {
        PVP, SANCTUARY, HEALING, MOBS, FIRE, LIGHTENING, CREEPER, TNT
    }

    /**
     * States for flags to be at
     * 
     * Using a State to be able to accept input of on/off and easily get the corresponding boolean value
     * 
     * @author TJ
     * 
     */
    public enum State
    {
        ON(true), OFF(false), INHERIT(true);

        private final boolean boolValue; // boolean value of the state

        State(boolean boolValue)
        {
            this.boolValue = boolValue;
        }

        /**
         * @return the boolean value of the state
         */
        public boolean getBoolValue()
        {
            return boolValue;
        }
    }
}
