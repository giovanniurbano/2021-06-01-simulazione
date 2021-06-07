package it.polito.tdp.genes.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Simulator {
	//coda degli eventi
	private PriorityQueue<Event> queue;

	//modello del mondo 
	//1) dato un ingegnere (0...n-1), dimmi su quale gene lavora
	private List<Genes> geneStudiato; //geneStudiato.get(nIng)
	//2) dato un gene, dimmi quanti ingegneri ci lavorano
	//private Map<Genes, Integer> numIngegneri;
	
	//parametri input
	private Genes startGene;
	private int nTotIng;
	private Graph<Genes, DefaultWeightedEdge> grafo;
	
	private int TMAX = 36; //numero mesi di simulazione
	private double probMantenereGene = 0.3;
	
	//valori calcolati
	//li dedurremo dal geneStudiato
	
	public Simulator(Genes start, int n, Graph<Genes, DefaultWeightedEdge> grafo) {
		this.startGene = start;
		this.nTotIng = n;
		this.grafo = grafo;
		
		if(this.grafo.degreeOf(this.startGene) == 0) {
			throw new IllegalArgumentException("Vertice di partenza isolato");
		}
		
		//inizializzo la coda
		this.queue = new PriorityQueue<Event>();
		for(int nIng=0; nIng<this.nTotIng; nIng++) {
			this.queue.add(new Event(0, nIng));
		}
		
		//inizializzo il mondo, creando un array con nTotIng valori paria startGene
		this.geneStudiato = new ArrayList<Genes>();
		for(int nIng=0; nIng<this.nTotIng; nIng++) {
			this.geneStudiato.add(this.startGene);
		}
	}
	
	public void run() {	
		while(!this.queue.isEmpty()) {
			Event ev = this.queue.poll();
			
			int T = ev.getT();
			int nIng = ev.getnIng();
			Genes g = this.geneStudiato.get(nIng);
			
			if(T < this.TMAX) {
				//cosa studierà nIng al mese T+1?
				if(Math.random() < this.probMantenereGene) {
					//mantieni
					this.queue.add(new Event(T+1, nIng));
				}
				else {
					//cambia gene
					
					//calcola somma dei pesi degli adiacenti, S
					double S = 0;
					for(DefaultWeightedEdge edge : this.grafo.edgesOf(g)) {
						S += this.grafo.getEdgeWeight(edge);
					}
					
					//estrai numero casuale R tra 0 ed S
					double R = Math.random()*S;
					
					//confronta R con la somma parziale dei pesi
					Genes nuovo = null;
					double somma = 0.0;
					for(DefaultWeightedEdge edge : this.grafo.edgesOf(g)) {
						somma += this.grafo.getEdgeWeight(edge);
						if(somma >= R) {
							nuovo = Graphs.getOppositeVertex(this.grafo, edge, g);
							break;
						}
					}
					this.geneStudiato.set(nIng, nuovo);
					this.queue.add(new Event(T+1, nIng));
				}
			}
		}
	}
	
	public Map<Genes, Integer> getGeniStudiati() {
		Map<Genes, Integer> studiati = new HashMap<Genes, Integer>();
		
		for(int nIng=0; nIng<this.nTotIng; nIng++) {
			Genes g = this.geneStudiato.get(nIng);
			if(studiati.containsKey(g))
				studiati.put(g, studiati.get(g)+1);
			else
				studiati.put(g, 1);
		}
		
		return studiati;
	}
}
