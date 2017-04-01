package diskUtilities;

import diskUnitExceptions.ExistingDiskException;
import diskUnitExceptions.InvalidParameterException;

public class DiskManager {
	/**
	 * Class which manages the disks and files stored in the disk unit. 
	 * @author Jahdiel Alvarez
	 */
	public DiskManager() {
		
	}
	
	/**
	 * Creates a new disk unit with the provided parameters.
	 * It also separates disks blocks for the i-nodes.
	 * @param name Name of the disk unit.
	 * @param capacity amount of disk blocks in the disk unit
	 * @param blockSize bytes per each disk block
	 */
	public static void createDiskUnit(String name, int capacity, int blockSize) throws ExistingDiskException, InvalidParameterException {
		
		//TODO: Set the root directory
		DiskUnit.createDiskUnit(name, capacity, blockSize);
	}

}
