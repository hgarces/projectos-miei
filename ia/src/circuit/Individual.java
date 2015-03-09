package circuit;

/**
 * 	Classe abstracta para representar um indivíduo da população.
 */
public abstract class Individual {
	/**
	 * 	fitness: representa a "abilidade"/adequabilidade do indivíduo para "resolver" o problema, 
	 * isto é, o custo que se pretende o menor possível
	 * @return fitness
	 */
	public abstract double fitness();
	/**
	 * método abstracto que cruza dois indivíduos e gera um array de indivíduos.
	 * @return Individual[], array de indivíduos
	 */
	public abstract Individual[] crossover(Individual other);
	/**
	 * 	Método abstracto que opera uma mutação num indivíduo.
	 */
	public abstract void mutate();
	
	public abstract Object clone();
}
