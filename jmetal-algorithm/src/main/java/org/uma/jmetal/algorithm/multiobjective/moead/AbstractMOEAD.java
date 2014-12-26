package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by ajnebro on 26/12/14.
 */
public abstract class AbstractMOEAD<S extends Solution> implements Algorithm<List<Solution>> {
  protected enum NeighborType {NEIGHBOR, POPULATION}
  protected enum FunctionType {TCHE, PBI, AGG}

  protected Problem problem ;

  /** Z vector in Zhang & Li paper */
  protected double[] idealPoint;
  /** Lambda vectors */
  protected double[][] lambda;
  /** T in Zhang & Li paper */
  protected int neighborSize;
  protected int[][] neighborhood;
  /** Delta in Zhang & Li paper */
  protected double neighborhoodSelectionProbability;
  /** nr in Zhang & Li paper */
  protected int maximumNumberOfReplacedSolutions;

  protected Solution[] indArray;
  protected FunctionType functionType;

  protected String dataDirectory;

  protected List<S> population;
  protected int populationSize;
  protected int resultPopulationSize ;

  protected int evaluations;
  protected int maxEvaluations;

  protected JMetalRandom randomGenerator ;

  protected CrossoverOperator crossoverOperator ;
  protected MutationOperator mutationOperator ;

  protected int numberOfThreads ;

  public AbstractMOEAD(Problem problem, int populationSize, int resultPopulationSize,
      int maxEvaluations, MutationOperator mutation,
      FunctionType functionType, String dataDirectory, double neighborhoodSelectionProbability,
      int maximumNumberOfReplacedSolutions, int neighborSize) {
    this.problem = problem ;
    this.populationSize = populationSize ;
    this.resultPopulationSize = resultPopulationSize ;
    this.maxEvaluations = maxEvaluations ;
    this.mutationOperator = mutation ;
    this.functionType = functionType ;
    this.neighborhoodSelectionProbability = neighborhoodSelectionProbability ;
    this.maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutions ;
    this.neighborSize = neighborSize ;

    crossoverOperator = null ;
    randomGenerator = JMetalRandom.getInstance() ;
  }

  /**
   * Initialize weight vectors
   */
  protected void initializeUniformWeight() {
    if ((problem.getNumberOfObjectives() == 2) && (populationSize <= 300)) {
      for (int n = 0; n < populationSize; n++) {
        double a = 1.0 * n / (populationSize - 1);
        lambda[n][0] = a;
        lambda[n][1] = 1 - a;
      }
    } else {
      String dataFileName;
      dataFileName = "W" + problem.getNumberOfObjectives() + "D_" +
          populationSize + ".dat";

      try {
        FileInputStream fis =
            new FileInputStream(
                this.getClass().getClassLoader().getResource(dataDirectory + "/" + dataFileName)
                    .getPath());
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        int i = 0;
        int j = 0;
        String aux = br.readLine();
        while (aux != null) {
          StringTokenizer st = new StringTokenizer(aux);
          j = 0;
          while (st.hasMoreTokens()) {
            double value = new Double(st.nextToken());
            lambda[i][j] = value;
            j++;
          }
          aux = br.readLine();
          i++;
        }
        br.close();
      } catch (Exception e) {
        throw new JMetalException("initializeUniformWeight: failed when reading for file: "
            + dataDirectory + "/" + dataFileName, e) ;
      }
    }
  }

  /**
   * Initialize neighborhoods
   */
  protected void initializeNeighborhood() {
    double[] x = new double[populationSize];
    int[] idx = new int[populationSize];

    for (int i = 0; i < populationSize; i++) {
      // calculate the distances based on weight vectors
      for (int j = 0; j < populationSize; j++) {
        x[j] = Utils.distVector(lambda[i], lambda[j]);
        idx[j] = j;
      }

      // find 'niche' nearest neighboring subproblems
      Utils.minFastSort(x, idx, populationSize, neighborSize);

      System.arraycopy(idx, 0, neighborhood[i], 0, neighborSize);
    }
  }

  protected void initializeIdealPoint() {
    for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
      idealPoint[i] = 1.0e+30;
    }

