package utils;

/**
 * Constants used all throughout the program, both on entities and on shared regions
 */

public final class Parameters {
    // Constants for Museum
    /**
     * Number of Rooms in the Museum.
     */
    public static final int N_ROOMS = 5;

    // Constants for Room
    /**
     * Maximum distance the painting are from the entrance.
     */
    public static final int MAX_DISTANCE = 30;
    /**
     * Minimum distance the painting are from the entrance.
     */
    public static final int MIN_DISTANCE = 15;
    /**
     * Maximum number of paintings in a room.
     */
    public static final int MAX_PAINTINGS = 16;
    /**
     * Minimum number of paintings in a room.
     */
    public static final int MIN_PAINTINGS = 8;

    // Constants for Thieves
    /**
     * Maximum distance a thief can be from the previous thief.
     */
    public static final int MAX_SEPARATION_LIMIT = 3;
    /**
     * Thief's maximum agility. Maximum number of steps a thief can take in a single move.
     */
    public static final int MAX_DISPLACEMENT = 6;
    /**
     * Thief's minimum agility. Minimum number of steps a thief can take in a single move.
     */
    public static final int MIN_DISPLACEMENT = 2;

    // Constants for AssaultParty
    /**
     * Number of Assault Parties.
     */
    public static final int N_ASSAULT_PARTIES = 1;
    /**
     * Number of thieves in each Assault Party.
     */
    public static final int N_THIEVES_PER_PARTY = 3;

    // Constants for main
    /**
     * Number of Master Thieves.
     */
    public static final int N_THIEVES_MASTER = 1;
    /**
     * Number of Ordinary Thieves.
     */
    public static final int N_THIEVES_ORDINARY = 3;

    // Constants for Control/Collection Site
    /**
     * Master Thief Appraise Sit decision to create a new Assault Party.
     */
    public static final int CREATE_ASSAULT_PARTY = 0;
    /**
     * Master Thief Appraise Sit decision to wait for the Canvas.
     */
    public static final int WAIT_FOR_CANVAS = 1;
    /**
     * Master Thief Appraise Sit decision to end the heist.
     */
    public static final int END_HEIST = 2;
}
