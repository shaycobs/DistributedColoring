package common.Messages;

import sinalgo.nodes.messages.Message;

public class ColorChangeMsg extends Message {

    public int newColor;

    public int forest;

    public ColorChangeMsg(int forest, int newColor) {
        this.forest = forest;
        this.newColor = newColor;
    }

    @Override
    public Message clone() {
        // This message requires a read-only policy
        return this;
    }
}
