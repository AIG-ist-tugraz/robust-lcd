/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.fm;

import at.tugraz.ist.ase.conflict.model.CDRModelFactory;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Requirement;
import at.tugraz.ist.ase.hiconfit.cdrmodel.AbstractCDRModel;
import at.tugraz.ist.ase.hiconfit.cdrmodel.fm.FMModelWithRequirement;
import at.tugraz.ist.ase.hiconfit.fm.core.AbstractRelationship;
import at.tugraz.ist.ase.hiconfit.fm.core.CTConstraint;
import at.tugraz.ist.ase.hiconfit.fm.core.Feature;
import at.tugraz.ist.ase.hiconfit.fm.core.FeatureModel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class FMCDRModelFactory implements CDRModelFactory {

    private @NonNull FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel;
    private Requirement requirement;
    private boolean cfInConflicts;

    public FMCDRModelFactory(@NonNull FeatureModel<Feature, AbstractRelationship<Feature>, CTConstraint> featureModel,
                             Requirement requirement,
                             boolean cfInConflicts) {
        this.featureModel = featureModel;
        this.requirement = requirement;
        this.cfInConflicts = cfInConflicts;
    }

    @Override
    public AbstractCDRModel createCDRModel() {
        FMModelWithRequirement<Feature, AbstractRelationship<Feature>, CTConstraint> diagModel
                = new FMModelWithRequirement<>(featureModel, requirement, false, true, cfInConflicts, false);
        diagModel.initialize();

        return diagModel;
    }
}
