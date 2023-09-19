/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.conflict.genetic.mutate;

import at.tugraz.ist.ase.conflict.genetic.Individual;
import at.tugraz.ist.ase.conflict.genetic.Population;

public interface IMutationStrategy<E, C extends Individual<E, C>> {
    C mutate();
    C mutate(C individual);

    Population<E, C> mutate(Population<E, C> parents);
}
