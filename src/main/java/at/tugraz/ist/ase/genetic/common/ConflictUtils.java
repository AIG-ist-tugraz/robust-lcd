/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.genetic.common;

import at.tugraz.ist.ase.genetic.UserRequirement;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.QuickXPlain;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.HSDAG;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.HSDAGPruningEngine;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.labeler.QuickXPlainLabeler;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.parameters.QuickXPlainParameters;
import at.tugraz.ist.ase.hiconfit.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.hiconfit.cacdr.eval.CAEvaluator;
import at.tugraz.ist.ase.hiconfit.cdrmodel.AbstractCDRModel;
import at.tugraz.ist.ase.hiconfit.common.ChocoSolverUtils;
import at.tugraz.ist.ase.hiconfit.common.ConstraintUtils;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static at.tugraz.ist.ase.hiconfit.cacdr.algorithms.QuickXPlain.TIMER_QUICKXPLAIN;
import static at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.AbstractHSConstructor.*;
import static at.tugraz.ist.ase.hiconfit.cacdr.checker.ChocoConsistencyChecker.TIMER_SOLVER;
import static at.tugraz.ist.ase.hiconfit.cacdr.eval.CAEvaluator.COUNTER_CONSISTENCY_CHECKS;
import static at.tugraz.ist.ase.hiconfit.cacdr.eval.CAEvaluator.printPerformance;
import static at.tugraz.ist.ase.hiconfit.eval.PerformanceEvaluator.*;
import static java.lang.System.out;

