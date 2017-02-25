package diskUnitExceptions;

public class ExistingDiskException extends RuntimeException {
	
	public ExistingDiskException() {}
	
	public ExistingDiskException(String arg0) {
		super(arg0);
	}

	public ExistingDiskException(Throwable arg0) {
		super(arg0);
	}

	public ExistingDiskException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
