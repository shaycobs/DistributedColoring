package projects.fasterColoring.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * A message holding the node color
 * @author Shay Yacobinski
 *
 */
public class NeighborColorMessage extends Message {

	public int color = 0;
	
	public NeighborColorMessage(int neighborColor) {
		this.color = neighborColor;
	}
	
	@Override
	public Message clone() {
		return new NeighborColorMessage(color);
	}

}
