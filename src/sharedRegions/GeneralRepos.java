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

    /**
     * Ordinary Thief situation
     * P if he is in a party
     * W if he is waiting for a party
     */

    private char[] ordinaryThiefSituation;

    /**
     * ID of the assault party that the thief is in.
     */
    private int [] ordinaryThiefAssaultPartyID;

    /**
     * ID of the room that the thief is in.
     */
    private int [] ordinaryThiefRoomID;


    /**
     * Position of the ordinary thief .
     */
    private int [] ordinaryThiefPosition;

    /**
     * If the ordinary thief has a canvas
     */
    private int[] ordinaryThiefCanvas;

    /**
     * Number of paintings collected
     */
    private int sumUpCanvas;

    /**
     * Displacement of the thief
     */
    private int [] thiefDisplacement;

    /**
     * Distance to the room
     */
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
     * True if the thief has a canvas.
     */
    private boolean hasCanvas;

    /**
     * Number of paintings in the room.
     */
    private int [] paintings;

    private int sumUp;

    private int displacement;

    private String s = "P";
    private int n_canvas;

    private int[] apRId;

    /**
     * General Repos constructor.
     * @param logFile name of the logging file.
     */

    public GeneralRepos(String logFile) {
        ordinaryThiefStates = new int[N_THIEVES_ORDINARY];
        ordinaryThiefSituation = new char[N_THIEVES_ORDINARY];
        ordinaryThiefAssaultPartyID = new int[N_THIEVES_ORDINARY];
        ordinaryThiefRoomID = new int[N_ASSAULT_PARTIES];
        ordinaryThiefPosition = new int[N_THIEVES_ORDINARY];
        ordinaryThiefCanvas = new int[N_THIEVES_ORDINARY];
        paintings = new int[N_THIEVES_ORDINARY];
        thiefDisplacement = new int[N_THIEVES_ORDINARY];
        distanceToRoom = new int[N_ROOMS];
        apRId = new int[N_ASSAULT_PARTIES];
        for (int i = 0; i < N_ASSAULT_PARTIES; i++) {
            apRId[i] = -1;
        }

        if ((logFile == null) || Objects.equals(logFile, ""))
            this.logFile = "logging";
        else this.logFile = logFile;
        ids = new int[N_THIEVES_ORDINARY];
        for (int i = 0; i < N_THIEVES_ORDINARY; i++) {
            ordinaryThiefStates[i] = OrdinaryThiefStates.CONCENTRATION_SITE;
            ids[i] = -1;
            ordinaryThiefSituation[i] = 'W';
            ordinaryThiefPosition[i] = 0;
            ordinaryThiefCanvas[i] = 0;
            paintings[i] = 0;
            thiefDisplacement[i] = 0;
            ordinaryThiefAssaultPartyID[i] = -1;
        }
        for(int i = 0; i < N_ASSAULT_PARTIES; i++){
            ordinaryThiefRoomID[i] = -1;
        }
        for(int i = 0; i < N_ROOMS; i++){
            distanceToRoom[i] = 0;
        }

        masterThiefStates = MasterThiefStates.PLANNING_HEIST;
        nPaintings = 0;
        distance = 0;
        position = 0;
        roomID = 0;
        hasCanvas = false;
        displacement = 0;
        sumUpCanvas = 0;
        n_canvas = 0;
        reportInitialStatus();


    }

    /**
     * Update the state of the ordinary thief t.
     * @param state state of the thief t.
     */
    public synchronized void updateMasterThiefState(int state) {
        switch (state){
            case MasterThiefStates.PLANNING_HEIST:
                //GenericIO.writelnString("Master Thief is planning the heist");
                break;
            case MasterThiefStates.DECIDING_WHAT_TO_DO:
                //GenericIO.writelnString("Master Thief is deciding what to do");
                break;
            case MasterThiefStates.ASSEMBLING_GROUP:
                //GenericIO.writelnString("Master Thief is assembling a group");
                break;
            case MasterThiefStates.WAITING_ARRIVAL:
                //GenericIO.writelnString("Master Thief is waiting for the group to arrive");
                break;
            case MasterThiefStates.PRESENTING_REPORT:
                //GenericIO.writelnString("Master Thief is presenting the report");
                break;
        }
        masterThiefStates = state;
        reportStatus();
    }


    /**
     * Update the logging file with the current state of the simulation.
     *
     */
    private void reportInitialStatus() {
        TextFile log = new TextFile();
        if (!log.openForWriting("./logs/", logFile)) {
            GenericIO.writelnString("The operation of creating the file " + logFile + " failed!");
            System.exit(1);
        }
        log.writelnString("                                    Heist to the Museum - Description of the internal state");
        log.writelnString(" "+"MstT    Thief 0      Thief 1      Thief 2      Thief 3      Thief 4      Thief 5");
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
            lineOne += "  " + ordinaryThiefSituation[i] + " " + thiefDisplacement[i] + "      ";
            if(i == 5) lineOne += "\n";
        }

            for (int i = 0; i < 6; i++) {
                if(i == 0 ){
                    lineTwo += "     "+String.format("%2d", apRId[0]) + "   ";
                }else if(i == 3){
                    lineTwo += "     "+String.format("%2d", apRId[1]) + "  ";
                }
                lineTwo += String.format("%2d", i) + "  ";
                lineTwo += String.format("%2d", ordinaryThiefPosition[i]) + "  ";
                lineTwo += String.format("%2d", ordinaryThiefCanvas[i]) + "     ";
            }

        lineTwo +="  ";

        // museum
        for(int roomID = 0; roomID < N_ROOMS; roomID++){
            lineTwo += "    ";
            lineTwo += String.format("%2d", paintings[roomID]) + "  ";
            lineTwo += String.format("%2d", distanceToRoom[roomID]) + "  ";
        }
        log.writelnString(lineOne+lineTwo+"\n");

        if (!log.close()) {
            GenericIO.writelnString("The operation of closing the file " + logFile + " failed!");
            System.exit(1);
        }
    }

    private int[] getThievesFromParty(int partyID) {
        int[] thieves = new int[N_THIEVES_PER_PARTY];
        for (int i = 0; i < N_THIEVES_PER_PARTY; i++)
            thieves[i] = -1;
        int count = 0;
        for (int thiefID : ordinaryThiefAssaultPartyID) {
            if (thiefID == partyID)
                thieves[count++] = thiefID;
        }
        GenericIO.writelnString("Thieves from party " + partyID + ": " + thieves[0] + " " + thieves[1] + " " + thieves[2]);
        return thieves;
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

    /**
     * Set the ordinary thief displacement
     * @param id the thief id
     * @param displacement the displacement
     */
    public synchronized void setOrdinaryThiefDisplacement(int id, int displacement){
        thiefDisplacement[id] = displacement;
        reportStatus();
    }

    public synchronized void setApRId(int id, int rId){
        apRId[id] = rId;
        reportStatus();
    }

    /**
     * Set the ordinary thief state
     * @param id the thief id
     * @param state the state
     */
    public synchronized void setOrdinaryThiefState(int id, int state){
        ordinaryThiefStates[id] = state;
        reportStatus();
    }

    /**
     * Set ordinary thief situation
     * if thief is waiting to join a party put 'W'
     * if thief is in a party put 'P'
     * @param id the thief id
     * @param situation boolean true if thief is in a party
     */
    public synchronized void setOrdinaryThiefSituation(int id, boolean situation){
        if(situation) ordinaryThiefSituation[id] = 'P';
        else ordinaryThiefSituation[id] = 'W';
        reportStatus();
    }

    /**
     * Set ordinary thief party id
     * @param id the thief id
     * @param assaultPartyID the party id
     */
    public synchronized void setOrdinaryThiefAssaultPartyID(int id, int assaultPartyID){
        ordinaryThiefAssaultPartyID[id] = assaultPartyID;
        reportStatus();
    }

    /**
     * Set ordinary thief room id
     * @param id the thief id
     * @param roomID the room id
     */
    public synchronized void setOrdinaryThiefRoomID(int id, int roomID){
        ordinaryThiefRoomID[id] = roomID;
        reportStatus();

    }

    /**
     * Set Distance to room
     * @param roomID the room id
     * @param distance the distance to the room
     */
    public synchronized void setDistanceToRoom(int roomID, int distance){
        distanceToRoom[roomID] = distance;
        reportStatus();
    }

    /**
     * Set the ordinary thief position
     * @param id the thief id
     * @param position the position of the thief
     */
    public synchronized void setOrdinaryThiefPosition(int id, int position) {
        ordinaryThiefPosition[id] = position;
        reportStatus();
    }


    /**
     * Set the ordinary thief canvas
     * if thief has canvas set 1
     * if thief doesn't have canvas set 0
     * @param id the thief id
     * @param hasCanvas boolean true if thief has canvas
     */
    public synchronized void setOrdinaryThiefCanvas(int id, boolean hasCanvas){
        if(hasCanvas)
            ordinaryThiefCanvas[id] = 1;
        else
            ordinaryThiefCanvas[id] = 0;
        reportStatus();
    }

    /**
     * Set the number of paintings in a room
     * @param roomID the room id
     * @param nPaintings the number of paintings
     */
    public synchronized void setnPaintings(int roomID,int nPaintings) {
        paintings[roomID] = nPaintings;
        reportStatus();
    }

    /**
     * Set the room distance
     * @param distance the distance
     */
    public synchronized void setDistance(int distance) {
        this.distance = distance;
    }

    public synchronized void setRoomID(int nRooms) {
        this.nRooms = nRooms;
    }

    /**
     * Set assault party id
     * @param id room id
     */
    public synchronized void setAssaultPartyID(int id) {
        this.roomID = id;
        reportStatus();
    }

    /**
     * Set the total number of paintings collected
     * @param n_canvas
     */
    public synchronized void setnCanvas(int n_canvas) {
        this.n_canvas = n_canvas;
    }

    /**
     * Print the sum up
     */
    public void printSumUp()
    {
        TextFile log = new TextFile ();

        if (!log.openForAppending ("./logs/", logFile))
        { GenericIO.writelnString ("The operation of opening for appending the file " + logFile + " failed!");
            System.exit (1);
        }

        log.writelnString("My friends, tonight's effort produced "+ n_canvas + " priceless paintings.");

        if (!log.close ())
        { GenericIO.writelnString ("The operation of closing the file " + logFile + " failed!");
            System.exit (1);
        }
    }


}

