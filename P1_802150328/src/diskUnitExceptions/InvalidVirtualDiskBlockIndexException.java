package diskUnitExceptions;

public class InvalidVirtualDiskBlockIndexException extends RuntimeException {
	
	public InvalidVirtualDiskBlockIndexException() {}
		
		public InvalidVirtualDiskBlockIndexException(String arg0) {
			super(arg0);
		}
	
		public InvalidVirtualDiskBlockIndexException(Throwable arg0) {
			super(arg0);
		}
	
		public InvalidVirtualDiskBlockIndexException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}
}
