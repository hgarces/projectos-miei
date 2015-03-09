package circuit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.jfree.data.xy.XYSeries;
/**
 * Classe que "implementa" o algoritmo genetico
 */
public class GeneticAlgorithm {

	public static final float DEFAULT_PROB_CROSSOVER = 0.6f;
	public static final float DEFAULT_PROB_MUTATION = 0.002f;
	public static final int DEFAULT_ELITE_SIZE = 10;
	public static final int DEFAULT_GENERATION_SIZE = 100;
	public static final int NUM_GENERATION = 1;
	public static final int TIME_LIMIT = 2;
	public static final int FITNESS_BOUND = 3;
	public static final int ROULETTE_WHEEL_SELECTION = 1;
	public static final int ELITISM_SELECTION = 2;

	private float pc =  DEFAULT_PROB_CROSSOVER;
	private float pm = DEFAULT_PROB_MUTATION;
	private Population pop;
	private int elite = DEFAULT_ELITE_SIZE;
	int generations = DEFAULT_GENERATION_SIZE;
	private int stopCriteria = NUM_GENERATION;
	private int selectCriteria = ELITISM_SELECTION;
	int elapseTime;
	int fitnessBound;
	private double bestSolution;
	private double worstSolution;
	private int generationOfBest;
	private int generationOfWorst;
	private int nTotalOfGenerations;
	private Individual bestIndividual;
	private Individual worstIndividual;
	private long startTime;
	private long endTime;
	private XYSeries series;

	/**
	 * Construtor
	 * @param pop uma popula��o
	 */
	GeneticAlgorithm(Population pop) {
		this.pop = pop;
		this.bestIndividual = null;
		this.worstIndividual = null;
		this.bestSolution = 0.0;
		this.worstSolution = 0.0;
		this.generationOfBest = 1;
		this.generationOfWorst = 1;
		this.nTotalOfGenerations = 0;
	}
	/**
	 * Construtor
	 * @param pop uma popula��o
	 * @param pcrossover a probabilidade de crossover
	 * @param pmutate a probabilidade de muta��o
	 */
	GeneticAlgorithm(Population pop, float pcrossover, float pmutate) {
		this(pop);
		this.pc = pcrossover;
		this.pm = pmutate;
	}

	/**
	 * 	M�todo que pesquisa e devolve o melhor indiv�duo encontrado
	 * @return pop.getBestIndividual(), o melhor indiv�duo
	 */

