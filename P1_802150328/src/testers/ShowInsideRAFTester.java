package testers;
import java.io.IOException;

import diskUtilities.*;

public class ShowInsideRAFTester {

	/**
	 * @param args systems argument
	 */
	public static void main(String[] args) {
		
		DiskUnit d = DiskUnit.mount("first"); // edit the name of the disk to mount
		
		System.out.println("The capacity is: "+d.getCapacity());
		System.out.println("The blocksize is: "+d.getBlockSize());
		System.out.println();
		
		ShowBlockZero(d);
		System.out.println();
		
		ShowINodeBlocks(d);
		System.out.println();
		
		ShowDataBlocks(d);
		
		d.shutdown(); 
	}
	
	public static void ShowBlockZero(DiskUnit d) {
		int numOfInt = d.getBlockSize() / 4;
		try {
			d.disk.seek(0);
			System.out.print("Block 0: ");
			for (int i=0; i < numOfInt; i++) {
				int num = d.disk.readInt();
				System.out.print(num+" ");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ShowINodeBlocks(DiskUnit d) {
		int count = 1;
		int blockSize = d.getBlockSize();
		int bytePos = blockSize;
		int numOfBlocks = (int) Math.ceil((d.getiNodeNum() * 9.0) / d.getBlockSize()) + 1;
		int nodePerBlock = blockSize / 9;

		try {
			d.disk.seek(bytePos);
			for (int i=1; i < numOfBlocks; i++) {
				System.out.println();
				System.out.print("Block "+count+": ");
				count++;	
				for (int j=0; j < nodePerBlock; j++) {
					int num = d.disk.readInt();
					int size = d.disk.readInt();
					boolean type = d.disk.readBoolean();
					System.out.print(num+" "+size+" "+type+" ");	
				}
				bytePos += blockSize;
				d.disk.seek(bytePos);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ShowDataBlocks(DiskUnit d) {	
		int numOfBlocks = (int) Math.ceil((d.getiNodeNum() * 9.0) / d.getBlockSize()) + 1;
		int blockSize = d.getBlockSize();
		int bytePos = blockSize * numOfBlocks;
		int count = numOfBlocks;
		int capacity = d.getCapacity();
		int numOfInt = blockSize / 4;
		try {
			d.disk.seek(bytePos);
			for (int i=1; i < capacity-numOfBlocks+1; i++) {
				System.out.println();
				System.out.print("Block "+count+": ");
				count++;	
				for (int j=0; j < numOfInt; j++) {
					int num = d.disk.readInt();
					System.out.print(num+" ");
				}
				bytePos += blockSize;
				d.disk.seek(bytePos);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}

