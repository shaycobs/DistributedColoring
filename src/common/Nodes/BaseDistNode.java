package common.Nodes;

import common.Messages.ColorChangeMsg;
import common.Messages.ForestJoinMsg;
import common.globals.BaseCustomGlobal;
import distributedAlgos.ColeVishkin;
import projects.colorDistribution.nodes.edges.DistBidirectionalEdge;
import projects.fasterColoring.nodes.messages.NeighborColorMessage;
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
import java.util.Vector;

public class BaseDistNode extends Node {
	
	/**
	 * The node color after calculations
	 */
	protected int uniColor = 0;
	
	/**
	 * False when the algorithm finishes with the CV stage
	 */
	protected boolean isCv = true;
	
	/**
	 * Indicates to start the final step in the inheriting sub-class
	 */
	protected boolean finalStep = true;
	
	/**
	 * Indicate the sub class to hold executing
	 */
	protected boolean isHold = false;
	
	/**
	 * Stores if neighbors are colored in color Key
	 */
	protected Vector<Boolean> colorPalette = new Vector<>();
    
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
    	return this.uniColor;
    }

    /**
     * Set the unified color of the node
     * @param uniColor
     */
    public void setUniColor(int uniColor) {
		this.uniColor = uniColor;
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
        
        colorPalette.clear();
        
        // Initialize color palette
		for (int i = 0; i <= BaseCustomGlobal.maxDegree; i++) {
			colorPalette.add(false);
		}
		
        // Handle incoming messages
        while(inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof ColorChangeMsg) {
                ColorChangeMsg m = (ColorChangeMsg)msg;
                updateParentColor(m.forest, m.newColor);
            } else if (msg instanceof ForestJoinMsg) {
                ForestJoinMsg m = (ForestJoinMsg)msg;
                onForestJoinMessage((BaseDistNode)inbox.getSender(), m.forest);
            } else if (msg instanceof NeighborColorMessage) {
				if (((NeighborColorMessage) msg).color < BaseCustomGlobal.maxDegree + 1) {
					colorPalette.set(((NeighborColorMessage) msg).color, true);
				}
			}
        }

        // Forest Decomposition
        if (Global.currentTime == 1) {
            forestDecomposition();

        } else if ((Global.currentTime > 2) && isCv) {
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
                            System.out.println("CV Node=" + this.ID + " Cole-Vishkin done! node on forest " + forest + " color " + getColorBitInt(forest));
                        }
                    } catch (Exception e) {
                        Main.fatalError("Node=" + this.ID + " Error running cole-Vishkin: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // Check if CV should calculate in the next round
            isCv = isNotAllComplete();
            if (!isCv) isHold = true;
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
        System.out.println("CV Node " + this.ID +"'s parent changed forest " + forest + " color to " + color);

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

                // TODO: test only. delete later
                colorEdge(distEdge, forestLbl);

                forestLbl++;
            } else {

                // "Remove" edge from screen
                distEdge.setColor(new Color(0, 0, 0, 0));

                // No label
                distEdge.setLabel(0);
            }
        }
        
        // Iterate all the forest
        for (int forest = 1; forest <= BaseCustomGlobal.maxDegree; forest++) {
	        setColorBitInt(forest, this.ID);
	        // Update children
	        sendColorToChildren(forest, this.ID);
	
	        // Notify the parent it joined a forest
	        BaseDistNode parent = parentsHash.get(forest);
	        if (parent != null) {
	        	send(new ForestJoinMsg(forest), parent);
	        }
        }
    }
    
    /**
     * Colors the edge with a color distinct to the forest it participates in, for a nice visualization
     * @param distEdge - The edge
     * @param forestLbl - Forest label
     */
    private void colorEdge(DistBidirectionalEdge distEdge, int forestLbl) {
    	switch (forestLbl) {
    	case(1):
    		distEdge.setColor(Color.RED);
    		break;
    	case(2):
    		distEdge.setColor(Color.GREEN);
    		break;
    	case(3):
    		distEdge.setColor(Color.BLUE);
    		break;
    	case(4):
    		distEdge.setColor(Color.YELLOW);
    		break;
    	case(5):
    		distEdge.setColor(Color.ORANGE);
    		break;
    	case(6):
    		distEdge.setColor(Color.BLACK);
    		break;
    	case(7):
    		distEdge.setColor(Color.CYAN);
    		break;
    	case(8):
    		distEdge.setColor(Color.DARK_GRAY);
    		break;
    	case(9):
    		distEdge.setColor(Color.LIGHT_GRAY);
    		break;
    	case(10):
    		distEdge.setColor(Color.MAGENTA);
    		break;
    	default:
    		distEdge.setColor(Color.PINK);    		
    	}
    }
    
    /**
     * @return true if all forests are done with Cole Vishkin. false otherwise.
     */
    protected boolean isNotAllComplete() {
    	for (ColeVishkin cv : forestCV.values()) {
			if (cv.phase != ColeVishkin.Phase.COMPLETED) {
				return true;
			}
		}
    	
    	return false;
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
