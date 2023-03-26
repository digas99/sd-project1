package sharedRegions;

import entities.MasterThief;
import entities.MasterThiefStates;
import entities.OrdinaryThief;
import entities.OrdinaryThiefStates;
import genclass.GenericIO;
import utils.MemException;

import static utils.Parameters.*;
import static utils.Utils.*;

public class AssaultParty {
   /**
    * ID of the Assault Party.
    */
   private int id;
   /**
    * Array of thieves in the party.
    */
   private OrdinaryThief[] thieves;
   /**
    * ID of the room being sacked.
    */
   private int roomID;
   /**
    * ID of the next thief to crawl.
    */
   private int nextThiefID;
   /**
    * Number of thieves that joined the party so far.
    */
   private int nThieves;
   /**
    * Room being sacked.
    */
   private Museum.Room room;

   private final GeneralRepos repos;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getRoomID() {
      return roomID;
   }

   public void setRoomID(int roomID) {
      this.roomID = roomID;
      repos.setRoomID(roomID);
   }

   public void resetThieves() {
       thieves = new OrdinaryThief[N_THIEVES_PER_PARTY];
       nThieves = 0;
       nextThiefID = roomID = -1;
   }

   public int size() {
      return nThieves;
   }

   public AssaultParty(int id, GeneralRepos repos) throws MemException {
      this.id = id;
      this.repos = repos;
      resetThieves();
   }

   @Override
   public String toString() {
      return "AssaultParty_"+id;
   }

   /**
    * Send Assault Party.
    * The master thief calls this method to wake up the first thief of the Assault Party and trigger his crawling.
    * It also sets up the ID for the next assault party and resets other variables.
    */

   public synchronized void sendAssaultParty() {
      MasterThief master = (MasterThief) Thread.currentThread();
      master.setThiefState(MasterThiefStates.DECIDING_WHAT_TO_DO);
      GenericIO.writelnString("Sending " + this + " to Room " + roomID);
      master.sentAnyAssaultParty(true);

      repos.setAssaultPartyID(id);

      GenericIO.writelnString("\n|| Beginning " + this + " ||\nLOG FORMAT: [" + this + "][OrdinaryThief][SPEED][POS][MOVES]\n");

      // wake up first thief
      notifyAll();
   }

    public synchronized void reverseDirection() {
        OrdinaryThief thief = (OrdinaryThief) Thread.currentThread();
        thief.setThiefState(OrdinaryThiefStates.CRAWLING_OUTWARDS);

        loggerCrawl(this, thief, "ready to reverse direction");
        // if this was the last thief to reverse direction, notify the first party member to begin the crawling out
        // the id of the last thief to arrive is saved in nextThiefID from the last call to crawlIn
        if (thief.getThiefID() == nextThiefID) {
            loggerCrawl(this, thief, "wakes up first thief to begin crawling out");
            // set next thief id to the first thief
            nextThiefID = thieves[0].getThiefID();
            notifyAll();
        }
    }

   public synchronized void crawlIn() {
      OrdinaryThief thief = (OrdinaryThief) Thread.currentThread();

      try {
        thieves[nThieves++] = thief;
      } catch (ArrayIndexOutOfBoundsException e) {}

      thief.setThiefState(OrdinaryThiefStates.CRAWLING_INWARDS);

      repos.setOrdinaryThiefStatus(thief.getThiefID(), thief.getThiefState(), 'P', this.id, this.roomID,thief.getPosition(), 0);

       // first thief to arrive sets next thief as himself
      if (nextThiefID == -1) {
         nextThiefID = thief.getThiefID();
         room = thief.getMuseum().getRoom(roomID);
         room.setAssaultPartyID(id);
         thief.canMove(true);

         GenericIO.writelnString("\n" + room + " has " + room.getPaintings() + " paintings and is " + room.getDistance() + " steps away.\n");
      }

      do {
          while (nextThiefID != thief.getThiefID() || nThieves < N_THIEVES_PER_PARTY) {
              try {
                  wait(); // sleep if not his turn
              } catch (InterruptedException e) {}
          }
      } while(crawl(thief, room.getDistance(), OrdinaryThiefStates.AT_A_ROOM, false));

      GenericIO.writelnString((Thread.currentThread()) + " left " + Thread.currentThread().getStackTrace()[1].getMethodName() + " function...");
      repos.setDistance(room.getDistance());
      loggerCrawl(this, thief, "REACHED ROOM");
   }

