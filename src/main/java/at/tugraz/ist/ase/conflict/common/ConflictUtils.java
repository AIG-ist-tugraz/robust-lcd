/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.common;

import at.tugraz.ist.ase.conflict.genetic.UserRequirement;
import at.tugraz.ist.ase.hiconfit.common.ConstraintUtils;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility methods for working with conflicts and user requirements.
 * Provides methods for printing messages, managing conflict set lists,
 * and checking relationships between user requirements and conflicts.
 */
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

    /**
     * Checks if ur1 is a subset of ur2
     * @param ur1 UserRequirement subset candidate
     * @param ur2 UserRequirement that may contain ur1
     * @return true if ur1 is subset or equal to ur2
     */
    public static boolean isSubset(UserRequirement ur1, UserRequirement ur2) {
        return  ur1.getAssignments().stream().noneMatch(assignment1 ->
                ur2.getAssignments().stream().noneMatch(assignment2 ->
                        Objects.equals(assignment1.toString(), assignment2.toString())));
    }
}
