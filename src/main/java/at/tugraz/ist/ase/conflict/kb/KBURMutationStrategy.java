/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.kb;

import at.tugraz.ist.ase.conflict.common.CombinationUtils;
import at.tugraz.ist.ase.conflict.genetic.Population;
import at.tugraz.ist.ase.conflict.genetic.UserRequirement;
import at.tugraz.ist.ase.conflict.genetic.mutate.IMutationStrategy;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.common.RandomUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A mutation strategy for Camera User Requirements
 */
public class KBURMutationStrategy implements IMutationStrategy<Assignment, UserRequirement> {

    private final List<Variable> variables;
    private final double noPreferenceProbability;
    private final double mutationProbability;
    private final int maxVariablesInUR;
    private static final Random random = new Random(RandomUtils.getSEED());

    public KBURMutationStrategy(List<Variable> variables,
                                double noPreferenceProbability,
                                double mutationProbability,
                                int maxVariablesInUR) {
        this.variables = variables;
        this.noPreferenceProbability = noPreferenceProbability;
        this.mutationProbability = mutationProbability;
        this.maxVariablesInUR = maxVariablesInUR;
    }

    /**
     * Create a new user requirement by mutation
     *
     * @return a new user requirement
     */
    @Override
    public UserRequirement mutate() {
        List<Assignment> assignments;

        List<Integer> indexes = CombinationUtils.selectIndexes(maxVariablesInUR, variables.size(), true);

        do {
            assignments = new ArrayList<>();
            for (Integer index : indexes) {
//                Feature feature = variables.get(index);
                Variable var = variables.get(index);

                Assignment assignment = mutate(var);
                if (assignment != null) {
                    assignments.add(assignment);
                }
            }
        } while (assignments.size() < 2);

        return UserRequirement.requirementBuilder()
                .assignments(assignments)
                .build();
    }

    private Assignment mutate(Variable var) {
        if (Math.random() <= (1 - noPreferenceProbability)) {
            List<String> domain = var.getDomain().getValues();
            // randomly select a value from the domain
            String value = domain.get(random.nextInt(domain.size()));

            return Assignment.builder()
                    .variable(var.getName())
                    .value(value)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public UserRequirement mutate(UserRequirement ur) {
        List<Assignment> assignments;

        List<Integer> indexes = CombinationUtils.selectIndexes(maxVariablesInUR, variables.size(), true);

        do {
            assignments = new ArrayList<>();
            for (Integer index : indexes) {
//                Feature feature = variables.get(index);
                Variable var = variables.get(index);
                String value;
                try {
                    value = ur.getAssignment(var.getName()).getValue();
                } catch (Exception e) {
                    value = null;
                }

                if (value == null) {
                    if (Math.random() <= mutationProbability) { // mutate the user requirement by adding a new assignment
                        Assignment assignment = mutate(var);
                        if (assignment != null) {
                            assignments.add(assignment);
                        }
                    }
                } else { // copy the value
                    if (Math.random() <= (1 - mutationProbability)) { // mutate the value
                        List<String> domain = var.getDomain().getValues();
                        // randomly select a value from the domain
                        String new_value = value;
                        do {
                            new_value = domain.get(random.nextInt(domain.size()));
                        } while (!new_value.equals(value));

                        value = new_value;
//                        value = value.equals("true") ? "false" : "true";
                    }

                    Assignment assignment = Assignment.builder()
                            .variable(var.getName())
                            .value(value)
                            .build();
                    assignments.add(assignment);
                }
            }
        } while (assignments.size() < 2);

        return UserRequirement.requirementBuilder()
                .assignments(assignments)
                .build();
    }

    @Override
    public Population<Assignment, UserRequirement> mutate(Population<Assignment, UserRequirement> population) {
        Population<Assignment, UserRequirement> newPopulation = new Population<>();

        for (UserRequirement ur : population) {
            newPopulation.addIndividual(mutate(ur));
        }

        return newPopulation;
    }
}
