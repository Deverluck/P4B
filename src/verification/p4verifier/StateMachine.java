package verification.p4verifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
 * A StateMachine for Parser States
 * Transition conditions are not considered. Because it doesn't influence verification result.
 * We need to know:
 *     1) which headers are extracted in a parse state
 *     2) transition relationships among states
 */
public class StateMachine {
	private static StateMachine instance;
	private ArrayList<State> states;
	private HashMap<String, State> map;
	private HashMap<String, Boolean> visit;
	private HashMap<Integer, String> mark;
	private int cnt;
	private HashMap<Integer, Integer> sdom;
	private HashMap<Integer, Integer> dfsPreq;
	
	private StateMachine() {
		states = new ArrayList<>();
		map = new HashMap<String, State>();
		visit = new HashMap<String, Boolean>();
		mark = new HashMap<>();
		cnt = 0;
		sdom = new HashMap<>();
		dfsPreq = new HashMap<>();
	}
	public static StateMachine getInstance() {
		if(instance == null) {
			instance = new StateMachine();
		}
		return instance;
	}
	public void addState(State state) {
		states.add(state);
		map.put(state.name, state);
		visit.put(state.name, false);
	}
	public ArrayList<State> getStates() {
		return states;
	}
	public void show() {
		for(State state:states) {
			System.out.println(state.name);
			for(String name:state.succ)
				System.out.println("    "+name);
		}
	}
	
	/*
	 * Test DFS
	 */
	void test() {
		states.clear();
		map.clear();
		cnt = 0;
		generateStates();
		updatePreq();
		dfsPreq.put(0, 0);
		dfs(map.get("R"));
		computeSdom();
	}
	void generateStates() {
		State a = new State("A");
		a.addSucc("D");
		addState(a);
		
		State b = new State("B");
		b.addSucc("A");
		b.addSucc("D");
		b.addSucc("E");
		addState(b);
		
		State c = new State("C");
		c.addSucc("F");
		c.addSucc("G");
		addState(c);
		
		State d = new State("D");
		d.addSucc("L");
		addState(d);
		
		State e = new State("E");
		e.addSucc("H");
		addState(e);
		
		State f = new State("F");
		f.addSucc("I");
		addState(f);
		
		State g = new State("G");
		g.addSucc("I");
		g.addSucc("J");
		addState(g);
		
		State h = new State("H");
		h.addSucc("E");
		h.addSucc("K");
		addState(h);
		
		State i = new State("I");
		i.addSucc("K");
		addState(i);
		
		State j = new State("J");
		j.addSucc("I");
		addState(j);
		
		State k = new State("K");
		k.addSucc("R");
		k.addSucc("I");
		addState(k);
		
		State l = new State("L");
		l.addSucc("H");
		addState(l);
		
		State r = new State("R");
		r.addSucc("A");
		r.addSucc("B");
		r.addSucc("C");
		addState(r);
	}
	void updatePreq() {
		for(State state:states) {
			for(String child:state.succ) {
				map.get(child).preq.add(state.name);
			}
		}
	}
	void dfs(State root) {
		if(!map.containsKey(root.name))
			return;
//		System.out.println(root.name);
		mark.put(cnt, root.name);
		root.mark = cnt;
		cnt++;
		visit.put(root.name, true);
		for(String child:root.succ) {
			if(visit.get(child)==false) {
				dfs(map.get(child));
				dfsPreq.put(map.get(child).mark, root.mark);
			}
		}
	}
	void computeSdom() {
		for(int i = cnt-1; i >= 0; i--) {
			int tmp = i;
			System.out.println(i);
			for(String preq : map.get(mark.get(i)).preq) {
				if(map.get(preq).mark<cnt && tmp>map.get(preq).mark)
					tmp = map.get(preq).mark;
			}
			for(int j = cnt-1; j > i; j--) {
				for(String preq : map.get(mark.get(i)).preq) {
					if(getDFSPreq(map.get(preq).mark).contains(j)) {
						if(tmp>sdom.get(j))
							tmp = sdom.get(j);
					}
				}
			}
			sdom.put(i, tmp);
		}
	}
	HashSet<Integer> getDFSPreq(int num) {
		HashSet<Integer> res = new HashSet<>();
		res.add(num);
		while(!res.contains(dfsPreq.get(num))) {
			res.add(dfsPreq.get(num));
			num = dfsPreq.get(num);
		}
		return res;
	}
	
	
	boolean work(String stateName) {
		updatePreq();
		State state = map.get(stateName);
		for(String preq:state.preq) {
			map.get(preq).succ.remove(stateName);
		}
		dfs(map.get("start"));
		for(String preq:state.preq) {
			map.get(preq).succ.add(stateName);
		}
		if(visit.get("accept")==false) {
			clear_visit();
			return true;
		}
		else {
			clear_visit();
			return false;
		}
	}
	void clear_visit() {
		for(State state:states) {
			visit.put(state.name, false);
		}
	}
	
}

class State{
	// the headers that are extracted in this state
	HashSet<String> headers;
	// State name
	String name;
	// successor states
	HashSet<String> succ;
	
	HashSet<String> preq;
	int mark;
	
	public State(String name) {
		this.name = name;
		headers = new HashSet<>();
		succ = new HashSet<>();
		preq = new HashSet<>();
	}
	public void addSucc(String state) {
		succ.add(state);
	}
	public void addSucc(HashSet<String> states) {
		succ.addAll(states);
	}
	public void addPreq(String state) {
		preq.add(state);
	}
}