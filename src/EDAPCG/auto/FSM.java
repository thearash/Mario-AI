package EDAPCG.auto;

import EDAPCG.level.Chunk;

public interface FSM {
	
	public void init();
	
	public Chunk step();

}
