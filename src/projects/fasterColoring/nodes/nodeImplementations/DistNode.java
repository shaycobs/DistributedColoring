package projects.fasterColoring.nodes.nodeImplementations;

import projects.fasterColoring.CustomGlobal;
import projects.fasterColoring.nodes.messages.NeighborColorMessage;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.runtime.Global;

import common.Nodes.BaseDistNode;

public class DistNode extends BaseDistNode {

	/**
	 * Counts up the forest color merge run
	 */
	private int forestCountUp = 1;

	/**
	 * Counts down the color reduction for current forest iteration
	 */
	private int reduceCountDown;

	/**
	 * The current color to reduce
	 */
	private int iterColor;

	/**
	 * Is in reduce
	 */
	private boolean isReduce = false;
	
	@Override
	public void handleMessages(Inbox inbox) {
		super.handleMessages(inbox);
		
		if ((Global.currentTime > 2) && !isCv && !isHold && finalStep) {			
			// Reduce color
			if (isReduce) {
				// No need to reduce the first forest iteration
				if (forestCountUp > 2) {
					colorReduction(iterColor, inbox);
				}
				
				// Reduce color for next iteration
				iterColor--;
				// Reduce count down to next iteration
				reduceCountDown--;
				
				// If we reached 0 we're no longer reducing
				if (reduceCountDown == 0) {
					isReduce = false;
				}
				
				// Send messages with the node color to all neighbors
				for (Edge e : this.outgoingConnections) {
		            BaseDistNode neighbor = (BaseDistNode)e.endNode;
		            
		            send(new NeighborColorMessage(getUniColor()), neighbor);
				}
				
			} else if (forestCountUp <= CustomGlobal.maxDegree) { // Merging colors for each forest in increasing order
				// Merge the current forest node color with the color calculated in the previous iteration
				mergeNodeColors();
				
				// Next round with start reducing
				isReduce = true;
				
				// And after the reducing is done we deal with the next forest
				forestCountUp++;
				
				// Initialize for color decomposition
				reduceCountDown = 2 * (CustomGlobal.maxDegree + 1);
				iterColor = 3 * (CustomGlobal.maxDegree + 1);
				
				// Send messages with the node color to all neighbors
				for (Edge e : this.outgoingConnections) {
		            BaseDistNode neighbor = (BaseDistNode)e.endNode;
		            
		            send(new NeighborColorMessage(getUniColor()), neighbor);
				}
			} else if (forestCountUp == CustomGlobal.maxDegree + 1) {
				finalStep = false;
				System.out.println("FC Node=" + this.ID + "; Faster Coloring done! Final color: " + this.uniColor);
				forestCountUp++;
			}
		}
		
		isHold = false;
	}

	/**
	 * Merge the current forest node's color with calculated color from previous iteration
	 */
	private void mergeNodeColors() {
		int mergeColor = getUniColor();
		
		if (forestCountUp == 1) {
			mergeColor = getColorBitInt(forestCountUp) - 1;
		} else {
			// New color is shifted two bits to the left and or'd the next forest color
			mergeColor = (mergeColor << 2) | (getColorBitInt(forestCountUp) - 1);

			// Lower for continuity (dirty dirty trick)
			mergeColor = mergeColor - (mergeColor / 4);
		}
		
		setUniColor(mergeColor);
		
		System.out.println("FC Node=" + this.ID + "; Merge for forest: " + forestCountUp + "; Computed color: " + this.uniColor);
	}
	
	/**
	 * If the node's color is current color, reduces it to a color from delta+1
	 * @param currentColor
	 */
	private void colorReduction(int currentColor, Inbox inbox) {
		// Reduce only the current iteration color
		if (this.uniColor == currentColor) {
			// Search for a free color in the palette
			for (int i = 0; i < colorPalette.size(); i++) {
				if (!colorPalette.get(i)) {
					setUniColor(i);
				}
			}
			
			System.out.println("FC Node=" + this.ID + "; Color reduced from " + currentColor + " to " + this.getUniColor());
		}
	}
}
