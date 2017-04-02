package diskUtilities;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class for managing the free disk blocks in the unit.
 * Works as a tree-like structure. 
 * @author jahdiel
 *
 */
public class FreeBlockManager {
	
	private final int INTEGERS_IN_BLOCK; // Amount of integers that fit inside a block, each of 4 bytes
	
	private DiskUnit disk;  // Disk in which the free blocks are managed.
	private int firstFLB;   // Root of the collection of free blocks.
	private int flIndex;    // Index of free blocks available.
	 
	
	/**
	 * Constructor - Sets the disk unit to work with.
	 * @param d DiskUnit to manage.
	 */
	public FreeBlockManager(DiskUnit d) {
		disk = d;
		firstFLB = d.getFirstDataBlock();
		flIndex = d.getNextFreeBlock();
		INTEGERS_IN_BLOCK = d.getBlockSize() / 4;
	}
	
	/**
	 * Obtains the next free block in the DiskUnit.
	 * @return Returns Block Number of free Block
	 */
	public int getFreeBN() {
		return 0;
	}
	
	/**
	 * Inserts a freed block into the free block structure.
	 * @param d DiskUnit
	 * @param bn Index of the freed block.
	 */
	public static void registerFB(DiskUnit d, int bn) {
		int INTEGERS_IN_BLOCK = d.getBlockSize() / 4;
		int firstFLB = d.getFirstDataBlock();
		int flIndex = d.getNextFreeBlock();
		
		if (firstFLB == 0) {
			firstFLB = bn;
			d.setFirstDataBlock(bn);
			setIntInsideBlock(d, firstFLB, 0, 0);
			d.setNextFreeBlock(0);   // flIndex = 0
		} else if (flIndex == INTEGERS_IN_BLOCK-1) { // If flIndex is the last integer in the block
			setIntInsideBlock(d, bn, 0, firstFLB);
			d.setNextFreeBlock(0);
			d.setFirstDataBlock(bn);
		} else {
			flIndex++;
			d.setNextFreeBlock(flIndex);
			setIntInsideBlock(d, firstFLB, flIndex, bn);
		}
	}
	
	/**
	 * Sets an integer in the provided index inside a free data block.
	 * Equivalent to block[index] = value;
	 * @param blockNum
	 * @param index
	 * @param value
	 */
	public static void setIntInsideBlock(DiskUnit d, int blockNum, int index, int value) {
		VirtualDiskBlock vdb = DiskManager.copyBlockToVDB(d, d.getBlockSize(), blockNum);
		DiskUtils.copyIntToBlock(vdb, 4*index, value); // Copies integer into the different possible indexes inside VDB
		d.write(blockNum, vdb);
	}
	
	/**
	 * 
	 * @param disk
	 */
	public static void initializeFreeBlocks(DiskUnit disk) {
		int firstFreeBlock = disk.getFirstDataBlock();
		for (int i=firstFreeBlock+1; i < disk.getCapacity(); i++) {
			registerFB(disk, i);
		}
	}
	
	
}
