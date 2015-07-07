package projects.fasterColoring.nodes.nodeImplementations;

import java.util.Vector;

import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import common.ColorPair;
import common.Nodes.BaseDistNode;

public class DistNode extends BaseDistNode {
	
	private ColorPair mergeColor;
	
	/**
	 * Stores if neighbors are colored in color Key
	 */
	private Vector<Boolean> colorPalette = new Vector<>();
	
	@Override
	public void init() {
		super.init();
		
		// Initialize color palette
		for (int i = 0; i < maxDegree; i++) {
			this.colorPalette.add(false);
		}
	}
	@Override
	public void handleMessages(Inbox inbox) {
		super.handleMessages(inbox);
		
        // Prepare anchor for 3(delta + 1) coloring
		if (roundAnchor < 0)
			roundAnchor = round;
		
		// merge colors
		if (round - roundAnchor < maxDegree) {
			mergeNodeColors();
		} else {
			// Prepare anchor for color reduction
			if (roundAnchor == maxDegree) {
				roundAnchor = round;
			}
			
			// Color reduction takes 2*(delta + 1)
			if (round - roundAnchor < 2 * (maxDegree + 1)) {
				colorReduction();
			}
		}
		
		round++;
	}
	
	private void mergeNodeColors() {
		if (round - roundAnchor == 0) {
			// First forest node color
			mergeColor = new ColorPair(null, vertexCV3ColorsPerForest.get(1));
		} else {
			int forestIndex = round - roundAnchor + 1;
			mergeColor = new ColorPair(mergeColor, vertexCV3ColorsPerForest.get(forestIndex));
		}
	}
	
	private void colorReduction() {
		for (Edge e : this.outgoingConnections) {
            BaseDistNode neighbor = (BaseDistNode)e.endNode;
            
            // If a neighbor is colored in a color for (delta+1) color palette, we can't use it
            if (neighbor.getColorBitInt() < maxDegree) {
                colorPalette.set(neighbor.getColorBitInt(), true);
            }
        }
		
		// Search for a free color in the palette
		for (int i = 0; i < colorPalette.size(); i++) {
			if (!colorPalette.get(i)) {
				this.setColorBitInt(i);
			}
		}
	}
}
