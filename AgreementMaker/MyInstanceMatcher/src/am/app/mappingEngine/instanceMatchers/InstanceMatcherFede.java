package am.app.mappingEngine.instanceMatchers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.AlignmentsOutput;
import am.app.mappingEngine.KeywordsUtils;
import am.app.mappingEngine.Porter;
import am.app.mappingEngine.ScoredInstance;
import am.app.mappingEngine.ScoredInstanceComparator;
import am.app.mappingEngine.WordNetUtils;
import am.app.mappingEngine.Mapping.MappingRelation;
import am.app.mappingEngine.StringUtil.StringMetrics;
import am.app.mappingEngine.instanceMatcher.LabelUtils;
import am.app.mappingEngine.referenceAlignment.MatchingPair;
import am.app.ontology.instance.Instance;
import am.utility.referenceAlignment.AlignmentUtilities;

public class InstanceMatcherFede extends AbstractMatcher {

	int ambiguous;
	int noResult;
	int singleResult;
	int solvable;
	
	int disambiguationMappings = 0;
	
	double labelSimThreshold = 0.9;
	double keyScoreThreshold = 1;
	
	double threshold = 0.8;
	
	boolean disambiguate = true;
	
	WordNetUtils wordNetUtils;
	
	Porter stemmer = new Porter();
	
	boolean matchingDBPedia = false;
	boolean verbose = false;
	private String outputFilename = "alignments.rdf";
	
	private static final long serialVersionUID = -8278698313888419789L;
	
	Logger log = Logger.getLogger(InstanceMatcherFede.class);;
	
	StatementsInstanceMatcher stim = new StatementsInstanceMatcher();
	LabelInstanceMatcher lim = new LabelInstanceMatcher();
	
	@Override
	protected void beforeAlignOperations() throws Exception {
		super.beforeAlignOperations();
		//log.setLevel(Level.DEBUG);
		
		wordNetUtils = new WordNetUtils();
		
		performSelection = false;
	}
	
	@Override
	protected MatchingPair alignInstanceCandidates(Instance sourceInstance,
			List<Instance> targetCandidates) throws Exception {
		
		//System.out.println("Source instance: " + sourceInstance );
		//System.out.println("Target instance list: " + targetCandidates );
		//System.out.println("");
		
		//progressDisplay.appendToReport(sourceInstance.toString() + "\n");
		
		int size = targetCandidates.size();
		if(size == 0){
			noResult++;
			return null;
		}
		else if(size == 1) singleResult++;
		else if(size > 1) ambiguous++;
		
		if(referenceAlignment != null && AlignmentUtilities.candidatesContainSolution(referenceAlignment, 
				sourceInstance.getUri(), targetCandidates) != null)
			solvable++;
				
		String sourceLabel = sourceInstance.getSingleValuedProperty("label");
		
		sourceLabel = LabelInstanceMatcher.processLabel(sourceLabel);
		
		List<String> sourceKeywords = new ArrayList<String>();
		String betweenParentheses = getBetweenParentheses(sourceLabel);
		if(betweenParentheses != null) sourceKeywords.add(betweenParentheses);
		
		log.debug("between parentheses: " + betweenParentheses);
		
		
		//TODO this has to be eliminated, it's not general. This is dataset preprocessing and should be done outside
		List<String> articles = sourceInstance.getProperty("article");
		Instance article;
		List<String> desKeywords;
		List<String> orgKeywords;	
		for (int i = 0; i < articles.size(); i++) {
			//Get the first of the list
			String articleURI = articles.get(i);
			article = sourceOntology.getInstances().getInstance(articleURI);
			//System.out.println(article);
			desKeywords = article.getProperty("descriptionKeywords");
			orgKeywords = article.getProperty("organizationKeywords");
			
			String title = article.getSingleValuedProperty("title");
			List<String> titleKeywords = new ArrayList<String>();
			titleKeywords.add(title);
			titleKeywords = KeywordsUtils.processKeywords(titleKeywords);
			
			if(desKeywords == null) desKeywords = orgKeywords;
						
			if(desKeywords != null){
				//desKeywords.addAll(titleKeywords);

				if(orgKeywords != null){
					desKeywords.addAll(orgKeywords);
				}
				String keyword;
				for (int j = 0; j < desKeywords.size(); j++) {
					keyword = desKeywords.get(j).toLowerCase();
					if(!sourceKeywords.contains(keyword)){
						sourceKeywords.add(keyword);
					}
				}
			}
//			System.out.println(article);
			//System.out.println("title: " + title);
		}
		
		if(size == 1){
			Instance target = targetCandidates.get(0);
			MatchingPair pair = null;
			
			double score = instanceSimilarity(sourceInstance, target, sourceLabel, sourceKeywords);
			
			// && score >= threshold
			if(!target.getUri().contains("wiki"))
				pair = new MatchingPair(sourceInstance.getUri(), target.getUri(), score, MappingRelation.EQUIVALENCE);
			debugMapping(pair);
			
			return pair;
		}
		
		else{
			if(disambiguate == false) return null;
			
			log.debug("Case of ambiguity:" + sourceInstance.getUri() + " " + sourceLabel + " " + targetCandidates.size());
			
			log.debug(sourceKeywords);
			sourceKeywords = KeywordsUtils.processKeywords(sourceKeywords);
			log.debug(sourceKeywords);
			
			Instance candidate;
			double score;
			List<ScoredInstance> scoredCandidates = new ArrayList<ScoredInstance>();
			for (int i = 0; i < targetCandidates.size(); i++) {
				candidate = targetCandidates.get(i);
				score = instanceSimilarity(sourceInstance, candidate, sourceLabel, sourceKeywords);
				scoredCandidates.add(new ScoredInstance(candidate, score));
			}
			
			Collections.sort(scoredCandidates, new ScoredInstanceComparator());	
			
			for (int i = 0; i < scoredCandidates.size(); i++) {
				ScoredInstance scoredInstance = scoredCandidates.get(i);
				if(scoredInstance.getInstance().getUri().contains("wiki")){
					scoredCandidates.remove(scoredInstance);
					i--;
				}
			}
			
			scoredCandidates = ScoredInstance.filter(scoredCandidates, 0.01);
			
			if(scoredCandidates.size() == 1){
				//System.out.println("mapping, score:" + scoredCandidates.get(0).getScore());
				disambiguationMappings++;
				double candidateScore = scoredCandidates.get(0).getScore();
				MatchingPair pair = new MatchingPair(sourceInstance.getUri(), scoredCandidates.get(0).getInstance().getUri(), candidateScore, MappingRelation.EQUIVALENCE);
				debugMapping(pair);
				if(verbose) System.out.println("Generated mapping: " + pair.sourceURI + " " + pair.targetURI);
				//System.out.println("About to match: " + candidateScore);
				if (candidateScore < threshold) return null;
				return pair;
			}
		}
		return null;
	}
	
