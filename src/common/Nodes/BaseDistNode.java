package common.Nodes;

import common.Messages.ColorChangeMsg;
import common.Messages.ForestJoinMsg;
import distributedAlgos.ColeVishkin;
import projects.colorDistribution.nodes.edges.DistBidirectionalEdge;
import sinalgo.configuration.AppConfig;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.runtime.Main;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class BaseDistNode extends Node {

    protected int round = 1;
    protected int roundAnchor = -1;

    protected int numOfVerteces = 90;
    protected int maxDegree = 4;
    
    /**
     * Vertex parents in the tree it belongs to, in the forest oriented by the edge labels
     */
    protected HashMap<Integer, BaseDistNode> parentsHash = new HashMap<>();

    protected HashMap<Integer, ArrayList<BaseDistNode>> children = new HashMap<>();

    /**
     * Holds all the labels in which the vertex is not a root
     */
    protected HashMap<Integer, Boolean> notRootsHash = new HashMap<>();
    
    /**
     * The vertex's color in a 3-color, per each forest. <Key = Forest, Value = color> 
     */
    protected HashMap<Integer, Integer> vertexCV3ColorsPerForest = new HashMap<>();

    /**
     * Node color bit strings for each forest.
     * Key is forest label
     * Value is bit string (as integer)
     *
     * Example: Get the color for forest 7:
     * <pre>
     * {@code int color = forestColor.get(7);}
     * </pre>
     */
    protected HashMap<Integer, Integer> forestColor = new HashMap<>();

    protected HashMap<Integer, ColeVishkin> forestCV = new HashMap<>();

    @Override
    public void init() {
    }

    /**
     * @param label
     * @return The parent of the vertex in a tree that belongs to a forest oriented by label.
     * Returns null if no such parent exists.
     */
    public BaseDistNode getParent(int label) {
        return parentsHash.get(label);
    }

    /**
     * @param label - The forest in which the vertex may be the root of a tree
     * @return - Is the vertex a root in a tree that belongs to the forest oriented by label
     */
    public boolean isRoot(int label) {
        if (notRootsHash.get(label) == null) {
            return true;
        }

        return notRootsHash.get(label);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(new Color(this.getUniColor() * 99999));
        drawAsDisk(g, pt, highlight, 5);
    }

    /**
     * Get united color (for all forests)
     * This should probably be implemented by sub-classes, according to different algos.
     *
     * @return combined forests color
     */
    public int getUniColor() {
        // quick & dirty solution to concat all colors
        String colorString = "";
        for (int forest : forestColor.keySet()) {
            colorString += forestColor.get(forest);
        }

        // convert back to int
        if (colorString.length() == 0)
            return 0;

        // Convert to longest int
        return (int)Long.parseLong(colorString) % Integer.MAX_VALUE;

    }

    /**
     * Gets the current color for this forest
     * @param forest
     * @return color int
     */
    public int getColorBitInt(int forest) {
        if (!forestColor.containsKey(forest))
            return 0;

        return forestColor.get(forest);
    }

    /**
     * Sets new color for the node on this forest
     * @param forest
     * @param colorBitInt
     */
    public void setColorBitInt(int forest, int colorBitInt) {
        forestColor.put(forest, colorBitInt);
    }

    /**
     * Notify the children of current color, per each forest.
     * Should be called whenever the color changes.
     * @param forest
     * @param colorBitInt
     */
    public void sendColorToChildren(int forest, int colorBitInt) {
        // Send new color to all the children on this forest
        ArrayList<BaseDistNode> forestChildren = children.get(forest);
        if (forestChildren == null || forestChildren.size() == 0)
            return; // no children

        for (BaseDistNode child : forestChildren) {
            send(new ColorChangeMsg(forest, colorBitInt), child);
        }
    }

    /**
     * Return true if none of this node's neighbors (parent or children) in the forest are using the color "color".
     *
     * @param forest
     * @param color
     * @return true if color is free
     */
    public boolean isColorFree(int forest, int color) {

        if (parentsHash.get(forest).getColorBitInt(forest) == color)
            return false; // color used by parent

        ArrayList<BaseDistNode> forestChildren = children.get(forest);
        if (forestChildren == null || forestChildren.size() == 0)
            return true; // no children

        for (BaseDistNode child : forestChildren) {
            if (child.getColorBitInt(forest) == color)
                return false; // color used by a child
        }

        return true;
    }

    /**
     * Called when a "forest join" message has arrived.
     * This message is assumed to be sent from a child.
     * Joins the current node to the forest: adds message sender to the children array
     * and initializes color to id on the new forest.
     * @param sender
     * @param forest
     */
    private void onForestJoinMessage(BaseDistNode sender, int forest) {
        // Add as child on this forest
        if (!children.containsKey(forest))
            children.put(forest, new ArrayList<BaseDistNode>());

        ArrayList<BaseDistNode> forestChildren = children.get(forest);
        forestChildren.add(sender);

        System.out.println("Node=" + this.ID +" joined forest " + forest + " Initing color to " + this.ID);
        setColorBitInt(forest, this.ID);
    }

    @Override
    public void handleMessages(Inbox inbox) {

        // Number of total nodes on the network
        int numNodes =  AppConfig.getAppConfig().generateNodesDlgNumNodes;

        // Handle incoming messages
        while(inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof ColorChangeMsg) {
                ColorChangeMsg m = (ColorChangeMsg)msg;
                updateParentColor(m.forest, m.newColor);
            } else if (msg instanceof ForestJoinMsg) {
                ForestJoinMsg m = (ForestJoinMsg)msg;
                onForestJoinMessage((BaseDistNode)inbox.getSender(), m.forest);
            }
        }

        // Forest Decomposition
        if (Global.currentTime == 1) {
            forestDecomposition();

        } else if (Global.currentTime > 2) {
            // Run cole-vishkin on all forests

            // cole-vishkin round is 1 less than global round number, as it starts on the 2nd round
            int cvRound = (int)Global.currentTime - 2;
            for (int forest : forestColor.keySet()) {
                if (!forestCV.containsKey(forest)) {
                    // Init ColeVishkin
                    ColeVishkin cv = new ColeVishkin(this, numNodes, forest);
                    forestCV.put(forest, cv);
                }
                // Get cv instance of this forest (created on round 2)
                ColeVishkin cv = forestCV.get(forest);
                if (cv.phase != ColeVishkin.Phase.COMPLETED) {
                    try {
                        ColeVishkin.Phase phase = cv.onRound(cvRound);

                        if (phase == ColeVishkin.Phase.COMPLETED) {
                            System.out.println("Node=" + this.ID + " Cole-Vishkin done! node on forest " + forest + " color " + getColorBitInt(forest));
                        }
                    } catch (Exception e) {
                        Main.fatalError("Node=" + this.ID + " Error running cole-Vishkin: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Updates the node's parent color (in node's inner representation)
     *
     * Note: this is not really needed in Sinalgo as there is only 1 real node instance per id
     * (there isn't really a different inner representation)
     * It is only done to show its possible and simulate a real network.
     * @param forest
     * @param color
     */
    private void updateParentColor(int forest, int color) {
        System.out.println("Node " + this.ID +"'s parent changed forest " + forest + " color to " + color);

        parentsHash.get(forest).setColorBitInt(forest, color);
    }

    /**
     * Decomposes the graph into at most delta forests
     */
    protected void forestDecomposition() {
        int forestLbl = 1;
        for (Edge e : this.outgoingConnections) {
            DistBidirectionalEdge distEdge = (DistBidirectionalEdge) e;
            // If start node's ID is smaller then end node's ID, than the edge is directed from start to end
            if (this.ID < e.endNode.ID) {

                // Set label for edge
                distEdge.setLabel(forestLbl);

                // Set the vertex parent in the forest oriented by label
                parentsHash.put(forestLbl, (BaseDistNode)e.endNode);
                
                // Not a root
                notRootsHash.put(forestLbl, false);

                // TODO: test only. delete later (good for grid2D test with 90 nodes)
                if (forestLbl == 1) {
                    distEdge.setColor(Color.RED);
                } else if (forestLbl == 2) {
                    distEdge.setColor(Color.GREEN);
                } else if (forestLbl == 3) {
                    distEdge.setColor(Color.BLUE);
                } else if (forestLbl == 4) {
                    distEdge.setColor(Color.YELLOW);
                }

                forestLbl++;
            } else {

                // "Remove" edge from screen
                distEdge.setColor(new Color(0, 0, 0, 0));

                // No label
                distEdge.setLabel(0);
            }
        }

        // Iterate all the forests we are currently members of
        for (int forest : parentsHash.keySet()) {
            setColorBitInt(forest, this.ID);
            // Update children
            sendColorToChildren(forest, this.ID);

            // Notify the parent it joined a forest
            BaseDistNode parent = parentsHash.get(forest);
            send(new ForestJoinMsg(forest), parent);
        }
    }

    @Override
    public void preStep() {

    }

    @Override
    public void neighborhoodChange() {

    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }
}
