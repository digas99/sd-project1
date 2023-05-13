package entities;

import sharedRegions.*;

import static utils.Parameters.*;
import static utils.Utils.*;

/**
 * Thread that represents the Ordinary Thief
 */

public class OrdinaryThief extends Thief {
    private int partyID;
    private int roomID;
    /**
     * Displacement of the thief
     */
    private final int displacement;

    /**
     * General repository
     */
    private GeneralRepos repos;

    /**
     * Ordinary Thief Constructor
     * @param threadName Thread name
     * @param thiefID Thief ID
     * @param museum Museum
     * @param concentrationSite Concentration Site
     * @param collectionSite Collection Site
     * @param assaultParties Assault Parties
     * @param repos General Repository
     */

    public OrdinaryThief(String threadName, int thiefID, Museum museum, ConcentrationSite concentrationSite, CollectionSite collectionSite, AssaultParty[] assaultParties, GeneralRepos repos){
        super(threadName, thiefID, museum, concentrationSite, collectionSite, assaultParties,repos);
        thiefState = OrdinaryThiefStates.CONCENTRATION_SITE;
        displacement = random(MIN_DISPLACEMENT, MAX_DISPLACEMENT);
        this.repos = repos;
        partyID = roomID = -1;
        repos.setOrdinaryThiefDisplacement(thiefID, displacement);
    }

    @Override
    public String toString() {
        return super.toString() + " [Party: " + partyID + ", Room: " + roomID + "]";
    }

    /**
     * Life cycle of the Ordinary Thief
     */
    @Override
    public void run() {
        while (concentrationSite.amINeeded()) {
            int[] assaultData = concentrationSite.prepareExcursion();
            if (assaultData != null) {
                partyID = assaultData[0];
                roomID = assaultData[1];
                Museum.Room room = museum.getRoom(roomID);
                AssaultParty party = assaultParties[partyID];

                party.crawlIn(room.getDistance(), displacement);
                boolean hasCanvas = museum.rollACanvas(room.getID());
                if (hasCanvas)
                    room.setPaintings(room.getPaintings() - 1);
                party.reverseDirection();
                party.crawlOut(room.getDistance(), displacement);

                collectionSite.handACanvas(partyID, roomID, hasCanvas);
                repos.setOrdinaryThiefSituation(getThiefID(), false);
            } else break;
        }
    }
}
