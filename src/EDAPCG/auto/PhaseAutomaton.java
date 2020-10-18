package EDAPCG.auto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import EDAPCG.level.Chunk;

public class PhaseAutomaton implements FSM, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected List<HashMap<String,Dummy>> dummies = new ArrayList<HashMap<String,Dummy>>();
	protected double[] odds;
	protected Stack<State> stack;
	
	public PhaseAutomaton(Automaton[] autos, double[] odds) throws Exception {
		if (autos.length != odds.length)
			throw new Exception("Unable to create phase automaton.");
		
		for (Automaton a : autos)
			if (null == a)
				throw new Exception("Invalid automaton provided");
			else
				this.dummies.add(a.dummies);
		
		this.odds = odds;
	}
	
	public void init() {
		stack = new Stack<State>();
		int i = max_odds();
		stack.push(dummies.get(i).get("initial"));
	}
	
	public Chunk step() {
		State st = stack.pop();
		State stx = null;
		int[] sodds = shuffle_odds();
		
		for (int i : sodds) {
			stx = dummies.get(i).get(st.toString());
			if (null != stx)
				return stx.execute(stack);
		}	

		// it wasn't found because it's a chunk
		return st.execute(stack);
	}
	
	protected int[] shuffle_odds() {
		int[] ix = new int[odds.length];
		boolean[] used = new boolean[odds.length];
		
		for (int i = 0; i < used.length; i++) {
			ix[i] = -1;
			used[i] = false;
		}
		
		for (int i = 0; i < odds.length; i++)
			while (ix[i] == -1) {
				double r = new Random().nextDouble();
				
				for (int j = 0; j < odds.length; j++)
					if (!used[j] && r < odds[j]) {
						ix[i] = j;
						used[j] = true;
						break;
					}
			}
		
		return ix;
	}

	protected int max_odds() {
		if (odds.length < 1) return -1;
		int maxi = 0;
		double maxv = odds[0];
		for (int i = 1; i < odds.length; i++) {
			if (odds[i]-odds[i-1] > maxv) {
				maxi = i;
				maxv = odds[i]-odds[i-1];
			}
		}
		return maxi;
	}
}
