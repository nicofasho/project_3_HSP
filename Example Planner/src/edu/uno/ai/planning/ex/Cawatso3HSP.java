package edu.uno.ai.planning.ex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.uno.ai.SearchBudget;
import edu.uno.ai.logic.Conjunction;
import edu.uno.ai.logic.Literal;
import edu.uno.ai.logic.Proposition;
import edu.uno.ai.logic.State;
import edu.uno.ai.planning.Heuristic;
import edu.uno.ai.planning.Plan;
import edu.uno.ai.planning.Step;
import edu.uno.ai.planning.ss.StateSpaceNode;
import edu.uno.ai.planning.ss.StateSpaceProblem;
import edu.uno.ai.planning.ss.StateSpaceSearch;
import edu.uno.ai.util.MinPriorityQueue;

public class Cawatso3HSP extends StateSpaceSearch {
    private Heuristic heuristic;
    private MinPriorityQueue<StateSpaceNode> frontier = new MinPriorityQueue<>();

    private Cawatso3HSP(StateSpaceProblem problem, SearchBudget budget, Heuristic heuristic) {
        super(problem, budget);
        this.heuristic = heuristic;
        this.frontier.push(this.root, heuristic.evaluate(this.root));
    }

    public Cawatso3HSP(StateSpaceProblem problem, SearchBudget budget) {
        this(problem, budget, new Cawatso3Heuristic());
    }

    @Override
    public Plan solve() {
        MinPriorityQueue<StateSpaceNode> frontier = new MinPriorityQueue<>();
        frontier.push(root, f(root, problem));

        Set<State> visited = new HashSet<>();

        while (!frontier.isEmpty() && budget.getRemainingOperations() > 0) {
            StateSpaceNode current = frontier.pop();

            if (problem.isSolution(current.plan)) {
                return current.plan;
            }

            if (!visited.add(current.state)) {
                continue;
            }

            for (Step step : problem.steps) {
                StateSpaceNode successor = current.expand(step);

                frontier.push(successor, f(successor, problem));
            }
        }

        return null;
    }

    private double f(StateSpaceNode node, StateSpaceProblem problem) {
        return g(node) + h(node, problem);
    }

    private double g(StateSpaceNode node) {
        return node.plan.size();
    }

    private double h(StateSpaceNode node, StateSpaceProblem problem) {
        ArrayList<Literal> list = new ArrayList<>();
        State state = node.state;

        int count = 0;

        if (problem.goal instanceof Literal) {
            list.add((Literal) problem.goal);
        } else {
            for (Proposition conjunct : ((Conjunction) problem.goal).arguments) {
                list.add((Literal) conjunct);
            }
        }

        for (Literal l : list) {
            if (!l.isTrue(state)) {
                count++;
            }
        }

        return count;
    }
}
