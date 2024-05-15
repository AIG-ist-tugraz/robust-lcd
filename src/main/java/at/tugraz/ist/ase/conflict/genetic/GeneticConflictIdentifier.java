/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.conflict.common.ConflictUtils;
import at.tugraz.ist.ase.conflict.common.StatisticsWriter;
import at.tugraz.ist.ase.conflict.genetic.crossover.ICrossOverStrategy;
import at.tugraz.ist.ase.conflict.genetic.mutate.IMutationStrategy;
import at.tugraz.ist.ase.conflict.genetic.resolve.IResolveStrategy;
import at.tugraz.ist.ase.conflict.model.CDRModelFactory;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.HSDAG;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.HSDAGPruningEngine;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.labeler.QuickXPlainLabeler;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.parameters.QuickXPlainParameters;
import at.tugraz.ist.ase.hiconfit.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.hiconfit.cacdr.eval.CAEvaluator;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Requirement;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.*;

import java.io.BufferedWriter;
import java.util.*;

import static at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.AbstractHSConstructor.*;
import static at.tugraz.ist.ase.hiconfit.cacdr.checker.ChocoConsistencyChecker.TIMER_SOLVER;
import static at.tugraz.ist.ase.hiconfit.common.ConstraintUtils.convertToStringWithMessage;
import static at.tugraz.ist.ase.hiconfit.eval.PerformanceEvaluator.setCommonTimer;

public class GeneticConflictIdentifier implements IGeneticAlgorithm<Assignment, UserRequirement, Double> {

    private final CDRModelFactory modelFactory;
//    private final FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel;
    @Getter @Setter
    private Population<Assignment, UserRequirement> population;
    private final boolean cfInConflicts;
    private final int numMaxConflicts;

    // listeners of genetic algorithm iterations (handle callback afterwards)
    private final List<IterationListener<Assignment, UserRequirement, Double>> iterationListeners = new LinkedList<>();

    private boolean terminate = false;
    private final int stopAfterXTimesNoConflict;
    private int numNoConflict = 0;
    private final int populationSize;
    @Getter
    private int currentIteration = 0;
    @Getter
    private int currentPopulation = 0;
    @Getter
    private int currentGeneration = 0;

    private final List<Set<Constraint>> allConflictSets; // TODO: start with some already known conflicts
    private final List<Set<Constraint>> allConflictSetsWithoutCF;

    @Setter
    private IMutationStrategy<Assignment, UserRequirement> mutationStrategy = null;
    @Setter
    private IResolveStrategy<Assignment, UserRequirement> resolveStrategy = null;
    @Setter
    private ICrossOverStrategy<Assignment, UserRequirement> crossOverStrategy = null;

    @Setter
    private BufferedWriter resultWriter = null;
    @Setter
    private BufferedWriter allConflictSetsWriter = null;
    @Setter
    private BufferedWriter allConflictSetsWithoutCFWriter = null;

    @Setter
    private int extinctAfterXTimesNoConflict = 0;
    @Setter
    private int stopAfterXExtinctions = 0;

    @Setter
    private StatisticsWriter statisticsWriter = null;

    @Builder
    public GeneticConflictIdentifier(@NonNull Population<Assignment, UserRequirement> population,
//                                     FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel,
                                    CDRModelFactory modelFactory,
                                     boolean cfInConflicts,
                                     int numMaxConflicts,
                                     int stopAfterXTimesNoConflict,
                                     List<Set<Constraint>> allConflictSets,
                                     List<Set<Constraint>> allConflictSetsWithoutCF) {
        this.population = population;
        this.populationSize = population.size();
//        this.featureModel = featureModel;
        this.modelFactory = modelFactory;
        this.cfInConflicts = cfInConflicts;
        this.numMaxConflicts = numMaxConflicts;
        this.stopAfterXTimesNoConflict = stopAfterXTimesNoConflict;
        this.allConflictSets = allConflictSets;
        this.allConflictSetsWithoutCF = allConflictSetsWithoutCF;
    }

