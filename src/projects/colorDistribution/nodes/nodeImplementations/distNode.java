package projects.colorDistribution.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.colorDistribution.nodes.edges.DistBidirectionalEdge;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.logging.Logging;

public class distNode extends Node {
	
	private Logging log = Logging.getLogger("dist_log");
	
	@Override
	public void handleMessages(Inbox inbox) {
		System.out.println("handle messages");
		// Forest Decomposition
		int label = 1;
		for (Edge e : this.outgoingConnections) {
			// If start node's ID is smaller then end node's ID, than the edge is directed from start to end
			if (this.ID < e.endNode.ID) {
				System.out.println("set label " + label + " for edge from " + this.ID + " to " + e.endNode.ID);
				((DistBidirectionalEdge) e).setLabel(label);
				
				// TODO: test only. delete later
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
			}
		}
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

}
