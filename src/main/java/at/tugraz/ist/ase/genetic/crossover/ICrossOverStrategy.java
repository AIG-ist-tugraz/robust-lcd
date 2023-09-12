/*
 * Genetic Conflict Seeker
 *
 * Copyright (c) 2023
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.genetic.crossover;

import at.tugraz.ist.ase.genetic.Individual;
import at.tugraz.ist.ase.genetic.Population;

import java.io.BufferedWriter;

public interface ICrossOverStrategy<E, C extends Individual<E, C>> {
    C crossover(C i1, C i2);

    Population<E, C> crossover(Population<E, C> parents);

    void setResultWriter(BufferedWriter resultWriter);
}
