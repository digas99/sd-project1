package sharedRegions;
import java.util.Objects;
import genclass.GenericIO;
import genclass.TextFile;
import entities.*;
import static utils.Parameters.*;

public class GeneralRepos {

    /**
     *  Name of the logging file.
     */
    private final String logFile;

    /**
     * Ordinary Thief states.
     */
    private int[] ordinaryThiefStates;

    /**
     * Master Thief state.
     */
    private int masterThiefStates;

    /**
     * Number of paintings in the museum.
     */
    private int nPaintings;

    /**
     * Number of rooms in the museum.
     */
    private int nRooms;

    /**
     * ID of the thief t
     */
    private int[] ids;

    private char[] ordinaryThiefSituation;

    private int [] ordinaryThiefAssaultPartyID;

    private int [] ordinaryThiefRoomID;

    private int [] ordinaryThiefPosition;

    private int[] ordinaryThiefCanvas;

    private int sumUpCanvas;

    private int [] thiefDisplacement;

    int [] distanceToRoom;




    /**
     * Distance to the concentration site.
     */
    private int distance;

    /**
     * Position of the thief t.
     */
    private int position;

    /**
     * ID of the room.
     */
    private int roomID;

    /**
     * Number of canvas in the room.
     */
    private int canvas;

    /**
     * Number of paintings in the room.
     */
    private int [] paintings;

    private String sumUp = "My friends, tonight's effort produced";

    private int displacement;

    private String s = "P";


    public GeneralRepos(String logFile) {
        ordinaryThiefStates = new int[N_THIEVES_ORDINARY];
        ordinaryThiefSituation = new char[N_THIEVES_ORDINARY];
        ordinaryThiefAssaultPartyID = new int[N_THIEVES_ORDINARY];
        ordinaryThiefRoomID = new int[N_ASSAULT_PARTIES];
        ordinaryThiefPosition = new int[N_THIEVES_ORDINARY];
        ordinaryThiefCanvas = new int[N_THIEVES_ORDINARY];
        paintings = new int[N_ROOMS];
        thiefDisplacement = new int[N_THIEVES_ORDINARY];
        distanceToRoom = new int[N_ROOMS];

        if ((logFile == null) || Objects.equals(logFile, ""))
            this.logFile = "logging";
        else this.logFile = logFile;
        ids = new int[N_THIEVES_ORDINARY];
        for (int i = 0; i < N_THIEVES_ORDINARY; i++) {
            ordinaryThiefStates[i] = OrdinaryThiefStates.CRAWLING_INWARDS;
            ids[i] = -1;
            ordinaryThiefSituation[i] = 'P';
            ordinaryThiefPosition[i] = -1;
            ordinaryThiefCanvas[i] = -1;
            paintings[i] = 0;
            thiefDisplacement[i] = 0;
            ordinaryThiefAssaultPartyID[i] = -1;
        }
        for(int i = 0; i < N_ASSAULT_PARTIES; i++){
            ordinaryThiefRoomID[i] = -1;
        }
        for(int i = 0; i < N_ROOMS; i++){
            distanceToRoom[i] = -1;
        }



        masterThiefStates = MasterThiefStates.PLANNING_HEIST;
        nPaintings = 0;
        distance = 0;
        position = 0;
        roomID = 0;
        canvas = 0;
        displacement = 0;
        sumUpCanvas = 0;

        reportInitialStatus();


    }

