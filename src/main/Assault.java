package main;

import entities.MasterThief;
import entities.OrdinaryThief;
import genclass.FileOp;
import genclass.GenericIO;
import sharedRegions.*;
import utils.MemException;

import java.util.concurrent.TimeUnit;

import static utils.Parameters.*;
import static utils.Utils.logger;

public class Assault {

    public static void main(String[] args) throws MemException {
        //GenericIO.writelnString("Starting program with " + N_THIEVES_ORDINARY + " Ordinary Thieves");

        // check proportions
        if (N_THIEVES_ORDINARY % N_ASSAULT_PARTIES != 0)
            throw new IllegalArgumentException("N_THIEVES_ORDINARY must be a multiple of N_ASSAULT_PARTIES");

        MasterThief[] masters;
        OrdinaryThief[] thieves;

        AssaultParty[] assaultParties;
        CollectionSite collectionSite;
        ConcentrationSite concentrationSite;
        Museum museum;
        GeneralRepos repos;
        String logFile;
        char opt;
        boolean success;

        //GenericIO.writelnString ("\n" + "      Heist to the Museum\n");
        do { GenericIO.writeString ("Logging file name? ");
            logFile = args.length == 0 ? GenericIO.readlnString () : args[0];
            if (!FileOp.exists(".", "logs"))
                FileOp.makeDirectory(".", "logs");

            if (FileOp.exists ("./logs/", logFile)){
                if (args.length == 0) {
                    do {
                        GenericIO.writeString ("There is already a file with this name. Delete it (y - yes; n - no)? ");
                        opt = GenericIO.readlnChar ();
                    } while ((opt != 'y') && (opt != 'n'));
                    if (opt == 'y')
                        success = true;

                    else success = false;
                } else success = true;
            }
            else success = true;
        } while (!success);

        // init shared regions
        repos = new GeneralRepos(logFile);
        collectionSite = new CollectionSite(repos);
        concentrationSite = new ConcentrationSite(repos);
        museum = new Museum(repos);
        assaultParties = new AssaultParty[N_ASSAULT_PARTIES];
        for (int i = 0; i < N_ASSAULT_PARTIES; i++)
            assaultParties[i] = new AssaultParty(i, repos);


        // init masters and thieves
        masters = new MasterThief[N_THIEVES_MASTER];
        thieves = new OrdinaryThief[N_THIEVES_ORDINARY];
        for (int i = 0; i < N_THIEVES_MASTER; i++)
            masters[i] = new MasterThief("Master_" + i, i, museum, concentrationSite, collectionSite, assaultParties,repos) ;
        for (int i = 0; i < N_THIEVES_ORDINARY; i++)
            thieves[i] = new OrdinaryThief("Ordinary_" + i, i, museum, concentrationSite, collectionSite, assaultParties,repos);

        // start counting elapsed time
        long start = System.currentTimeMillis();

        // start threads
        for (int i = 0; i < N_THIEVES_MASTER; i++)
            masters[i].start();
        for (int i = 0; i < N_THIEVES_ORDINARY; i++)
            thieves[i].start();

        // join threads
        for (int i = 0; i < N_THIEVES_MASTER; i++) {
            try {
                masters[i].join();
            } catch (InterruptedException e) {e.printStackTrace();}
            logger(masters[i], "has terminated!");
        }
        for (int i = 0; i < N_THIEVES_ORDINARY; i++) {
            try {
                thieves[i].join();
            } catch (InterruptedException e) {e.printStackTrace();}
            logger(thieves[i], "has terminated!");
        }

        // end counting elapsed time
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime); // get the number of seconds
        long millis = elapsedTime % 1000; // get the number of milliseconds (mod 1000 to get the remainder)
        String readable = String.format("%d.%ds", seconds, millis);
        GenericIO.writelnString("The Heist took " + readable + " to complete.");
    }
}