	public Individual search() {
		Random gen = new Random();
		startTime = System.nanoTime();	
		int count_gen = 0;
		Individual[] children = new Individual[2];
		Individual x,y;
		bestSolution = pop.getBestIndividual().fitness();
		worstSolution = pop.getWorstIndividual().fitness();
		bestIndividual = pop.getBestIndividual();
		worstIndividual = pop.getWorstIndividual();
		Population newpop;
		if(stopCriteria == NUM_GENERATION) {
			while(count_gen != generations) {
				series.add(count_gen, pop.getBestIndividual().fitness());
				if(selectCriteria == ELITISM_SELECTION)
					newpop = pop.getElite(elite);
				else
					newpop = new Population();
				while(newpop.getSize() < pop.getSize()) {
					//selecionar individuo
					//gerar numeros reais para as probabilidades

					x = pop.selectIndividual();
					y = pop.selectIndividual();

					if(gen.nextFloat() < pc) {
						children = x.crossover(y);
					}
					else {
						children[0] = x;
						children[1] = y;
					}

					if(gen.nextFloat() < pm)
						children[0].mutate();
					if(gen.nextFloat() < pm)
						children[1].mutate();

					newpop.addIndividual(children[0]);
					newpop.addIndividual(children[1]);
				}
				pop = newpop;
				
				if(pop.getWorstIndividual().fitness() > worstSolution) {
					worstIndividual = pop.getWorstIndividual();
					worstSolution = worstIndividual.fitness();
					generationOfWorst = count_gen;
				}

				if(selectCriteria == ELITISM_SELECTION)
					generationOfBest = count_gen;
				else {
					if(pop.getBestIndividual().fitness() < bestSolution) {
						bestIndividual = pop.getBestIndividual();
						generationOfBest = count_gen;
					}
					bestSolution = bestIndividual.fitness();
				}
				//pop.addIndividual(hillClimbing());
				count_gen++;
			}
		}

		if(stopCriteria == 2) {
			while(startTime < elapseTime) {
				if(selectCriteria == ELITISM_SELECTION)
					newpop = pop.getElite(elite);
				else
					newpop = new Population();
				while(newpop.getSize() < pop.getSize()) {

					x = pop.selectIndividual();
					y = pop.selectIndividual();

					if(gen.nextFloat() < pc) {
						children = x.crossover(y);
					}
					else {
						children[0] = x;
						children[1] = y;
					}

					if(gen.nextFloat() < pm)
						children[0].mutate();
					if(gen.nextFloat() < pm)
						children[1].mutate();

					newpop.addIndividual(children[0]);
					newpop.addIndividual(children[1]);
				}
				pop = newpop;

				if(pop.getWorstIndividual().fitness() > worstSolution) {
					worstIndividual = pop.getWorstIndividual();
					worstSolution = worstIndividual.fitness();
					generationOfWorst = count_gen;
				}

				if(pop.getBestIndividual().fitness() < bestSolution) {
					bestIndividual = pop.getBestIndividual();
					bestSolution = bestIndividual.fitness();
					generationOfBest = count_gen;
				}
				count_gen++;
			}
		}
		if(stopCriteria == 3)
		{

			while(pop.getBestIndividual().fitness() < fitnessBound)
			{
				if(selectCriteria == ELITISM_SELECTION)
					newpop = pop.getElite(elite);
				else
					newpop = new Population();
				while(newpop.getSize() < pop.getSize()) {


					x = pop.selectIndividual();
					y = pop.selectIndividual();

					if(gen.nextFloat() < pc) {
						children = x.crossover(y);
					}
					else {
						children[0] = x;
						children[1] = y;
					}

					if(gen.nextFloat() < pm)
						children[0].mutate();
					if(gen.nextFloat() < pm)
						children[1].mutate();

					newpop.addIndividual(children[0]);
					newpop.addIndividual(children[1]);
				}
				pop = newpop;

				if(pop.getWorstIndividual().fitness() > worstSolution) {
					worstIndividual = pop.getWorstIndividual();
					worstSolution = worstIndividual.fitness();
					generationOfWorst = count_gen;
				}

				if(pop.getBestIndividual().fitness() < bestSolution) {
					bestIndividual = pop.getBestIndividual();
					bestSolution = bestIndividual.fitness();
					generationOfBest = count_gen;
				}
				count_gen++;
			}
		}
		nTotalOfGenerations = count_gen;
		endTime = System.nanoTime() - startTime;
		
		return bestIndividual;

	}


	public Individual getWorstIndividual() {
		return worstIndividual;
	}

	public Map<String,Number> getResults() {
		Map<String,Number> results = new LinkedHashMap<String,Number>();

		results.put("Fitness of the best", bestSolution);
		results.put("Generation of the best", generationOfBest);
		results.put("\nFitness of the worst", worstSolution);
		results.put("Generation of the worst", generationOfWorst);
		results.put("\nNum total of generations", nTotalOfGenerations);
		results.put("Time", endTime/1E9);

		return results;
	}

	public void setStopCriteria(int n, int value) {
		System.out.println("stop");
		if(n == NUM_GENERATION) {
			this.stopCriteria = NUM_GENERATION;
			this.generations = value;
		}
		else if(n == TIME_LIMIT) {
			this.stopCriteria = TIME_LIMIT;
			this.elapseTime = value;
		}
		else if(n == FITNESS_BOUND) {
			this.stopCriteria = FITNESS_BOUND;
			this.fitnessBound = value;
		}
	}

	public void setSelectCriteria(int n, int eliteSize) {
		if(n == ROULETTE_WHEEL_SELECTION)
			selectCriteria = ROULETTE_WHEEL_SELECTION;
		else if(n == ELITISM_SELECTION)
			selectCriteria = ELITISM_SELECTION;
		this.elite = eliteSize;
	}

	public Individual hillClimbing() {
		Individual[] elite = pop.getEliteArray(this.elite);
		int i = 0;
		boolean done = false;
		while(!done) {
			Individual ind = elite[i];
			ind.mutate();
			
			if(i == elite.length-1) {
				if(ind.fitness() < elite[0].fitness())
					return ind;
				else
					i = 0;
			}
			else if(ind.fitness() < elite[++i].fitness())
				return ind;
			
		}
		return null;
	}
	
	public void setDataSet(XYSeries s) {
		this.series = s;
	}
}
