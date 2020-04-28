package verification.parser;

public class Commands {
	boolean checkHeaderValidity;
	boolean checkHeaderStackBound;
	boolean checkForwardOrDrop;
	boolean checkReadOnlyMetadata;
	public Commands() {
		checkHeaderValidity = false;
		checkHeaderStackBound = false;
		checkForwardOrDrop = false;
		checkReadOnlyMetadata = false;
	}
	
	// Header Validity
	boolean ifCheckHeaderValidity() {
		return checkHeaderValidity;
	}
	void setCheckHeaderValidity() {
		checkHeaderValidity = true;
	}
	
	// Header Stack out of Bound
	boolean ifCheckHeaderStackBound() {
		return checkHeaderStackBound;
	}
	void setCheckHeaderStackBound() {
		checkHeaderStackBound = true;
	}
	
	/* The Switch must forward or drop a packet.
	   Undefined forward action is not allowed. */
	boolean ifCheckForwardOrDrop() {
		return checkForwardOrDrop;
	}
	void setCheckForwardOrDrop() {
		checkForwardOrDrop = true;
	}
	
	// Some metadata is read-only in egress control
	boolean ifCheckReadOnlyMetadata() {
		return checkReadOnlyMetadata;
	}
	void setCheckReadOnlyMetadata() {
		checkReadOnlyMetadata = true;
	}
}
