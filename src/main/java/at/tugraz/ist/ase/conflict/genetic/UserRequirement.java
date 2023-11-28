/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic;

import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Solution;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Iterator;
import java.util.List;

public class UserRequirement extends Solution implements Individual<Assignment, UserRequirement> {

    /**
     * weight of the resolved UR -> the more conflicts where identified in the original UR, the higher the weight
     */
    @Getter
    @Builder.Default
    private int weight = 0;

    /**
     * ID to identify resolved UR of the same predecessor UR
     */
    @Getter@Builder.Default
    private int resolvedID = 0;

    @Builder(builderMethodName = "requirementBuilder")
    public UserRequirement(@NonNull List<Assignment> assignments, int weight, int resolvedID) {
        super(assignments);
        this.weight = weight;
        this.resolvedID = resolvedID;
    }

    public UserRequirement clone() throws CloneNotSupportedException {
        UserRequirement clone = (UserRequirement) super.clone();
        clone.weight = this.weight;
        clone.resolvedID = this.resolvedID;
        return clone;
    }

    @Override
    public Iterator<Assignment> iterator() {
        return this.assignments.iterator();
    }
}
