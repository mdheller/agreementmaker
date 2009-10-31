package am.app.feedback.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import am.app.feedback.FilteredAlignmentMatrix;
import am.app.feedback.InitialMatchers;
import am.app.feedback.measures.FamilialSimilarity;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.AlignmentSet;
import am.app.ontology.Node;
import am.userInterface.vertex.Vertex;

public class ExtrapolatingFS extends AbstractMatcher {
	
	InitialMatchers im = new InitialMatchers();
	
	
	protected FilteredAlignmentMatrix inputClassesMatrix = null;
	protected FilteredAlignmentMatrix inputPropertiesMatrix = null;
	
	protected FilteredAlignmentMatrix matrix;
	
	
	public void match( AlignmentSet<Alignment> userMappings ) throws Exception {
	
		FamilialSimilarity fs_measure = new FamilialSimilarity( threshold );
		AlignmentSet<Alignment> newMappings = new AlignmentSet<Alignment>();
		
		
		int numMappings = userMappings.size();
		for( int i = 0; i < numMappings; i++ ) {
			
			Alignment e1e2 = userMappings.getAlignment(i);
			
			ArrayList<Vertex> e1_sibblingList = new ArrayList<Vertex>();
			
			int numChildren;
			Vertex parent = (Vertex) e1e2.getEntity1().getVertex().getParent();
			if( parent == null ) { numChildren = 0; }
			else { numChildren = parent.getChildCount(); }
			
			for( int j = 0; j < numChildren; j++ ) {
				Vertex s1 = (Vertex) parent.getChildAt(j);
				if( !s1.getNode().equals(e1e2.getEntity1()) ) e1_sibblingList.add(s1);
			}
			
			HashMap<Node, Double> e1_simAboveThreshold = fs_measure.simSetAboveThreshold(e1_sibblingList, e1e2.getEntity1().getVertex() );
			
			
			
			
			ArrayList<Vertex> e2_sibblingList = new ArrayList<Vertex>();
			
			parent = (Vertex) e1e2.getEntity2().getVertex().getParent();
			if( parent == null ) { numChildren = 0; }
			else { numChildren = parent.getChildCount(); }
			
			for( int j = 0; j < numChildren; j++ ) {
				Vertex s2 = (Vertex) parent.getChildAt(j);
				if( !s2.getNode().equals( e1e2.getEntity2() ) ) {
					e2_sibblingList.add(s2);
				}
			}
			
			HashMap<Node, Double> e2_simAboveThreshold = fs_measure.simSetAboveThreshold( e2_sibblingList, e1e2.getEntity2().getVertex() );
			
			
			
			
			newMappings.addAll( compare_sets( e1_simAboveThreshold , e2_simAboveThreshold ) );

			
			
		}
	
		
		// now add the new mappings to the alignment sets
		classesAlignmentSet = new AlignmentSet<Alignment>();
		propertiesAlignmentSet = new AlignmentSet<Alignment>();
		for( int i = 0; i < newMappings.size(); i++ ) {
			Alignment j = newMappings.getAlignment(i);
			
			if ( j.getAlignmentType() == alignType.aligningClasses ) {
				classesAlignmentSet.addAlignment(j);
			} else {
				propertiesAlignmentSet.addAlignment(j);
			}
		}
	
	}


	private AlignmentSet<Alignment> compare_sets( HashMap<Node, Double> e1SimAboveThreshold, HashMap<Node, Double> e2SimAboveThreshold) {
		
		AlignmentSet<Alignment> newMappings = new AlignmentSet<Alignment>();
		
		Set<Node> e1s = e1SimAboveThreshold.keySet();
		Set<Node> e2s = e2SimAboveThreshold.keySet();
		
		Iterator<Node> e1i = e1s.iterator();
		while( e1i.hasNext() ) {
			Node e1n = e1i.next();
			
			Iterator<Node> e2i = e2s.iterator();
			while( e2i.hasNext() ) {
				Node e2n = e2i.next();
				
				if( approx( e1SimAboveThreshold.get(e1n) , e2SimAboveThreshold.get(e2n) , 0.01 ) ) {
					Alignment newmapping = new Alignment(e1n, e2n, 1.00d );
					newMappings.addAlignment( newmapping );
				}
				
			}
		}
		
		
		return newMappings;
		
	}
	
	private boolean approx( Double i, Double j, Double delta ) {
		
		
		if( j <= i+delta && j >= i-delta ) return true;
		return false;
	}

}