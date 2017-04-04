package diskUtilities;

import java.io.File;
import java.util.ArrayList;

public class FileManager {

	/**
	 * Attempts to read a new file into the current directory in the current 
	 * working disk unit. The first operand is the name of the file to read. 
	 * Such file must exist in the same directory where the program is being executed. 
	 * If no such file, then the command ends with an appropriate message. 
	 * The second operand is the name of the new file; how the system will record it 
	 * in the current directory. If such name already exists in the current directory, 
	 * and if it corresponds to an existing data file, then the current content of such 
	 * file is erased and replaced by the content of the file being read. If the given 
	 * name (second operand) is new, then a new file with that name is created and its 
	 * content will be a copy of the actual content of the file being read. If the disk 
	 * unit does not have enough space for the new file, the command also ends with a message.
	 * @param extFile Name of the file to read
	 * @param file Name of the new file
	 */
	public static void loadFile(String extFile, String file) {
		
		File fileToRead = new File(extFile); // file to read from
		if (!fileToRead.exists()) {
			System.out.println(extFile+": No such file.");
		}	
		File newFile = new File(file);  // file to copy into inside the disk unit
		
		// Format file string to fit 20 bytes
		file = DiskUtils.formatFileName(file);
		
		// Place file content inside an ArrayList of VirtualDiskBlock
		DiskUnit disk = DiskManager.mountedDiskUnit;
		int blockSize = disk.getBlockSize();
		ArrayList<VirtualDiskBlock> extFileArrayList = DiskUtils.setFileContentToVDBs(fileToRead, blockSize);
		
		// Block Number of the root directory
		int rootBlockNum = INodeManager.getDataBlockFromINode(disk, 0);
		
		// Verify if File already exists.
		ArrayList<Integer> foundFile = findFileInDir(disk, file, rootBlockNum);
		 
		if (foundFile != null) { // If found the file erases the foundFile and creates it with new content.
			// TODO: If found the file erase the foundFile and create it with new content.
			VirtualDiskBlock foundFileBlock = DiskUtils.copyBlockToVDB(disk, foundFile.get(0));
			int fileBytePos = foundFile.get(1);
			// Get iNode reference to that file 
			int iNodeRef = DiskUtils.getIntFromBlock(foundFileBlock, fileBytePos+20); // Reads the integer right after the filename, which is the iNode ref
			int fileDataBlock = INodeManager.getDataBlockFromINode(disk, iNodeRef);  // Data block from the i-node
			
			// Delete file from disk
			deleteFileFromDisk(disk, fileDataBlock);
			// Write new file in place of the older one
			writeNewFileIntoDisk(disk, fileDataBlock, extFileArrayList);
			
		}
		else { // Create the new file.
			// Write new file into root directory
			int INodeRef = writeNewFileIntoDirectory(disk, file, rootBlockNum); // Returns the iNode reference.
			// Enter the iNode and assign a free block
			// Get free block number
			int freeBN = FreeBlockManager.getFreeBN(disk);
			// Set data block in i-node to the free block 
			INodeManager.setDataBlockToINode(disk, INodeRef, freeBN);
			// Write file into the free block
			writeNewFileIntoDisk(disk, freeBN, extFileArrayList);
		}
		
	}
	/**
	 * Provides ArrayList with block number in first index and free byte position in second index.
	 * @param d DiskUnit in use
	 * @param blockNum Number of data block
	 * @param blockSize Bytes per block
	 * @return ArrayList with block number in first index and free byte position in second index.
	 */
	public static ArrayList<Integer> getFreePosInDirectory(DiskUnit d, int blockNum, int blockSize) {
		
		int usableBytes = blockSize - 4;
		int filesPerBlock = usableBytes / 24;
		
		VirtualDiskBlock vdb = DiskUtils.copyBlockToVDB(d, blockNum);
		
		int nextBlockInt = DiskUtils.getIntFromBlock(vdb, usableBytes); // Read the last 4 bytes in the block
		
		if ( nextBlockInt == 0) {
			for (int i=1; i <= filesPerBlock; i++) {
				int iNodeIdx = DiskUtils.getIntFromBlock(vdb, (i*24)-4); // i-node index inside the directory, after the filename
				if (iNodeIdx == 0) {  // byte position = blockNum*blockSize+((i*24)-24)
					ArrayList<Integer> freeDirArray = new ArrayList<>();
					int bytePos = (i*24)-24;
					freeDirArray.add(blockNum);
					freeDirArray.add(bytePos);
					return freeDirArray;
				}
			}
		}
		return getFreePosInDirectory(d, nextBlockInt, blockSize);
	}
	
