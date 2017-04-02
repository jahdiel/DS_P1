/**
 * 
 */
package theSystem;

import java.io.IOException;

import diskUtilities.DiskManager;
import systemGeneralClasses.SystemController;

/**
 * @author Pedro I. Rivera-Vega
 *
 */
public class MySystem {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException  {
		
		// Initialing Shell
		DiskManager.createDiskDirectory(); // Creates the DiskUnit directory
		DiskManager.getDiskUnitNames();    // Place in memory the names of the already created DiskUnits
		
		// Processing commands
		SystemController system = new SystemController(); 
		system.start(); 
		// the system is shutting down...
		System.out.println("+++++ SYSTEM SHUTDOWN +++++"); 
	}

}
