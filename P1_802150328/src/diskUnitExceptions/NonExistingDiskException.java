package diskUnitExceptions;

public class NonExistingDiskException extends RuntimeException {
	
	public NonExistingDiskException() {}
	
	public NonExistingDiskException(String arg0) {
		super(arg0);
	}

	public NonExistingDiskException(Throwable arg0) {
		super(arg0);
	}

	public NonExistingDiskException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
