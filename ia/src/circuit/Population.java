package circuit;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Collections;

/**
 * 	Classe usada para a representação de uma população.
 */
public class Population {

	private final static int CAP = 100;

	ArrayList<Individual> pop;
	ArrayList<Double> acum;

	private static Random gen = new Random();
	private double sumOfFitness;
	private int size;
	private boolean currupt;
	private Individual bestInd;
	private Individual worstInd;
	private double bestFit;
	private double worstFit;
	

	/**
	 * 	Construtor relativo à classe Population
	 */
	public Population() {
		this.size = 0;
		this.pop = new ArrayList<Individual>(CAP);
		this.acum = new ArrayList<Double>(CAP);
		this.sumOfFitness = 0.0;
		this.currupt = true;
		this.bestInd = null;
		this.worstInd = null;
		this.bestFit = Double.POSITIVE_INFINITY;
		this.worstFit = Double.NEGATIVE_INFINITY;
		
	}

	/**
	 * 	Construtor onde se especifica a popolação
	 * @param p um array de indivíduos
	 */
	public Population(Individual[] p) {
		this();
		for(Individual i : p)
			addIndividual(i);
	}

	/**
	 * Selecciona e devolve um indivíduo da população, tendo em conta a sua fitness
	 * @return um array de indivíduos
	 */
	public Individual selectIndividual() {

		// Verifica se necessita de calcular os valores de probabilidade de selecção de cada indivíduo
		if( currupt ) {
			double total=0.0;
			for(int i=0; i < pop.size(); i++) {
				total += 1/pop.get(i).fitness();
				acum.add(total/sumOfFitness);
			}
			currupt = false;
		}

		double r = gen.nextDouble();
		Collections.sort(acum);
		int pos = Collections.binarySearch(acum, r);
		
		if( pos >= 0)
			return pop.get(pos);
		else if (-(pos + 1) >= size) 
			return pop.get(size - 1);
		else
			return pop.get(-(pos+1));

	}
	/**
	 * Adiciona um indivíduo à população
	 * @param ind, um indivíduo
	 */
	public void addIndividual(Individual ind) {
		size++;
		pop.add(ind);
		double f = ind.fitness();
		sumOfFitness += 1/f; 
		if( f > worstFit ) {
			worstFit = f;
			worstInd = ind;
		}
		if( f < bestFit ) {
			bestFit = f;
			bestInd = ind;
		}
		
	}

	public Individual getBestIndividual() {
		return this.bestInd;
	}

	public Individual getWorstIndividual() {
		return this.worstInd;
	}

	public double getBestFitness() {
		return this.bestFit;
	}

	public double getWorstFitness() {
		return this.worstFit;
	}

	public Population getElite(int n) {
		//ArrayList<Individual> aux = (ArrayList<Individual>) pop.clone();
		//Collections.sort(aux, new FitnessComparator());
		Queue<Individual> pqueue = new PriorityQueue<Individual>(10,new FitnessComparator());
		pqueue.addAll(pop);
		
		if(n > size)
			n = size;
		Individual[] eliteArray = new Individual[size];
		pqueue.toArray(eliteArray);
		Individual[] e = new Individual[n];
		for(int i = 0; i < e.length; i++) {
			e[i] = eliteArray[i];
		}
		Population elite = new Population(e);
		return elite;
	}

	public double getAvgFitness() {
		return sumOfFitness/size;
	}

	public int getSize() {
		return this.size;
	}
	
	public Individual[] getEliteArray(int n) {
		Queue<Individual> pqueue = new PriorityQueue<Individual>(10,new FitnessComparator());
		pqueue.addAll(pop);
		
		if(n > size)
			n = size;
		Individual[] eliteArray = new Individual[size];
		pqueue.toArray(eliteArray);
		return eliteArray;
	}
	
	public Individual getFromElite(int dimElite)
	{
		int n = gen.nextInt(dimElite);
		Population elite = getElite(n); // os 10 melhores da elite, por exemplo
		int genNum = gen.nextInt(elite.size-1);
		return elite.pop.get(genNum);
		
	}
}
