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
		ArrayList<VirtualDiskBlock> vbdArray = DiskUtils.getFileContentToVDBs(fileToRead, blockSize);
		
		// Byte position of the next free space to write the filename inside the directory.
		int rootBlockNum = getDataBlockFromINode(disk, 0, blockSize);
		ArrayList<Integer> fileNameBytePos = getFreePosInDirectory(disk, rootBlockNum, blockSize);
		
		// Write file name inside root and assign a free i-node to reference it.
		
		
	}
	/**
	 * Provides the byte position of the next free space to write the file name and i-node index
	 * @param d DiskUnit in use
	 * @param blockNum Number of data block
	 * @param blockSize Bytes per block
	 * @return byte position of free space to write file name and i-node index
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
	 * Returns ArrayList with the block number of where the i-node is stored and
	 * byte position of where the iNode is in the block.
	 * @param iNodeIndex Index of the i-node
	 * @param blockSize Bytes per block
	 * @return ArrayList with the block number of where the i-node is stored and
	 * byte position of where the iNode is in the block 
	 */
	public static ArrayList<Integer> getINodePos(int iNodeIndex, int blockSize) {
		
		int nodesPerBlock = blockSize / 9;
		int blockNum = (1 + (iNodeIndex / nodesPerBlock)); // block number of where the i-node is stored
		int iNodeBytePos = ((iNodeIndex % nodesPerBlock) * 9); // byte position of where the iNode is in the block
		
		ArrayList<Integer> nodeArray = new ArrayList<>();
		nodeArray.add(blockNum);
		nodeArray.add(iNodeBytePos);
		
		return nodeArray;
	}
	/**
	 * Gets the first data block from an i-node using its index.
	 * @param d
	 * @param iNodeIndex
	 * @param blockSize
	 * @return
	 */
	public static int getDataBlockFromINode(DiskUnit d, int iNodeIndex, int blockSize) {
		
		ArrayList<Integer> iNodeInfo = getINodePos(iNodeIndex, blockSize);
		int iNodeBlockNum = iNodeInfo.get(0); // get blockNum of the iNode 
		int iNodeBytePos = iNodeInfo.get(1);  // get iNode byte position
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(iNodeBlockNum, vdb);
		
		return DiskUtils.getIntFromBlock(vdb, iNodeBytePos);
	}
	
	/**
	 * Find if file is inside the root directory.
	 * @param d
	 * @param file
	 * @param dirBlockNum
	 * @param blockSize
	 * @return
	 */
	public static ArrayList<Integer> doesFileExist(DiskUnit d, String file, int dirBlockNum, int blockSize) {
		// TODO: Finish this method.
		int usableBytes = blockSize - 4;
		int filesPerBlock = usableBytes / 24;	
		int lastBlockNum = (getFreePosInDirectory(d, dirBlockNum, blockSize)).get(0); // Last block in the directory
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		d.read(dirBlockNum, vdb);
		
		for (int i=1; i < filesPerBlock; i++) {
			
		}
		
		int nextBlockInt = DiskUtils.getIntFromBlock(vdb, usableBytes); // Read the last 4 bytes in the block
		
		if ( nextBlockInt == 0) {
			for (int i=1; i <= filesPerBlock; i++) {
				int iNodeIdx = DiskUtils.getIntFromBlock(vdb, (i*24)-4); // i-node index inside the directory, after the filename
				if (iNodeIdx == 0) {  // byte position = blockNum*blockSize+((i*24)-24)
					ArrayList<Integer> freeDirArray = new ArrayList<>();
					int bytePos = (i*24)-24;
					freeDirArray.add(dirBlockNum);
					freeDirArray.add(bytePos);
					return freeDirArray;
				}
			}
		}
		return doesFileExist(d, file, nextBlockInt, blockSize);
	}
	
	/**
	 * Reads a directory virtual disk block and verifies if file already exists.
	 * @param vdb
	 * @param file
	 * @return
	 */
	private static boolean isFileInBlock(VirtualDiskBlock vdb, String file) {
		
		int blockSize = vdb.getCapacity();
		int usableBytes = blockSize - 4;
		int filesPerBlock = usableBytes / 24;
		
		for (int i=0; i<(filesPerBlock*24); i+=24) {
			byte[] charArray = new byte[20];
			for (int j=0; j<20; j++) {
				charArray[j] = vdb.getElement(i+j);
			}
			if (file.equals(new String(charArray)))
				return true;
		}
		return false;
		
	}
	
	
	
	
	
	
	
	
}

