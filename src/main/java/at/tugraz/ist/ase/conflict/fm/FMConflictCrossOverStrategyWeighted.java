/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.fm;

import at.tugraz.ist.ase.conflict.common.ConflictUtils;
import at.tugraz.ist.ase.conflict.genetic.Population;
import at.tugraz.ist.ase.conflict.genetic.UserRequirement;
import at.tugraz.ist.ase.conflict.genetic.crossover.ICrossOverStrategy;
import at.tugraz.ist.ase.conflict.genetic.resolve.IResolveStrategy;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.common.RandomUtils;
import at.tugraz.ist.ase.hiconfit.fm.core.Feature;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.Setter;
import org.javatuples.Pair;

import java.io.BufferedWriter;
import java.util.*;

public class FMConflictCrossOverStrategyWeighted implements ICrossOverStrategy<Assignment, UserRequirement> {

    private static final double BASE_PROBABILITY = 0.5;

    private final List<Feature> leafFeatures;
    private static final Random random = new Random(RandomUtils.getSEED());

    private final boolean weightedPopulation;
    private final boolean noSameID;
    private final boolean weightedCrossover;
    private final double crossoverFactor;

    private final int populationSize;

    @Setter
    private List<Set<Constraint>> knownConflicts = null;
    @Setter
    private BufferedWriter resultWriter = null;
    @Setter
    private IResolveStrategy<Assignment, UserRequirement> resolveStrategy = null;

    public FMConflictCrossOverStrategyWeighted(List<Feature> leafFeatures, int populationSize, boolean weightedPopulation, boolean avoidSameID, boolean weightedCrossover, double crossoverFactor) {
        this.leafFeatures = leafFeatures;
        this.populationSize = populationSize;
        this.weightedPopulation = weightedPopulation;
        this.noSameID = avoidSameID;
        this.weightedCrossover = weightedCrossover;
        this.crossoverFactor = crossoverFactor;

        assert populationSize > 0;
    }

    @Override
    public Population<Assignment, UserRequirement> crossover(Population<Assignment, UserRequirement> parents) {
        Population<Assignment, UserRequirement> population = new Population<>();
        if (parents.size() <= 1) {
            return parents;
        }

        LoggerUtils.indent();

        List<UserRequirement> parentsList;
        if (weightedPopulation) {
            parentsList = getWeightDistributedList(parents);
        }
        else {
            parentsList = new ArrayList<>(){};
            parents.forEach(parentsList::add);
        }


        while (population.size() < populationSize) {
            // get two parents
            Pair<UserRequirement, UserRequirement> parentPair = selectParents(parentsList);
            UserRequirement parent1 = parentPair.getValue0();
            UserRequirement parent2 = parentPair.getValue1();

            var message = "";
            message = String.format("%sFather: %s", LoggerUtils.tab(), parent1);
            ConflictUtils.printMessage(resultWriter, message);
            message = String.format("%sMother: %s", LoggerUtils.tab(), parent2);
            ConflictUtils.printMessage(resultWriter, message);

            UserRequirement crossovered = crossover(parent1, parent2);

            message = String.format("%sNew individual: %s", LoggerUtils.tab(), crossovered);
            ConflictUtils.printMessage(resultWriter, message);

            // check known conflicts
            if (knownConflicts != null) {
                // if known conflict solve and add resolved URs to population
                // else add crossovered UR to population
                if (ConflictUtils.hasKnownConflict(crossovered, knownConflicts)) {
                    message = String.format("%sNew individual has a known conflict.", LoggerUtils.tab());
                    ConflictUtils.printMessage(resultWriter, message);

                    List<UserRequirement> resolvedParents = Collections.emptyList();
                    if (resolveStrategy != null) {
                        resolvedParents = resolveStrategy.resolve(crossovered, knownConflicts);

                        message = String.format("%sResolved individuals:", LoggerUtils.tab());
                        ConflictUtils.printMessage(resultWriter, message);

                        LoggerUtils.indent();
                        int index = 0;
                        for (UserRequirement userRequirement : resolvedParents) {
                            message = String.format("%s%s. %s", LoggerUtils.tab(), ++index, userRequirement);

                            ConflictUtils.printMessage(resultWriter, message);
                        }
                        LoggerUtils.outdent();
                    }
                    resolvedParents.forEach(population::addIndividual);
                } else {
                    message = String.format("%sNew individual has no known conflict. Add to population!", LoggerUtils.tab());
                    ConflictUtils.printMessage(resultWriter, message);

                    population.addIndividual(crossovered);
                }
            } else {
                population.addIndividual(crossovered);
            }
        }

        LoggerUtils.outdent();
        return population;
    }

