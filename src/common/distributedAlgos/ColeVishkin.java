package common.distributedAlgos;

import common.Util;
import common.nodes.nodeImplementations.BaseDistNode;

/**
 * Cole-Vishkin Algorithm for distributed 3-coloring of a tree, in O(log*n)+O(1) rounds.
 *
 * The algorithm assumes each node is already colored (e.g. in its ID number) and will
 * reduces the number of colors to 3 in several steps
 * 1. First, it will reduce the coloring to 6-color. (O(log*n) rounds)
 * 2. Then, it will reduce the coloring to 3-color using three shift-down + first-free operations (3 rounds = O(1))
 */
public class ColeVishkin {

    private BaseDistNode node;

    // Label of the forest this node belongs to
    private int forestLabel;

    // Number of rounds needed for the reduce procedure
    private int reduceColorRounds;

    // when we are shifting down colors, this will be the current color.
    private int currentShiftDownColor = 6;

    /**
     * Possible phases for the algorithm
     */
    public enum Phase {
        REDUCE, SHIFT_DOWN, FIRST_FREE, COMPLETED;
    }

    // Current algorithm phase
    public Phase phase;

    public ColeVishkin(BaseDistNode node, int numNodes, int forestLabel) {

        this.node = node;
        this.forestLabel = forestLabel;

        /**
         * In order to reduce the number of colors to 6, we need to run reduceColor procedure
         * for exactly log*n rounds + 3
         */
        reduceColorRounds = Util.logStar((double)numNodes) + 3;

        // init phase
        phase = Phase.REDUCE;
    }

    /**
     * This should be called on each round, with the round number.
     * It will decide which algorithm procedure to run, based on the round number
     * and will return the algorithm phase.
     *
     * When the algorithm is done, the phase it will return is Phase.COMPLETED.
     *
     * @param round round number
     * @return current algorithm phase
     * @throws Exception
     */
    public Phase onRound(int round) throws Exception {


        switch (phase) {
            case REDUCE:
                System.out.println("CV Node=" + node.ID + " Reduce phase. forest: " + forestLabel + " round " + round + " reduce color rounds: " + reduceColorRounds);
                reduceColor();
                if (round == reduceColorRounds) {
                    phase = Phase.SHIFT_DOWN;
                }
                break;
            case SHIFT_DOWN:
                System.out.println("CV Node=" + node.ID + " Shift-down on forest " + forestLabel);
                shiftDown();
                phase = Phase.FIRST_FREE;
                break;
            case FIRST_FREE:
                if (node.getColorBitInt(forestLabel) == currentShiftDownColor) {
                    firstFree();
                    System.out.println("CV Node=" + node.ID + " First-free. forest: " + forestLabel + " next color " + currentShiftDownColor);
                }
                // Go to next color
                currentShiftDownColor--;
                phase = currentShiftDownColor >= 4 ? Phase.SHIFT_DOWN : Phase.COMPLETED;
                break;
            case COMPLETED:
                if (node.getColorBitInt(forestLabel) <= 3) {
                    System.out.println("Painted node " + node.ID + " on forest " + forestLabel
                            + " successfully by color " + node.getColorBitInt(forestLabel)
                            + " parent color is " + node.getParent(forestLabel).getColorBitInt(forestLabel));
                } else {
                    throw new Exception("ERROR! CV algo completed but node " + node.ID + " on forest " + forestLabel + " color is " + node.getColorBitInt(forestLabel));
                }
        }

        return phase;
    }

    /**
     * This is the first phase of the algorithm: reduces number of colors to 6 in log*n rounds.
     */
    private void reduceColor() throws Exception {
        int newColor;
        // Get my color bit string
        int myColor = getColorBitInt();
        /**
         * difference index: for root pick "0" (arbitrary),
         * For other nodes pick an index where the color bit string is different from parent's bit string
         */
        int diffInd = node.isRoot(forestLabel) ? 0 : getParentDiffIndex();
        // Get the bit value of this index
        int x = (int)Math.pow(2, (double)diffInd);
        int bitValue = (myColor & x) > 0 ? 1 : 0;

        // Now create the new color by concatenating the different bit index to its value (+1, to make colors start at 1)
        newColor = concatBitToNum(diffInd+1, bitValue);

        // Set this as the new color
        setColorBitInt(newColor);
    }

    /**
     * Root will pick a new random color from {1,2,3}.
     * Other nodes will inherit parent's color.
     * @throws Exception
     */
    private void shiftDown() throws Exception {
        int newColor = node.isRoot(forestLabel) ?
                chooseNewColor() :
                node.getParent(forestLabel).getColorBitInt(forestLabel);

        setColorBitInt(newColor);
    }

    /**
     * Colors the node in a free color from {1,2,3}.
     * Run this method for each node that is colored by {6,5,4}
     * @throws Exception
     */
    private void firstFree() throws Exception {
        int newColor = findFreeColor();
        setColorBitInt(newColor);
    }

    /**
     * Find a free color from {1,2,3}.
     * @return found color number
     * @throws Exception
     */
    private int findFreeColor() throws Exception {
        for (int i = 1; i <= 3; i++) {
            if (node.isColorFree(forestLabel, i)) {
                return i;
            }
        }
        // If we got to this point, there is a problem...
        throw new Exception("No free color found!");
    }

    /**
     * Choose a new color from colors {1,2,3}, once that is not the current node color.
     * @return new color
     */
    private int chooseNewColor() throws Exception {
        for (int i = 1; i <= 3; i++) {
            if (i != getColorBitInt())
                return i;
        }

        // If we got to this point, there is a problem...
        throw new Exception("No different color found!");
    }

    /**
     * Sets node color bit int for current forest
     * @param colorInt
     */
    private void setColorBitInt(int colorInt) {
        node.setColorBitInt(forestLabel, colorInt);
        node.sendColorToChildren(forestLabel, colorInt);
    }

    /**
     * Gets node color bit int for current forest
     * @return Color int
     */
    private int getColorBitInt() {
        return node.getColorBitInt(forestLabel);
    }

    /**
     * Gets the smallest index i where this node's color bit string is different
     * from the parent's color bit string.
     * @return index i of color bitstring
     */
    private int getParentDiffIndex() throws Exception {
        int myColor = getColorBitInt();
        BaseDistNode parent = node.getParent(forestLabel);
        int parentColor = parent.getColorBitInt(forestLabel);

        // XOR colors to find which bits are different
        int colorDiff = myColor ^ parentColor;

        /**
         * Find the smallest index for a set (1) bit on the colorDiff:
         * Start by assuming the set bit is on index 0 and check by doing a bitwise-and for colorDiff and x=2^0=1.
         * If it's not zero - we found the smallest index.
         *
         * Otherwise, increase the index by 1 and multiply x by 2, so that x = 2^i on each iteration, and try again.
         */
        int x = 1, i = 0;
        while ((colorDiff & x) == 0) {
            if (i > 63) { // color is 64 bit at most
                throw new Exception("Illegal coloring: node " + node.ID + " and parent " + parent.ID + " have the same color");
            }
            x *= 2;
            i++;
        }

        return i;
    }

    /**
     *
     * Concat a bit (0/1) to a given number.
     *
     * For example: running concatBitToNum(3,1) will return 7:
     *
     * 3 = (11) in binary. Shifting it left we will get (110).
     * Then, we will "or" it with the bit "1" and get (111) = 7 - a concatenation of 11 and 1 as needed.
     *
     *
     * @param num number we want to concat to
     * @param bit - 0 or 1
     * @return concatenated value
     */
    private int concatBitToNum(int num, int bit) {
        return (num << 1) | bit;
    }
}
