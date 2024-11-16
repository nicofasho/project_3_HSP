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

public class Cawatso3HSP extends StateSpaceSearch {
    private MinPriorityQueue<StateSpaceNode> frontier = new MinPriorityQueue<>();

    public Cawatso3HSP(StateSpaceProblem problem, SearchBudget budget) {
        super(problem, budget);
    }

    @Override
    public Plan solve() {
        frontier.push(root, 0.0);

        HashMap<StateSpaceNode, Double> g_costs = new HashMap<>();

        g_costs.put(root, 0.0);

        HashSet<StateSpaceNode> visited = new HashSet<StateSpaceNode>();

        while (!frontier.isEmpty()) {
            StateSpaceNode current = frontier.pop();

            if (this.problem.goal.isTrue(current.state)) {
                return current.plan;
            }

            visited.add(current);

            Iterator<Step> iterator = this.problem.steps.iterator();

            while (iterator.hasNext()) {
                Step step = iterator.next();

                if (step.precondition.isTrue(current.state)) {
                    StateSpaceNode newNode = current.expand(step);

                    Double g_cost = g_costs.get(current) + 1;
                    Double h_cost = calculateHeuristic(newNode.state);
                    Double f_cost = g_cost + h_cost;

                    if (!visited.contains(newNode) || g_cost < g_costs.get(newNode)) {
                        g_costs.put(newNode, g_cost);
                        frontier.push(newNode, f_cost);
                    }
                }

            }
        }
        return null;
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