	private double instanceSimilarity(Instance sourceInstance,
			Instance candidate, String sourceLabel, List<String> sourceKeywords) throws Exception {

		double keyScore = 0;
		double freebaseScore = 0;
		double labelSim = 0;
				
		labelSim = lim.instanceSimilarity(sourceInstance, candidate);
		
		String value = candidate.getSingleValuedProperty("score");
		
		if(value != null) freebaseScore = Double.valueOf(value);
		else freebaseScore = -1;
		if(freebaseScore != -1) freebaseScore /= 100;
		
		if(verbose) System.out.println("candidate: " + candidate.getUri() + " " + candidate.getSingleValuedProperty("label"));
		List<String> types = candidate.getProperty("type");
		
		//Specific for freebase
		if(types != null){
			types = KeywordsUtils.processKeywords(types);
			keyScore = keywordsSimilarity(sourceKeywords, types);
			if(verbose) System.out.println("types: " + types);
		}
		
		List<Statement> stmts = candidate.getStatements();
		List<String> candidateKeywords = new ArrayList<String>();
		for (int i = 0; i < stmts.size(); i++) {
			if(stmts.get(i).getPredicate().equals(RDFS.comment)){
				String comment = stmts.get(i).getObject().asLiteral().getString();
				candidateKeywords.add(comment);
				if(verbose) System.out.println("Comment:" + comment);
			}
		}
		candidateKeywords = KeywordsUtils.processKeywords(candidateKeywords);
		if(verbose) System.out.println(sourceKeywords);
		//System.out.println(candidateKeywords);
		
//		if(types != null)
//			candidateKeywords.addAll(types);	
		
		keyScore = keywordsSimilarity(sourceKeywords, candidateKeywords);
				
		if(verbose) System.out.println(keyScore);
		
		double stmtSim = stim.instanceSimilarity(sourceInstance, candidate);
		
		if(verbose) System.out.println("lab:" + labelSim + " frb:" + freebaseScore + " key:" + keyScore + " stmtSim:" + stmtSim);
		
		if(freebaseScore == -1) freebaseScore = 0;
		if(stmtSim == -1) stmtSim = 0;
		
		double score = labelSim/2 + stmtSim/2 + 1*keyScore + freebaseScore/2;
		
		//double score = labelSim;
		
		
		if(verbose) System.out.println("score:" + score);
	
		
		return score;
	}

