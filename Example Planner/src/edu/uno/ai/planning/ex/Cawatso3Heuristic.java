package edu.uno.ai.planning.ex;

import java.util.HashMap;

import edu.uno.ai.logic.Conjunction;
import edu.uno.ai.logic.Literal;
import edu.uno.ai.logic.Proposition;
import edu.uno.ai.planning.Heuristic;
import edu.uno.ai.planning.ss.StateSpaceNode;

public class Cawatso3Heuristic implements Heuristic<StateSpaceNode> {
    private HashMap<Literal, Double> values = new HashMap<>();

    @Override
    public double evaluate(StateSpaceNode node) {
        Proposition proposition = node.state.toProposition();
        if (proposition instanceof Literal) {
            return evaluate((Literal) proposition);
        } else {
            double total = 0.0;

            for (Proposition subProposition : ((Conjunction) proposition).arguments) {
                double subValue = evaluate(subProposition);
                if (subValue == Double.POSITIVE_INFINITY) {
                    return Double.POSITIVE_INFINITY;
                }

                total += subValue;
            }

            return total;
        }
    }

    public double evaluate(Proposition proposition) {
        if (proposition instanceof Literal) {
            return this.evaluate((Literal) proposition);
        } else {
            double value = 0.0;

            for (Proposition subProposition : ((Conjunction)proposition).arguments) {
                double subPropValue = this.evaluate(subProposition);
            
                if ((subPropValue == Double.POSITIVE_INFINITY)) {
                    return Double.POSITIVE_INFINITY;
                }

                value += subPropValue;
            }

            return value;
        }
    }

    public Double evaluate(Literal literal) {
        Double value = values.getOrDefault(literal, null);
        return value == null ? Double.POSITIVE_INFINITY : value;
    }

    public Boolean evaluate(Proposition proposition, Double value) {
        if (proposition instanceof Literal) {
            Literal literal = (Literal) proposition;
            double currentValue = evaluate(literal);
            if (value < currentValue) {
                values.put(literal, value);
                return true;
            } else {
                return false;
            }
        } else {
            boolean updated = false;

            for (Proposition subProposition : ((Conjunction) proposition).arguments) {
                updated = evaluate(subProposition, value) || updated;
            }

            return updated;
        }
    }
}