   public synchronized void crawlOut() {
       OrdinaryThief thief = (OrdinaryThief) Thread.currentThread();
       thief.setThiefState(OrdinaryThiefStates.CRAWLING_OUTWARDS);

       // if this is the first thief to start crawl out, reset his moves left
       if (thief.getThiefID() == thieves[0].getThiefID())
           thief.canMove(true);

       do {
           while (nextThiefID != thief.getThiefID()) {
               try {
                   wait(); // sleep if not his turn
               } catch (InterruptedException e) {}
           }
       } while (crawl(thief, 0, OrdinaryThiefStates.COLLECTION_SITE, true));

       GenericIO.writelnString((Thread.currentThread()) + " left " + Thread.currentThread().getStackTrace()[1].getMethodName() + " function...");
       loggerCrawl(this, thief, "REACHED COLLECTION SITE");
   }

   private boolean crawl(OrdinaryThief thief, int goal, int endState, boolean backwards) {
       //printPositions();

       while(thief.canMove()) // move the most
           move(thief, goal, backwards);

       //if (!wakeUpNextThief(thief, backwards)) // wake up next thief
           //printPositions(); // if last thief, print final positions
        wakeUpNextThief(thief, backwards);


       repos.setOrdinaryThiefPosition(thief.getThiefID(), thief.getPosition());

       if (thief.getPosition() == goal) { // check for end of path
           thief.setThiefState(endState);
           thief.resetMovesLeft();
           thief.canMove(false);
           return false;
       }

       return true;
   }

  private void move(OrdinaryThief thief, int goal, boolean backwards) {
     if (thief.getMovesLeft() == 0) {
        noMoreMoves(thief);
        return;
     }

     int distanceToMove = min(thief.getDisplacement(), Math.abs(goal - thief.getPosition()), thief.getMovesLeft());

     // is front or middle thief, check the distance to the next thief
     boolean lastThief = !backwards ? isLastThief(thief) : isFirstThief(thief);
     if (!lastThief) {
         OrdinaryThief lowerThief = !backwards ? lowerThief(thief) : higherThief(thief);
         distanceToMove = min(distanceToMove, MAX_SEPARATION_LIMIT - Math.abs(thief.getPosition() - lowerThief.getPosition()));
         if (distanceToMove == 0) {
             noMoreMoves(thief);
             return;
         }
     }

     // make the move
     updatePosition(thief, distanceToMove, backwards);
     thief.setMovesLeft(thief.getMovesLeft() - distanceToMove);
     //loggerCrawl(this, thief, "MOVED " + (backwards ? "-" : "") + distanceToMove + " positions to " + thief.getPosition());

     if (thief.getPosition() == goal) {
        noMoreMoves(thief);
        return;
     }

     // fix excess moves or if went to an occupied position
     if (checkExcessMove(thief, distanceToMove, backwards) || checkOccupiedMove(thief, backwards)) thief.setMovesLeft(0);
  }

   private void updatePosition(OrdinaryThief thief, int value, boolean backwards) {
      thief.setPosition(!backwards ? thief.getPosition() + value : thief.getPosition() - value);
      repos.setOrdinaryThiefPosition(thief.getThiefID(), thief.getPosition());
   }

   /**
    * Find a next thief to wake up.
    *
    * @param thief Thief that just moved
    * @return true if found a thief to wake up, false otherwise
    */

   private boolean wakeUpNextThief(OrdinaryThief thief, boolean backwards) {
      OrdinaryThief currentThief = thief;
      OrdinaryThief nextThief;
      int goal = !backwards ? room.getDistance() : 0;
      int counter = 0;
      do {
         nextThief = !backwards ? lowerThief(thief) : higherThief(thief);
         thief = nextThief;
         if (++counter == N_THIEVES_PER_PARTY) break;
      } while (nextThief.getPosition() == goal);

      if (currentThief == nextThief) return false;

      // only choose next thief if not all thieves are at the room
      if (counter != N_THIEVES_PER_PARTY) {
         nextThiefID = nextThief.getThiefID();
         nextThief.resetMovesLeft();
         nextThief.canMove(true);
         notifyAll();
         return true;
      }

      return false;
   }

   /**
    * Check if thief moved in excess.
    * Fix thief position if it moved past another thief and went too far related to that new lower thief.
    *
    * @param thief
    * @param distanceToMove
    * @return true if a fix in the position was made.
    */

   private boolean checkExcessMove(OrdinaryThief thief, int distanceToMove, boolean backwards) {
      OrdinaryThief newLowerThief = !backwards ? lowerThief(thief) : higherThief(thief);
      //GenericIO.writelnString("new lower thief: " + newLowerThief);
      int distanceToNextThief = Math.abs(newLowerThief.getPosition() - thief.getPosition());
      if (distanceToNextThief > MAX_SEPARATION_LIMIT) {
         int excessMove = distanceToMove - distanceToNextThief;
         // fix position
         updatePosition(thief, excessMove, !backwards);
         //loggerCrawl(this, thief, "[EXCESS MOVE] fixing...");
         //loggerCrawl(this, thief, "MOVED " + excessMove + " positions to "+ thief.getPosition());
         return true;
      }
      return false;
   }

