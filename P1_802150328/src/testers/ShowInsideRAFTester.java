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
		try {
			d.disk.seek(0);
			System.out.print("Block 0: ");
			for (int i=0; i < 8; i++) {
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
		int bytePos = 32;
		int numOfBlocks = d.getFirstDataBlock();

		try {
			d.disk.seek(bytePos);
			for (int i=1; i < numOfBlocks; i++) {
				System.out.println();
				System.out.print("Block "+count+": ");
				count++;	
				for (int j=0; j < 3; j++) {
					int num = d.disk.readInt();
					int size = d.disk.readInt();
					boolean type = d.disk.readBoolean();
					System.out.print(num+" "+size+" "+type+" ");	
				}
				bytePos += 32;
				d.disk.seek(bytePos);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ShowDataBlocks(DiskUnit d) {	
		int numOfBlocks = d.getFirstDataBlock();
		int bytePos = 32 * numOfBlocks;
		int count = numOfBlocks;
		int capacity = d.getCapacity();
		try {
			d.disk.seek(bytePos);
			for (int i=1; i < capacity-numOfBlocks+1; i++) {
				System.out.println();
				System.out.print("Block "+count+": ");
				count++;	
				for (int j=0; j < 8; j++) {
					int num = d.disk.readInt();
					System.out.print(num+" ");
				}
				bytePos += 32;
				d.disk.seek(bytePos);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




}

