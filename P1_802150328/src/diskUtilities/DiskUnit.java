package diskUtilities;
import diskUnitExceptions.*;

import java.io.*;



public class DiskUnit implements DiskUnitInterface{

	private static final int DEFAULT_CAPACITY = 1024;  // default number of blocks 	
	private static final int DEFAULT_BLOCK_SIZE = 256; // default number of bytes per block
	private static final int I_NODE_SIZE = 9;          // default number of bytes per i-node
	
	private int capacity;     	// number of blocks of current disk instance
	private int blockSize; 	    // size of each block of current disk instance
	private int firstDataBlock; // index of the first data block (the root)
	private int nextFreeBlock;  // index representing top 4 bytes position in block firstFLB
	private int firstFreeINode; // index of first free i-node
	private int iNodeNum;       // total number of i-nodes in the disk (free + taken)
	
	
	// the file representing the simulated  disk, where all the disk blocks
	// are stored
	private RandomAccessFile disk;

	// the constructor -- PRIVATE
	/**
	    @param name is the name of the disk
	 **/
	private DiskUnit(String name) {
		try {
			disk = new RandomAccessFile(name, "rw");
		}
		catch (IOException e) {
			System.err.println("Unable to start the disk");
			System.exit(1);
		}
	}

	/**
	 * Turns on an existing disk unit whose name is given. If successful, it makes
	 * the particular disk unit available for operations suitable for a disk unit.
	 * @param name the name of the disk unit to activate
	 * @return the corresponding DiskUnit object
	 * @throws NonExistingDiskException whenever no
	 *    "disk" with the specified name is found.
	*/
	public static DiskUnit mount(String name) throws NonExistingDiskException {
		File file=new File(name);
		   if (!file.exists())
		       throw new NonExistingDiskException("No disk has name : " + name);
		  
		   DiskUnit dUnit = new DiskUnit(name);
		   	
		   // get the capacity and the block size of the disk from the file
		   // representing the disk
		   try {
			   dUnit.disk.seek(0);
			   dUnit.capacity = dUnit.disk.readInt();
			   dUnit.blockSize = dUnit.disk.readInt();
			   dUnit.firstDataBlock = dUnit.disk.readInt();
			   dUnit.nextFreeBlock = dUnit.disk.readInt();
			   dUnit.firstFreeINode = dUnit.disk.readInt();
			   dUnit.iNodeNum = dUnit.disk.readInt();
			   
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
		   	
		   return dUnit;     	
	}
	/**
	 * Creates a new disk unit with the given name. The disk is formatted
	 * as having default capacity (number of blocks), each of default
	 * size (number of bytes). Those values are: DEFAULT_CAPACITY and
	 * DEFAULT_BLOCK_SIZE. The created disk is left as in off mode.
	 * @param name the name of the file that is to represent the disk.
	 * @throws ExistingDiskException whenever the name attempted is
	 * already in use.
	*/
	public static void createDiskUnit(String name) throws ExistingDiskException
	{
		createDiskUnit(name, DEFAULT_CAPACITY, DEFAULT_BLOCK_SIZE);
	}
	   
	/**
	 * Creates a new disk unit with the given name. The disk is formatted
	 * as with the specified capacity (number of blocks), each of specified
	 * size (number of bytes).  The created disk is left as in off mode.
	 * @param name the name of the file that is to represent the disk.
	 * @param capacity number of blocks in the new disk
	 * @param blockSize size per block in the new disk
	 * @throws ExistingDiskException whenever the name attempted is
	 * already in use.
	 * @throws InvalidParameterException whenever the values for capacity
	 *  or blockSize are not valid according to the specifications
	*/
	public static void createDiskUnit(String name, int capacity, int blockSize) 
			throws ExistingDiskException, InvalidParameterException {
		File file=new File(name);
		if (file.exists())
			throw new ExistingDiskException("Disk name is already used: " + name);

		RandomAccessFile disk = null;
		if (capacity < 0 || blockSize < 32 ||
				!isPowerOfTwo(capacity) || !isPowerOfTwo(blockSize))
			throw new InvalidParameterException("Invalid values: " +
					" capacity = " + capacity + " block size = " +
					blockSize);
		// disk parameters are valid... hence create the file to represent the
		// disk unit.
		try {
			disk = new RandomAccessFile(name, "rw");
		}
		catch (IOException e) {
			System.err.println ("Unable to start the disk");
			System.exit(1);
		}

		reserveDiskSpace(disk, capacity, blockSize);

		// after creation, just leave it in shutdown mode - just
		// close the corresponding file
		try {
			disk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * Reserve Disk Space
	 * @param disk
	 * @param capacity
	 * @param blockSize
	 */
	private static void reserveDiskSpace(RandomAccessFile disk, int capacity, int blockSize)
	{
		try {
			disk.setLength(blockSize * capacity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// write disk parameters (number of blocks, bytes per block) in
		// block 0 of disk space
		try {
			disk.seek(0);
			disk.writeInt(capacity);  // Writes into disk the capacity
			disk.writeInt(blockSize); // Writes into disk the blockSize
			
			int iNodeNum = (int) (blockSize * capacity * 0.01) / 9;      // number of i-nodes in disk instance
			int numOfINodeBlocks =  (int) Math.max(1, Math.ceil(I_NODE_SIZE * ( (double)iNodeNum / blockSize)));  // index of the block reserved for i-nodes
			int nextFreeBlock = 0;             //TODO: Finish this implementation
			int firstFreeINode = blockSize + 9;    // index of first free i-node,adds 9 because root takes the first i-node.
			
			disk.writeInt(numOfINodeBlocks + 1); // Writes into disk the index of the first data block (the root)
			disk.writeInt(nextFreeBlock);        // Writes into disk the index representing top 4 bytes position in block firstFLB
			disk.writeInt(firstFreeINode);       // Writes into disk the byte index of first free i-node
			disk.writeInt(iNodeNum);             // Writes into disk the total number of i-nodes in the disk (free + taken)
			
			reserveINodesSpace(disk, capacity, blockSize, iNodeNum, numOfINodeBlocks);
		
			
		} catch (IOException e) {
			e.printStackTrace();
		} 	
	}
	 /**
	  * Reserves space for the i-nodes in the disk.
	  * @param disk
	  *	@param capacity
	  * @param blockSize
	  */
	private static void reserveINodesSpace(RandomAccessFile disk, int capacity, int blockSize, int numOfINodes, int numOfINodeBlocks) {
		
		int nodesPerBlock = blockSize / 9;
		int firstINode = blockSize;
		int iNodeCounter = 0;              // Counts the amount of i-nodes created   
		try {
			for (int i=firstINode; i <= (blockSize * numOfINodeBlocks); i+=blockSize) { // Iterates through the disk blocks with i-nodes.
				disk.seek(i);
				
				for (int j=1; j <= nodesPerBlock; j++) {  // Creates the amount of i-nodes that fit inside a block.
					iNodeCounter++;
					if (iNodeCounter == numOfINodes) {			
						disk.writeInt(0);			  // Number of first file block or 0 because there aren't more i-nodes. (First block)
						disk.writeInt(0);             // Number of bytes the file has. (Size)
						disk.writeBoolean(false);	  // Indicates if i-node corresponds to a file or directory. Is 0 if it is a data file. (Type)
						return;
					} 
					
					if (j == nodesPerBlock)  		  // If i-node is last in block, point to the next i-node block 
						disk.writeInt(i+blockSize);   // Number of first file block or next free i-node (first block)
					else
						disk.writeInt(i + (j * 9));   // i + (j * 9) points to the byte index of the next i-node in the same block.
					disk.writeInt(0);                 // Number of bytes the file has. (Size)
					disk.writeBoolean(false);	      // Indicates if i-node corresponds to a file or directory. Is 0 if it is a data file. (Type)
					
				}	
			}	
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	private static boolean isPowerOfTwo(int n) {
	    return n>0 && (n&n-1) ==0;
	}

	
	@Override
	public void write(int blockNum, VirtualDiskBlock b) throws InvalidBlockNumberException, InvalidBlockException {
		
		try {
			if (blockNum < 1 || blockNum >= capacity)
				throw new InvalidBlockNumberException("The block number "+blockNum+" is invalid.");
			if (b == null || b.getCapacity() != blockSize)
				throw new InvalidBlockException("Invalid block instance.");
			
			int bytePos = blockNum * blockSize;
			disk.seek(bytePos);
			for (int i=0; i < b.getCapacity(); i++)
				disk.write(b.getElement(i));
			
		} catch (InvalidBlockNumberException e) {
			e.printStackTrace();
		} catch (InvalidBlockException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		
	}

	@Override
	public void read(int blockNum, VirtualDiskBlock b) throws InvalidBlockNumberException, InvalidBlockException {
		
		try {
			if (blockNum < 0 || blockNum >= capacity)
				throw new InvalidBlockNumberException("The block number "+blockNum+" is invalid.");
			if (b == null || b.getCapacity() != blockSize)
				throw new InvalidBlockException("Invalid block instance.");
			
			int bytePos = blockNum * blockSize;
			disk.seek(bytePos);
			for (int i=0; i < b.getCapacity(); i++)
				b.setElement(i, disk.readByte());
			
		} catch (InvalidBlockNumberException e) {
			e.printStackTrace();
		} catch (InvalidBlockException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
	}

	@Override
	public int getCapacity() {
		try {
			disk.seek(0);
			return disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;

	}

	@Override
	public int getBlockSize() {
		try {
			disk.seek(4);
			return disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getFirstDataBlock() {
		try {
			disk.seek(8);
			return disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setFirstDataBlock(int firstDataBlock) {
		try {
			disk.seek(8);
			disk.writeInt(firstDataBlock);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNextFreeBlock() {
		try {
			disk.seek(12);
			return disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setNextFreeBlock(int nextFreeBlock) {
		try {
			disk.seek(12);
			disk.writeInt(nextFreeBlock);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getFirstFreeINode() {
		try {
			disk.seek(16);
			return disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setFirstFreeINode(int firstFreeINode) {
		try {
			disk.seek(16);
			disk.writeInt(firstFreeINode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getiNodeNum() {
		try {
			disk.seek(20);
			return disk.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setiNodeNum(int iNodeNum) {
		try {
			disk.seek(20);
			disk.writeInt(iNodeNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lowLevelFormat() {
		
		VirtualDiskBlock vdb = new VirtualDiskBlock(blockSize);
		for (int i=0; i<vdb.getCapacity(); i++) {
			vdb.setElement(i, (byte) 0);
		}
		for (int bn=1; bn < capacity; bn++) {
			write(bn, vdb);
		}
		
	}

	@Override
	/** Simulates shutting-off the disk. Just closes the corresponding RAF. **/
	public void shutdown() {
		try {
			disk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
