package common.nodes.messages;

import sinalgo.nodes.messages.Message;


public class ForestJoinMessage extends Message {

    public int forest;

    public ForestJoinMessage(int forest) {
        this.forest = forest;
    }

    @Override
    public Message clone() {
        // This message requires a read-only policy
        return this;
    }
}
