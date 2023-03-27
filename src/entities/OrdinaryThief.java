package entities;

import genclass.GenericIO;
import sharedRegions.AssaultParty;
import sharedRegions.CollectionSite;
import sharedRegions.ConcentrationSite;
import sharedRegions.Museum;

import static utils.Parameters.*;
import static utils.Utils.*;

public class OrdinaryThief extends Thief {
    private AssaultParty party;
    private int displacement;
    private boolean hasCanvas;
    private Museum.Room[] rooms;

    public void setAssaultParty(int partyID) {
        party = assaultParties[partyID];
    }

    public void setRoomOfParty(int roomID) {
        party.setRoom(rooms[roomID]);
    }

    public int getDisplacement() {
        return displacement;
    }

    public void setDisplacement(int displacement) {
        this.displacement = displacement;
    }

    public boolean hasCanvas() {
        return hasCanvas;
    }

    public void hasCanvas(boolean hasCanvas) {
        this.hasCanvas = hasCanvas;
    }

    public AssaultParty getParty() {
        return party;
    }

    public void setParty(AssaultParty party) {
        this.party = party;
    }

    public int getRoomID() {
        return party.getRoom().getID();
    }

    public OrdinaryThief(String threadName, int thiefID, Museum museum, ConcentrationSite concentrationSite, CollectionSite collectionSite, AssaultParty[] assaultParties) {
        super(threadName, thiefID, museum, concentrationSite, collectionSite, assaultParties);
        thiefState = OrdinaryThiefStates.CONCENTRATION_SITE;
        displacement = random(MIN_DISPLACEMENT, MAX_DISPLACEMENT);
        hasCanvas = false;
        rooms = museum.getRooms();
    }

    @Override
    public void run() {
        while (concentrationSite.amINeeded()) {
            concentrationSite.prepareExcursion();
            party.crawlIn();
            museum.rollACanvas(party.getID());
            party.reverseDirection();
            party.crawlOut();
            collectionSite.handACanvas();
        }
    }
}