    public synchronized void updateMasterThiefState(int state) {
        switch (state){
            case MasterThiefStates.PLANNING_HEIST:
                GenericIO.writelnString("Master Thief is planning the heist");
                break;
            case MasterThiefStates.DECIDING_WHAT_TO_DO:
                GenericIO.writelnString("Master Thief is deciding what to do");
                break;
            case MasterThiefStates.ASSEMBLING_GROUP:
                GenericIO.writelnString("Master Thief is assembling a group");
                break;
            case MasterThiefStates.WAITING_ARRIVAL:
                GenericIO.writelnString("Master Thief is waiting for the group to arrive");
                break;
            case MasterThiefStates.PRESENTING_REPORT:
                GenericIO.writelnString("Master Thief is presenting the report");
                break;
        }
        masterThiefStates = state;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefStates(int[] states) {
        ordinaryThiefStates = states;
        reportStatus();
    }



    private void reportInitialStatus() {
        TextFile log = new TextFile();
        if (!log.openForWriting("./logs/", logFile)) {
            GenericIO.writelnString("The operation of creating the file " + logFile + " failed!");
            System.exit(1);
        }
        log.writelnString("                                    Heist to the Museum - Description of the internal state");
        log.writelnString(" "+"MstT    Thief 1      Thief 2      Thief 3      Thief 4      Thief 5      Thief 6");
        log.writelnString(" "+"Stat    Stat S MD    Stat S MD    Stat S MD    Stat S MD    Stat S MD    Stat S MD");
        log.writelnString("     "+"                Assault party 1                                  Assault party 2                                                 Museum");
        log.writelnString("     "+"      Elem 1         Elem 2        Elem 3                    Elem 1        Elem 2         Elem 3          Room 1      Room 2      Room 3      Room 4      Room 5");
        log.writelnString("     "+"Rid  Id Pos Cv     Id Pos Cv     Id Pos Cv             Rid  Id Pos Cv     Id Pos Cv     Id Pos Cv         NP DT       NP DT       NP DT       NP DT       NP DT");
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFile + " failed!");
            System.exit(1);
        }

    }

    private void reportStatus() {
        TextFile log = new TextFile();

        String lineOne = "";
        String lineTwo = "";

        if (!log.openForAppending("./logs/", logFile)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFile + " failed!");
            System.exit(1);
        }
        switch (masterThiefStates) {
            case MasterThiefStates.PLANNING_HEIST:
                lineOne += "PLHT";
                break;
            case MasterThiefStates.DECIDING_WHAT_TO_DO:
                lineOne += "DWTD";
                break;
            case MasterThiefStates.ASSEMBLING_GROUP:
                lineOne += "ASGR";
                break;
            case MasterThiefStates.WAITING_ARRIVAL:
                lineOne += "WTAR";
                break;
            case MasterThiefStates.PRESENTING_REPORT:
                lineOne += "PRRP";
                break;
        }
        for (int i = 0; i < N_THIEVES_ORDINARY; i++) {
            switch (ordinaryThiefStates[i]) {
                case OrdinaryThiefStates.CONCENTRATION_SITE:
                    lineOne += "CRST";
                    break;
                case OrdinaryThiefStates.CRAWLING_INWARDS:
                    lineOne += "CRIN";
                    break;
                case OrdinaryThiefStates.AT_A_ROOM:
                    lineOne += "ATAR";
                    break;
                case OrdinaryThiefStates.CRAWLING_OUTWARDS:
                    lineOne += "CROU";
                    break;
                case OrdinaryThiefStates.COLLECTION_SITE:
                    lineOne += "CLST";
                    break;
            }
            lineOne += " " + ordinaryThiefSituation[i] + " " + thiefDisplacement[i] + "   ";
            if(i == 5) lineOne += "\n";
        }


        for(int partyID = 0; partyID< N_ASSAULT_PARTIES; partyID++){
            // roomID
            lineTwo += String.format("%2d", ordinaryThiefRoomID[partyID]) + "  ";
            for (int thiefID = 0; thiefID < N_THIEVES_PER_PARTY; thiefID++) {
                // if thief is in the party
                if (ordinaryThiefAssaultPartyID[thiefID] == partyID) {
                    lineTwo += String.format("%2d", thiefID) + "  ";
                    lineTwo += String.format("%2d", ordinaryThiefPosition[thiefID]) + "  ";
                    lineTwo += String.format("%2d", ordinaryThiefCanvas[thiefID]) + "  ";
                }
            }
        }

        // museum
        for(int roomID = 0; roomID < N_ROOMS; roomID++){
            lineTwo += String.format("%2d", paintings[roomID]) + "  ";
            lineTwo += String.format("%2d", distanceToRoom[roomID]) + "  ";
        }
        log.writelnString(lineOne);
        log.writelnString(lineTwo);



       /* log.writelnString("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        lineOne += String.format("%4d",masterThiefStates)+
                "   "+ String.format("%4d", ordinaryThiefStates[0]) + "  "+String.format(s )+"  "+String.format("%1d",displacement) + "    " +
                String.format("%4d", ordinaryThiefStates[1]) + "  "+String.format(s )+"  "+String.format("%1d",displacement) + "   " +
                String.format("%4d", ordinaryThiefStates[2]) + "  "+String.format(s )+"  "+String.format("%1d",displacement) + "   " +
                "\n" +
                "    "+ String.format("%2d", roomID) + "  " + String.format("%2d", ids[0]) + "  " + String.format("%2d", position) + "  " + String.format("%2d", canvas)+ "  "+
                "  " + String.format("%2d", ids[1]) + "  " + String.format("%2d", position) + "  " + String.format("%2d", canvas) + "  " +
                "  " + String.format("%2d", ids[2]) + "  " + String.format("%2d", position) + "  " + String.format("%2d", canvas)+
                "            " + String.format("%2d", roomID) + "  " + String.format("%2d", ids[0]) + "  " + String.format("%2d", position) + "  " + String.format("%2d", canvas) +"   "+
                "  " + String.format("%2d", ids[1]) + "  " + String.format("%2d", position) + "  " + String.format("%2d", canvas) + "  " +
                "  " + String.format("%2d", ids[2]) + "  " + String.format("%2d", position) + "  " + String.format("%2d", canvas) + "  " +
                "      " + String.format("%2d",nPaintings ) + "  " + String.format("%2d", distance) +
                "      " + String.format("%2d",nPaintings ) + "  " + String.format("%2d", distance) +
                "      " + String.format("%2d",nPaintings ) + "  " + String.format("%2d", distance) +
                "      " + String.format("%2d",nPaintings ) + "  " + String.format("%2d", distance) +
                "      " + String.format("%2d",nPaintings ) + "  " + String.format("%2d", distance) ;
        log.writelnString(lineOne);
        */

        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFile + " failed!");
            System.exit(1);
        }
    }

    public void reportLegend() {
        TextFile log = new TextFile();                    // instantiation of a text file handler
        String lineStatus = "";                                    // state line to be printed
        if (!log.openForAppending("./logs/", logFile)) {
            GenericIO.writelnString("The operation of opening for appending the file " + logFile + " failed!");
            System.exit(1);
        }

        lineStatus += "\n\n";
        lineStatus += "Legend:\n";
        lineStatus += "MstT Stat    – state of the master thief\n";
        lineStatus += "Thief # Stat - state of the ordinary thief # (# - 1 .. 6)\n";
        lineStatus += "Thief # S    – situation of the ordinary thief # (# - 1 .. 6) either 'W' (waiting to join a party) or 'P' (in party)\n";
        lineStatus += "Thief # MD   – maximum displacement of the ordinary thief # (# - 1 .. 6) a random number between 2 and 6\n";
        lineStatus += "Assault party # RId  – assault party # (# - 1,2) elem # (# - 1 .. 3) room identification (1 .. 5)\n";
        lineStatus += "Assault party # Elem # Id – assault party # (# - 1,2) elem # (# - 1 .. 3) member identification (1 .. 6)\n";
        lineStatus += "Assault party # Elem # Pos – assault party # (# - 1,2) elem # (# - 1 .. 3) present position (0 .. DT RId)\n";
        lineStatus += "Assault party # Elem # Cv – assault party # (# - 1,2) elem # (# - 1 .. 3) carrying a canvas (0,1)\n";
        lineStatus += "Museum Room # NP - room identification (1 .. 5) number of paintings presently hanging on the walls\n";
        lineStatus += "Museum Room # DT - room identification (1 .. 5) distance from outside gathering site, a random number between 15 and 30\n";
        ;

        log.writelnString(lineStatus);
        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFile + " failed!");
            System.exit(1);
        }
    }


    public synchronized void setOrdinaryThiefStatus(int id, int state, char situation, int assaultPartyID, int roomID, int position, int canvas){
        ordinaryThiefStates[id] = state;
        ordinaryThiefSituation[id] = situation;
        ordinaryThiefAssaultPartyID[id] = assaultPartyID;
        ordinaryThiefRoomID[id] = roomID;
        ordinaryThiefPosition[id] = position;
        ordinaryThiefCanvas[id] = canvas;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefDisplacement(int id, int displacement){
        thiefDisplacement[id] = displacement;
        reportStatus();
    }
    public synchronized void setMasterThiefState(int state) {
        masterThiefStates = state;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefState(int id, int state){
        ordinaryThiefStates[id] = state;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefSituation(int id, char situation){
        ordinaryThiefSituation[id] = situation;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefAssaultPartyID(int id, int assaultPartyID){
        ordinaryThiefAssaultPartyID[id] = assaultPartyID;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefRoomID(int id, int roomID){
        ordinaryThiefRoomID[id] = roomID;
        reportStatus();
    }

    public synchronized void setDistanceToRoom(int roomID, int distance){
        distanceToRoom[roomID] = distance;
        reportStatus();
    }

    public synchronized void setOrdinaryThiefPosition(int id, int position) {
        ordinaryThiefPosition[id] = position;
        reportStatus();
        reportStatus();
    }

    public synchronized void setOrdinaryThiefCanvas(int id, int canvas,int roomID) {
        ordinaryThiefCanvas[id] = canvas;
        if(canvas== 0)
            paintings[roomID] = 0;
        else
            paintings[roomID]--;
            this.sumUpCanvas++;
        reportStatus();
    }

    public synchronized void setnPaintings(int roomID,int nPaintings) {
        paintings[roomID] = nPaintings;
        reportStatus();
    }

    public synchronized void setDistance(int distance) {
        this.distance = distance;
    }

    public synchronized void setRoomID(int nRooms) {
        this.nRooms = nRooms;
    }

    public synchronized void getCanvas(int canvas) {
        this.canvas = canvas;
        reportStatus();
    }

    public synchronized void setAssaultPartyID(int id) {
        this.roomID = id;
        reportStatus();
    }

    public void printSumUp()
    {
        TextFile log = new TextFile ();

        if (!log.openForAppending (".", logFile))
        { GenericIO.writelnString ("The operation of opening for appending the file " + logFile + " failed!");
            System.exit (1);
        }

        log.writelnString("\n" + sumUp + ".");

        if (!log.close ())
        { GenericIO.writelnString ("The operation of closing the file " + logFile + " failed!");
            System.exit (1);
        }
    }
}