    @Override
    public void evolve() {
        // check all individuals for conflicts
        var message = String.format("%sGENERATION %d: Checking all individuals for conflicts ...", LoggerUtils.tab(), currentIteration);
        ConflictUtils.printMessage(resultWriter, message);

        LoggerUtils.indent();
        int index = 0;

        List<Set<Constraint>> allNewConflictSets = new ArrayList<>();
        List<Set<Constraint>> allNewConflictSetsWithoutCF = new ArrayList<>();
        Population<Assignment, UserRequirement> parents = new Population<>();
        for (UserRequirement userRequirement : this.population) {
            Requirement ur = Requirement.requirementBuilder()
                    .assignments(userRequirement.getAssignments())
                    .build();

            message = String.format("%s%s. %s", LoggerUtils.tab(), ++index, ur);
            ConflictUtils.printMessage(resultWriter, message);

            List<Set<Constraint>> newConflictSets = identifyConflicts(ur);

            System.gc();

            LoggerUtils.indent();
            if (!newConflictSets.isEmpty()) {
                message = String.format("%sINCONSISTENT", LoggerUtils.tab());
                ConflictUtils.printMessage(resultWriter, message);

                printConflictInformation(newConflictSets);

                newConflictSets.forEach(cs -> {
                    ConflictUtils.addCSToCSList(cs, allNewConflictSets, null, resultWriter);

                    Set<Constraint> csWithoutCF = new HashSet<>();
                    for (Constraint c : cs) {
                        String[] items = c.toString().split("=");

                        if (items.length == 2) { // add to csWithoutCF
                            csWithoutCF.add(c);
                        }
                    }

                    ConflictUtils.addCSToCSList(csWithoutCF, allNewConflictSetsWithoutCF, null, resultWriter);
                });

                // resolve and add to parents
                List<UserRequirement> resolvedParents = Collections.emptyList();
                if (resolveStrategy != null) {
                    resolvedParents = resolveStrategy.resolve(userRequirement, allNewConflictSetsWithoutCF);
                }
                resolvedParents.forEach(parents::addIndividual);
            } else {
                message = String.format("%sconsistent", LoggerUtils.tab());
                ConflictUtils.printMessage(resultWriter, message);

                // clone and add to parents
                try {
                    parents.addIndividual(userRequirement.clone());
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }
            LoggerUtils.outdent();
        }
        LoggerUtils.outdent();

        message = String.format("%sGENERATION %d: Found %d unique minimal conflict sets in this round.", LoggerUtils.tab(), currentIteration, allNewConflictSets.size());
        ConflictUtils.printMessage(resultWriter, message);
        int knownMinConflictsBefore = allConflictSets.size();
        allNewConflictSets.forEach(cs -> ConflictUtils.addCSToCSList(cs, allConflictSets, allConflictSetsWriter, resultWriter));
        allNewConflictSetsWithoutCF.forEach(cs -> ConflictUtils.addCSToCSList(cs, allConflictSetsWithoutCF, allConflictSetsWithoutCFWriter, resultWriter));
        int newMinConflicts = allConflictSets.size() - knownMinConflictsBefore;
        message = String.format("%sGENERATION %d: Found %d globally new minimal conflict sets in this round.", LoggerUtils.tab(), currentIteration, newMinConflicts);
        ConflictUtils.printMessage(resultWriter, message);

        statisticsWriter.write(currentPopulation, currentGeneration, allNewConflictSets.size(), newMinConflicts, allConflictSets.size());

        this.population = parents;

        if (newMinConflicts > 0) {
            message = String.format("%sGENERATION %d: Resolved population.", LoggerUtils.tab(), currentIteration);
            ConflictUtils.printMessage(resultWriter, message);
            for (IterationListener<Assignment, UserRequirement, Double> l : this.iterationListeners) {
                l.update(this);
            }
        } else {
            message = String.format("%sGENERATION %d: No new minimal conflict sets found. Evolving population.", LoggerUtils.tab(), currentIteration);
            ConflictUtils.printMessage(resultWriter, message);
        }

        System.gc();

        // randomly crossover
        if (crossOverStrategy != null) {
            message = String.format("%sGENERATION %d: Generating a new generation of %d individuals with genetic crossover...", LoggerUtils.tab(), currentIteration, populationSize);
            ConflictUtils.printMessage(resultWriter, message);

            crossOverStrategy.setKnownConflicts(allConflictSetsWithoutCF);
            crossOverStrategy.setResultWriter(resultWriter);
            crossOverStrategy.setResolveStrategy(resolveStrategy);
            population = crossOverStrategy.crossover(population);

            for (IterationListener<Assignment, UserRequirement, Double> l : this.iterationListeners) {
                l.update(this);
            }
        }

        System.gc();

        // mutate
        if (mutationStrategy != null) {
            message = String.format("%sGENERATION %d: Mutating new generation ...", LoggerUtils.tab(), currentIteration);
            ConflictUtils.printMessage(resultWriter, message);

            if (population.size() > 0) {
                population = mutationStrategy.mutate(population);
            } else {
//                numNoConflict++;
//                if (numNoConflict > stopAfterXTimesNoConflict) {
//                    message = String.format("%sGENERATION %d: %d times new population without conflicts. Terminating.", LoggerUtils.tab(), currentIteration, numNoConflict);
//                    ConflictUtils.printMessage(resultWriter, message);
//                    terminate = true;
//                    return;
//                }

                population = Populations.newPopulations(populationSize, mutationStrategy);
            }

            for (IterationListener<Assignment, UserRequirement, Double> l : this.iterationListeners) {
                l.update(this);
            }
        }

        System.gc();

//        mutatedParents.trim(pupolationSize);

        // TODO: think about the stop condition
        if (newMinConflicts > 0) {
            numNoConflict = 0;
            currentGeneration++;
        } else {
            numNoConflict++;
            currentGeneration++;
            if (extinctAfterXTimesNoConflict > 0 && numNoConflict > extinctAfterXTimesNoConflict) {
                currentPopulation++;
                currentGeneration = 0;
                if (currentPopulation >= stopAfterXExtinctions) {
                    message = String.format("%sEXTINCTION %d: %d times without conflicts in %d iterations. Terminating.", LoggerUtils.tab(), currentPopulation, numNoConflict, currentIteration);
                    ConflictUtils.printMessage(resultWriter, message);
                    terminate = true;
                } else {
                    message = String.format("%sEXTINCTION %d: %d times without conflicts. Current iteration %d.", LoggerUtils.tab(), currentPopulation, numNoConflict, currentIteration);
                    ConflictUtils.printMessage(resultWriter, message);
                    population = Populations.newPopulations(populationSize, mutationStrategy);
                }
            } else if (stopAfterXTimesNoConflict > 0 && numNoConflict > stopAfterXTimesNoConflict) {
                message = String.format("%sGENERATION %d: %d times without conflicts. Terminating.", LoggerUtils.tab(), currentIteration, numNoConflict);
                ConflictUtils.printMessage(resultWriter, message);
                terminate = true;
            } else {
                message = String.format("%sGENERATION %d: No new minimal conflict sets found in this generation.", LoggerUtils.tab(), currentIteration);
                ConflictUtils.printMessage(resultWriter, message);
            }
        }
        currentIteration++; // next iteration
    }

    // TODO: this function can be used to identify all Conflicts
    private List<Set<Constraint>> identifyConflicts(Requirement ur) {
//        FMModelWithRequirement<Feature, AbstractRelationship<Feature>, CTConstraint> diagModel
//                = new FMModelWithRequirement<>(featureModel, ur, false, true, cfInConflicts, false);
//        diagModel.initialize();
        modelFactory.setRequirement(ur);
        val diagModel = modelFactory.createCDRModel();

        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(diagModel);

        Set<Constraint> C = diagModel.getPossiblyFaultyConstraints();
        Set<Constraint> B = diagModel.getCorrectConstraints();

        QuickXPlainParameters parameter = QuickXPlainParameters.builder()
                .C(C)
                .B(B).build();
        QuickXPlainLabeler quickXplain = new QuickXPlainLabeler(checker, parameter);

        HSDAG hsdag = new HSDAG(quickXplain);
        // hsdag.setNodeLabels(); // TODO: set existing conflict sets
        hsdag.setPruningEngine(new HSDAGPruningEngine(hsdag));
        if (numMaxConflicts != 0) {
            hsdag.setMaxNumberOfConflicts(numMaxConflicts);
        }

        CAEvaluator.reset();
        setCommonTimer(TIMER_SOLVER);
        setCommonTimer(TIMER_HS_CONSTRUCTION_SESSION);
        setCommonTimer(TIMER_NODE_LABEL);
        setCommonTimer(TIMER_PATH_LABEL);
        hsdag.construct();

        return hsdag.getConflicts();
    }

    private void printConflictInformation(List<Set<Constraint>> newConflictSets) {
        String message;
        message = String.format("%s=========================================", LoggerUtils.tab());
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%sConflict sets:", LoggerUtils.tab());
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%s%s", LoggerUtils.tab(), convertToStringWithMessage(newConflictSets, "Conflict", "\t", ", ", true));
        ConflictUtils.printMessage(resultWriter, message);

        message = String.format("%sNumber of conflicts: %d", LoggerUtils.tab(), newConflictSets.size());
        ConflictUtils.printMessage(resultWriter, message);
    }

    @Override
    public void evolve(int maxIterations) {
        this.terminate = false;

        currentIteration = 0;
        numNoConflict = 0;
        while (currentIteration < maxIterations) {
            if (this.terminate) {
                break;
            }

            this.evolve();
        }

        statisticsWriter.close();
    }

    public void addIterationListener(IterationListener<Assignment, UserRequirement, Double> listener) {
        this.iterationListeners.add(listener);
    }

    public void removeIterationListener(IterationListener<Assignment, UserRequirement, Double> listener) {
        this.iterationListeners.remove(listener);
    }
}
