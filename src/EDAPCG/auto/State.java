package EDAPCG.auto;

import java.util.Stack;

import EDAPCG.level.Chunk;

public interface State {
	
	public Chunk execute(Stack<State> stack);

}
