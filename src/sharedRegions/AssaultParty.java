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
   }

   public int size() {
      return nThieves;
   }

   public Museum.Room getRoom() {
      return room;
   }

   public void setRoom(Museum.Room room) {
      this.room = room;
   }

   public AssaultParty(int id, GeneralRepos repos) throws MemException {
      this.id = id;
      this.thieves = new OrdinaryThief[N_THIEVES_PER_PARTY];
      nThieves = 0;
      nextThiefID = roomID = -1;
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

      GenericIO.writelnString("\n|| Beginning " + this + " ||\nLOG FORMAT: [" + this + "][OrdinaryThief][SPEED][POS][MOVES]\n");

      // wake up first thief
      notifyAll();
   }

   public synchronized void crawlIn() {
      OrdinaryThief thief = (OrdinaryThief) Thread.currentThread();
      thieves[nThieves++] = thief;
      thief.setThiefState(OrdinaryThiefStates.CRAWLING_INWARDS);

      // first thief to arrive sets next thief as himself
      if (nextThiefID == -1) {
         nextThiefID = thief.getThiefID();
         room = thief.getMuseum().getRoom(roomID);
         room.setAssaultPartyID(id);

         GenericIO.writelnString("\n" + room + " has " + room.getPaintings() + " paintings and is " + room.getDistance() + " steps away.\n");

         thief.canIMove(true);
         //loggerCrawl(this, thief, "first thief to crawl");
      }

      // wait for turn
      while (true) {
         // sleep if not his turn
         while (nextThiefID != thief.getThiefID() || nThieves < N_THIEVES_PER_PARTY) {
            try {
               wait();
            } catch (InterruptedException e) {}
         }

         printPositions();

         //loggerCrawl(this, thief, "woke up");
         while (thief.canIMove())
            move(thief, false);

         // check for endOfPath
         boolean endOfPath = thief.getPosition() == room.getDistance();
         if (endOfPath) {
            loggerCrawl(this, thief, "REACHED ROOM");
            thief.setThiefState(OrdinaryThiefStates.AT_A_ROOM);
         }

         // wake up next thief
         if (!wakeUpNextThief(thief, false)) {
            // print final positions if there are no more thieves to wake up
            printPositions();
         }

         if (endOfPath) {
            GenericIO.writelnString((Thread.currentThread()) + " left CrawlIn function...");
            thief.resetMovesLeft();
            thief.canIMove(false);
            break;
         }
      }
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

   public synchronized void crawlOut() {
      OrdinaryThief thief = (OrdinaryThief) Thread.currentThread();
      thief.setThiefState(OrdinaryThiefStates.CRAWLING_OUTWARDS);

      while (true) {
         // sleep if not his turn
         while (nextThiefID != thief.getThiefID()) {
            try {
               wait();
            } catch (InterruptedException e) {}
         }

         printPositions();

         // if this is the first thief to start crawl out, reset his moves left
         if (thief.getThiefID() == thieves[0].getThiefID())
            thief.canIMove(true);

         loggerCrawl(this, thief, "woke up");

         while(thief.canIMove())
            move(thief, true);

         // check for endOfPath
         boolean endOfPath = thief.getPosition() == 0;
         if (endOfPath) {
            loggerCrawl(this, thief, "Collection Site");
            thief.setThiefState(OrdinaryThiefStates.COLLECTION_SITE);
         }

         // wake up next thief
         if (!wakeUpNextThief(thief, true)) {
            // print final positions if there are no more thieves to wake up
            printPositions();
         }

         if (endOfPath) {
            GenericIO.writelnString((Thread.currentThread()) + " left CrawlOut function...");
            break;
         }
      }
   }

  private void move(OrdinaryThief thief, boolean backwards) {
     if (thief.getMovesLeft() == 0) {
        noMoreMoves(thief);
        return;
     }

     OrdinaryThief lowerThief = !backwards ? lowerThief(thief) : higherThief(thief);
     int distanceToNextThief = Math.abs(thief.getPosition() - lowerThief.getPosition());
     int goal = !backwards ? room.getDistance() : 0;
     int distanceToGoal = Math.abs(goal - thief.getPosition());
     int distanceToMove = min(thief.getDisplacement(), distanceToGoal, thief.getMovesLeft());
     //loggerCrawl(this, thief, "initial distance to move: " + distanceToMove);

     // is first thief
     boolean frontThief = !backwards ? isFirstThief(thief) : isLastThief(thief);
     if (frontThief) {
        //loggerCrawl(this, thief, "is in the front");
        distanceToMove = min(distanceToMove, MAX_SEPARATION_LIMIT-distanceToNextThief);
        if (distanceToMove == 0) {
           noMoreMoves(thief);
           return;
        }

        updatePosition(thief, distanceToMove, backwards);
        thief.setMovesLeft(thief.getMovesLeft() - distanceToMove);
        loggerCrawl(this, thief, "MOVED " + (backwards ? "-" : "") + distanceToMove + " positions to " + thief.getPosition());
        return;
     }

     // if middle or last thief
     boolean lastThief = !backwards ? isLastThief(thief) : isFirstThief(thief);
     if (!lastThief) {
        distanceToMove = min(distanceToMove, MAX_SEPARATION_LIMIT - distanceToNextThief);
        if (distanceToMove == 0) {
           noMoreMoves(thief);
           return;
        }
     }

     updatePosition(thief, distanceToMove, backwards);
     thief.setMovesLeft(thief.getMovesLeft() - distanceToMove);
     loggerCrawl(this, thief, "MOVED " + (backwards ? "-" : "") + distanceToMove + " positions to " + thief.getPosition());

     if (thief.getPosition() == goal) {
        noMoreMoves(thief);
        return;
     }

     // fix excess moves or if went to an occupied position
     if (checkExcessMove(thief, distanceToMove, backwards) || checkOccupiedMove(thief, backwards)) thief.setMovesLeft(0);
  }

   private void updatePosition(OrdinaryThief thief, int value, boolean backwards) {
      thief.setPosition(!backwards ? thief.getPosition() + value : thief.getPosition() - value);
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
      GenericIO.writelnString("Counter: " + counter);
      if (counter != N_THIEVES_PER_PARTY) {
         nextThiefID = nextThief.getThiefID();
         nextThief.resetMovesLeft();
         nextThief.canIMove(true);
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
      GenericIO.writelnString("new lower thief: " + newLowerThief);
      int distanceToNextThief = Math.abs(newLowerThief.getPosition() - thief.getPosition());
      if (distanceToNextThief > MAX_SEPARATION_LIMIT) {
         int excessMove = distanceToMove - distanceToNextThief;
         // fix position
         updatePosition(thief, excessMove, !backwards);
         loggerCrawl(this, thief, "[EXCESS MOVE] fixing...");
         loggerCrawl(this, thief, "MOVED " + excessMove + " positions to "+ thief.getPosition());
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
         loggerCrawl(this, thief, "[MOVED TO OCCUPIED POSITION] fixing...");
         loggerCrawl(this, thief, "MOVED " + "1 positions to "+ thief.getPosition());
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
      //loggerCrawl(this, thief, "no more moves left");
      thief.setMovesLeft(0);
      thief.canIMove(false);
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