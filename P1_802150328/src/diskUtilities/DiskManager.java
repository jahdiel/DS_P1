package diskUtilities;

import diskUnitExceptions.ExistingDiskException;
import diskUnitExceptions.InvalidParameterException;

public class DiskManager {
	/**
	 * Class which manages the disks and files stored in the disk unit. 
	 * @author Jahdiel Alvarez
	 */
	
	public static final int I_NODE_SIZE = 9; 
	
	public DiskManager() {
		
	}
	
	/**
	 * Creates a new disk unit with the provided parameters.
	 * It also separates disks blocks for the i-nodes.
	 * @param name Name of the disk unit.
	 * @param capacity amount of disk blocks in the disk unit
	 * @param blockSize bytes per each disk block
	 */
	public static void createDiskUnit(String name, int capacity, int blockSize) 
			throws ExistingDiskException, InvalidParameterException {
		
		//TODO: create DiskNames text file if doesn't exist
		
		DiskUnit.createDiskUnit(name, capacity, blockSize);
		// Mount the disk unit in order to create its root directory.
		DiskUnit d = DiskUnit.mount(name);
		
		// Set the root directory
		setRootDirectory(d, blockSize);
		
		// Initialize the free block structure
		initializeFreeBlocks(d);
		
		d.shutdown(); // Shutdown the disk
	}
	
	/**
	 * Copies the contents of a disk block into a Virtual Disk Block
	 * @param d DiskUnit
	 * @param blockSize Bytes in the Block. 
	 * Needs to be the same as the amount of bytes per block of the disk blocks.
	 * @param blockNum Index of disk block to copy.
	 * @return
	 */
	public static VirtualDiskBlock copyBlockToVDB(DiskUnit d, int blockSize, int blockNum) {
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(blockNum, vdb);
		
		return vdb;
	}
	
	/**
	 * Initializes the free block structure. In the beginning all data blocks
	 * are part of the structure.
	 * @param d DiskUnit to initialize its free blocks (all data blocks)
	 */
	private static void initializeFreeBlocks(DiskUnit d) {
		FreeBlockManager.initializeFreeBlocks(d);
	}
	
	/**
	 * Sets the root directory in the first data block. And sets the i-node 0's first block
	 * to refer to the root directory disk block.
	 * @param d DiskUnit to work with.
	 * @param blockSize Amount of bytes per block.
	 */
	private static void setRootDirectory(DiskUnit d, int blockSize) {
		// Set the root directory
		VirtualDiskBlock root = new VirtualDiskBlock(blockSize);
		int rootDataBlock = d.getFirstDataBlock()-1; // index of the root block number
		DiskUtils.copyIntToBlock(root, blockSize-4, 0);
		d.write(rootDataBlock, root);

		// Set i-node 0 to reference root
		int rootINodePos = 1;   // Index of the root directory i-node 
		// Copy the first i-node block into the Virtual Disk Block
		VirtualDiskBlock firstBlockRef = copyBlockToVDB(d, blockSize, rootINodePos);  
		// Write in the i-node 0 the reference to the root directory in the data blocks
		// and set i-node type to directory
		DiskUtils.copyIntToBlock(firstBlockRef, 0, rootDataBlock);
		firstBlockRef.setElement(I_NODE_SIZE-1, (byte) 1);
		// Write into the disk the virtual disk block with updated reference to the root directory data block
		d.write(rootINodePos, firstBlockRef);
	}
	
	
	
	

}
