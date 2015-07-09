package projects.colorDistribution.nodes.nodeImplementations;

import sinalgo.nodes.messages.Inbox;
import sinalgo.runtime.Global;
import common.Nodes.BaseDistNode;

public class DistNode extends BaseDistNode {
	
	@Override
	public void handleMessages(Inbox inbox) {
		super.handleMessages(inbox);
		
		if ((Global.currentTime > 2) && !isCv && finalStep) {
			// Set unicolor to 1 so first mult won't do any difference
			uniColor = 0;
			
			// The new color is a multiply of the vertex colors from all forests
			for (Integer color : forestColor.values()) {
				uniColor = (uniColor << 2) | (color-1);
				

				// Lower for continuity (dirty dirty trick)
				uniColor = uniColor - (uniColor / 4);
				
				//this.uniColor *= color;
			}
			
			System.out.println("CD Node=" + this.ID + "; color distribution done! Merged color: " + this.uniColor);
			
			finalStep = false;
		}
	}

}
