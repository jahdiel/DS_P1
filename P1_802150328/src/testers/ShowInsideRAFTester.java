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
		try {
			d.disk.seek(bytePos);
			
			for (int i=1; i < 256; i++) {
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




}

