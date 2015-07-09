package projects.fasterColoring.nodes.nodeImplementations;

import java.util.Vector;

import projects.fasterColoring.CustomGlobal;
import projects.fasterColoring.nodes.messages.NeighborColorMessage;

import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
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
	public void init() {
		super.init();
		
		reduceCountDown = 2 * (CustomGlobal.maxDegree + 1);
		iterColor = 3 * (CustomGlobal.maxDegree + 1);
	}
	
	@Override
	public void handleMessages(Inbox inbox) {
		super.handleMessages(inbox);
		
		if ((Global.currentTime > 2) && !isCv && !isHold && finalStep) {			
			// Reduce color
			if (isReduce) {
				colorReduction(iterColor, inbox);
				
				// Reduce color for next iteration
				iterColor--;
				// Reduce count down to next iteration
				reduceCountDown--;
				
				// If we reached 0 we're no longer reducing
				if (reduceCountDown == 0) {
					isReduce = false;
					
					// Initialize for next forest iteration
					reduceCountDown= 2 * (CustomGlobal.maxDegree + 1);
					iterColor = 3 * (CustomGlobal.maxDegree + 1);
				}
				
				// Send messages with the node color to all neighbors
				for (Edge e : this.outgoingConnections) {
		            BaseDistNode neighbor = (BaseDistNode)e.endNode;
		            
		            send(new NeighborColorMessage(this.uniColor), neighbor);
				}
				
			} else if (forestCountUp <= CustomGlobal.maxDegree) { // Merging colors for each forest in increasing order
				// Merge the current forest node color with the color calculated in the previous iteration
				mergeNodeColors();
				
				// Next round with start reducing
				isReduce = true;
				
				// And after the reducing is done we deal with the next forest
				forestCountUp++;
				
				// Send messages with the node color to all neighbors
				for (Edge e : this.outgoingConnections) {
		            BaseDistNode neighbor = (BaseDistNode)e.endNode;
		            
		            send(new NeighborColorMessage(this.uniColor), neighbor);
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
			//if (getColorBitInt(forestCountUp) != 0) {
				// First forest node color
				mergeColor = getColorBitInt(forestCountUp) - 1;
			//}
			
		} else {
			// New color is from (delta+1) * 3 (because of 3-coloring). we add 1 so we won't multiply with 0,
			// and then 1 is reduces to have 0->delta colors and not 1->delta+1 colors
			//mergeColor = mergeColor * getColorBitInt(forestCountUp);
			
			//if (getColorBitInt(forestCountUp) != 0) {
				// New color is shifted two bits to the left and or'd the next forest color
				mergeColor = (mergeColor << 2) | (getColorBitInt(forestCountUp) - 1);
				
	
				// Lower for continuity (dirty dirty trick)
				mergeColor = mergeColor - (mergeColor / 4);
			//}
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
			// Get the colors of all neighbors
			/*for (Edge e : this.outgoingConnections) {
	            BaseDistNode neighbor = (BaseDistNode)e.endNode;

	            // If a neighbor is colored in a color for (delta+1) color palette, we can't use it
	            if (neighbor.getColorBitInt() < maxDegree) {
	                colorPalette.set(neighbor.getColorBitInt()-1, true);
	            }
	        }*/
			
			// Stores if neighbors are colored in color Key
			Vector<Boolean> colorPalette = new Vector<>();
			
			// Initialize color palette
			for (int i = 0; i <= CustomGlobal.maxDegree; i++) {
				colorPalette.add(false);
			}
			
			// Get the colors of all neighbors
			while (inbox.hasNext()) {
				Message msg = inbox.next();
				
				if (msg instanceof NeighborColorMessage) {
					if (((NeighborColorMessage) msg).color <= CustomGlobal.maxDegree + 1) {
						colorPalette.set(((NeighborColorMessage) msg).color - 1, true);
					}
				}
			}

			// Search for a free color in the palette
			for (int i = 0; i < colorPalette.size(); i++) {
				if (!colorPalette.get(i)) {
					this.setUniColor(i+1);
				}
			}
			
			System.out.println("FC Node=" + this.ID + "; Color reduced from " + currentColor + " to " + this.uniColor);
		}
	}
}
