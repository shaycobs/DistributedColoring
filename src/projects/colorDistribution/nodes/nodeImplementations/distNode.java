package projects.colorDistribution.nodes.nodeImplementations;

import common.Nodes.BaseDistNode;
import projects.colorDistribution.nodes.edges.*;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;

import java.awt.*;

public class DistNode extends BaseDistNode {
	
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
	

}