	private double keywordsSimilarity(List<String> sourceList, List<String> targetList){
		//Compute score
		double score = 0;
		String source;
		String target;
		for (int j = 0; j < sourceList.size(); j++) {
			source = sourceList.get(j);
			if(source.isEmpty()) continue;
			source = source.toLowerCase();
			for (int t = 0; t < targetList.size(); t++) {
				target = targetList.get(t).toLowerCase();
				if(target.isEmpty()) continue;
				//System.out.println(type + "|" + keyword);
				if(source.equals(target)){
					score++;
				}
				
				if(wordNetUtils.areSynonyms(source, target) ){
					score += 0.5;
					//System.out.println("matched syn: " + source + "|" + target);
				}
				else{
					boolean condition = false;
					try{
						condition = stemmer.stripAffixes(source).equals(stemmer.stripAffixes(target));
						if(condition)
							score += 0.5;
					}
					catch (Exception e) {
						System.err.println("Error when stemming " + source + " with " + target);
					}
				}
			}
		}
		
		double avg = (Math.min(sourceList.size(), targetList.size()) 
				+ Math.max(sourceList.size(), targetList.size())) / 2;
		double max = Math.max(sourceList.size(), targetList.size());
		
		if(max == 0) return 0;
		
		score /= max;
		
		return score;
	}
	
	

	@Override
	protected void afterAlignOperations() {
		super.afterAlignOperations();
		System.out.println("Ambiguous: " + ambiguous);
		System.out.println("No Results: " + noResult);
		System.out.println("Single result: " + singleResult);
		System.out.println("Total: " + (ambiguous + noResult + singleResult));
		if(referenceAlignment != null)
			System.out.println("InReference: " + referenceAlignment.size());
		
		System.out.println("Solvable: " + solvable);
		
		System.out.println("Disambiguated: " + disambiguationMappings);
		
		//checkDoubleMappings();
		
		if(matchingDBPedia){
			//cleanDBPediaMappings();
		}
		
		System.out.println("Writing on file...");
		String output = AlignmentsOutput.alignmentsToOutput(instanceAlignmentSet);
		FileOutputStream fos;
		
		try {
			fos = new FileOutputStream("alignment.rdf");
			fos.write(output.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
		
		AlignmentsOutput.writeMappingsOnDisk(outputFilename, instanceAlignmentSet);
		
	}
	
	private void checkDoubleMappings() {
		HashMap<String, List<MatchingPair>> pairsByTarget = new HashMap<String, List<MatchingPair>>(); 
		int count = 0;
		MatchingPair mp;
		for(int i = 0; i < instanceAlignmentSet.size(); i++){
			mp = instanceAlignmentSet.get(i);
			if(!pairsByTarget.containsKey(mp.targetURI)){
				List<MatchingPair> pairs = new ArrayList<MatchingPair>();
				pairs.add(mp);
				pairsByTarget.put(mp.targetURI, pairs);
			}
			else{
				List<MatchingPair> pairs = pairsByTarget.get(mp.targetURI);
				pairs.add(mp);
				log.info("Duplicated mapping: " + mp.targetURI);
				count++;
			}
		}
		log.info("We have " + count + " duplicated mappings");
		
		for(String key: pairsByTarget.keySet()){
			List<MatchingPair> pairs = pairsByTarget.get(key);
			if(pairs.size() > 1){
				log.info(pairs);
				for (int i = 0; i < pairs.size(); i++) {
					instanceAlignmentSet.remove(pairs.get(i));
				}
			}
		}
	}
	

	
	public static String getBetweenParentheses(String label){
		if(label.contains("(")){
			int beg = label.indexOf('(');
			int end = label.indexOf(')');
			return label.substring(beg + 1, end);
		}
		return null; 
	}
	
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}
	
	public boolean areMatched(String sourceURI, String targetURI){
		for (int i = 0; i < referenceAlignment.size(); i++) {
			if(referenceAlignment.get(i).sourceURI.equals(sourceURI)){
				if(referenceAlignment.get(i).targetURI.equals(targetURI)){
					return true;
				}
				else return false;
			}
		}
		return false;
	}
	
	public void debugMapping(MatchingPair pair){
		if(pair == null) return;
		String source = pair.sourceURI;
		for (int i = 0; i < referenceAlignment.size(); i++) {
			if(referenceAlignment.get(i).sourceURI.equals(source)){
				if(referenceAlignment.get(i).sameTarget(pair)){
					log.debug("RIGHT MAPPING " + referenceAlignment.get(i));
				}
				else{
					log.debug("WRONG MAPPING right:" + referenceAlignment.get(i));
				}
			}
		}
	}
	
	@Override
	public String processLabelBeforeCandidatesGeneration(String label, String type) {
		log.debug(label + "\t" + type);
		
		if(type == null) return super.processLabelBeforeCandidatesGeneration(label, type);
		
		if(type.toLowerCase().endsWith("organization"))
			return LabelUtils.processOrganizationLabel(label);
		
		else if(type.toLowerCase().endsWith("person"))
			return LabelUtils.processPersonLabel(label);
		
		else if(type.toLowerCase().endsWith("location"))
			return LabelUtils.processLocationLabel(label);
		
		return super.processLabelBeforeCandidatesGeneration(label, type);
	}
}
