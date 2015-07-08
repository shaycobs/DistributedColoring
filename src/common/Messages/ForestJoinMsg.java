package common.Messages;

import sinalgo.nodes.messages.Message;


public class ForestJoinMsg extends Message {

    public int forest;

    public ForestJoinMsg(int forest) {
        this.forest = forest;
    }

    @Override
    public Message clone() {
        // This message requires a read-only policy
        return this;
    }
}
