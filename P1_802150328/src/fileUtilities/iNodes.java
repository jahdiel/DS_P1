package fileUtilities;

public class iNodes {
	
	
	
	// Indicates if the i-node corresponds to a data file or to a directory. 
	// If 0, then it corresponds to a data file.
	private boolean isDir;
	// Number of bytes the file has
	private int bSize; 
	// Index of the physical block in the disk 
	// which corresponds to the first logical block (the first block) of the file
	private int firstBlockIndex;
	
	/*
	 * Constructor for the File I-Nodes class.
	 */
	public iNodes(boolean isDir, int bSize, int firstBlockIndex) {
		this.isDir = isDir;
		this.bSize =bSize;
		this.firstBlockIndex = firstBlockIndex;
		
	}

	public boolean isDir() {
		return isDir;
	}

	public int getbSize() {
		return bSize;
	}

	public int getFirstBlockIndex() {
		return firstBlockIndex;
	}
	
	
}
