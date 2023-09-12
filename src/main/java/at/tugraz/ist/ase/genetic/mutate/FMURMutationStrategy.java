/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.genetic.mutate;

import at.tugraz.ist.ase.genetic.Population;
import at.tugraz.ist.ase.genetic.UserRequirement;
import at.tugraz.ist.ase.genetic.common.CombinationUtils;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.common.RandomUtils;
import at.tugraz.ist.ase.hiconfit.fm.core.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A mutation strategy for Feature Model User Requirements
 */
public class FMURMutationStrategy implements IMutationStrategy<Assignment, UserRequirement> {

    private final List<Feature> leafFeatures;
    private final double noPreferenceProbability;
    private final double mutationProbability;
    private final int maxFeaturesInUR;
    private static final Random random = new Random(RandomUtils.getSEED());

    public FMURMutationStrategy(List<Feature> leafFeatures,
                                double noPreferenceProbability,
                                double mutationProbability,
                                int maxFeaturesInUR) {
        this.leafFeatures = leafFeatures;
        this.noPreferenceProbability = noPreferenceProbability;
        this.mutationProbability = mutationProbability;
        this.maxFeaturesInUR = maxFeaturesInUR;
    }

    /**
     * Create a new user requirement by mutation
     *
     * @return a new user requirement
     */
    @Override
    public UserRequirement mutate() {
        List<Assignment> assignments;

        List<Integer> indexes = CombinationUtils.selectIndexes(maxFeaturesInUR, leafFeatures.size(), true);

        do {
            assignments = new ArrayList<>();
            for (Integer index : indexes) {
                Feature feature = leafFeatures.get(index);
                Assignment assignment = mutate(feature);
                if (assignment != null) {
                    assignments.add(assignment);
                }
            }
        } while (assignments.size() < 2);

        return UserRequirement.requirementBuilder()
                .assignments(assignments)
                .build();
    }

    private Assignment mutate(Feature feature) {
        if (Math.random() <= (1 - noPreferenceProbability)) {
            List<String> domain = List.of("true", "false");
            // randomly select a value from the domain
            String value = domain.get(random.nextInt(domain.size()));

            return Assignment.builder()
                    .variable(feature.getName())
                    .value(value)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public UserRequirement mutate(UserRequirement ur) {
        List<Assignment> assignments;

        List<Integer> indexes = CombinationUtils.selectIndexes(maxFeaturesInUR, leafFeatures.size(), true);

        do {
            assignments = new ArrayList<>();
            for (Integer index : indexes) {
                Feature feature = leafFeatures.get(index);
                String value;
                try {
                    value = ur.getAssignment(feature.getName()).getValue();
                } catch (Exception e) {
                    value = null;
                }

                if (value == null) {
                    if (Math.random() <= mutationProbability) { // mutate the user requirement by adding a new assignment
                        Assignment assignment = mutate(feature);
                        if (assignment != null) {
                            assignments.add(assignment);
                        }
                    }
                } else { // copy the value
                    if (Math.random() <= (1 - mutationProbability)) {
                        value = value.equals("true") ? "false" : "true";
                    }

                    Assignment assignment = Assignment.builder()
                            .variable(feature.getName())
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
