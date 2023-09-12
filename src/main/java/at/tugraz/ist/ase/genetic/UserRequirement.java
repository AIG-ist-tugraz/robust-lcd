/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.genetic;

import at.tugraz.ist.ase.hiconfit.cacdr_core.Assignment;
import at.tugraz.ist.ase.hiconfit.cacdr_core.Solution;
import lombok.Builder;
import lombok.NonNull;

import java.util.Iterator;
import java.util.List;

public class UserRequirement extends Solution implements Individual<Assignment, UserRequirement> {

    @Builder(builderMethodName = "requirementBuilder")
    public UserRequirement(@NonNull List<Assignment> assignments) {
        super(assignments);
    }

    public UserRequirement clone() throws CloneNotSupportedException {
        return (UserRequirement) super.clone();
    }

    @Override
    public Iterator<Assignment> iterator() {
        return this.assignments.iterator();
    }
}
