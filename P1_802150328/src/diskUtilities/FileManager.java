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
		
		// Place file content inside an ArrayList of VirtualDiskBlock
		DiskUnit disk = DiskManager.mountedDiskUnit;
		int blockSize = disk.getBlockSize();
		ArrayList<VirtualDiskBlock> vbdArray = DiskUtils.setFileContentToVDBs(fileToRead, blockSize);
		
		// Block Number of the root directory
		int rootBlockNum = INodeManager.getDataBlockFromINode(disk, 0, blockSize);
		
		// Verify if File already exists.
		// TODO: Implemenent verifying if file already exists.
		ArrayList<Integer> foundFile = findFileInDir(disk, file, rootBlockNum);
		
		if (foundFile != null) {
			// TODO: Erase the foundFile and create it with new content.
		}
		// Write new file into the directory. 
		else {
			// Write new file into root directory
			writeNewFileIntoDirectory(disk, file, rootBlockNum);
			// TODO: enter the iNode and assign a free block
			// TODO: Write file into the free block\
			
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
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(blockNum, vdb);
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
			VirtualDiskBlock vdb = new VirtualDiskBlock(d.getBlockSize());
			d.read(blockNum, vdb);
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
	 * Write the file name and i-node reference into a directory.
	 * Writes file name right after the last file name in directory.
	 * @param d
	 * @param file
	 * @param blockNum
	 * @param blockSize
	 * @throws IllegalArgumentException
	 */
	public static void writeNewFileIntoDirectory(DiskUnit d, String file, int blockNum) 
			throws IllegalArgumentException {
		
		int blockSize = d.getBlockSize();
		// New file string into char array.
		// TODO: Format string to be of size 20 bytes
		char[] fileCharArray = file.toCharArray();
		if (fileCharArray.length > 20) {
			throw new IllegalArgumentException("File name is greater than 20 characters.");
		}
			
		// Get free byte position in directory
		ArrayList<Integer> newFileInRoot = getFreePosInDirectory(d, blockNum, blockSize);
		// Write file name inside root and assign a free i-node to reference it.
		// Obtain free i-node index
		int iNodeRef = INodeManager.getFreeINode(d);
		// Write file name and node index into root;
		int newFileBlockNum = newFileInRoot.get(0);
		int newFileBytePos = newFileInRoot.get(1);
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(blockNum, vdb);
		
		// Write file name inside the block
		for (int i=0; i<fileCharArray.length; i++) {
			DiskUtils.copyCharToBlock(vdb, newFileBytePos, fileCharArray[i]);
		}
		// Copy i-node reference into the directory.
		DiskUtils.copyIntToBlock(vdb, newFileBytePos+20, iNodeRef);
		// Write the Virtual block back into the disk unit.
		d.write(newFileBlockNum, vdb);
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
		
	
	
	
	
	
	
	
}

