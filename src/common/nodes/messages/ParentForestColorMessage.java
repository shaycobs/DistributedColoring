package common.nodes.messages;

import sinalgo.nodes.messages.Message;

public class ParentForestColorMessage extends Message {

    public int newColor;

    public int forest;

    public ParentForestColorMessage(int forest, int newColor) {
        this.forest = forest;
        this.newColor = newColor;
    }

    @Override
    public Message clone() {
        // This message requires a read-only policy
        return this;
    }
}
