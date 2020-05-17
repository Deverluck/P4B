package verification.parser;

public class Commands {
	boolean addControlPlaneConstrain;
	boolean checkHeaderValidity;
	boolean checkHeaderStackBound;
	boolean checkForwardOrDrop;
	boolean checkReadOnlyMetadata;
	
	// check assignment statements
	boolean checkAssignmentCondition; 
	
	public Commands() {
		addControlPlaneConstrain = false;
		checkHeaderValidity = false;
		checkHeaderStackBound = false;
		checkForwardOrDrop = false;
		checkReadOnlyMetadata = false;
		checkAssignmentCondition = false;
	}
	
	boolean ifConstrainControlPlane() {
		return addControlPlaneConstrain;
	}
	
	void setControlPlaneConstrain() {
		addControlPlaneConstrain = true;
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
	
	boolean ifCheckAssignmentCondition() {
		return checkAssignmentCondition;
	}
	void setCheckAssignmentCondition() {
		checkAssignmentCondition = false;
	}
}