	/**
	 * Find if file is inside the directory. Returns the block number 
	 * and byte position in the block if it is found. If not found returns null. 
	 * @param d DiskUnit to be used
	 * @param file Name of file being searched.
	 * @param dirBlockNum Block number of the directory.
	 * @return Returns ArrayList<Integer> with blockNumber in first index and
	 * byte position in block in the second index. If file not found returns null.
	 */
	public static ArrayList<Integer> findFileInDir(DiskUnit d, String file, int dirBlockNum) {

		ArrayList<Integer> dirBlockNums = allFileBlockNums(d, dirBlockNum);
		for (Integer blockNum : dirBlockNums) {
			VirtualDiskBlock vdb = DiskUtils.copyBlockToVDB(d, blockNum);
			Integer bytePos = findFileInBlock(vdb, file); // Find file in the block.
			if (bytePos != null) {
				ArrayList<Integer> foundFileInfo = new ArrayList<>();
				foundFileInfo.add(blockNum);   // BlockNum of where the file resides.
				foundFileInfo.add(bytePos);    // Byte position in the block specified in the line before. 
				return foundFileInfo;
			}
		}
		
		return null;
	}
	/**
	 * Reads the file names inside a block and returns the byte position where
	 * the file name begins in reference to the block.
	 * @param vdb
	 * @return Integer with the byte position of the filename. 
	 */
	public static Integer findFileInBlock(VirtualDiskBlock vdb, String file) {
		
		int usableBytes = vdb.getCapacity() - 4;
		int filesPerBlock = usableBytes / 24;
		
		for (int i=1; i <= filesPerBlock; i++) {
			
			int iNodeIdx = DiskUtils.getIntFromBlock(vdb, (i*24)-4); // i-node index inside the directory, after the filename
			if (iNodeIdx == 0)   // byte position = blockNum*blockSize+((i*24)-24)
				break;
			
			char[] fileCharArray = new char[20];
			int fileBytePos = (i*24) - 24;
			for (int j=0; j<20; j++) {
				fileCharArray[j] = DiskUtils.getCharFromBlock(vdb, (fileBytePos+j));
			}
			String filename = new String(fileCharArray);
			if (filename.equals(file)) {
				return fileBytePos;
			}	
		}
		return null;
	}
	
	/**
	 * Returns an ArrayList with all the block numbers of a file or directory.
	 * @param d DiskUnit to be used.
	 * @param firstFileBlockNum First block number of the file
	 * @return
	 */
	private static ArrayList<Integer> allFileBlockNums(DiskUnit d, int firstFileBlockNum) {
		
		int blockSize = d.getBlockSize();
		int lastInt = blockSize - 4;
		ArrayList<Integer> dirBlockNums = new ArrayList<>();
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(firstFileBlockNum, vdb);
		int nextBlockInt = DiskUtils.getIntFromBlock(vdb, lastInt); // Read the last 4 bytes in the block
		dirBlockNums.add(firstFileBlockNum);
		
		while (nextBlockInt != 0) {
			dirBlockNums.add(nextBlockInt);
			vdb = new VirtualDiskBlock(blockSize);
			d.read(nextBlockInt, vdb);
			nextBlockInt = DiskUtils.getIntFromBlock(vdb, lastInt);
		}
		return dirBlockNums;
	}
	
