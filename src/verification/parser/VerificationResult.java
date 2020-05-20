package verification.parser;

public class VerificationResult {
	Counter headerValidityCounter;
	Counter headerStackOutOfBound;
	Counter arithmeticOverflow;
	Counter modifyReadOnlyField;
	public VerificationResult() {
		headerValidityCounter = new Counter();
		headerStackOutOfBound = new Counter();
		arithmeticOverflow = new Counter();
		modifyReadOnlyField = new Counter();
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