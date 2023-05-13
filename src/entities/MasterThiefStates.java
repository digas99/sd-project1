package entities;

/**
 * Class that contains the states of the Master Thief
 */

public final class MasterThiefStates {
    public static final int PLANNING_HEIST = 0;
    public static final int DECIDING_WHAT_TO_DO = 1;
    public static final int ASSEMBLING_GROUP = 2;
    public static final int WAITING_ARRIVAL = 3;
    public static final int PRESENTING_REPORT = 4;

    private MasterThiefStates() {}
}
