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

package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.Problem;

/**
 * Builder class for algorithm MOEA/D
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class MOEADBuilder {
  protected Problem problem ;

  /** T in Zhang & Li paper */
  protected int neighborSize;
  /** Delta in Zhang & Li paper */
  protected double neighborhoodSelectionProbability;
  /** nr in Zhang & Li paper */
  protected int maximumNumberOfReplacedSolutions;

  protected MOEAD.FunctionType functionType;

  protected CrossoverOperator crossover;
  protected MutationOperator mutation;
  protected String dataDirectory;

  protected int populationSize;
  protected int resultPopulationSize ;

  protected int maxEvaluations;

  protected int numberOfThreads ;

  /** Constructor */
  public MOEADBuilder(Problem problem) {
    this.problem = problem ;
    populationSize = 300 ;
    resultPopulationSize = 300 ;
    maxEvaluations = 150000 ;
    crossover = new DifferentialEvolutionCrossover() ;
    mutation = new PolynomialMutation(1.0/problem.getNumberOfVariables(), 20.0);
    functionType = MOEAD.FunctionType.TCHE ;
    neighborhoodSelectionProbability = 0.1 ;
    maximumNumberOfReplacedSolutions = 2 ;
    dataDirectory = "" ;
    neighborSize = 20 ;
    numberOfThreads = 1 ;
  }

  /* Getters/Setters */
  public int getNeighborSize() {
    return neighborSize;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

  public int getPopulationSize() {
    return populationSize;
  }

  public int getResultPopulationSize() {
    return resultPopulationSize;
  }

  public String getDataDirectory() {
    return dataDirectory;
  }

  public MutationOperator getMutation() {
    return mutation;
  }

  public CrossoverOperator getCrossover() {
    return crossover;
  }

  public MOEAD.FunctionType getFunctionType() {
    return functionType;
  }

  public int getMaximumNumberOfReplacedSolutions() {
    return maximumNumberOfReplacedSolutions;
  }

  public double getNeighborhoodSelectionProbability() {
    return neighborhoodSelectionProbability;
  }

  public int getNumberOfThreads() {
    return numberOfThreads ;
  }

  public MOEADBuilder setPopulationSize(int populationSize) {
    this.populationSize = populationSize;

    return this;
  }

  public MOEADBuilder setResultPopulationSize(int resultPopulationSize) {
    this.resultPopulationSize = resultPopulationSize;

    return this;
  }

  public MOEADBuilder setMaxEvaluations(int maxEvaluations) {
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public MOEADBuilder setNeighborSize(int neighborSize) {
    this.neighborSize = neighborSize ;

    return this ;
  }

  public MOEADBuilder setNeighborhoodSelectionProbability(double neighborhoodSelectionProbability) {
    this.neighborhoodSelectionProbability = neighborhoodSelectionProbability ;

    return this ;
  }

  public MOEADBuilder setFunctionType(MOEAD.FunctionType functionType) {
    this.functionType = functionType ;

    return this ;
  }

  public MOEADBuilder setMaximumNumberOfReplacedSolutions(int maximumNumberOfReplacedSolutions) {
    this.maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutions ;

    return this ;
  }

  public MOEADBuilder setCrossover(CrossoverOperator crossover) {
    this.crossover = crossover ;

    return this ;
  }

  public MOEADBuilder setMutation(MutationOperator mutation) {
    this.mutation = mutation ;

    return this ;
  }

  public MOEADBuilder setDataDirectory(String dataDirectory) {
    this.dataDirectory = dataDirectory ;

    return this ;
  }

  public MOEADBuilder setNumberOfThreads(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads ;

    return this ;
  }

  public Algorithm build(String moeadVariant) {
    return new MOEAD(problem, populationSize, resultPopulationSize, maxEvaluations,
        mutation, functionType, dataDirectory, neighborhoodSelectionProbability,
        maximumNumberOfReplacedSolutions, neighborSize) ;
  }
}
