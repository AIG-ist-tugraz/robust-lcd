/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
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

/**
 * Represents a user requirement as an individual in the genetic algorithm population.
 * <p>
 * A user requirement consists of a list of feature assignments (e.g., "Feature1=true").
 * It extends {@link Solution} and implements {@link Individual} for use in the GA.
 * <p>
 * When conflicts are identified and resolved, the weight and resolvedID fields track
 * the origin and importance of resolved requirements for weighted crossover operations.
 */
@Getter
public class UserRequirement extends Solution implements Individual<Assignment, UserRequirement> {

    /**
     * weight of the resolved UR -> the more conflicts where identified in the original UR, the higher the weight
     */
    private int weight = 0;

    /**
     * ID to identify resolved UR of the same predecessor UR
     */
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