    /**
     * Returns list of siblings <br/>
     * Siblings are actually new individuals, <br/>
     * created using any of crossover strategy
     */
    @Override
    public UserRequirement crossover(UserRequirement i1, UserRequirement i2) {
        List<Assignment> assignments = new ArrayList<>();
        double fatherProbability = weightedCrossover ? calculateProbability(i1.getWeight(), i2.getWeight()) : BASE_PROBABILITY;

        for (Feature feature : leafFeatures) {
            // get value of father
            String father_value;
            try {
                father_value = i1.getAssignment(feature.getName()).getValue();
            } catch (Exception e) {
                father_value = null;
            }

            // get value of mother
            String mother_value;
            try {
                mother_value = i2.getAssignment(feature.getName()).getValue();
            } catch (Exception e) {
                mother_value = null;
            }

            enum CROSSOVERTYPE {FATHER, MOTHER, NONE}
            CROSSOVERTYPE crossOverType = CROSSOVERTYPE.NONE;

            boolean selectFather = random.nextDouble() < fatherProbability; //selectFather(fatherProbability, father_value==null, mother_value==null);
            boolean selectMother = !selectFather;

            if (selectFather && father_value != null) {
                crossOverType = CROSSOVERTYPE.FATHER;
            }
            else if (selectMother && mother_value != null) {
                crossOverType = CROSSOVERTYPE.MOTHER;
            }

            Assignment assignment = switch (crossOverType) {
                case FATHER -> Assignment.builder()
                        .variable(feature.getName())
                        .value(father_value)
                        .build();
                case MOTHER -> Assignment.builder()
                        .variable(feature.getName())
                        .value(mother_value)
                        .build();
                default -> null;
            };
            if (assignment != null) {
                assignments.add(assignment);
            }
        }

        return UserRequirement.requirementBuilder()
                .assignments(assignments)
                .build();
    }

    private Pair<UserRequirement, UserRequirement> selectParents(List<UserRequirement> weightedParents) {

        // init parent1
        int randIndex1 = random.nextInt(weightedParents.size());
        UserRequirement parent1 = weightedParents.get(randIndex1);

        // init parent2
        int randIndex2 = random.nextInt(weightedParents.size());
        while (randIndex2 == randIndex1) {randIndex2 = random.nextInt(weightedParents.size());}
        UserRequirement parent2 = weightedParents.get(randIndex2);

        // check if parent1 resolved CS and from same original conflict as parent2
        if (noSameID && parent1.getResolvedID() != 0 && parent1.getResolvedID() == parent2.getResolvedID()) {
            while (parent1.getResolvedID() == parent2.getResolvedID()){
                randIndex2 = random.nextInt(weightedParents.size());
                parent2 = weightedParents.get(randIndex2);
            }
        }

        var message = String.format("%sCrossover parent1 %d and parent2 %d", LoggerUtils.tab(), randIndex1, randIndex2);
        ConflictUtils.printMessage(resultWriter, message);
        return new Pair<>(parent1, parent2);
    }

    /**
     * Create List of all UserRequirements in Population but duplicate UserRequirements
     * with weight higher than 0 by that factor.
     */
    private List<UserRequirement> getWeightDistributedList(Population<Assignment, UserRequirement> population) {
        List<UserRequirement> weightedList = new ArrayList<>();

        for (UserRequirement individual : population ) {
            int weight = individual.getWeight();
            weightedList.add(individual);
            while (weight > 0) {
                weightedList.add(individual);
                weight--;
            }
        }

        return weightedList;
    }

    /**
     * Calculate the probability of an ancestor with the given weight,
     * against the ancestor with the comparable weight.
     * <p>
     * @param weight of the primary ancestor of whom the probability shall be calculated.
     * @param comparativeWeight of the secondary ancestor to be compared against.
     * @return the probability that the primary ancestor shall be selected.
     */
    private double calculateProbability(int weight, int comparativeWeight) {
        int weightDelta = Math.abs(weight - comparativeWeight);
        double probability = BASE_PROBABILITY;

        if (weightDelta > 0){
            // double k = -Math.log((1-crossoverProbability)/(1-BASE_PROBABILITY));
            // probability = 1 - (1-BASE_PROBABILITY) * Math.exp(-k * weightDelta); //BASE_PROBABILITY + (1 - probabilityStep) * Math.log(weightDelta + 1) / Math.log(WEIGHT_BASE); //BASE_PROBABILITY + Math.pow(2, weightDelta - 1) * probabilityStep;
            probability = Math.atan(weightDelta/Math.pow(Math.PI, crossoverFactor)) * 0.3 + BASE_PROBABILITY;
        }

        return weight >= comparativeWeight ? probability : 1 - probability;
    }
}