    for (int i = 0; i < populationSize; i++) {
      updateIdealPoint(population.get(i));
    }
  }

  void updateIdealPoint(Solution individual) {
    for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
      if (individual.getObjective(n) < idealPoint[n]) {
        idealPoint[n] = individual.getObjective(n);
      }
    }
  }

  protected NeighborType chooseNeighborType() {
    double rnd = randomGenerator.nextDouble();
    NeighborType neighborType ;

    if (rnd < neighborhoodSelectionProbability) {
      neighborType = NeighborType.NEIGHBOR;
    } else {
      neighborType = NeighborType.POPULATION;
    }
    return neighborType ;
  }

  protected List<S> parentSelection(int subProblemId, NeighborType neighborType) {
    Vector<Integer> matingPool = new Vector<Integer>();
    matingSelection(matingPool, subProblemId, neighborType);

    List<S> parents = new ArrayList<>(3);

    parents.add(population.get(matingPool.get(0)));
    parents.add(population.get(matingPool.get(1)));
    parents.add(population.get(subProblemId));

    return parents ;
  }

  protected void matingSelection(Vector<Integer> listOfSolutions, int subproblemId, NeighborType neighbourType) {
    // list : the set of the indexes of selected mating parents
    // subProblemId  : the id of current subproblem
    // numberOfSolutionsToSelect : the number of selected mating parents
    // type : 1 - neighborhood; otherwise - whole population
    int neighbourSize;
    int selectedSolution;
    int numberOfSolutionsToSelect = 2 ;

    neighbourSize = neighborhood[subproblemId].length;
    while (listOfSolutions.size() < numberOfSolutionsToSelect) {
      int random;
      if (neighbourType == NeighborType.NEIGHBOR) {
        random = randomGenerator.nextInt(0, neighbourSize - 1);
        selectedSolution = neighborhood[subproblemId][random];
      } else {
        selectedSolution = randomGenerator.nextInt(0, populationSize - 1);
      }
      boolean flag = true;
      for (Integer individualId : listOfSolutions) {
        if (individualId == selectedSolution) {
          flag = false;
          break;
        }
      }

      if (flag) {
        listOfSolutions.addElement(selectedSolution);
      }
    }
  }

  void updateNeighborhood(Solution individual, int subProblemId, NeighborType neighborType) throws JMetalException {
    int size;
    int time;

    time = 0;

    if (neighborType == NeighborType.NEIGHBOR) {
      size = neighborhood[subProblemId].length;
    } else {
      size = population.size();
    }
    int[] perm = new int[size];

    Utils.randomPermutation(perm, size);

    for (int i = 0; i < size; i++) {
      int k;
      if (neighborType == NeighborType.NEIGHBOR) {
        k = neighborhood[subProblemId][perm[i]];
      } else {
        k = perm[i];
      }
      double f1, f2;

      f1 = fitnessFunction(population.get(k), lambda[k]);
      f2 = fitnessFunction(individual, lambda[k]);

      if (f2 < f1) {
        population.set(k, (S)individual.copy());
        time++;
      }

      if (time >= maximumNumberOfReplacedSolutions) {
        return;
      }
    }
  }

  double fitnessFunction(Solution individual, double[] lambda) throws JMetalException {
    double fitness;

    if (MOEAD.FunctionType.TCHE.equals(functionType)) {
      double maxFun = -1.0e+30;

      for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
        double diff = Math.abs(individual.getObjective(n) - idealPoint[n]);

        double feval;
        if (lambda[n] == 0) {
          feval = 0.0001 * diff;
        } else {
          feval = diff * lambda[n];
        }
        if (feval > maxFun) {
          maxFun = feval;
        }
      }

      fitness = maxFun;
    } else if (MOEAD.FunctionType.AGG.equals(functionType)) {
      double sum = 0.0;
      for (int n = 0; n < problem.getNumberOfObjectives(); n++) {
        sum += (lambda[n]) * individual.getObjective(n);
      }

      fitness = sum;

    } else if (MOEAD.FunctionType.PBI.equals(functionType)) {
      double d1, d2, nl;
      double theta = 5.0;

      d1 = d2 = nl = 0.0;

      for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
        d1 += (individual.getObjective(i) - idealPoint[i]) * lambda[i];
        nl += Math.pow(lambda[i], 2.0);
      }
      nl = Math.sqrt(nl);
      d1 = Math.abs(d1) / nl;

      for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
        d2 += Math.pow((individual.getObjective(i) - idealPoint[i]) - d1 * (lambda[i] / nl), 2.0);
      }
      d2 = Math.sqrt(d2);

      fitness = (d1 + theta * d2);
    } else {
      throw new JMetalException(" MOEAD.fitnessFunction: unknown type " + functionType);
    }
    return fitness;
  }

}
