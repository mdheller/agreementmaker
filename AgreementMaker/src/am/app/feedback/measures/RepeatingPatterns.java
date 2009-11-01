package am.app.feedback.measures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.stanford.nlp.parser.lexparser.GermanUnknownWordModel;

import am.app.Core;
import am.app.feedback.CandidateConcept;
import am.app.feedback.CandidateSelection;
import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.ontology.Node;
import am.app.ontology.Ontology;

public class RepeatingPatterns extends RelevanceMeasure{

	Ontology sourceOntology;
	Ontology targetOntology;
	
	ArrayList<Node> sClasses;
	ArrayList<Node> tClasses;
	ArrayList<Node> sProps;
	ArrayList<Node> tProps;
	
	NodeComparator nc;
	ArrayList<Node> noParentNodes;
	
	//ArrayList<ArrayList<Node>> patterns;
	ArrayList<Integer> patternFreqs;
	ArrayList<Double> repetitiveMeasure;
	
	CandidateConcept.ontology whichOntology;
	alignType whichType;
	
	HashMap<Pattern, Integer> patterns;
	
	public RepeatingPatterns(double th) {
		super(th);
		nc = new NodeComparator();
		Core core = Core.getInstance();
    	sourceOntology = core.getSourceOntology();
    	targetOntology = core.getTargetOntology();
    	sClasses = sourceOntology.getClassesList();
    	tClasses = targetOntology.getClassesList();
    	sProps = sourceOntology.getPropertiesList();
    	tProps = targetOntology.getPropertiesList();
    	//patterns = new ArrayList<ArrayList<Node>>();
    	patternFreqs = new ArrayList<Integer>();
    	repetitiveMeasure = new ArrayList<Double>();
    	patterns = new HashMap<Pattern, Integer>();
	}
	
	//Returns Lex sorted children of a node
	public ArrayList<Node> getChildrenSorted(Node n){
		ArrayList<Node> sortedChildren = new ArrayList<Node>();
		sortedChildren = n.getChildren();
		if(sortedChildren != null){
			sortNodesLex(sortedChildren);
			return sortedChildren;
		}
		else{
			return null;
		}
		
	}
	
	//Sorts given list in Lexicographical order
	public ArrayList<Node> sortNodesLex(ArrayList<Node> nodeList){
		Collections.sort(nodeList, nc);
		return nodeList;
	}
	
	//Sorts given list in Lexicographical order
	public ArrayList<Edge> sortEdgesLex(ArrayList<Edge> edgeList){
		Collections.sort(edgeList);
		return edgeList;
	}
	
	/*
	//
	public void calculateRelevances(int k) {

		whichOntology = CandidateConcept.ontology.source;
		whichType     = alignType.aligningClasses;
		getPatternsGivenLength(sClasses, k);
		
		int i = 0;
		for( i = 0 ; i < patterns.size(); i++){
			createCandidateList(patterns.get(i));
		}
		
		whichOntology = CandidateConcept.ontology.target;
		getPatternsGivenLength(tClasses, k);
		
		int j = 0;
		for( j = i ; j < patterns.size(); j++){
			createCandidateList(patterns.get(j));
		}
		
		whichOntology = CandidateConcept.ontology.source;
		whichType     = alignType.aligningProperties;
		getPatternsGivenLength(sProps, k);
		
		int m = 0;
		for( m = j ; m < patterns.size(); m++){
			createCandidateList(patterns.get(m));
		}
		
		whichOntology = CandidateConcept.ontology.target;
		getPatternsGivenLength(tProps, k);
		
		int n = 0;
		for( n = m ; n < patterns.size(); n++){
			createCandidateList(patterns.get(n));
		}
	}
	*/
	
	//
	public void createCandidateList(ArrayList<Node> ar){
		if(ar.size() == 0 || ar == null)
			return;
		for(int i = 0; i < ar.size(); i++){
			if(!candidateList.contains( ar.get(i) ) )
					candidateList.add( new CandidateConcept( ar.get(i), 1.0d, whichOntology, whichType ));
		}
	}
	
	/*
	//update the relevance measure of candidates in the list
	public void updateCandidateList(){
		for(CandidateConcept cc: candidateList){

			for(int i = 0; i < patterns.size(); i++){
				if(patterns.get(i).contains(cc.getNode())){
					double r = repetitiveMeasure.get(i);
					cc.setRelevance(r);
					if(patternFreqs.get(i) > 1)
						cc.setPatternRepeats(true);
				}
			}
		}
	}
	*/
	
	//
	public ArrayList<Edge> createEdgesFromNodeList(ArrayList<Node> list){
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for(Node n: list){
			ArrayList<Node> nAdj = n.getChildren();
			for(Node child: nAdj){
				Edge e = new Edge(n, child);
				edges.add(e);
			}			
		}
		return edges;
	}
	
	//
	public void run(int k){
		sClasses = sortNodesLex(sClasses);
		ArrayList<Edge> start = createEdgesFromNodeList(sClasses);
		ArrayList<Pattern> finalPatterns = getPatternsGivenLength(start, k);
		printPatterns(finalPatterns);
	}
	
