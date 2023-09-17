/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic.resolve;

import at.tugraz.ist.ase.conflict.genetic.Individual;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;

import java.util.List;
import java.util.Set;

public interface IResolveStrategy<E, C extends Individual<E, C>> {
    List<C> resolve(C individual, List<Set<Constraint>> conflictsWithoutCF);
}
