package diskUtilities;

import java.util.ArrayList;

public class INodeManager {

	
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
	 * Gets the next free i-node.
	 * @return Index of the next free i-node
	 */
	public static int getFreeINode(DiskUnit d) {
		int freeINodeIdx = d.getFirstFreeINode();
		int nextFreeINodeIdx = getDataBlockFromINode(d, freeINodeIdx, d.getBlockSize());
		d.setFirstFreeINode(nextFreeINodeIdx);
		
		return freeINodeIdx;
	}
	
	
	
	
	
}