@UtilityClass
public class ConflictUtils {
    public static void printMessage(BufferedWriter writer, String message) {
        System.out.println(message);
        if (writer != null) {
            try {
                writer.write(message); writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printConflictInformation(List<Set<Constraint>> conflicts) {
        out.println("\t\t=========================================");
        out.println("\t\tConflicts found by QuickXPlain:");
        out.println(ConstraintUtils.convertToStringWithMessage(conflicts,"Conflict","\t\t\t",", ",true));
        out.println("\t\tNumber of conflicts: " + conflicts.size());
//        printPerformance();
    }

    public Set<Constraint> findConflict(AbstractCDRModel diagModel) {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(diagModel);

        Set<Constraint> C = diagModel.getPossiblyFaultyConstraints();
        Set<Constraint> B = diagModel.getCorrectConstraints();

        // run the quickXPlain to find diagnoses
        QuickXPlain quickXPlain = new QuickXPlain(checker);

        reset();
        setCommonTimer(TIMER_QUICKXPLAIN);
        return quickXPlain.findConflictSet(C, B);
    }

    public List<Set<Constraint>> findConflicts(AbstractCDRModel diagModel, int numCS) {
        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(diagModel);

        Set<Constraint> C = diagModel.getPossiblyFaultyConstraints();
        Set<Constraint> B = diagModel.getCorrectConstraints();

        // run the hstree to find diagnoses
        QuickXPlainParameters parameter = QuickXPlainParameters.builder()
                .C(C)
                .B(B).build();
        QuickXPlainLabeler quickXplain = new QuickXPlainLabeler(checker, parameter);

        HSDAG hsdag = new HSDAG(quickXplain);
        hsdag.setPruningEngine(new HSDAGPruningEngine(hsdag));
        if (numCS != 0) {
            hsdag.setMaxNumberOfDiagnoses(numCS);
        }

        CAEvaluator.reset();
        setCommonTimer(TIMER_SOLVER);
        setCommonTimer(TIMER_HS_CONSTRUCTION_SESSION);
        setCommonTimer(TIMER_NODE_LABEL);
        setCommonTimer(TIMER_PATH_LABEL);
        hsdag.construct();

//        List<Set<Constraint>> allDiagnoses = hsdag.getDiagnoses();
        return hsdag.getConflicts();
    }

    public void addCSToCSList(Set<Constraint> aCstrSet, List<Set<Constraint>> aListOfCstrSets,
                              BufferedWriter allCstrSetsWriter, BufferedWriter resultWriter) {
        if (!ConstraintUtils.containsAll(aListOfCstrSets, aCstrSet)) {
            aListOfCstrSets.add(aCstrSet);
            if (allCstrSetsWriter != null) {
                try {
                    allCstrSetsWriter.write(ConstraintUtils.convertToString(aCstrSet, " --- ", null, false)); allCstrSetsWriter.newLine();
                    allCstrSetsWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            LoggerUtils.indent();
            val message = String.format("%s%s", LoggerUtils.tab(), "ALREADY - " + ConstraintUtils.convertToString(aCstrSet, ", ", null, true));
            printMessage(resultWriter, message);
            LoggerUtils.outdent();
        }
    }

    public void saveConflict(BufferedWriter combWriter,
                             String conflictFile,
                             String value_combination,
                             List<Set<Constraint>> CSs,
                             double complexity) throws IOException {
        BufferedWriter cdWriter = new BufferedWriter(new FileWriter(conflictFile));
        cdWriter.write(value_combination); cdWriter.newLine();
        cdWriter.flush();

        cdWriter.write("Conflicts:"); cdWriter.newLine();
        for (Set<Constraint> cstrSet : CSs) {
            cdWriter.write(ConstraintUtils.convertToString(cstrSet," --- ", null, false)); cdWriter.newLine();
        }
        cdWriter.write("Choco constraints of conflicts:"); cdWriter.newLine();
        for (Set<Constraint> cstrSet : CSs) {
            cdWriter.write(ChocoSolverUtils.convertToString(cstrSet.stream().map(Constraint::getChocoConstraints).toList(), " --- ", false)); cdWriter.newLine();
        }
        cdWriter.write("Cardinality: " + CSs.get(0).size()); cdWriter.newLine();
        cdWriter.write("Complexity: " + complexity); cdWriter.newLine();
        printPerformance(cdWriter);

        combWriter.write(" - " + CSs.get(0).size());
//        Timer timer = getTimer(TIMER_QUICKXPLAIN + "[thread=1]");
        combWriter.write(" - " + getTimer(TIMER_QUICKXPLAIN + "[thread=1]").getTimings().get(0) / 1000000000.0);
//        combWriter.write(" - " + totalCommonTimer(TIMER_QUICKXPLAIN) / 1000000000.0);
        combWriter.write(" - " + getCounter(COUNTER_CONSISTENCY_CHECKS));
        combWriter.write(" - " + complexity);
        combWriter.write(" - " + conflictFile);
        combWriter.flush();

        cdWriter.close();
    }

    public double conflictAnalysis(Set<Constraint> cs, Set<Constraint> C) {
        List<Constraint> listCS = Lists.newArrayList(cs);
        List<Constraint> listC = Lists.newArrayList(C);

        double complexity = 0.0;
        for (Constraint c : listCS) {
            if (complexity + Math.log(listC.indexOf(c)) > Double.MAX_VALUE) {
                complexity = Double.MAX_VALUE;
                break;
            }
            complexity += Math.log(listC.indexOf(c));
        }

        return complexity;
    }

//    public String convertConstraintsToLine(Set<Constraint> ac) {
//        return Joiner.on(" --- ").join(ac);
//    }

//    public String convertChocoConstraintsToLine(Set<Constraint> ac) {
//        return Joiner.on(" --- ").join(ac.stream().map(Constraint::getChocoConstraints).toArray());
//    }
//
//    public String convertToString(@NonNull List<List<org.chocosolver.solver.constraints.Constraint>> ac, @NonNull String delimiter, boolean brackets) {
//        String ex = Joiner.on(delimiter).join(ac);
////        String ex = ac.stream().map(Constraint::toString).collect(Collectors.joining(delimiter));
//        return brackets ? "[" + ex + "]" : ex;
//    }

//    public boolean existInAllConflictSet(List<Set<Constraint>> allConflictSet, Set<Constraint> conflict) {
//        for (Set<Constraint> cs : allConflictSet) {
//            if ((cs.size() == conflict.size()) && cs.containsAll(conflict)) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static boolean hasKnownConflict(UserRequirement userRequirement, List<Set<Constraint>> knownConflicts) {
        /*for (Set<Constraint> conflict : knownConflicts) {
            if (containsAll(userRequirement, conflict)) {
                return true;
            }
        }
        return false;*/
        return knownConflicts.stream().anyMatch(conflict -> containsAll(userRequirement, conflict));
    }

    public static boolean containsAll(UserRequirement ur, Set<Constraint> conflict) {
        /*for (Constraint c: conflict) {
            boolean found = false;
            for (Assignment assignment : ur.getAssignments()) {
                if (Objects.equals(assignment.toString(), c.toString())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;*/
        return conflict.stream().noneMatch(c -> ur.getAssignments().stream().noneMatch(assignment -> Objects.equals(assignment.toString(), c.toString())));
    }
}
