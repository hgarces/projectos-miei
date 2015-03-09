package circuit;

import java.util.*;
import java.lang.*;
/**
 * 	Classe que caracteriza o "mapa" dos pontos a visitar pelo robot.
 */
public class ObservationData {

	public final static int SOL = (24*60+39)*60+35;
	
	private final int size;
	protected final ObservationSpot[] spots;
	protected final int[][] costs;
	/**
	 * 	Classe que representa o intervalo de observação dos pontos de visita (Spots)
	 */
	public class ObservationInterval implements Comparable<ObservationInterval> {

		int mintime;
		int maxtime;
		/**
		 * 	Construtor da classe
		 * 	@param mintime o limite inferior no tempo
		 *  @param maxtime o limite superior no tempo
		 */
		public ObservationInterval(int mintime, int maxtime) {
			this.mintime = mintime;
			this.maxtime = maxtime;
		} 
		/**
		 * 	Construtor da classe
		 * @param s uma string com formato previsto
		 */
		public ObservationInterval(String s) {
			 StringTokenizer st = new StringTokenizer(s,"[;]",true);
			 if( !st.nextToken().equals("[") )
				 throw new RuntimeException();
			 mintime = Integer.parseInt(st.nextToken());
			 if( !st.nextToken().equals(";") )
				 throw new RuntimeException();
			 maxtime = Integer.parseInt(st.nextToken());
			 if( !st.nextToken().equals("]"))
				 throw new RuntimeException();
			 if( st.hasMoreTokens() )
				 throw new RuntimeException();
		}
		/**
		 * 	Construtor da classe
		 * @param s
		 */
		public ObservationInterval(Scanner s) {
			this.mintime = mintime;
			this.maxtime = maxtime;
		}
		/**
		 * 	Método para testar se um valor no tempo está entre os limites do intervalo
		 * @param time
		 * @return boolean
		 */
		public boolean inInterval(int time) {
			return time >= mintime && time <= maxtime;
		}
		/**
		 * 	Método para testar se um valor no tempo está antes do limite superior
		 * @param time
		 * @return boolean
		 */
		public boolean beforeInterval(int time) {
			return time < maxtime;
		}
		/**
		 * 	Método para testar se um valor no tempo está depois do limite superior
		 * @param time
		 * @return boolean
		 */
		public boolean afterInterval(int time) {
			return time > maxtime;
		}
		/**
		 * 	Método que retorna os limites do tempo segundo um certo formato
		 * @return uma string
		 */
		public String toString() {
			return "["+mintime+";"+maxtime+"]";
		}

		
		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + maxtime;
			result = PRIME * result + mintime;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ObservationInterval other = (ObservationInterval) obj;
			if (maxtime != other.maxtime)
				return false;
			if (mintime != other.mintime)
				return false;
			return true;
		}
	
		public int compareTo(ObservationInterval o) {
			if( o == null )
				throw new NullPointerException();
			if( this.mintime < o.mintime )
				return -1;
			else if( this.mintime > o.mintime )
				return 1;
			else
				return this.maxtime - o.maxtime;
		}
	}
	/**
	 * Classe que catacteriza cada ponto a visitar pelo "robot" 
	 * 
	 */
	public class ObservationSpot {
		 private String name;
		 private int duration;
		 private int x;
		 private int y;
		 private SortedSet<ObservationInterval> obstimes;
		 
		 /**
			 * Construtor da classe
			 * @param name (nome do spot)
			 * @param x coordenada
			 * @param y coordenada
			 * @param duration tempo que demora a visita ao spot
			 * @param obstimes conjunto de intervalos possíveis para visita
			 */
		public ObservationSpot(String name, int x, int y, int duration, SortedSet<ObservationInterval> obstimes) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.duration = duration;
			this.obstimes = obstimes;
		}
		 /**
		* Construtor da classe
		 * @parm s, uma string com um formato previsto
		 */
		public ObservationSpot(String s) {
			 StringTokenizer st = new StringTokenizer(s);
			 name = st.nextToken();
			 x = Integer.parseInt(st.nextToken());
			 y = Integer.parseInt(st.nextToken());			 
			 duration = Integer.parseInt(st.nextToken());
			 obstimes = new TreeSet<ObservationInterval>();
			 while( st.hasMoreTokens() )
				 obstimes.add(new ObservationInterval(st.nextToken()));
		}
		/**
		* método que devolve o nome do spot (ponto de visita)
		 * @return name
		 */
		public String getName() {
			return name;
		}
		/**
		* método que devolve o tempo de demora efectivo de visita do spot, em função do parâmetro currtime
		 * @param currtime, um int
		 * @return duration, a demora efectiva
		 */
		public int durationObservation(int currtime) {
		
			// Convert time to Martian time
			int time = currtime % SOL;
			
			// Try to find interval starting after current time
			for(ObservationInterval interval: obstimes) {
				if( time <= interval.maxtime ) {
					if( time >= interval.mintime ) {
						return duration;
					} else {
						return duration + (interval.mintime - time);
					}
				}
			}
			// Time is after all intervals, so it must wait for the next day...
			ObservationInterval interval = obstimes.first();
			return duration + ( SOL - time + interval.mintime);
		}
		/**
		* método que devolve o menor limite inferior (tempo) num conjunto de intervalos
		* @return mintime (o menor dentro do conjunto)
		 */
		public int firstTime() {
			return obstimes.first().mintime;
		}
		/**
		* método que devolve uma string com o nome, as coordenadas e a duração da visita a um spot
		* @return out, uma string
		 */
		public String toString() {
			String out = name+" "+x+" "+y+" "+duration;
			for(ObservationInterval i: obstimes)
				out += " " + i;
			return out;
		}
		
		 
	}
	/**
	* Construtor da classe
	* @parm size; array de spots, array de custos por spot
	 */
	public ObservationData(int size, ObservationSpot[] spots, int[][] costs) {
		this.size = size;
		this.spots = spots;
		this.costs = costs;
	}
	/**
	* Construtor da classe
	* @parm s, uma string com um formato previsto
	 */
	public ObservationData(String s) {
		 StringTokenizer st = new StringTokenizer(s,"\n");
		 //Reads size of problem
		 size = Integer.parseInt(st.nextToken());
		 //Reads first all the observation spots
		 spots = new ObservationSpot[size];
		 for(int i=0; i< size; i++)
			 spots[i] = new ObservationSpot(st.nextToken());
		 //Reads the cost matrix
		 costs = new int[size][size];
		 for(int i=0; i < size; i++) {
			 StringTokenizer linest = new StringTokenizer(st.nextToken());
			 for(int j=0; j < size; j++)
				 costs[i][j] = Integer.parseInt(linest.nextToken());
		 }
	}
	/**
	* Método que retorna o número de spots
	* @return size, o número de spots que é necessário visitar
	 */
	public int getSize() {
		return size;
	}
	/**
	* Método que retorna o spot indicado em parâmetro
	* @parm i, o índice para aceder ao spot
	* @return spots[i], um spot específico
	 */
	public ObservationSpot getSpot(int i) {
		return spots[i];
	}
	/**
	* Método que retorna o custo entre os spots i e j
	* @parm i um spot (ponto)
	* @param j um spot (ponto)
	* @return costs[i][j] o custo entre os spots i e j
	 */
	public int getCost(int i,int j) {
		return costs[i][j];
	}

	public String toString() {
		String out = size + "\n";
		for(int i=0; i < size; i++)
			out += spots[i]+"\n";
		for(int i=0; i < size; i++) {
			for(int j=0; j < size; j++)
				out += costs[i][j]+" ";
			out += "\n";
		}
		return out;		
	}
	
}