	//Generate patterns of length k
	public ArrayList<Pattern> getPatternsGivenLength(ArrayList<Edge> list, int k){
		
		ArrayList<Pattern> pats = new ArrayList<Pattern>();
		ArrayList<Edge> edges = sortEdgesLex(list);
		Node srcNode = null;
		
		ArrayList<Edge> edgeSeq = new ArrayList<Edge>();
		
		for(Edge a : edges){
			srcNode = a.getSourceNode();
			ArrayList<Node> srcNodeChildren = getChildrenSorted(srcNode);
			
			ArrayList<Edge> asd = createEdgesFromNodeList(srcNodeChildren);
			if(asd.contains(a)){
				asd.remove(a);
			}
			
			
			a.setSourceVisit(1);
			a.setTargetVisit(2);
			edgeSeq.add(a);
			Pattern p = new Pattern(1, edgeSeq, null);
			p.setLastVisit(2);
			
			if(patterns.containsKey(p)){
				Integer aVal = patterns.get(p);
				int val = aVal.intValue() + 1;
				aVal = new Integer(val);
				patterns.put(p, aVal);
			}
			else{
				Integer freq = new Integer(1);
				patterns.put(p, freq);
			}
			//get the siblings of the edge passed in, and recurse again on them
			growEdge(p, a, k, pats, false);
			//create edgelist of that list
			//
			ArrayList<Pattern> pp = new ArrayList<Pattern>();
			for(Edge b: asd){
				growEdge(p, b, k, pp, true);
			}
		}
		
		return pats;
	}
	
	
	
	//Find repeating patterns here
	public void growEdge(Pattern p, Edge a, int k, ArrayList<Pattern> pats, boolean st){

		//Decide growing source or target
		Node nodeToGrow = null;
		if(st){
			nodeToGrow = a.getSourceNode();
		}
		else{
			nodeToGrow = a.getTargetNode();
		}
		
		ArrayList<Pattern> pats2 = new ArrayList<Pattern>();
		
		//Nodes are sorted lex
		ArrayList<Node> ntgADJ = nodeToGrow.getChildren();
		ntgADJ = sortNodesLex(ntgADJ);
		
		//ArrayList<Edge> ntgEdges = createEdgesFromNodeList(ntgADJ);
		
		while(!ntgADJ.isEmpty()){
			Node removed = ntgADJ.get(0);
			Edge passInRecursion = new Edge(nodeToGrow, removed);
			ntgADJ.remove(ntgADJ.get(0));
			ArrayList<Edge> edgSeq = new ArrayList<Edge>();
			edgSeq.add(passInRecursion);
			
			Pattern nextPattern = new Pattern(p.getLength()+1, edgSeq, p);
			Pattern copyOfPattern = new Pattern(nextPattern);//push ...
						
			pats.add(nextPattern);
			//increment freq
			//...
			
			//check max len pattern size is reached
			//first recursion
			//if less than k do recursions
			if(copyOfPattern.getLength() < k)
				growEdge(copyOfPattern, passInRecursion, k, pats2, st);
			
			for(int i = 0; i < pats2.size(); i++)
			{
				Pattern p2 = pats2.get(i);
				pats.add(pats2.get(i));
				
				//Node curr = passInRecursion.getTargetNode();
				//ArrayList<Node> currN = getChildrenSorted(curr);
				//ArrayList
				
				growEdge(p2, passInRecursion, k, pats, st); //grow also on pats2.get(i)
			}
			
		}
		
		if(patterns.containsKey(p)){
			Integer aVal = patterns.get(p);
			int val = aVal.intValue() + 1;
			aVal = new Integer(val);
			patterns.put(p, aVal);
		}
		else{
			Integer freq = new Integer(1);
			patterns.put(p, freq);
		}
		
	}
	
	//Print patterns in the list
	public void printPatterns(){
		for(int i = 0; i < patterns.size(); i++){
			System.out.println(patterns.get(i));
		}
	}
	
	//Print patterns in the list
	public void printPatterns(ArrayList<Pattern> pats){
		for(int i = 0; i < pats.size(); i++){
			System.out.println(pats.get(i).toString());
		}
	}
	
	/*
	//Calculates frequencies for all patterns
	public double patternFreq(){
		for(int i = 0; i < patterns.size(); i++){
			int freq = 0;
		for(ArrayList<Node> ar1: patterns){
				if(ar1.equals(patterns.get(i))){
					freq++;
				}
			}
			patternFreqs.add(new Integer(freq));
		}
		return 0.0;
	}
	*/
	
	//Returns length of given pattern
	public double patternLen(ArrayList<Node> pattern){
		return pattern.size();
	}
	
	/* 
	//Calculate repetitive measure for each pattern in the list
	public void repetitiveMeasure(){
		for(int i = 0; i < patterns.size(); i++){
			double repM = patternFreqs.get(i)/patternLen(patterns.get(i));
			repetitiveMeasure.add(repM);
		}
	}
	*/
	
	/*
	//Returns a pattern with highest repetitive measure for a given node
	public ArrayList<Node> getRepeatingPattern(Node a){
		ArrayList<Node> maxRepMlist = new ArrayList<Node>();
		int maxRep = -1;
		for(int i = 0; i < patterns.size(); i++){
			if(patterns.get(i).contains(a) && patternFreqs.get(i) > maxRep){
				maxRepMlist = patterns.get(i);
			}
		}
		return maxRepMlist;
	}
	*/
	
	//Returns frequency of given pattern
	public int getFreqOfPattern(ArrayList<Node> ar){
		for(int i = 0; i < patterns.size(); i++){
			if(ar.equals(patterns.get(i)))
			{
				return patternFreqs.get(i);
			}
		}
		return -1;
	}
	
	/*
	//Returns true if a given node is in a repeating pattern
	public boolean repeats(Node n){
		int i = 0;
		for(ArrayList<Node> ar: patterns){
			if(ar.contains(n) && patternFreqs.get(i)>1)
				return true;
			i++;
		}
		return false;
		
	}
	*/
	
	//
	public double averageRelevance(){
		return 0.0;
	}
}