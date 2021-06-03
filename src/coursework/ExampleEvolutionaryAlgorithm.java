package coursework;

import java.util.ArrayList;
import java.util.Collections;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that extends {@link NeuralNetwork} 
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork {
	

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run() {		
		//Initialise a population of Individuals with random weights
		population = oppBasedInitialise();

		//Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */		
		
		while (evaluations < Parameters.maxEvaluations) {

			/**
			 * this is a skeleton EA - you need to add the methods.
			 * You can also change the EA if you want 
			 * You must set the best Individual at the end of a run
			 * 
			 */

			// Select 2 Individuals from the current population. Currently returns random Individual
			Individual parent1 = rouletteSelection(); 
			Individual parent2 = rouletteSelection();

			// Generate a child by crossover. Not Implemented			
			ArrayList<Individual> children = twoPointCrossover(parent1, parent2);			
			
			//mutate the offspring
			mutate(children);
			
			// Evaluate the children
			evaluateIndividuals(children);			

			// Replace children in population
			tournamentReplace(children);

			// check to see if the best has improved
			best = getBest();
			
			// Implemented in NN class. 
			outputStats();
			
			//Increment number of completed generations			
		}

		//save the trained network to disk
		saveNeuralNetwork();
	}

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals) {
		for (Individual individual : individuals) {
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}

	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise() {
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i) {
			//chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}
	
	
	private ArrayList<Individual> oppBasedInitialise() {
		population = new ArrayList<>();

		for (int i = 0; i < Parameters.popSize; ++i) {
			Individual individual = new Individual();
			Individual flippedIndividual = individual.copy();
			
			for (int j=0; j<flippedIndividual.chromosome.length; j++) {
				// Flip chromes
				flippedIndividual.chromosome[j] = 0 - flippedIndividual.chromosome[j];
			}
			
			individual.fitness = Fitness.evaluate(individual, this);
			flippedIndividual.fitness = Fitness.evaluate(flippedIndividual, this);
			
			if (individual.fitness < flippedIndividual.fitness) {
				population.add(individual);
			} else {
				population.add(flippedIndividual);
			}
		}
		return population;
		
	}
	
	

	/**
	 * Selection --
	 * 
	 * NEEDS REPLACED with proper selection this just returns a copy of a random
	 * member of the population
	 */
	private Individual select() {		
		Individual parent = population.get(Parameters.random.nextInt(Parameters.popSize));
		return parent.copy();
	}
	
	//get n random elements in pop, hold a tournament to find the best
	private Individual tournamentSelection() {
		Individual parent = new Individual();
		
		//randomise population
		Collections.shuffle(population);

		//subsection of pop to be considered in tournament
		ArrayList<Individual> tournamentPop = new ArrayList<>();
	    for (int i = 0; i < 31; i++) {
	        tournamentPop.add(population.get(i)); 
	    }
	    
	    for (Individual individual : tournamentPop) {
	    	if (best == null) {
				best = individual.copy();
				parent = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
				parent = individual.copy();
			}
	    }
	    
	    return parent.copy();
	}
	
	
	private Individual rouletteSelection() {
		
		double totalFitness = 0;
		double probability = 0;
		double totalProbability = 0;
		Individual selectedIndividual = new Individual();
		
		//get total fitness
		for(Individual individual : population) {
			totalFitness += individual.fitness;
		}
		
		//get probability
		for(Individual individual : population) {
			probability = totalProbability + (individual.fitness / totalProbability);
			totalProbability += probability;
			
			//get random number i.e. where roulette wheel falls
			double randomNumber = totalFitness * Parameters.random.nextDouble();
			
			//get segment
			if (randomNumber <= probability) {
				selectedIndividual = individual.copy();
			}
		}

		return selectedIndividual;
	}
	
	
	//TO-DO - RANKED FITNESS
	
	

	/**
	 * Crossover / Reproduction
	 * 
	 * NEEDS REPLACED with proper method this code just returns exact copies of the
	 * parents. 
	 */
	private ArrayList<Individual> reproduce(Individual parent1, Individual parent2) {
		ArrayList<Individual> children = new ArrayList<>();
		children.add(parent1.copy());
		children.add(parent2.copy());			
		return children;
	} 
	
	//randomly choose a length across the chromosome and exchange across the split
	private ArrayList<Individual> onePointCrossover (Individual parent1, Individual parent2) {
	
		//initialise offspring
		Individual offspringA = new Individual();
		Individual offspringB = new Individual();
		
		int chromosomeLength = parent1.chromosome.length;
		//the point at which parts are exhanged
		int cutPoint = Parameters.random.nextInt(chromosomeLength);
		
		//for the length of the chromosome
		for (int i = 0; i < parent1.chromosome.length; i++) {
			//exchange first chunk
			if (i < cutPoint) {
				offspringA.chromosome[i] = parent1.chromosome[i];
				offspringB.chromosome[i] = parent2.chromosome[i];
			}
			else {
			 //exchange second chunk
			offspringA.chromosome[i] = parent2.chromosome[i];
			offspringB.chromosome[i] = parent1.chromosome[i];
			}
		}
		
		ArrayList<Individual> offspring = new ArrayList<>();
		offspring.add(offspringA);
		offspring.add(offspringB);	
		return offspring;
		
	}
	
	//create two cut points and swap the middle
	private ArrayList<Individual> twoPointCrossover (Individual parent1, Individual parent2) {
		
		//initialise offsring
		Individual offspringA = new Individual();
		Individual offspringB = new Individual();
		
		int chromosomeLength = parent1.chromosome.length;
		int cutPoint1 = Parameters.random.nextInt(chromosomeLength);
		//next cutpoint is a random int after the previous
		int cutPoint2 = Parameters.random.nextInt((chromosomeLength - cutPoint1) + 1) + cutPoint1;
		
		for (int i = 0; i < chromosomeLength; i++) {
			//between the head and the tail
			if(i < cutPoint1 || i >= cutPoint2){
				offspringA.chromosome[i] = parent1.chromosome[i];
				offspringB.chromosome[i] = parent2.chromosome[i];
				} 
			else {
			    offspringA.chromosome[i] = parent2.chromosome[i];
			    offspringB.chromosome[i] = parent1.chromosome[i];
				}
			}
			
			//return the result of the for loop
			ArrayList<Individual> offspring = new ArrayList<>();
			offspring.add(offspringA);
			offspring.add(offspringB);	
			return offspring;
		}
		
	//treat each gene individually, make random choice whether to swap
	private ArrayList<Individual> uniformCrossover(Individual parent1, Individual parent2){
		//initialise offspring
		Individual offspringA = new Individual();
		Individual offspringB = new Individual();
		
		int chromosomeLength = parent1.chromosome.length;
		
		//for each gene
		for (int i = 0; i < chromosomeLength; i++) {
			//get random bool to determine
			if(Parameters.random.nextBoolean()) {
				offspringA.chromosome[i] = parent1.chromosome[i];
				offspringB.chromosome[i] = parent2.chromosome[i];
			}
			else {
				offspringA.chromosome[i] = parent2.chromosome[i];
			    offspringB.chromosome[i] = parent1.chromosome[i];
			}
		}
		
		//return the result of the for loop
		ArrayList<Individual> offspring = new ArrayList<>();
		offspring.add(offspringA);
		offspring.add(offspringB);	
		return offspring;
	}
	
	
	/**
	 * Mutation
	 * 
	 * 
	 */
	private void mutate(ArrayList<Individual> individuals) {		
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() < Parameters.mutateRate) {
					if (Parameters.random.nextBoolean()) {
						individual.chromosome[i] += (Parameters.mutateChange);
					} else {
						individual.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}		
	}
	
	
	//swap mutation
	private void swapMutation(ArrayList<Individual> individuals) {
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() < Parameters.mutateRate) {
					if (Parameters.random.nextBoolean()) { 
						int chromosomeLength = individual.chromosome.length;
						int swapPoint = Parameters.random.nextInt(chromosomeLength);
						individual.chromosome[i] = swapPoint;
					}
				}
			}
		}
	}
	

	/**
	 * 
	 * Replaces the worst member of the population 
	 * (regardless of fitness)
	 * 
	 */
	private void replace(ArrayList<Individual> individuals) {
		for(Individual individual : individuals) {
			int idx = getWorstIndex();		
			population.set(idx, individual);
		}		
	}
	
	//get n random elements in pop, determine worst and replace it
	private void tournamentReplace(ArrayList<Individual> individuals) {
		
		for (Individual individual : individuals) {
			
			Collections.shuffle(population);
			
			for (int i = 0; i < 10; i++) {
				int idx = getWorstIndex();		
				if (individual.fitness < idx) {
					population.set(idx, individual);
				}
			}
		}
	}
	
	
	//replace the worst individual in the population 
	private void replaceWorst(ArrayList<Individual> individuals) {
		for(Individual individual : individuals) {
			
			Collections.shuffle(population);
			
			int idx = getWorstIndex();
			if (individual.fitness < idx) {
				population.set(idx, individual);
			}
		}		
	}

	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest() {
		best = null;;
		for (Individual individual : population) {
			if (best == null) {
				best = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
			}
		}
		return best;
	}


	/**
	 * Returns the index of the worst member of the population
	 * @return
	 */
	private int getWorstIndex() {
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i; 
			}
		}
		return idx;
	}	

	@Override
	public double activationFunction(double x) {
		if (x < -20.0) {
			return -1.0;
		} else if (x > 20.0) {
			return 1.0;
		}
		return Math.tanh(x);
	}
	
	
	
}
