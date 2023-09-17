/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.kb;

import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Solution;
import at.tugraz.ist.ase.hiconfit.cacdr_core.translator.ISolutionTranslatable;
import at.tugraz.ist.ase.hiconfit.common.ChocoSolverUtils;
import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import at.tugraz.ist.ase.hiconfit.kb.core.IIntVarKB;
import at.tugraz.ist.ase.hiconfit.kb.core.KB;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.chocosolver.solver.variables.IntVar;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class SolutionTranslator implements ISolutionTranslatable {
    /**
     * Translates a Camera solution to Constraint
     */
    @Override
    public Constraint translate(@NonNull Solution solution, @NonNull KB kb) {
        IIntVarKB iIntVarKB = (IIntVarKB) kb;

        log.trace("{}Translating solution [solution={}] >>>", LoggerUtils.tab(), solution);
        int startIdx = kb.getNumChocoConstraints();

        int counter = 0;
        for (Assignment assign: solution.getAssignments()) {
            String varName = assign.getVariable();
            IntVar var = iIntVarKB.getIntVar(varName);
            String value = assign.getValue();
            int chocoValue = iIntVarKB.getIntValue(varName, value);

            if (isCorrectAssignment(varName, var, value, chocoValue)) {
                counter++;
                kb.getModelKB().arithm(var, "=", chocoValue).post();
            }
        }

        assert  (solution.size() - solution.getNumNULL()) == counter : "not equal";

        // add the translated constraints to a Constraint
        Constraint constraint = new Constraint(solution.toString());
        if (counter > 0) {
            constraint.getChocoConstraints().addAll(ChocoSolverUtils.getConstraints(kb.getModelKB(), startIdx, kb.getNumChocoConstraints() - 1));
//            constraint.addChocoConstraints(iIntVarKB.getModelKB(), startIdx, iIntVarKB.getNumChocoConstraints() - 1, false);
        }

        // remove the translated constraints from the Choco model
        kb.getModelKB().unpost(kb.getModelKB().getCstrs());

        log.debug("{}Translated solution [solution={}] >>>", LoggerUtils.tab(), solution);
        return constraint;
    }

    /**
     * Translates a Siemens solution to a list of Constraints
     */
    @Override
    public List<Constraint> translateToList(@NonNull Solution solution, @NonNull KB kb) {
        IIntVarKB iIntVarKB = (IIntVarKB) kb;

        log.trace("{}Translating solution [solution={}] >>>", LoggerUtils.tab(), solution);
        List<Constraint> constraints = new LinkedList<>();

        int counter = 0;
        for (Assignment assign: solution.getAssignments()) {
            String varName = assign.getVariable();
            IntVar var = iIntVarKB.getIntVar(varName);
            String value = assign.getValue();
            int chocoValue = iIntVarKB.getIntValue(varName, value);

            if (isCorrectAssignment(varName, var, value, chocoValue)) {
                counter++;

                int startIdx = kb.getNumChocoConstraints();
                kb.getModelKB().arithm(var, "=", chocoValue).post();

                // add the translated constraints to a Constraint
                Constraint constraint = new Constraint(assign.toString());
                constraint.getChocoConstraints().addAll(ChocoSolverUtils.getConstraints(kb.getModelKB(), startIdx, kb.getNumChocoConstraints() - 1));
//                constraint.addChocoConstraints(iIntVarKB.getModelKB(), startIdx, iIntVarKB.getNumChocoConstraints() - 1, false);

                // add to the list of constraints
                constraints.add(constraint);
            }
        }

        assert  (solution.size() - solution.getNumNULL()) == counter : "not equal";

        // remove the translated constraints from the Choco model
        kb.getModelKB().unpost(kb.getModelKB().getCstrs());

        log.debug("{}Translated solution [solution={}] >>>", LoggerUtils.tab(), solution);
        return constraints;
    }

    private boolean isCorrectAssignment(String varName, IntVar var, String value, int chocoValue) {
        return var != null && (!value.equals("NULL"))
                && (chocoValue != -1);
    }
}
