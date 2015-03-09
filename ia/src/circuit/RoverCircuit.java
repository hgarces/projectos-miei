package circuit;

import java.util.*;

/**
 * 	Classe que instancia a classe abstracta Individual
 */
public class RoverCircuit extends Individual {

	public static final int OX1 = 1;
	public static final int OX2 = 2;
	public static final int SWAP_MUT = 1;
	public static final int INVERSION_MUT = 2;
	
	private int crossoverType;
	private int mutationType;
	
	private static Random gen = new Random();
	private static Individual[] children = new Individual[2];
	private int size;
	private int[] circuit;
	private ObservationData data;
	private double fitness;	//tempo corrente


	public RoverCircuit(ObservationData data, int crossType, int mutatType) {
		this.data = data;
		this.size = data.getSize();
		this.circuit = new int[size];
		this.fitness = 0.0;
		this.crossoverType = crossType;
		this.mutationType = mutatType;

		List<Integer> listInt = new ArrayList<Integer>();
		for(int i = 0; i < size; i++)
			listInt.add(i);

		Collections.shuffle(listInt);
		for(int i = 0; i < size; i++)
			circuit[i] = listInt.get(i);
	}

	public RoverCircuit(ObservationData data, int[] circuit, int crossType, int mutatType) {
		this.data = data;
		this.size = data.getSize();
		this.circuit = circuit;
		this.fitness = 0.0;
		this.crossoverType = crossType;
		this.mutationType = mutatType;
	}

	@Override
	public double fitness() {
		 int fit = data.getSpot(circuit[0]).firstTime();
         fit += data.getSpot(circuit[0]).durationObservation(fit);
         for(int i=1; i < size; i++ ) {
                 fit += data.getSpot(circuit[i]).durationObservation(fit) + data.getCost(circuit[i-1],circuit[i]);
         }
         fit += data.getCost(circuit[size-1], circuit[0]); //adiciona o custo do ultimo para o primeiro

         fitness = (double) fit;
		return fitness;
	}

	@Override
	public Individual[] crossover(Individual other) {
		if(crossoverType == OX1)
			return crossOX1(other);
		else
			return crossOX2(other);
	}
	
	private Individual[] crossOX1(Individual other)
	{
		RoverCircuit mother = (RoverCircuit) other;
		int r1 = gen.nextInt(size-1);
		int r2 = gen.nextInt(size-2);
		int cut1, cut2;

		if(r2 >= r1) {
			cut1 = r1 + 1;
			cut2 = r2 + 2; 
		}
		else {
			cut1 = r2 + 1;
			cut2 = r1 + 2;
		}
		List<Integer> tmp1 = new ArrayList<Integer>();
		List<Integer> tmp2 = new ArrayList<Integer>();
		int[] c1 = new int[this.size];	//filho1
		int[] c2 = new int[this.size];	//filho2
		Arrays.fill(c1, -1);
		Arrays.fill(c2, -1);
		
		for(int i = cut1; i < cut2; i++) {
			c1[i] = circuit[i];
			tmp1.add(circuit[i]);

			c2[i] = mother.circuit[i];
			tmp2.add(mother.circuit[i]);
		}
		
		
		int aux1, aux2;
		aux1 = cut2;
		aux2 = cut2;

		while(tmp1.size() < size) //copia valores do 2ยบ progenitor, partindo de cut2
		{
			if(aux2 == size) {
				aux2 = 0;
				aux1 = 0;
			}
			
			while(c1[aux1] == -1)
			{
				if(!tmp1.contains(mother.circuit[aux2]))
				{	
					tmp1.add(mother.circuit[aux2]);
					c1[aux1] = mother.circuit[aux2];
					aux2++;
					aux1++;
					if(aux1 == size)
						aux1 = 0;
					if(aux2 == size)
						aux2 = 0;
				} else {
					aux2++; 
					if(aux2 == mother.circuit.length)
						aux2 = 0;
				}
			} 
		}
		
		aux1 = cut2;
		aux2 = cut2;
		while(tmp2.size() < size) //copia valores do 1ยบ progenitor, partindo de cut2
		{
			if(aux2 == size) {
				aux2 = 0;
				aux1 = 0;
			}
			while(c2[aux1] == -1)
			{
				if(!tmp2.contains(circuit[aux2]))
				{	
					tmp2.add(circuit[aux2]);
					c2[aux1] = circuit[aux2];
					aux2++;
					aux1++;
					if(aux1 == size)
						aux1 = 0;
					if(aux2 == size)
						aux2 = 0;
					
				} else {
					aux2++; 
					if(aux2 == size)
						aux2 = 0;
				}
				
			} 
		}
		
		children[0] = new RoverCircuit(data, c1, crossoverType, mutationType);
		children[1] = new RoverCircuit(data, c2, crossoverType, mutationType);

		return children;
	}
	
