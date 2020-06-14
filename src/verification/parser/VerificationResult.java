package verification.parser;

public class VerificationResult {
	Counter headerValidityCounter;
	Counter headerStackOutOfBound;
	Counter arithmeticOverflow;
	Counter modifyReadOnlyField;
	
	Counter headerValidityAssertionTotal;
	
	public VerificationResult() {
		headerValidityCounter = new Counter();
		headerStackOutOfBound = new Counter();
		arithmeticOverflow = new Counter();
		modifyReadOnlyField = new Counter();
		
		headerValidityAssertionTotal = new Counter();
	}
	String helper(int cnt) {
		if(cnt <= 1)
			return cnt+" bug";
		else
			return cnt+" bugs";
	}
	void show() {
		System.out.println("Header Stack Out Of Bound: "+helper(headerStackOutOfBound.cnt));
		System.out.println("Modify ReadOnly Field: "+helper(modifyReadOnlyField.cnt));
		System.out.println("Header Validity Assertion Total Number: "+headerValidityAssertionTotal.cnt);
	}
}

class Counter{
	int cnt;
	public Counter(){
		cnt = 0;
	}
	void inc() {
		cnt++;
	}
	void dec() {
		if(cnt>0)
			cnt--;
	}
	int value() {
		return cnt;
	}
}