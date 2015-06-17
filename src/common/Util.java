package common;

/**
 * General-Utility class
 */
public class Util {

    /**
     * Calculates the logStar (iterated log) of n.
     * @param n number
     * @return iteratedLog(n)
     */
    public static int logStar(double n)
    {
        int count=0;
        while (n >= 2) {
            n = Math.log(n) / Math.log(2);
            count++;
        }
        return count;
    }
}
