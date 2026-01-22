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
 * Basic conflict resolution strategy for user requirements.
 * <p>
 * When a conflict is detected, this strategy creates multiple resolved variants
 * by removing each conflicting constraint one at a time. This generates all
 * possible minimal resolutions of the conflict.
 */
public class URResolveStrategy implements IResolveStrategy<Assignment, UserRequirement> {

    /*
    @Override
    public List<UserRequirement> resolve(UserRequirement individual, List<Set<Constraint>> conflictsWithoutCF) {
        List<UserRequirement> resolvedParents = new ArrayList<>();
        resolvedParents.add(individual);

        if (conflictsWithoutCF == null || conflictsWithoutCF.isEmpty()) {
            resolvedParents.add(individual);
        } else {

            for (Set<Constraint> conflict : conflictsWithoutCF) {

                List<UserRequirement> newResolvedParents = new ArrayList<>();
                for (UserRequirement ur: resolvedParents) {
                    if (ConflictUtils.containsAll(ur, conflict)) {
                        for (Constraint constraint : conflict) {
                            List<Assignment> assignments = new ArrayList<>();

                            for (Assignment assignment : ur.getAssignments()) {
                                if (!Objects.equals(assignment.toString(), constraint.toString())) {
                                    assignments.add(assignment);
                                }
                            }

                            UserRequirement resolvedParent = UserRequirement.requirementBuilder()
                                    .assignments(assignments)
                                    .build();
                            newResolvedParents.add(resolvedParent);
                        }
                    }
                }
                resolvedParents = newResolvedParents;
            }
        }

        return resolvedParents;
    }
    */


    @Override
    public List<UserRequirement> resolve(UserRequirement individual, List<Set<Constraint>> conflictsWithoutCF) {
        List<UserRequirement> resolvedParents = new ArrayList<>();
        resolvedParents.add(individual);

        if (conflictsWithoutCF == null || conflictsWithoutCF.isEmpty()) {
            resolvedParents.add(individual);
        } else {

            for (Set<Constraint> conflict : conflictsWithoutCF) {
                resolvedParents = resolveConflict(resolvedParents, conflict);
            }
        }

        return resolvedParents;
    }

    private List<UserRequirement> resolveConflict(List<UserRequirement> userReqList, Set<Constraint> conflict){
        List<UserRequirement> resolvedList = new ArrayList<>();

        for (UserRequirement individual : userReqList) {
            if (ConflictUtils.containsAll(individual, conflict)) {
                for (Constraint constraint : conflict) {
                    List<Assignment> assignments = new ArrayList<>();

                    for (Assignment assignment : individual.getAssignments()) {
                        if (!Objects.equals(assignment.toString(), constraint.toString())) {
                            assignments.add(assignment);
                        }
                    }

                    UserRequirement resolvedIndividual = UserRequirement.requirementBuilder()
                            .assignments(assignments)
                            .build();

                    resolvedList.add(resolvedIndividual);
                }
            }
            else {
                resolvedList.add(individual);
            }
        }

        return resolvedList;
    }

    private boolean alreadyInList(List<UserRequirement> userReqList, UserRequirement individual) {
        boolean inList = false;
        for (UserRequirement ref : userReqList) {
            if (ConflictUtils.isSubset(individual, ref)) {
                inList = true;
                break;
            }
        }
        assert inList == userReqList.stream().noneMatch(ref -> ConflictUtils.isSubset(individual, ref));
        return inList;
    }
}
