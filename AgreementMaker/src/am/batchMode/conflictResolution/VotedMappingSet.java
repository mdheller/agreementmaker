package am.batchMode.conflictResolution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;

import am.application.mappingEngine.AbstractMatcher;
import am.application.mappingEngine.Alignment;
import am.application.mappingEngine.AlignmentSet;
import am.application.ontology.Node;

public class VotedMappingSet {
	
	
	public HashMap<Integer,VotedMapping> sourceMappings;
	public HashMap<Integer,VotedMapping> targetMappings;
	public int sourceOntologyIndex;
	public int targetOntologyIndex;
	
	public VotedMappingSet(AlignmentSet aset, int sourceOnt, int targetOnt){
		sourceOntologyIndex = sourceOnt;
		targetOntologyIndex = targetOnt;
		Alignment a;
		VotedMapping v;
		sourceMappings = new HashMap<Integer, VotedMapping>();
		targetMappings = new HashMap<Integer, VotedMapping>();
		for(int i = 0; i < aset.size(); i++){
			a = aset.getAlignment(i);
			v = new VotedMapping(a, this);
			sourceMappings.put(a.getSourceKey(),v);
			targetMappings.put(a.getTargetKey(),v);
		}
	}
	//GET SOURCE 
	public VotedMapping getSourceVotedMapping(int k){
		return sourceMappings.get(k);
	}
	
	public VotedMapping getSourceVotedMapping(Node n){
		return getSourceVotedMapping(n.getIndex());
	}
	
	public VotedMapping getSourceVotedMapping(Alignment a){
		return getSourceVotedMapping(a.getSourceKey());
	}
	//GET TARGET
	public VotedMapping getTargetVotedMapping(int k){
		return targetMappings.get(k);
	}
	
	public VotedMapping getTargetVotedMapping(Node n){
		return getTargetVotedMapping(n.getIndex());
	}
	
	public VotedMapping getTargetVotedMapping(Alignment a){
		return getTargetVotedMapping(a.getTargetKey());
	}
	
	
	//PUT MAPPING
	public void putVotedMapping(Node sNode, Node tNode, double sim, String rel){
		Alignment a = new Alignment(sNode, tNode, sim, rel);
		putVotedMapping(a);
	}
	
	public void putVotedMapping(Alignment a){
		 VotedMapping v = new VotedMapping(a, this);
		 putVotedMapping(v);
	}
	
	public void putVotedMapping(VotedMapping v){
		sourceMappings.put(v.mapping.getSourceKey(),v);
		targetMappings.put(v.mapping.getTargetKey(),v);
	}
	//DEL
	public void delVotedMapping(Alignment a){
		//while(sourceMappings.containsKey(a.getSourceKey()))
			sourceMappings.remove(a.getSourceKey());
			boolean joption = false;
			if(sourceMappings.containsKey(a.getSourceKey())){
				System.out.println("Wrong deletion sources still contains "+a);
				joption = true;
				
			}
			 if(sourceMappings.get(a.getSourceKey()) != null){
				joption = true;
				System.out.println("Wrong deletion sources still get "+sourceMappings.get(a.getSourceKey())+" get "+a);
			}
		//while(targetMappings.containsKey(a.getTargetKey()))
			targetMappings.remove(a.getTargetKey());
			if(targetMappings.containsKey(a.getTargetKey())){
				System.out.println("Wrong deletion targets still contains "+a);
				joption = true;
			}
			if(targetMappings.get(a.getTargetKey()) != null){
				 joption = true;
				System.out.println("Wrong deletion targets still get "+targetMappings.get(a.getTargetKey())+" get "+a);
			}
			if(joption){
				JOptionPane.showInputDialog("");
			}
	}
	
	public void delVotedMapping(VotedMapping v){
		delVotedMapping(v.mapping);
	}
	
	public Collection<VotedMapping> getVotedMappings(){
		//is the same if doing targetMappings.values()
		return sourceMappings.values();
	}
	

	public AlignmentSet getAlignmentSet(){
		AlignmentSet set = new AlignmentSet();
		Iterator<VotedMapping> it = getVotedMappings().iterator();
		VotedMapping v;
		while(it.hasNext()){
			v = it.next();
			if(v.validated){
				set.addAlignment(v.mapping);
			}
			else throw new RuntimeException("Development error: all mappings should be validated or deleted in the conflict resultion");
		}
		return set;
	}
	
	public String toString(){
		Iterator<VotedMapping> it = getVotedMappings().iterator();
		VotedMapping v;
		String result = "VotedMappingSet between onto "+sourceOntologyIndex+" and "+targetOntologyIndex+"\n";
		while(it.hasNext()){
			v = it.next();
			result+=v+"\n";
		}
		return result;
	}

	

}
