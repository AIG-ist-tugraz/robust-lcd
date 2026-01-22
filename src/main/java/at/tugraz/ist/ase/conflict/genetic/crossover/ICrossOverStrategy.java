/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023-2026
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic.crossover;

import at.tugraz.ist.ase.conflict.genetic.Individual;
import at.tugraz.ist.ase.conflict.genetic.Population;
import at.tugraz.ist.ase.conflict.genetic.resolve.IResolveStrategy;
import at.tugraz.ist.ase.hiconfit.kb.core.Constraint;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Set;

/**
 * Strategy interface for genetic crossover operations.
 * Implementations define how two parent individuals are combined to produce offspring.
 *
 * @param <E> the element type
 * @param <C> the individual type
 */
public interface ICrossOverStrategy<E, C extends Individual<E, C>> {
    C crossover(C i1, C i2);

    Population<E, C> crossover(Population<E, C> parents);

    void setResultWriter(BufferedWriter resultWriter);

    void setResolveStrategy(IResolveStrategy<E, C> resolveStrategy);

    void setKnownConflicts(List<Set<Constraint>> knownConflicts);
}
