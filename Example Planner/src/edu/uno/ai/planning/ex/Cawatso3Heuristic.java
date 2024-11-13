package edu.uno.ai.planning.ex;

import java.util.HashMap;

import edu.uno.ai.logic.Conjunction;
import edu.uno.ai.logic.Literal;
import edu.uno.ai.logic.Proposition;
import edu.uno.ai.planning.Heuristic;
import edu.uno.ai.planning.ss.StateSpaceNode;

public class Cawatso3Heuristic implements Heuristic  {
    private HashMap<Literal, Double> values = new HashMap<>();

    public double evaluate(StateSpaceNode root) {
        return 0.0;
    }

    public Double evaluateProposition(Proposition proposition) {
        if (proposition instanceof Literal) {
            return evaluateLiteral((Literal) proposition);
        } else {
            double total = 0.0;

            for (Proposition subProposition : ((Conjunction) proposition).arguments) {
                double subValue = evaluateProposition(subProposition);
                if (subValue == Double.POSITIVE_INFINITY) {
                    return Double.POSITIVE_INFINITY;
                }

                total += subValue;
            }

            return total;
        }
    }

    public Double evaluateLiteral(Literal literal) {
        Double value = values.getOrDefault(literal, null);
        return value == null ? Double.POSITIVE_INFINITY : value;
    }

    public Boolean heuristicValue(Proposition proposition, Double value) {
        if (proposition instanceof Literal) {
            Literal literal = (Literal) proposition;
            double currentValue = evaluateLiteral(literal);
            if (value < currentValue) {
                values.put(literal, value);
                return true;
            } else {
                return false;
            }
        } else {
            boolean updated = false;

            for (Proposition subProposition : ((Conjunction) proposition).arguments) {
                updated = heuristicValue(subProposition, value) || updated;
            }

            return updated;
        }
    }
}
