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
		
		//TODO: create DiskNames text file if doesn't exist
		
		DiskUnit.createDiskUnit(name, capacity, blockSize);
		// Mount the disk unit in order to create its root directory.
		DiskUnit d = DiskUnit.mount(name);
		// Set the root directory
		VirtualDiskBlock root = new VirtualDiskBlock(blockSize);
		DiskUtils.copyIntToBlock(root, blockSize-4, 0);
		d.write(d.getFirstDataBlock(), root);
		// Set i-node 0 to reference root
		VirtualDiskBlock firstBlockRef = new VirtualDiskBlock(blockSize);  // Virtual Disk block of the i-node where the root is stored
		int rootINodePos = 1;   // Byte index of the root directory i-node 
		// Copy the first i-node block into the Virtual Disk Block
		d.read(rootINodePos, firstBlockRef);          
		// Write in the i-node 0 the reference to the root directory in the data blocks
		DiskUtils.copyIntToBlock(firstBlockRef, 0, d.getFirstDataBlock());
		// Write into the disk the virtual disk block with updated reference to the root directory data block
		d.write(rootINodePos, firstBlockRef);
		d.shutdown(); // Shutdown the disk
	}
	
	
	
	

}
