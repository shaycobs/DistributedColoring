package projects.colorDistribution.nodes.nodeImplementations;

import sinalgo.nodes.messages.Inbox;
import common.Nodes.BaseDistNode;

public class DistNode extends BaseDistNode {
	
	@Override
	public void handleMessages(Inbox inbox) {
		super.handleMessages(inbox);
		
		round++;
	}

}
