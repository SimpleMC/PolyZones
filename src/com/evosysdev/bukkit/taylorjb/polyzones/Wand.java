package com.evosysdev.bukkit.taylorjb.polyzones;

import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.evosysdev.bukkit.taylorjb.polyzones.zone.Zone;

public class Wand
{
    private static Map<Player, Wand> wands = new HashMap<Player, Wand>(); // our wands
    public static int wandId = 280; // ID used for the wand

    private Zone editing; // zone this wand is editing
    private PolyZones plugin; // pluging using our wand

    // these lists should all stay parallel
    private List<Point> points; // list of points we have selected
    private List<LinkedList<Integer>> savedBlockMats; // saved block materials we changed with pylons
    private List<LinkedList<Block>> savedBlocks; // saved block materials we changed with pylons

    /**
     * Initialize the wand
     * 
     * @param player
     *            player who owns this wand
     * @param editing
     *            zone this wand is editing
     */
    public Wand(Zone editing, PolyZones plugin, Player p)
    {
        this.editing = editing;
        this.plugin = plugin;

        savedBlockMats = new LinkedList<LinkedList<Integer>>();
        savedBlocks = new LinkedList<LinkedList<Block>>();

        // if we're editing a new zone, we have an empty points list
        if (editing.isEmpty()) points = new LinkedList<Point>();
        else
        // editing an already established zone, get existing points
        {
            points = editing.getPoints();
            List<Block> blocks = new LinkedList<Block>();

            // all the blocks we need
            for (int i = 0; i < points.size(); i++)
            {
                Point curPoint = points.get(i);
                blocks.add(plugin.getServer().getWorld(editing.getWorld()).getBlockAt(curPoint.x, p.getLocation().getBlockY(), curPoint.y));
            }

            // add all the pylons
            addPylons(blocks);
        }
    }

    /**
     * Add pylons to all blocks in a list
     * 
     * @param blocks
     *            blocks to add pylons to
     */
    private void addPylons(List<Block> blocks)
    {
        for (Block b : blocks)
            addPylon(b);
    }

    /**
     * Toggle inclusion of a point
     * 
     * @param p
     *            point we are toggling
     * @return false if point was removed, true if added
     */
    public boolean togglePoint(Block block)
    {
        Point p = new Point(block.getX(), block.getZ());

        if (points.contains(p))
        {
            delPylon(points.indexOf(p));
            points.remove(new Point(block.getX(), block.getZ()));
            return false;
        }
        else
        {
            addPylon(block);
            points.add(p);
            return true;
        }
    }

    /**
     * Add a pylon above block
     * 
     * @param b
     *            block to add a pylon above
     */
    private void addPylon(Block b)
    {
        LinkedList<Integer> pylonMats = new LinkedList<Integer>();
        LinkedList<Block> pylonBlocks = new LinkedList<Block>();

        // save old blocks where we are putting a pylon and change blocks to bedrock
        for (int i = 0; i < 3; i++)
        {
            Block pylonBlock = b.getRelative(BlockFace.UP, i + 2);
            pylonMats.add(pylonBlock.getTypeId());
            pylonBlocks.add(pylonBlock);
            pylonBlock.setType(Material.BEDROCK);
        }

        savedBlockMats.add(pylonMats);
        savedBlocks.add(pylonBlocks);
    }

    /**
     * Remove a pylon by placing original points back
     * 
     * @param pylonIndex
     *            index in the list of the pylon
     */
    private void delPylon(int pylonIndex)
    {
        // set points of pylon to their saved values
        for (int i = 0; i < 3; i++)
            savedBlocks.get(pylonIndex).get(i).setTypeId(savedBlockMats.get(pylonIndex).get(i));

        // remove data from lists
        savedBlockMats.remove(pylonIndex);
        savedBlocks.remove(pylonIndex);
    }

    /**
     * Go through and clear all pylons
     */
    public void clearPylons()
    {
        while (!savedBlocks.isEmpty())
            delPylon(0);
    }

    /**
     * Save the zone we're editing and remove the wand from the list
     * 
     * @param player
     *            owner of the wand
     * @throws IOException if saving failed
     */
    public void save(Player player) throws IOException
    {
        // if a new zone, add it
        if (editing.isEmpty()) plugin.getZoneManager().addZone(editing);
        else
            // if not, just reset the selection
            editing.reset();

        // add all of our points to the zone before saving
        for (Point p : points)
        {
            if (p == null) throw new NullPointerException("DOH! There is a null point!");
            if (editing == null) throw new NullPointerException("DOH! Our zone appears to be null!");

            editing.addPoint(p);
        }

        clearPylons();
        wands.remove(player); // let's remove ourselves from the map
        plugin.getZoneManager().saveZone(editing); // save the zone
    }

    /**
     * Reset selection
     */
    public void reset()
    {
        clearPylons();
        points = new LinkedList<Point>();
    }

    /**
     * Remove wand without saving zone
     * 
     * @param player
     *            owner of the wand
     */
    public void cancel(Player player)
    {
        clearPylons();
        wands.remove(player); // let's remove ourselves from the map
    }

    /**
     * @return zone the wand s editing
     */
    public Zone getZone()
    {
        return editing;
    }

    /**
     * Add a wand to the map
     * 
     * @param player
     *            player controlling the wand
     * @param wand
     *            wand we're adding
     * @return if the wand was added to the map
     */
    public static boolean addWand(Player player, Wand wand)
    {
        // if we already have a wand for the player, return false and don't add
        if (wands.containsKey(player)) return false;

        wands.put(player, wand);
        return true;
    }

    /**
     * Get the wand of a given player
     * 
     * @param p
     *            player we want to find the wand of
     * @return player's wand or null if they have none
     */
    public static Wand getWand(Player p)
    {
        return wands.get(p);
    }
}
