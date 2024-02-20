/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.common;

import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class ConflictSetBuilder {
    /**
     * Example of a requirement: "B+ Tree=false --- Unindexed=false"
     * @param stringUR a string of requirements
     */
    public Set<Constraint> build(@NonNull String stringUR) {
        log.trace("{}Building conflictSet from [ur={}] >>>", LoggerUtils.tab(), stringUR);
        LoggerUtils.indent();

        Set<Constraint> constraints = new LinkedHashSet<>();

        String[] tokens = stringUR.split(" --- ");

        for (String token : tokens) {
            Constraint c = Constraint.builder()
                    .constraint(token)
                    .variables(Collections.emptyList())
                    .build();

            constraints.add(c);
        }

//        if (reverse) {
//            Collections.reverse(assignments);
//            Collections.reverse(constraints);
//        }

//        ConflictSet conflictSet = ConflictSet.builder()
//                .assignments(assignments)
//                .constraints(constraints)
//                .build();
//
//        // set bitmap
//        if (kbIndex != null) {
//            conflictSet.setBitmap(kbIndex.getBitmap(conflictSet));
//        }

        LoggerUtils.outdent();
        log.trace("{}Built a conflictSet [ur={}]", LoggerUtils.tab(), constraints);

        return constraints;
    }
}
