package se.mansehr.photomickr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import se.mansehr.photomickr.middletier.FlickrMiddletier;
import se.mansehr.photomickr.middletier.TagSource;

public class PhotoMickr {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		TagSource ts = new FlickrMiddletier();
		
		if(ts.setUp() == false) {
			System.out.println("Failed setting upp the tag source!");
			System.exit(1);
		}
		
		SearchDispatcher sd = new SearchDispatcher(ts);		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command;
		
		sd.start();
		
        System.out.println("=========== PhotoMickr ===========");
        printHelp();
        
        while (true) {
            System.out.print("Command: ");
            command = br.readLine();
            
            if (command.startsWith("!")) {
                if (command.equals("!quit")) {
                    break;
                } else if (command.equals("!help")) {
                    printHelp();
                } else if (command.equals("!status")) {
                    sd.printStatus();
                } else if (command.equals("!verbose")) {
                    //MessageDisplay.setVerbosity(MessageDisplay.DEBUG);
                } else if (command.equals("!no_verbose") || command.equals("!")) {
                    //MessageDisplay.setVerbosity(MessageDisplay.NO_OUTPUT);
                } else {
                    System.out.println("Unknown command!");
                }
                continue;
            } else {
            	System.out.println("Unknown command!");
            }
        }
        System.exit(0);
	}

	private static void printHelp() {
		System.out.println("\n=========== Help ===========");
        System.out.println("Predefined commands: !quit, !status, !wordinfo, !verbose, !no_verbose");
	}

}
