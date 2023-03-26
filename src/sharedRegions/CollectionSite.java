package sharedRegions;

import entities.MasterThief;
import entities.MasterThiefStates;
import entities.OrdinaryThief;
import genclass.GenericIO;
import utils.MemException;
import utils.MemFIFO;

import java.util.Arrays;

import static utils.Parameters.*;
import static utils.Utils.*;

public class CollectionSite {
    private MemFIFO<Integer> arrivedThieves;

    /**
     * The number of thieves from each party that are in the site.
     */
    private final int[] partyThievesInSite;
    private final boolean[] roomEmpty;

    @Override
    public String toString() {
        return "Collection Site";
    }

    public boolean[] getRoomsEmpty() {
        return roomEmpty;
    }

    public CollectionSite(GeneralRepos repos) {
        try {
            arrivedThieves = new MemFIFO<>(new Integer[N_THIEVES_ORDINARY]);
        } catch (MemException e) {}
        roomEmpty = new boolean[N_ROOMS];
        for (int i = 0; i < N_ROOMS; i++)
            roomEmpty[i] = false;
        partyThievesInSite = new int[N_ASSAULT_PARTIES];
        for (int i = 0; i < N_ASSAULT_PARTIES; i++)
            partyThievesInSite[i] = 0;
    }

    public synchronized int appraiseSit() {
        MasterThief master = (MasterThief) Thread.currentThread();

        // check if the heist should end
        GenericIO.writelnString("Rooms : " + Arrays.toString(roomEmpty));
        if (all(roomEmpty)) {
            // update concentration site
            master.getConcentrationSite().endHeist(true);
            return END_HEIST;
        }

        // check if it should wait for canvas
        if (master.sentAnyAssaultParty() && master.getConcentrationSite().numberOfThieves() < N_THIEVES_PER_PARTY) {
            GenericIO.writelnString("Thieves in site: " + master.getConcentrationSite().numberOfThieves());
            return WAIT_FOR_CANVAS;
        }

        // otherwise, make more assault parties
        return CREATE_ASSAULT_PARTY;
    }

    public synchronized void takeARest() {
        MasterThief master = (MasterThief) Thread.currentThread();
        master.setThiefState(MasterThiefStates.WAITING_ARRIVAL);

        while (arrivedThieves.size() == 0 && master.getConcentrationSite().numberOfThieves() < N_THIEVES_ORDINARY) {
            try {
                logger(this, master, "Waiting for thieves to arrive...");
                wait();
                logger(this, master, "Woke up! " + master.getConcentrationSite().numberOfThieves() + " thieves in site.");
            } catch (InterruptedException e) {}
        }
    }

    public synchronized void handACanvas() {
        OrdinaryThief thief = (OrdinaryThief) Thread.currentThread();
        if (!thief.hasCanvas()) {
            roomEmpty[thief.getParty().getRoomID()] = true;
            thief.getMuseum().getRoom(thief.getParty().getRoomID()).setAssaultPartyID(-1);
        }
        partyThievesInSite[thief.getParty().getId()]++;

        // wake up master
        try {
            arrivedThieves.write(thief.getThiefID());
            notifyAll();
        } catch (MemException e) {}

        // wait for master to collect canvas
        while (arrivedThieves.has(thief.getThiefID())) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        partyThievesInSite[thief.getParty().getId()]--;
        thief.hasCanvas(false);
        loggerCrawl(this, thief, "Handed canvas to master thief.");

        // if last thief from party handing a canvas, free room
        if (partyThievesInSite[thief.getParty().getId()] == 0) {
            ((OrdinaryThief) Thread.currentThread()).getConcentrationSite().setRoomState(thief.getParty().getRoomID(), FREE_ROOM);
            notifyAll();
        }
    }

    public synchronized void collectACanvas() {
        try {
            // collect a canvas by taking a thief from the queue
            arrivedThieves.read();
        } catch (MemException e) {}

        // update rooms states
        for (int i = 0; i < N_ROOMS; i++) {
            if (roomEmpty[i])
                ((MasterThief) Thread.currentThread()).getConcentrationSite().setRoomState(i, EMPTY_ROOM);
        }
    }

    public synchronized void sumUpResults() {
        MasterThief master = (MasterThief) Thread.currentThread();
        master.setThiefState(MasterThiefStates.PRESENTING_REPORT);
        GenericIO.writelnString("Master Thief is presenting the report.");

        notifyAll();
    }
}