	/**
	 * Write the file name and i-node reference into a directory.
	 * Writes file name right after the last file name in directory.
	 * @param d DiskUnit in use.
	 * @param file New file to write into directory.
	 * @param blockNum Number of the block to write in.
	 * @param blockSize Bytes per block.
	 * @throws IllegalArgumentException Thrown if filename length not 20 characters long.
	 * @return Returns reference to the i-node of the new file.
	 */
	public static int  writeNewFileIntoDirectory(DiskUnit d, String file, int blockNum) 
			throws IllegalArgumentException {
		
		int blockSize = d.getBlockSize();
		
		// New file string into char array.
		char[] fileCharArray = file.toCharArray();
		if (fileCharArray.length != 20) {
			throw new IllegalArgumentException("File name is not 20 characters.");
		}
			
		// Get free byte position in directory
		ArrayList<Integer> newFileInRoot = getFreePosInDirectory(d, blockNum, blockSize);
		
		// TODO: Verify file fits into new block;
		
		// Write file name inside root and assign a free i-node to reference it.
		// Obtain free i-node index
		int iNodeRef = INodeManager.getFreeINode(d);
		// Write file name and node index into root;
		int newFileBlockNum = newFileInRoot.get(0);
		int newFileBytePos = newFileInRoot.get(1);
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(newFileBlockNum, vdb);
		
		// Write file name inside the block
		for (int i=0; i<fileCharArray.length; i++) {
			DiskUtils.copyCharToBlock(vdb, newFileBytePos+i, fileCharArray[i]);
		}
		// Copy i-node reference into the directory.
		DiskUtils.copyIntToBlock(vdb, newFileBytePos+20, iNodeRef);
		// Write the Virtual block back into the disk unit.
		d.write(newFileBlockNum, vdb);
		
		return iNodeRef; // Returns reference to the i-node of the new file.
	}
	
	/**
	 * Writes the new file into the disk unit.
	 * @param d
	 * @param firstFreeBlock
	 * @param vdbArray
	 */
	private static void writeNewFileIntoDisk(DiskUnit d, int firstFreeBlock, ArrayList<VirtualDiskBlock> vdbArray) {
		
		VirtualDiskBlock vdb = vdbArray.get(0);  // Get the VirtualDiskBlock from the ArrayList
		int nextFreeBlock = FreeBlockManager.getFreeBN(d); // Look for a free block
		DiskUtils.copyIntToBlock(vdb, vdb.getCapacity()-4, nextFreeBlock);  // Write free block number into last 4-bytes of block
		d.write(firstFreeBlock, vdb);   // Write virtual disk block into disk 
		
		for (int i=1; i<vdbArray.size(); i++) {
			int freeBlock = nextFreeBlock;  // Save the next block, in order to write in that block
			vdb = vdbArray.get(i);
			nextFreeBlock = FreeBlockManager.getFreeBN(d);  // Look for a free block
			DiskUtils.copyIntToBlock(vdb, vdb.getCapacity()-4, nextFreeBlock);  // Write free block number into last 4-bytes of block
			d.write(freeBlock, vdb);  // Write virtual disk block into disk 
		}
		
	}
	/**
	 * Deletes a file from the disk by wiping its data blocks.
	 * @param d DiskUnit in use
	 * @param firstFreeBlock Number of the first data block in the file.
	 */
	private static void deleteFileFromDisk(DiskUnit d, int firstFreeBlock) {
		
		ArrayList<Integer> fileBlockNums = allFileBlockNums(d, firstFreeBlock);
		
		for (Integer blockNum : fileBlockNums) {
			VirtualDiskBlock vdb = DiskUtils.copyBlockToVDB(d, blockNum);
			clearDiskBlock(d, blockNum, vdb);         // Clear the block
			FreeBlockManager.registerFB(d, blockNum); // register free block to the free block collection. 
		}
	}
	/**
	 * Clears a block by setting all its bytes to zero.
	 * @param d
	 * @param blockNum
	 * @param vdb
	 */
	private static void clearDiskBlock(DiskUnit d, int blockNum, VirtualDiskBlock vdb) {
		
		for (int i=0; i<vdb.getCapacity(); i++) {
			vdb.setElement(i, (byte) 0); // set every byte to 0 in the block
		}
		d.write(blockNum, vdb); // Write the wiped block into the disk.
		
	}
		
	
	
	
	
	
	
	
}

