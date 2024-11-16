package edu.uno.ai.planning.ex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.uno.ai.SearchBudget;
import edu.uno.ai.logic.Conjunction;
import edu.uno.ai.logic.Literal;
import edu.uno.ai.logic.Proposition;
import edu.uno.ai.logic.State;
import edu.uno.ai.planning.Plan;
import edu.uno.ai.planning.Step;
import edu.uno.ai.planning.ss.StateSpaceNode;
import edu.uno.ai.planning.ss.StateSpaceProblem;
import edu.uno.ai.planning.ss.StateSpaceSearch;
import edu.uno.ai.util.MinPriorityQueue;

/**
 * Implementation of Heuristic Search Planning algorithm.
 */
public class Cawatso3HSP extends StateSpaceSearch {
    private final MinPriorityQueue<StateSpaceNode> frontier;
    private final Map<StateSpaceNode, Double> pathCosts;
    private final Set<StateSpaceNode> visitedNodes;

    public Cawatso3HSP(StateSpaceProblem problem, SearchBudget budget) {
        super(problem, budget);
        this.frontier = new MinPriorityQueue<>();
        this.pathCosts = new HashMap<>();
        this.visitedNodes = new HashSet<>();
    }

    @Override
    public Plan solve() {
        initializeSearch();
        return performSearch();
    }

    private void initializeSearch() {
        frontier.push(root, 0.0);
        pathCosts.put(root, 0.0);
    }

    private Plan performSearch() {
        while (!frontier.isEmpty()) {
            StateSpaceNode currentNode = frontier.pop();

            if (isGoalState(currentNode)) {
                return currentNode.plan;
            }

            if (!visitedNodes.contains(currentNode)) {
                visitedNodes.add(currentNode);
                expandNode(currentNode);
            }
        }
        return null; // No solution found
    }

    private boolean isGoalState(StateSpaceNode node) {
        return problem.goal.isTrue(node.state);
    }

    private void expandNode(StateSpaceNode node) {
        Iterator<Step> availableSteps = problem.steps.iterator();
        while (availableSteps.hasNext()) {
            Step step = availableSteps.next();

            if (step.precondition.isTrue(node.state)) {
                StateSpaceNode newNode = node.expand(step);

                Double g_cost = pathCosts.get(node) + 1;
                Double h_cost = calculateHeuristic(newNode.state);
                Double f_cost = g_cost + h_cost;

                if (!visitedNodes.contains(newNode) || g_cost < pathCosts.get(newNode)) {
                    pathCosts.put(newNode, g_cost);
                    frontier.push(newNode, f_cost);
                }
            }
        }
    }

    private Set<Literal> extractLiterals(Proposition prop) {
        Set<Literal> literals = new HashSet<>();
        if (prop instanceof Literal) {
            literals.add((Literal) prop);
        } else if (prop instanceof Conjunction) {
            Conjunction conj = (Conjunction) prop;
            for (Proposition arg : conj.arguments) {
                literals.addAll(extractLiterals(arg));
            }
        }

        return literals;
    }

    private Double calculateHeuristic(State state) {
        Map<Literal, Double> literalCosts = new HashMap<>();

        for (Literal l : problem.literals) {
            literalCosts.put(l, Double.POSITIVE_INFINITY);
        }

        Proposition stateProp = state.toProposition();
        for (Literal l : extractLiterals(stateProp)) {
            literalCosts.put(l, 0.0);
        }

        boolean changed;
        do {
            changed = false;
            for (Step step : problem.steps) {
                double precondCost = calculateCost(step.precondition, literalCosts);

                for (Literal effect : extractLiterals(step.effect)) {
                    double newCost = precondCost + 1;
                    if (newCost < literalCosts.get(effect)) {
                        literalCosts.put(effect, newCost);
                        changed = true;
                    }
                }
            }
        } while (changed);

        return calculateCost(problem.goal, literalCosts);
    }

    private Double calculateCost(Proposition prop, Map<Literal, Double> costs) {
        if (prop instanceof Literal) {
            return costs.get((Literal) prop);
        } else {
            double sum = 0.0;
            Conjunction conj = (Conjunction) prop;
            for (Proposition arg : conj.arguments) {
                sum += calculateCost(arg, costs);
            }
            return sum;
        }
    }
}
