package entities;

import sharedRegions.*;

public abstract class Thief extends Thread {

    /**
     * Concentration Site object of the thief
     */
    protected final ConcentrationSite concentrationSite;

    /**
     * Collection Site object of the thief
     */
    protected final CollectionSite collectionSite;

    /**
     * Museum object of the thief
     */
    protected final Museum museum;

    /**
     * Assault Party array that stores the assault parties of the thief
     */
    protected AssaultParty[] assaultParties;

    /**
     * Thief ID
     */
    protected int thiefID;

    /**
     * Thief state
     */
    protected int thiefState;


    /**
     * Set the thief state
     * @param thiefState Thief state
     */
    public void setThiefState(int thiefState) {
        this.thiefState = thiefState;
    }

    /**
     * Get the thief ID
     * @return Thief ID
     */
    public int getThiefID() {
        return thiefID;
    }

    /**
     * Set the thief ID
     * @param thiefID Thief ID
     */
    public void setThiefID(int thiefID) {
        this.thiefID = thiefID;
    }

    /**
     * Get the Museum
     * @return Museum object
     */
    public Museum getMuseum() {
        return museum;
    }

    /**
     * THief constructor
     * @param threadName Thread name
     * @param id Thief ID
     * @param museum Museum
     * @param concentrationSite Concentration Site
     * @param collectionSite Collection Site
     * @param assaultParties Assault Parties
     * @param repos General Repository
     */
    public Thief(String threadName, int id, Museum museum, ConcentrationSite concentrationSite, CollectionSite collectionSite, AssaultParty[] assaultParties, GeneralRepos repos) {
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
