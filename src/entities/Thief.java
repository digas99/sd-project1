package entities;

import sharedRegions.AssaultParty;
import sharedRegions.CollectionSite;
import sharedRegions.ConcentrationSite;
import sharedRegions.Museum;

import static utils.Parameters.*;
import static utils.Utils.random;

public abstract class Thief extends Thread {
    protected final ConcentrationSite concentrationSite;
    protected final CollectionSite collectionSite;
    protected final Museum museum;
    protected AssaultParty[] assaultParties;
    protected int thiefID;
    protected int thiefState;

    public int getThiefState() {
        return thiefState;
    }

    public void setThiefState(int thiefState) {
        this.thiefState = thiefState;
    }

    public int getThiefID() {
        return thiefID;
    }

    public void setThiefID(int thiefID) {
        this.thiefID = thiefID;
    }

    public Museum getMuseum() {
        return museum;
    }

    public ConcentrationSite getConcentrationSite() {
        return concentrationSite;
    }

    public CollectionSite getCollectionSite() {
        return collectionSite;
    }

    public Thief(String threadName, int id, Museum museum, ConcentrationSite concentrationSite, CollectionSite collectionSite, AssaultParty[] assaultParties) {
        super(threadName);

        this.thiefID = id;
        this.museum = museum;
        this.concentrationSite = concentrationSite;
        this.collectionSite = collectionSite;
        this.assaultParties = assaultParties;
    }

    @Override
    public String toString() {
        return getName();
    }

}
