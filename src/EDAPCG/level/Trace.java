package EDAPCG.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class Trace implements Iterable<Chunk> {
	
	protected ArrayList<Chunk> trace;
	
	public Trace() {
		trace = new ArrayList<Chunk>();
	}
	
	public boolean addChunk(Chunk g) {
		return trace.add(g);
	}
	
	public int size() {
		return trace.size();
	}
	
	public Trace copy() {
		return (Trace) trace.clone();
	}
	
	public Chunk getChunk(int i) {
		return trace.get(i);
	}
	
	@Override
	public Iterator<Chunk> iterator() {
        Iterator<Chunk> i = Collections.unmodifiableList(trace).iterator();
        return i; 
	}
	
	public EDALevel buildLevel(int type) {
		try { 
			EDALevel lvl = new EDALevel();
			Builder lkb = new Builder(lvl);			
			
			lvl.setType(type);
			
			lkb.createStartPlug();
			
			for (Chunk g : trace)
				if (!g.genesis(lkb)) {
					System.out.println(g);
					return null;
				}
	
			lkb.createEndPlug();
			
			lkb.fixLevel();

			return lvl;
			
		} catch (Exception e) {
			System.out.println(e.getCause().getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
