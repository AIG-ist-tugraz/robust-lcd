///*
// * Genetic Conflict Seeker
// *
// * Copyright (c) 2022-2023
// *
// * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
// */
//
//package at.tugraz.ist.ase.conflict.kb;
//
//import at.tugraz.ist.ase.hiconfit.cacdr_core.Solution;
//import at.tugraz.ist.ase.hiconfit.cacdr_core.translator.ISolutionTranslatable;
//import at.tugraz.ist.ase.hiconfit.cdrmodel.AbstractCDRModel;
//import at.tugraz.ist.ase.hiconfit.cdrmodel.IChocoModel;
//import at.tugraz.ist.ase.hiconfit.common.LoggerUtils;
//import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;
//import at.tugraz.ist.ase.hiconfit.kb.core.KB;
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.chocosolver.solver.Model;
//
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//
//@Slf4j
//public class KBDiagnosisModel extends AbstractCDRModel implements IChocoModel {
//    @Getter
//    private final Model model;
//    private final KB kb;
//
//    @Getter
//    private final boolean reversedConstraintsOrder;
//
//    private final Solution solution;
//    private final ISolutionTranslatable translator;
//
//    public KBDiagnosisModel(@NonNull KB kb, @NonNull Solution solution, @NonNull ISolutionTranslatable translator,
//                            boolean reversedConstraintsOrder) {
//        super("KBDiagnosisModel");
//
////        this.kb = new CameraKB(false);
//        this.kb = kb;
//        this.model = kb.getModelKB();
//
//        this.reversedConstraintsOrder = reversedConstraintsOrder;
//
//        this.solution = solution;
//        this.translator = translator;
//    }
//
//    @Override
//    public void initialize() {
//        log.debug("{}Initializing CameraDiagnosisModel for {} >>>", LoggerUtils.tab(), getName());
//        LoggerUtils.indent();
//
//        // sets possibly faulty constraints to super class
//        log.trace("{}Adding possibly faulty constraints", LoggerUtils.tab());
//        List<Constraint> C = translator.translateToList(solution, kb);
//        if (isReversedConstraintsOrder()) {
//            Collections.reverse(C); // in default, this shouldn't happen
//        }
//        this.setPossiblyFaultyConstraints(C);
//
//        // sets correct constraints to super class
//        log.trace("{}Adding correct constraints", LoggerUtils.tab());
//        List<Constraint> B = new LinkedList<>(kb.getConstraintList());
//        this.setCorrectConstraints(B);
//
//        // remove all Choco constraints, cause we just need variables and test cases
//        model.unpost(model.getCstrs());
//
//        LoggerUtils.outdent();
//        log.debug("{}<<< Model {} initialized", LoggerUtils.tab(), getName());
//    }
//}
//
