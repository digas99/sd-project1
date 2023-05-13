package entities;

import sharedRegions.*;
import utils.MemException;

import static utils.Parameters.*;
import static utils.Utils.logger;

/**
 * Thread that represents the Master Thief
 */

public class MasterThief extends Thief {
    /**
     * General repository
     */
    private GeneralRepos repos;

    /**
     * MasterThief constructor
     * @param threadName Thread name
     * @param thiefID Thief ID
     * @param museum Museum
     * @param concentrationSite ConcentrationSite
     * @param collectionSite CollectionSite
     * @param assaultParties AssaultParty array
     * @param repos GeneralRepos
     */

    public MasterThief(String threadName, int thiefID, Museum museum, ConcentrationSite concentrationSite, CollectionSite collectionSite, AssaultParty[] assaultParties, GeneralRepos repos) throws MemException {
        super(threadName, thiefID, museum, concentrationSite, collectionSite, assaultParties,repos);
        setThiefState(MasterThiefStates.PLANNING_HEIST);
        this.repos = repos;
    }

    /**
     * Life cycle of the Master Thief
     */
    @Override
    public void run() {
        concentrationSite.startOperations();
        lifecycle: while(true) {
            switch (collectionSite.appraiseSit(concentrationSite.occupancy(), concentrationSite.nActiveParties(), concentrationSite.getFreeParty())) {
                case CREATE_ASSAULT_PARTY:
                    logger(this, "CREATE_ASSAULT_PARTY");
                    repos.updateMasterThiefState(MasterThiefStates.PLANNING_HEIST);
                    int assaultPartyID = concentrationSite.prepareAssaultParty();
                    if (assaultPartyID >= 0) {
                        concentrationSite.setPartyActive(assaultPartyID, true);
                        assaultParties[assaultPartyID].sendAssaultParty();
                    }
                    break;
                case WAIT_FOR_CANVAS:
                    logger(this, "WAIT_FOR_CANVAS");
                    repos.updateMasterThiefState(MasterThiefStates.WAITING_ARRIVAL);
                    collectionSite.takeARest();

                    int[] partyState = collectionSite.collectACanvas();
                    int partyID = partyState[0];
                    int roomID = partyState[1];
                    int roomState = partyState[2];
                    boolean lastThief = partyState[3] == 1;

                    if (lastThief) {
                        concentrationSite.setPartyActive(partyID, false);
                        assaultParties[partyID].resetAssaultParty();
                    }

                    // must only update if room not busy
                    if (roomState != BUSY_ROOM) {
                        // only need to update room state if it's not empty already
                        if (concentrationSite.getRoomState(roomID) != EMPTY_ROOM)
                            concentrationSite.setRoomState(roomID, roomState);
                    }

                    break;
                case END_HEIST:
                    logger(this, "END_HEIST");
                    repos.updateMasterThiefState(MasterThiefStates.PRESENTING_REPORT);
                    collectionSite.sumUpResults();
                    break lifecycle;
            }
        }
        concentrationSite.endOperations();
    }
}
