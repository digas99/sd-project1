package entities;

import genclass.GenericIO;
import sharedRegions.*;

import static utils.Parameters.*;
import static utils.Utils.*;

public class OrdinaryThief extends Thief {
    private AssaultParty party;
    private boolean canIMove;
    private int position;
    private int displacement;
    private int movesLeft;
    private boolean hasCanvas;
    public void joinParty(int partyID) {
        this.party = assaultParties[partyID];
    }

    public AssaultParty getParty() {
        return party;
    }

    public boolean canMove() {
        return canIMove;
    }

    public void canMove(boolean canIMove) {
        this.canIMove = canIMove;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getDisplacement() {
        return displacement;
    }

    public int getMovesLeft() {
        return movesLeft;
    }

    public void setMovesLeft(int movesLeft) {
        this.movesLeft = movesLeft;
    }

    public void resetMovesLeft() {
        movesLeft = max(displacement, MAX_SEPARATION_LIMIT);
    }

    public boolean hasCanvas() {
        return hasCanvas;
    }

    public void hasCanvas(boolean hasCanvas) {
        this.hasCanvas = hasCanvas;
    }

    public OrdinaryThief(String threadName, int thiefID, Museum museum, ConcentrationSite concentrationSite, CollectionSite collectionSite, AssaultParty[] assaultParties, GeneralRepos repos) {
        super(threadName, thiefID, museum, concentrationSite, collectionSite, assaultParties);
        thiefState = OrdinaryThiefStates.CONCENTRATION_SITE;
        displacement = random(MIN_DISPLACEMENT, MAX_DISPLACEMENT);
        resetMovesLeft();
        canIMove = hasCanvas = false;
    }

    @Override
    public void run() {
        while (concentrationSite.amINeeded()) {
            concentrationSite.prepareExcursion();
            party.crawlIn();
            museum.rollACanvas(party.getId());
            party.reverseDirection();
            party.crawlOut();
            collectionSite.handACanvas();
        }
    }
}
