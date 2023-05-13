package utils;

import genclass.GenericIO;

/**
 * Several general purpose static functions
 */

public class Utils {

    /**
     * Random number.
     * Get a random number between a given interval
     *
     * @param min lowest number from the interval
     * @param max highest number from the interval
     * @return a number between min and max
     */
    public static int random(int min, int max) {
        if (min >= max)
            throw new IllegalArgumentException("Max must be greater than Min");

        return (int)(Math.random() * (max - min + 1)) + min;
    }

    /**
     * Min value of all arguments passed.
     *
     * @param values variable number of arguments
     * @return the minimum value of all arguments passed
     */
    public static int min(int... values) {
        int min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * Max value of all arguments passed.
     *
     * @param values variable number of arguments
     * @return the maximum value of all arguments passed
     */
    public static int max(int... values) {
        int max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * Entity logger.
     * Conveniently formatted print to log messages from an entity
     *
     * @param entity any instance of class that has a toString()
     * @param message text presented after the enunciation of the entity
     */

    public static void logger(Object entity, String message) {
        GenericIO.writelnString("[" + entity + "]: " + message);
    }

    /**
     * Entity logger.
     * Conveniently formatted print to log messages from an entity
     *
     * @param place any instance of class that has a toString()
     * @param entity any instance of class that has a toString()
     * @param message text presented after the enunciation of the entity
     */

    public static void logger(Object place, Object entity, String message) {
        GenericIO.writelnString("[" + place.toString() + "][" + entity.toString() + "]: " + message);
    }

    /**
     * Is Any
     * Check if any element of an array is true
     *
     * @param array array of booleans
     * @return true if any element is true, false otherwise
     */
    public static boolean any(boolean[] array) {
        for (boolean b : array)
            if (b) return true;
        return false;
    }

    /**
     * Is All
     * Check if all elements of an array are true
     *
     * @param array array of booleans
     * @return true if all elements are true, false otherwise
     */
    public static boolean all(boolean[] array) {
        for (boolean b : array)
            if (!b) return false;
        return true;
    }

    /**
     * Count
     * Count the number of true elements in an array
     *
     * @param array array of booleans
     * @return number of true elements
     */
    public static int count(boolean[] array) {
        int count = 0;
        for (boolean b : array)
            if (b) count++;
        return count;
    }
}
