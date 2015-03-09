package circuit;

import java.*;
import java.util.*;
import java.awt.BorderLayout;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JTextArea;

//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;






import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import static java.lang.Math.*;

public class CircuitTest {

	private static JFrame frame;
	private static XYSeries series;
	private static JTextArea output;

	private static int crossoverType = RoverCircuit.OX2;
	private static int mutationType = RoverCircuit.SWAP_MUT;
	private static float probCrossover = GeneticAlgorithm.DEFAULT_PROB_CROSSOVER;
	private static float probMutation = GeneticAlgorithm.DEFAULT_PROB_MUTATION;
	private static int dimElite = GeneticAlgorithm.DEFAULT_ELITE_SIZE;


	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.println("NOME FICHEIRO.txt: \n");
		Scanner in = new Scanner(System.in);
		String command = in.next();

		String datastr = "";
		BufferedReader reader = new BufferedReader( new FileReader(readFile(command)));

		String line = reader.readLine();
		while( line != null) {
			datastr += line + "\n";
			line = reader.readLine();
		}
		reader.close();
		ObservationData r = new ObservationData(datastr);
		GeneticAlgorithm ga;

		System.out.println("POPULACO: 1-Inserir  2-Random\n");
		int popType = in.nextInt();
		int dimPop;
		if(popType == 1) //POP INSERIDA PELO USER
		{	
			System.out.println("Dimensao: \n");
			dimPop = in.nextInt();
		}
		else{
			System.out.println("Dimensao Random\n");
			Random gen = new Random();
			dimPop = gen.nextInt(100);
		}

		System.out.println("Operador de cruzamento: 1-OX1  2-0X2\n");
		int cross = in.nextInt();
		if(cross==1)
			crossoverType =RoverCircuit.OX1;
		else crossoverType = RoverCircuit.OX2;

		System.out.println("Probabilidade de cruzamento:  1-Inserir  2-Default\n");
		int crossP = in.nextInt();
		if(crossP == 1)
		{	System.out.println("Probabilidade: \n");
			float probC = in.nextFloat();
			probCrossover = probC;
		} 

		System.out.println("Operador de mutacao: 1-TROCA  2-INVERSAO\n");
		int mutate = in.nextInt();
		if(mutate == 1)
			mutationType = RoverCircuit.SWAP_MUT;
		else mutationType = RoverCircuit.INVERSION_MUT;

		System.out.println("Probabilidade de mutacao:  1-Inserir  2-Default\n");
		int mutateP = in.nextInt();
		if(mutateP == 1)
		{	System.out.println("Probabilidade: \n");
		float probM = in.nextFloat();
		probMutation = probM;
		}
		
		Individual[] ind = new Individual[dimPop];
		for(int i=0; i < ind.length; i++) 
		{
			ind[i] = new RoverCircuit(r, crossoverType, mutationType);
		}
		Population p = new Population(ind);
		ga = new GeneticAlgorithm(p, probCrossover, probMutation);
		
		System.out.println("Metodo Seleccao: 1-ROLETA  2-ELITISMO\n"); //Falta meter select no construct
		int selectMethod =in.nextInt();
		if(selectMethod == GeneticAlgorithm.ELITISM_SELECTION) {
			System.out.println("Dimensao da elite:  1-Inserir  2-Default\n");
			int choice = in.nextInt();
			if(choice ==1)
			{	int dimE = in.nextInt();
				dimElite = dimE;
			}
		}
		else 	
		{	
			System.out.println("Dimensao Default\n");
			dimElite = GeneticAlgorithm.DEFAULT_ELITE_SIZE;
		}
		ga.setSelectCriteria(selectMethod, dimElite);
		
		System.out.println("Criterio de Paragem: 1-NUMERO DE GERACOES  2-TEMPO LIMITE  3-VALOR DE FITNESS\n");
		int criterio = in.nextInt();
		System.out.println("Valor: \n");
		int value = in.nextInt();
		ga.setStopCriteria(criterio, value);

		getGraphic();
		ga.setDataSet(series);
		Individual best = ga.search();
		output.setText("");
		String s ="";
		s +="Best individual: "+best;
		s +="\nWorst individual: "+ga.getWorstIndividual();
		s+="\n"+ga.getResults();
		output.setText(s);
		
		System.out.println("--------------------");
		System.out.println("Best individual: "+best);
		System.out.println("Worst individual: "+ga.getWorstIndividual());
		System.out.println(ga.getResults());
		
		System.out.println("\nAplicar Hill-Climbing: 1-SIM 2-NAO");
		if(in.nextInt() == 1) {
			Individual hc = ga.hillClimbing();
			System.out.println(hc);
			System.out.println(hc.fitness());
		}
		System.exit(0);
	}

	public static String readFile(String filename) {
		Path path = Paths.get(filename);
		String filepath = path.toAbsolutePath().toString();
		String javaPath = filepath.replace("\\", "/");
		return javaPath;
	}
	
	public static void getGraphic() {
		frame = new JFrame();
		frame.setSize(700, 500);
		frame.setTitle("RoverCircuit");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		 XYSeriesCollection dataset = new XYSeriesCollection();
		 series = new XYSeries("Best Solution");
		 dataset.addSeries(series);
		 
		 JFreeChart chart = ChartFactory.createXYLineChart("Genetic Algorithm", "generation", "fitness", dataset);
		 
		 frame.add(new ChartPanel(chart),BorderLayout.CENTER);
		 output = new JTextArea();
		 output.setEditable(false);
		 frame.add(output, BorderLayout.SOUTH);
	}

}
