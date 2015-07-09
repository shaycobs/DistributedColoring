package common.nodes.edges;

import sinalgo.nodes.edges.Edge;

import java.awt.*;

/**
 * Adds label support for BidirectionalEdge
 */
public class BaseDistBidirectionalEdge extends Edge {
    /**
     * Edge label. Each label is a different graph in the forest decomposition
     */
    private int label;

    /**
     * Edge color. For debugging purposes.
     */
    private Color edgeColor;

    /**
     * @return Edge label
     */
    public int getLabel() {
        return label;
    }

    /**
     * @param label - Number from 1 to max degree of the graph
     */
    public void setLabel(int label) {
        this.label = label;
    }

    public void setColor(Color color) {
        this.edgeColor = color;
    }

    @Override
    public Color getColor() {
        return edgeColor;
    }
}
