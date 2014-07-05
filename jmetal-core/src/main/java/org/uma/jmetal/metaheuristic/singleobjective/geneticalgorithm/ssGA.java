//  ssGAS.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.uma.jmetal.metaheuristic.singleobjective.geneticalgorithm;

import org.uma.jmetal.core.*;
import org.uma.jmetal.operator.selection.WorstSolutionSelection;
import org.uma.jmetal.util.Configuration;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Class implementing a steady-state genetic algorithm
 */
public class ssGA extends Algorithm {
  private static final long serialVersionUID = -6340093758636629106L;

  /** Constructor */
  public ssGA() {
    super();
  }

  /**
   * Execute the SSGA algorithm
   *
   * @throws org.uma.jmetal.util.JMetalException
   */
  public SolutionSet execute() throws JMetalException, ClassNotFoundException {
    int populationSize;
    int maxEvaluations;
    int evaluations;

    SolutionSet population;
    Operator mutationOperator;
    Operator crossoverOperator;
    Operator selectionOperator;

    Comparator<Solution> comparator;

    comparator = new ObjectiveComparator(0);

    HashMap<String, Object> selectionParameters = new HashMap<String, Object>();
    selectionParameters.put("comparator", comparator);

    Operator findWorstSolution = new WorstSolutionSelection(selectionParameters);

    // Read the parameters
    populationSize = (Integer) this.getInputParameter("populationSize");
    maxEvaluations = (Integer) this.getInputParameter("maxEvaluations");

    // Initialize the variables
    population = new SolutionSet(populationSize);
    evaluations = 0;

    // Read the operator
    mutationOperator = this.operators_.get("mutation");
    crossoverOperator = this.operators_.get("crossover");
    selectionOperator = this.operators_.get("selection");

    // Create the initial population
    Solution newIndividual;
    for (int i = 0; i < populationSize; i++) {
      newIndividual = new Solution(problem_);
      problem_.evaluate(newIndividual);
      evaluations++;
      population.add(newIndividual);
    }

    // main loop
    while (evaluations < maxEvaluations) {
      Solution[] parents = new Solution[2];

      // Selection
      parents[0] = (Solution) selectionOperator.execute(population);
      parents[1] = (Solution) selectionOperator.execute(population);

      // Crossover
      Solution[] offspring = (Solution[]) crossoverOperator.execute(parents);

      // Mutation
      mutationOperator.execute(offspring[0]);

      // Evaluation of the new individual
      problem_.evaluate(offspring[0]);

      evaluations++;

      // Replacement: replace the last individual is the new one is better
      int worstIndividual = (Integer) findWorstSolution.execute(population);

      if (comparator.compare(population.get(worstIndividual), offspring[0]) > 0) {
        population.remove(worstIndividual);
        population.add(offspring[0]);
      }
    }

    // Return a population with the best individual


    SolutionSet resultPopulation = new SolutionSet(1);
    resultPopulation.add(population.best(comparator));

    Configuration.logger_.info("Evaluations: " + evaluations);

    return resultPopulation;
  }
}
