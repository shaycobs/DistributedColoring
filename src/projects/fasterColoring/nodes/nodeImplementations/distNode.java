package projects.fasterColoring.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;

import projects.colorDistribution.nodes.edges.DistBidirectionalEdge;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;

public class DistNode extends Node {
	
	int round = 1;
	
	/**
	 * Vertex parents in the tree it belongs to, in the forest oriented by the edge labels
	 */
	HashMap<Integer, Node> parentsHash = new HashMap<>(); 
	
	/**
	 * Is this vertex a root in the tree it belongs to, in the forest oriented by the edge labels
	 */
	HashMap<Integer, Boolean> rootsHash = new HashMap<>();
	
	@Override
	public void handleMessages(Inbox inbox) {		
		// Forest Decomposition
		if (round == 1) {
			forestDecomposition();
		} else if (round == 2) {
			// TODO: Test, delete later
			for (int label = 1; label < 5; label++) {
				if (getParent(label) != null)
					System.out.println("Vertex " + ID + " parent is " + getParent(label).ID + " for label " + label);
				
				if (isRoot(label))
					System.out.println("Vertex " + ID + " is a root in forest " + label);
			}
		}
		
		// Increase number of rounds
		round++;
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		
	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.setColor(new Color(ID*99999));
		drawAsDisk(g, pt, highlight, 5);
	}
	
	/**
	 * Orients the graph into at most delta forests
	 */
	private void forestDecomposition() {
		int label = 1;
		for (Edge e : this.outgoingConnections) {
			// If start node's ID is smaller then end node's ID, than the edge is directed from start to end
			if (this.ID < e.endNode.ID) {
				// Debugging
				System.out.println("set label " + label + " for edge from " + this.ID + " to " + e.endNode.ID);
				
				// Set label for edge
				((DistBidirectionalEdge) e).setLabel(label);
				
				// Set the vertex parent in the forest oriented by label
				parentsHash.put(label, e.endNode);
				
				// TODO: test only. delete later (good for grid2D test with 90 nodes)
				if (label == 1) {
					((DistBidirectionalEdge) e).setColor(Color.RED);
				} else if (label == 2) {
					((DistBidirectionalEdge) e).setColor(Color.GREEN);
				} else if (label == 3) {
					((DistBidirectionalEdge) e).setColor(Color.BLUE);
				} else if (label == 4) {
					((DistBidirectionalEdge) e).setColor(Color.YELLOW);
				}
				
				label++;
			} else {
				// "Remove" edge from screen
				((DistBidirectionalEdge) e).setColor(new Color(0, 0, 0, 0));
				
				// No label
				((DistBidirectionalEdge) e).setLabel(0);
			}
		}
		
		// For all current labels, set is the vertex is a root in a tree oriented by a label
		for (int tempLabel = 1; tempLabel <= label; tempLabel++) {
			boolean isRoot = true;
			for (Edge e : this.outgoingConnections) {
				if (((DistBidirectionalEdge) e).getLabel() == tempLabel) {
					isRoot = false;
					break;
				}
			}
			
			rootsHash.put(tempLabel, isRoot);
		}
	}
	
	private void colorReduction() {
		
	}
	
	/**
	 * @param label
	 * @return The parent of the vertex in a tree that belongs to a forest oriented by label.
	 * Returns null if no such parent exists.
	 */
	private Node getParent(int label) {
		return parentsHash.get(label);
	}
	
	/**
	 * @param label - The forest in which the vertex may be the root of a tree
	 * @return - Is the vertex a root in a tree that belongs to the forest oriented by label
	 */
	private boolean isRoot(int label) {
		if (rootsHash.get(label) == null) {
			return true;
		}
		
		return rootsHash.get(label);
	}
}