	private Individual[] crossOX2(Individual other) {
		RoverCircuit mother = (RoverCircuit) other;
		int r1 = gen.nextInt(size-1);
		int r2 = gen.nextInt(size-2);
		int cut1, cut2;

		//atribui os valores aleatorios ao cut1 e cut2
		if(r2 >= r1) {
			cut1 = r1 + 1;
			cut2 = r2 + 2; 
		}
		else {
			cut1 = r2 + 1;
			cut2 = r1 + 2;
		}
		
		int[] c1 = new int[this.size];	//filho1
		int[] c2 = new int[this.size];	//filho2
		List<Integer> tmp1 = new ArrayList<Integer>(); 	//listas temporarias para verificar se um filho ja contem
		List<Integer> tmp2 = new ArrayList<Integer>();	//um determinado numero 
		
		Arrays.fill(c1, -1);
		Arrays.fill(c2, -1);
		
		for(int i = cut1; i < cut2; i++) {
			c1[i] = circuit[i];
			tmp1.add(circuit[i]);

			c2[i] = mother.circuit[i];
			tmp2.add(mother.circuit[i]);
		}

		int m = 0, n = 0;
		boolean done = false;
		//cria filho1
		while(!done) {
			
			if(c1[n] == -1) {
				if(!tmp1.contains(mother.circuit[m])) {
					c1[n++] = mother.circuit[m];
					tmp1.add(mother.circuit[m]);
					m++;
					
					if(m == size || n == size)
						done = true;
				}
				else {
					m++;
					if(m == size )
						done = true;
				}
			} 
			
			else {
				n++;
				if(n == size )
					done = true;
			}
		}
		
		//cria filho2
		m = 0; n = 0; done = false;
		while(!done) {
			if(c2[n] == -1) {
				if(!tmp2.contains(circuit[m])) {
					c2[n] = circuit[m];
					tmp2.add(circuit[m]);
					m++;
					if(m == size || n == size)
						done = true;
				}
				else {
					m++;
					if(m == size )
						done = true;
				}
			}
			else {
				n++;
				if(n == size )
					done = true;
			}
		}
		
		children[0] = new RoverCircuit(data, c1, crossoverType, mutationType);
		children[1] = new RoverCircuit(data, c2, crossoverType, mutationType);

		return children;

	}

	@Override
	//Random swap
	public void mutate() {
		if(mutationType == SWAP_MUT)
			swap();
		else
			inversion();
	}

	private void swap() {
		int r1 = gen.nextInt(size-1);
		int r2 = gen.nextInt(size-1);
		if(r2 >= r1)
			r2++;
		int aux = circuit[r1];
		circuit[r1] = circuit[r2];
		circuit[r2] = aux;
	}
	
	private void inversion() {
		int r1 = gen.nextInt(size-1);
		int r2 = gen.nextInt(size-2);
		if(r2 >= r1)
			r2++;
		int cut1 = Math.min(r1, r2);
		int cut2 = Math.max(r1, r2);
		for(int i = cut1; i < cut2; i++) {
			int tmp = circuit[i];
			circuit[i] = circuit[cut2];
			circuit[cut2--] = tmp;
		}
	}
	
//	public void mutateByInsertion() {
//		int r1 = gen.nextInt(size-1);
//		int r2 = gen.nextInt(size-1);
//		if(r2 == r1)
//			r2++;
//		int aux = circuit[r2];
//		circuit[r1] = aux;
//	}
	
	@Override
	public Object clone() {
		return new RoverCircuit(data, circuit.clone(), crossoverType, mutationType);
	}

	@Override
	public String toString() {
		return Arrays.toString(circuit);
	}
}
