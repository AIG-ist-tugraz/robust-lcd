///*
// * Genetic Conflict Seeker
// *
// * Copyright (c) 2023
// *
// * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
// */
//
//package at.tugraz.ist.ase.conflict.kb;
//
//import at.tugraz.ist.ase.hiconfit.cacdr_core.Requirement;
//import at.tugraz.ist.ase.hiconfit.cacdr_core.translator.kb.KBSolutionTranslator;
//import at.tugraz.ist.ase.hiconfit.cdrmodel.AbstractCDRModel;
//import at.tugraz.ist.ase.hiconfit.cdrmodel.ICDRModelFactory;
//import at.tugraz.ist.ase.hiconfit.kb.core.KB;
//import lombok.Getter;
//import lombok.NonNull;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class KBCDRModelFactory implements ICDRModelFactory {
//
//    private @NonNull KB kb;
//    private Requirement requirement;
////    private boolean cfInConflicts;
//
//    public KBCDRModelFactory(@NonNull KB kb, Requirement requirement) {
//        this.kb = kb;
//        this.requirement = requirement;
////        this.cfInConflicts = cfInConflicts;
//    }
//
//
//    @Override
//    public AbstractCDRModel createModel() {
////        FMModelWithRequirement<Feature, AbstractRelationship<Feature>, CTConstraint> diagModel
////                = new FMModelWithRequirement<>(featureModel, requirement, false, true, cfInConflicts, false);
////        diagModel.initialize();
//
//        KBDiagnosisModel diagModel = new KBDiagnosisModel(kb, requirement, new KBSolutionTranslator(), false);
//        diagModel.initialize();
//
//        return diagModel;
//    }
//}
