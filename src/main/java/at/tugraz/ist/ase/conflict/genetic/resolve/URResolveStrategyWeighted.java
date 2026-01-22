/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic.resolve;

import at.tugraz.ist.ase.conflict.common.ConflictUtils;
import at.tugraz.ist.ase.conflict.genetic.UserRequirement;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Weighted conflict resolution strategy for user requirements.
 * <p>
 * Extends the basic resolution strategy by assigning weights to resolved requirements
 * based on the number of conflicts found. Requirements with higher weights (more conflicts
 * resolved) are given priority during crossover selection.
 * <p>
 * Also assigns unique IDs to resolved requirements from the same parent to enable
 * the "avoid same original conflict" feature during crossover.
 */
public class URResolveStrategyWeighted implements IResolveStrategy<Assignment, UserRequirement> {

    private int idCounter = 0;

    @Override
    public List<UserRequirement> resolve(UserRequirement individual, List<Set<Constraint>> conflictsWithoutCF) {
        List<UserRequirement> resolvedParents = new ArrayList<>();
        List<Set<Constraint>> conflicts = getAllConflicts(individual, conflictsWithoutCF);

        if (conflictsWithoutCF.isEmpty() || conflicts.isEmpty()) {
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
