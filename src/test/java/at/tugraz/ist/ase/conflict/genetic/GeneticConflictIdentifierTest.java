/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.HSDAG;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.HSDAGPruningEngine;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.labeler.QuickXPlainLabeler;
import at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.parameters.QuickXPlainParameters;
import at.tugraz.ist.ase.hiconfit.cacdr.checker.ChocoConsistencyChecker;
import at.tugraz.ist.ase.hiconfit.cacdr.eval.CAEvaluator;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Requirement;
import at.tugraz.ist.ase.hiconfit.cdrmodel.fm.FMRequirementCdrModel;
import at.tugraz.ist.ase.hiconfit.fm.factory.FeatureModels;
import at.tugraz.ist.ase.hiconfit.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static at.tugraz.ist.ase.hiconfit.cacdr.algorithms.hs.AbstractHSConstructor.*;
import static at.tugraz.ist.ase.hiconfit.cacdr.checker.ChocoConsistencyChecker.TIMER_SOLVER;
import static at.tugraz.ist.ase.hiconfit.eval.PerformanceEvaluator.setCommonTimer;

class GeneticConflictIdentifierTest {

    @Test
    void test() throws FeatureModelParserException {
        val file = new File("src/test/resources/linux-2.6.33.3.xml");
        val featureModel = FeatureModels.fromFile(file);

        // PANEL_LCD_HWIDTH=true --- FB_UDL=true
        Requirement requirement = Requirement.requirementBuilder()
                .assignments(List.of(Assignment.builder()
                                .variable("PANEL_LCD_HWIDTH")
                                .value("true").build(),
                                    Assignment.builder()
                                .variable("FB_UDL")
                                .value("true").build()))
                .build();

        val diagModel
                = new FMRequirementCdrModel<>(featureModel, requirement, false, true, false, false);
        diagModel.initialize();

        ChocoConsistencyChecker checker = new ChocoConsistencyChecker(diagModel);

        Set<Constraint> C = diagModel.getPossiblyFaultyConstraints();
        Set<Constraint> B = diagModel.getCorrectConstraints();

        QuickXPlainParameters parameter = QuickXPlainParameters.builder()
                .C(C)
                .B(B).build();
        QuickXPlainLabeler quickXplain = new QuickXPlainLabeler(checker, parameter);

        HSDAG hsdag = new HSDAG(quickXplain);
        hsdag.setPruningEngine(new HSDAGPruningEngine(hsdag));
        hsdag.setMaxNumberOfConflicts(5);

        CAEvaluator.reset();
        setCommonTimer(TIMER_SOLVER);
        setCommonTimer(TIMER_HS_CONSTRUCTION_SESSION);
        setCommonTimer(TIMER_NODE_LABEL);
        setCommonTimer(TIMER_PATH_LABEL);
        hsdag.construct();

        // print out
        hsdag.getConflicts().forEach(System.out::println);
    }

}