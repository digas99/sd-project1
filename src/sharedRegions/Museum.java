package sharedRegions;

import entities.OrdinaryThief;
import entities.OrdinaryThiefStates;

import static utils.Parameters.*;
import static utils.Utils.*;

/**
 * Shared Region with a method used by the Ordinary Thieves.
 * This class is responsible for keeping track of the paintings in the museum.
 */


public class Museum {

    /**
     * Array of rooms in the museum
     */
    private final Room[] rooms = new Room[N_ROOMS];

    /**
     * General Repository of Information
     */
    GeneralRepos repos;

    /**
     * Museum constructor
     * @param repos GeneralRepos
     */
    public Museum(GeneralRepos repos) {
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = new Room(i);
        }
        this.repos = repos;
        for (int i = 0; i < rooms.length; i++) {
            repos.setnPaintings(i, rooms[i].getTotalPaintings());
        }

    }

    @Override
    public String toString() {
        return "Museum";
    }

    /**
     * Method used by the setnPaintings
     * Ordinary Thieves to roll a canvas from a room
     */
    public synchronized boolean rollACanvas(int roomID) {
        OrdinaryThief ordinaryThief = (OrdinaryThief) Thread.currentThread();
        ordinaryThief.setThiefState(OrdinaryThiefStates.AT_A_ROOM);
        logger(ordinaryThief, "Rolling a canvas");
        Room room = getRoom(roomID);

        boolean hasCanvas = room.getPaintings() > 0;


        if (room.getPaintings() == 0)
            logger(ordinaryThief, "Left empty handed from " + room);
        else
            logger(ordinaryThief, "Rolled a canvas from " + room + ". " + room.getPaintings() + "/"+ room.getTotalPaintings() +" left");

        repos.setOrdinaryThiefCanvas(ordinaryThief.getThiefID(), hasCanvas);

        return hasCanvas;
    }

    /**
     * Get a room
     * @param roomID Room ID
     * @return Room
     */
    public Room getRoom(int roomID) {
        for (Room room : rooms) {
            if (room.getID() == roomID)
                return room;
        }
        return null;
    }

    /**
     * Helper class to represent a room in the museum
     */

    public class Room {

        private int id;
        private int distance;
        private int paintings;
        private final int totalPaintings;
        private int assaultPartyID;


        /**
         * Get the distance of the room
         * @return Distance
         */
        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        /**
         * Get the number of paintings in the room
         * @return Number of paintings
         */
        public int getPaintings() {
            return paintings;
        }

        /**
         * Set the number of paintings in the room
         * @param paintings Number of paintings
         */
        public void setPaintings(int paintings) {
            this.paintings = paintings;
        }

        /**
         * Get the AssaultParty ID of the room
         * @return AssaultParty ID
         */
        public int getAssaultPartyID() {
            return assaultPartyID;
        }

        /**
         * Set the AssaultParty ID of the room
         * @param assaultPartyID AssaultParty ID
         */
        public void setAssaultPartyID(int assaultPartyID) {
            this.assaultPartyID = assaultPartyID;
        }

        /**
         * Get the total number of paintings in the room
         * @return Total number of paintings
         */
        public int getTotalPaintings() {
            return totalPaintings;
        }

        /**
         * Get the ID of the room
         * @return ID
         */
        public int getID() {
            return id;
        }

        /**
         * Set the ID of the room
         * @param id
         */

        public void setID(int id) {
            this.id = id;
        }

        /**
         * Room constructor that takes an ID to initialize the room
         * It also initializes the distance and number of paintings in the room randomly
         * @param id Room ID
         */
        public Room(int id) {
            this.id = id;
            distance = random(MIN_DISTANCE, MAX_DISTANCE);
            paintings = totalPaintings = random(MIN_PAINTINGS, MAX_PAINTINGS);
            assaultPartyID = -1;
        }

        @Override
        public String toString() {
            return "Room_"+id;
        }


    }
}
