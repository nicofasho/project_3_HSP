package edu.uno.ai.planning.ex;

import edu.uno.ai.SearchBudget;
import edu.uno.ai.planning.Heuristic;
import edu.uno.ai.planning.ss.StateSpacePlanner;
import edu.uno.ai.planning.ss.StateSpaceProblem;
import edu.uno.ai.planning.ss.StateSpaceSearch;

/**
 * A planner that uses heuristic search through the space of states.
 * 
 * @author Christian Watson
 */

public class Cawatso3 extends StateSpacePlanner {

	private Heuristic heuristic;

	private Cawatso3(String name) {
		super("Cawatso3");
	}

		@Override
	protected StateSpaceSearch makeStateSpaceSearch(StateSpaceProblem problem, SearchBudget budget) {
		return new Cawatso3HSP(problem, budget);
	}

}
