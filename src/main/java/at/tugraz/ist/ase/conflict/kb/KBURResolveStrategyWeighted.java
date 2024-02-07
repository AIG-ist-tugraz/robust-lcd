/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.kb;

import at.tugraz.ist.ase.conflict.common.ConflictUtils;
import at.tugraz.ist.ase.conflict.genetic.UserRequirement;
import at.tugraz.ist.ase.conflict.genetic.resolve.IResolveStrategy;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KBURResolveStrategyWeighted implements IResolveStrategy<Assignment, UserRequirement> {

    private int idCounter = 0;

    @Override
    public List<UserRequirement> resolve(UserRequirement individual, List<Set<Constraint>> conflictsWithoutCF) {
        List<UserRequirement> resolvedParents = new ArrayList<>();
        List<Set<Constraint>> conflicts = getAllConflicts(individual, conflictsWithoutCF);

        if (conflictsWithoutCF == null || conflictsWithoutCF.isEmpty() || conflicts.isEmpty()) {
            resolvedParents.add(individual);
        }
        else {
            idCounter++;
            List<UserRequirement> akkUR = new ArrayList<>();
            akkUR.add(individual);

            for (Set<Constraint> conflict : conflicts) {
                List<UserRequirement> tempUR = new ArrayList<>();
                for (UserRequirement ur : akkUR) {
                    for (Constraint constraint : conflict) {
                        List<Assignment> assignments = removeConstraint(ur, constraint);
                        UserRequirement newUR = UserRequirement.requirementBuilder()
                                .resolvedID(idCounter)
                                .weight(conflicts.size())
                                .assignments(assignments)
                                .build();

                        tempUR.add(newUR);
                    }
                }
                akkUR = tempUR;
            }

            resolvedParents = akkUR;
        }

        return resolvedParents;
    }

    private List<Set<Constraint>> getAllConflicts(UserRequirement userRequirement, List<Set<Constraint>> conflictsWithoutCF){
        List<Set<Constraint>> allConflicts = new ArrayList<>();

        for (Set<Constraint> conflict : conflictsWithoutCF){
            if (ConflictUtils.containsAll(userRequirement, conflict)) {
                allConflicts.add(conflict);
            }
        }

        return allConflicts;
    }

    private List<Assignment> removeConstraint(UserRequirement userRequirement, Constraint constraint){
        List<Assignment> assignments = new ArrayList<>();
        for (Assignment assignment : userRequirement.getAssignments()) {
            if (!Objects.equals(assignment.toString(), constraint.toString())) {
                assignments.add(assignment);
            }
        }
        return assignments;
    }
}
