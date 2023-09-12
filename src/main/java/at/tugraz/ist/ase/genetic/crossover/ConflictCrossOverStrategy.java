/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.genetic.crossover;

import at.tugraz.ist.ase.genetic.Population;
import at.tugraz.ist.ase.genetic.UserRequirement;
import at.tugraz.ist.ase.genetic.common.ConflictUtils;
import at.tugraz.ist.ase.genetic.resolve.IResolveStrategy;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.common.RandomUtils;
import at.tugraz.ist.ase.hiconfit.fm.core.Feature;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.Setter;

import java.io.BufferedWriter;
import java.util.*;

public class ConflictCrossOverStrategy implements ICrossOverStrategy<Assignment, UserRequirement> {

    private final List<Feature> leafFeatures;
    private static final Random random = new Random(RandomUtils.getSEED());

    @Setter
    private List<Set<Constraint>> knownConflicts = null;
    @Setter
    private BufferedWriter resultWriter = null;
    @Setter
    private IResolveStrategy<Assignment, UserRequirement> resolveStrategy = null;

    public ConflictCrossOverStrategy(List<Feature> leafFeatures) {
        this.leafFeatures = leafFeatures;
    }

    @Override
    public Population<Assignment, UserRequirement> crossover(Population<Assignment, UserRequirement> parents) {
        Population<Assignment, UserRequirement> population = new Population<>();
        if (parents.size() <= 1) {
            return parents;
        }

        LoggerUtils.indent();
        for (int indexFather = 0; indexFather < parents.size(); indexFather++) {
            int indexMother = indexFather;

            // father and mother should be different
            while (indexFather == indexMother) {
                indexMother = random.nextInt(parents.size());
            }

            UserRequirement father = parents.getIndividualByIndex(indexFather);
            UserRequirement mother = parents.getIndividualByIndex(indexMother);

            var message = String.format("%sCrossover father %d and mother %d", LoggerUtils.tab(), indexFather, indexMother);
            ConflictUtils.printMessage(resultWriter, message);
            message = String.format("%sFather: %s", LoggerUtils.tab(), father);
            ConflictUtils.printMessage(resultWriter, message);
            message = String.format("%sMother: %s", LoggerUtils.tab(), mother);
            ConflictUtils.printMessage(resultWriter, message);

            UserRequirement crossovered = crossover(father, mother);

            message = String.format("%sNew individual: %s", LoggerUtils.tab(), crossovered);
            ConflictUtils.printMessage(resultWriter, message);

            // check known conflicts
            if (knownConflicts != null) {
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
//                    indexFather--;
                } else {
                    message = String.format("%sNew individual has no known conflict. Add to population!", LoggerUtils.tab());
                    ConflictUtils.printMessage(resultWriter, message);

                    population.addIndividual(crossovered);
                }
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
            if (father_value != null || mother_value != null) {
                if (Objects.equals(father_value, mother_value) || mother_value == null) {
                    // if both parents have the same value,
                    // or if the mother has no value, the father has a value,
                    // then the child has the same value
                    crossOverType = CROSSOVERTYPE.FATHER;
                } else if (father_value == null) { // if the father has no value, the mother has a value
                    crossOverType = CROSSOVERTYPE.MOTHER;
                } else { // if both parents have different values
                    if (random.nextBoolean()) { // randomly select the father's value or the mother's value
                        crossOverType = CROSSOVERTYPE.FATHER;
                    } else {
                        crossOverType = CROSSOVERTYPE.MOTHER;
                    }
                }
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
}
