package am.app.mappingEngine.PRAintegration;

import java.util.ArrayList;
import java.util.HashSet;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.AbstractMatcherParametersPanel;
import am.app.mappingEngine.Mapping;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.MappedNodes;
import am.app.mappingEngine.MatcherFactory;
import am.app.mappingEngine.MatchersRegistry;
import am.app.mappingEngine.SimilarityMatrix;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentParametersPanel;
import am.app.mappingEngine.similarityMatrix.ArraySimilarityMatrix;
import am.app.ontology.Node;

public class PRAintegrationMatcher extends AbstractMatcher {
	
	private static final long serialVersionUID = -2666184985092759802L;
	
	//It uses the same parameters and parameters panel of the ReferenceAlignmentMatcher
	AbstractMatcher referenceAlignmentMatcher;
	
	public PRAintegrationMatcher(){
		super();
		maxInputMatchers = 1;
		minInputMatchers = 1;
		needsParam = true;
		
	}
	
	
	public AbstractMatcherParametersPanel getParametersPanel() {
		if(parametersPanel == null){
			parametersPanel = new ReferenceAlignmentParametersPanel();
		}
		return parametersPanel;
	}
	
	public void match() throws Exception {
    	matchStart();
		super.beforeAlignOperations();
		relation = inputMatchers.get(0).getRelation();
		referenceAlignmentMatcher = MatcherFactory.getMatcherInstance(MatchersRegistry.ImportAlignment, 0);
		referenceAlignmentMatcher.setInputMatchers(inputMatchers);
		referenceAlignmentMatcher.setParam(param);
		referenceAlignmentMatcher.match();
	    
		integrateAlignment(sourceOntology.getPropertiesList(), targetOntology.getPropertiesList(), inputMatchers.get(0).getPropertyAlignmentSet(), referenceAlignmentMatcher.getPropertyAlignmentSet(),referenceAlignmentMatcher.getPropertiesMatrix() ,  alignType.aligningProperties);
		integrateAlignment(sourceOntology.getClassesList(), targetOntology.getClassesList(), inputMatchers.get(0).getClassAlignmentSet(), referenceAlignmentMatcher.getClassAlignmentSet(), referenceAlignmentMatcher.getClassesMatrix(), alignType.aligningClasses);

    	matchEnd();
    	//System.out.println("Classes alignments found: "+classesAlignmentSet.size());
    	//System.out.println("Properties alignments found: "+propertiesAlignmentSet.size());
    }
    

	public void buildSimilarityMatrices()throws Exception{
		//do nothing
    }

    public void select() {
    	//do nothing
    }
	
	protected void integrateAlignment(ArrayList<Node> sourceList,
			ArrayList<Node> targetList, Alignment<Mapping> inputAlignmentSet,
			Alignment<Mapping> refAlignmentSet, SimilarityMatrix refAlignmentMatrix, alignType typeOfNodes)  throws Exception{
		

		SimilarityMatrix resultMatrix = new ArraySimilarityMatrix(sourceList.size(), targetList.size(), typeOfNodes, inputMatchers.get(0).getRelation());
		Alignment<Mapping> resultSet = new Alignment<Mapping>();
		HashSet<Mapping> mappings = new HashSet<Mapping>();
		
		//make the mapping set equals to the reference matching.
		Mapping alignment;
		for(int i = 0; i < refAlignmentSet.size(); i++){
			alignment = refAlignmentSet.get(i);
			mappings.add(alignment);
			resultSet.add(alignment);
			resultMatrix.set( alignment.getEntity1().getIndex(), alignment.getEntity2().getIndex(), 
					new Mapping(alignment.getEntity1(), alignment.getEntity2(), alignment.getSimilarity()));
		}

		//keep track of what is already been mapped enough times in the PRA and that can't be mapped by the matcher
		MappedNodes refMappedNodes = new MappedNodes(sourceList, targetList, refAlignmentSet, getMaxSourceAlign(), getMaxTargetAlign());
		
		//add to the mapping set those mappings found by the matcher that are compatible with reference
		Node source;
		Node target;
		for(int i = 0; i < inputAlignmentSet.size(); i++){
			alignment = inputAlignmentSet.get(i);
			source = alignment.getEntity1();
			target = alignment.getEntity2();
			if(!refMappedNodes.isSourceMapped(source) && !refMappedNodes.isTargetMapped(target)){
				if(!mappings.contains(alignment)){
					mappings.add(alignment);
					resultSet.add(alignment);
					resultMatrix.set( alignment.getEntity1().getIndex(), alignment.getEntity2().getIndex(), 
							new Mapping(alignment.getEntity1(), alignment.getEntity2(), alignment.getSimilarity()));
					refMappedNodes.addAlignment(alignment);
				}
			}
		}
		
		if(typeOfNodes.equals(alignType.aligningClasses)){
			classesAlignmentSet = resultSet;
			classesMatrix = resultMatrix;
		}
		else{
			propertiesAlignmentSet = resultSet;
			propertiesMatrix = resultMatrix;
		}
	}




}