   /**
    * Check if thief move went to an occupied position.
    * The thief's position is fixed (decreased) until it is no longer in an occupied position.
    *
    * @param thief
    * @return true if a fix in the position was made.
    */

   private boolean checkOccupiedMove(OrdinaryThief thief, boolean backwards) {
      boolean wrongMove = false;
      // fix if it got to occupied position
      while (inOccupiedPos(thief)) {
         wrongMove = true;
         updatePosition(thief, 1, !backwards);
         //loggerCrawl(this, thief, "[MOVED TO OCCUPIED POSITION] fixing...");
         //loggerCrawl(this, thief, "MOVED " + "1 positions to "+ thief.getPosition());
      }
      return wrongMove;
   }

   /**
    * Check if thief is in occupied position.
    *
    * @param thief
    * @return true if it is in occupied position, false if not
    */

   private boolean inOccupiedPos(OrdinaryThief thief) {
      int thiefPos = thief.getPosition();
      for (OrdinaryThief t : thieves) {
         if (thief != t) {
            if (thiefPos == t.getPosition())
               return true;
         }
      }
      return false;
   }

   /**
    * Get the thief that is in a lower position than the one given.
    * This behaves in a circular way.
    *
    * @param thief
    * @return thief in a lower position
    */

   private OrdinaryThief lowerThief(OrdinaryThief thief) {
      int thiefPos = thief.getPosition() == 0 ? 1 : thief.getPosition(); // handle first time situation
      int nextPos = -1;
      OrdinaryThief nextThief = null;
      // add highest to make it circular, if needed
      int highestPos = 0;
      OrdinaryThief highestThief = null;

      for (OrdinaryThief t : thieves) {
         // skip itself
         if (thief == t) continue;

         // get lower thief
         if (t.getPosition() < thiefPos && t.getPosition() > nextPos) {
            nextPos = t.getPosition();
            nextThief = t;
         }

         // get highest thief
         if (t.getPosition() > highestPos) {
            highestPos = t.getPosition();
            highestThief = t;
         }
      }

      // make it circular
      if (nextThief == null) nextThief = highestThief;

      return nextThief;
   }

   /**
    * Get the thief that is in a higher position that the one given.
    * This behaves in a circular way.
    *
    * @param thief
    * @return thief in a higher position
    */

   private OrdinaryThief higherThief(OrdinaryThief thief) {
      int thiefPos = thief.getPosition() == room.getDistance() ? room.getDistance() - 1 : thief.getPosition(); // handle first time situation
      int prevPos = Integer.MAX_VALUE;
      OrdinaryThief prevThief = null;
      // add lowest to make it circular, if needed
      int lowestPos = Integer.MAX_VALUE;
      OrdinaryThief lowestThief = null;

      for (OrdinaryThief t : thieves) {
         // skip itself
         if (thief == t) continue;

         // get higher thief

         if (t.getPosition() > thiefPos && t.getPosition() < prevPos) {
            prevPos = t.getPosition();
            prevThief = t;
         }

         // get lowest thief
         if (t.getPosition() < lowestPos) {
            lowestPos = t.getPosition();
            lowestThief = t;
         }
      }

      // make it circular
      if (prevThief == null) prevThief = lowestThief;

      return prevThief;
   }

   private boolean isMiddleThief(OrdinaryThief thief) {
      return !isFirstThief(thief) && !isLastThief(thief);
   }

   /**
    * Check if given thief is the first thief (the one in the highest position)
    *
    * @param thief
    * @return true if it is the first, false if not
    */

   private boolean isFirstThief(OrdinaryThief thief) {
      int highestPos = 0;
      OrdinaryThief firstThief = null;
      for (OrdinaryThief t : thieves) {
         if (t.getPosition() > highestPos) {
            highestPos = t.getPosition();
            firstThief = t;
         }
      }
      return firstThief == null || thief == firstThief;
   }

   /**
    * Check if given thief is the last thief (the one in the lowest position)
    *
    * @param thief
    * @return true if it is the last, false if not
    */

   private boolean isLastThief(OrdinaryThief thief) {
      int leastPos = Integer.MAX_VALUE;
      OrdinaryThief lastThief = null;
      for (OrdinaryThief t : thieves) {
         if (t.getPosition() < leastPos) {
            leastPos = t.getPosition();
            lastThief = t;
         }
      }
      return thief == lastThief;
   }

   private void noMoreMoves(OrdinaryThief thief) {
      thief.setMovesLeft(0);
      thief.canMove(false);
   }

   private void printPositions() {
      String print = "\nPositions for " + this + " in " + room + ":\n";
      for (OrdinaryThief t : thieves) {
         print += "["+t.getThiefID()+"]";
      }
      print += "\n---------\n";
      for (OrdinaryThief t : thieves) {
         print += "["+t.getPosition()+"]";
      }
      GenericIO.writelnString(print+"\n");
   }
}