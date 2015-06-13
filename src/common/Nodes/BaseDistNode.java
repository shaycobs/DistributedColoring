package common.Nodes;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;

import java.awt.*;
import java.util.HashMap;

public class BaseDistNode extends Node {

    protected int colorBitInt;
    protected int round = 1;

    /**
     * Vertex parents in the tree it belongs to, in the forest oriented by the edge labels
     */
    protected HashMap<Integer, Node> parentsHash = new HashMap<>();

    /**
     * Is this vertex a root in the tree it belongs to, in the forest oriented by the edge labels
     */
    protected HashMap<Integer, Boolean> rootsHash = new HashMap<>();

    @Override
    public void init() {
        // Init color is node's ID
        this.colorBitInt = this.ID;
    }

    /**
     * @param label
     * @return The parent of the vertex in a tree that belongs to a forest oriented by label.
     * Returns null if no such parent exists.
     */
    public Node getParent(int label) {
        return parentsHash.get(label);
    }

    /**
     * @param label - The forest in which the vertex may be the root of a tree
     * @return - Is the vertex a root in a tree that belongs to the forest oriented by label
     */
    public boolean isRoot(int label) {
        if (rootsHash.get(label) == null) {
            return true;
        }

        return rootsHash.get(label);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(new Color(ID*99999));
        drawAsDisk(g, pt, highlight, 5);
    }

    public int getColorBitInt() {
        return colorBitInt;
    }

    public void setColorBitInt(int colorBitInt) {
        this.colorBitInt = colorBitInt;
    }

    @Override
    public void handleMessages(Inbox inbox) {

    }

    @Override
    public void preStep() {

    }

    @Override
    public void neighborhoodChange() {

    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }
}
