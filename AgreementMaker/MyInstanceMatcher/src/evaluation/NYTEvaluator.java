package evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

import am.app.mappingEngine.referenceAlignment.MatchingPair;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentMatcher;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentParameters;

public class NYTEvaluator {
	/*
	 * Put the path of the alignment file you want to evaluate
	 * (the one you generated)
	 */
	static String toEvaluate = "C:/Users/federico/workspace/MyInstanceMatcher/alignment.rdf";
	/*
	 * Put the path of the reference alignment file
	 * Also paths relative to the root of the project are ok.
	 */
	static String reference = "OAEI2011/NYTReference/nyt-freebase-people-mappings.rdf";
	
	static boolean printWrongMappings = true;
	
	public static String evaluate(String file, String reference, double threshold) throws Exception{
		ReferenceAlignmentMatcher matcher = new ReferenceAlignmentMatcher();
		
		ReferenceAlignmentParameters param = new ReferenceAlignmentParameters();
		param.fileName = toEvaluate;
		matcher.setParam(param);		
		ArrayList<MatchingPair> filePairs = matcher.parseStandardOAEI();
		
		param.fileName = reference;
		ArrayList<MatchingPair> refPairs = matcher.parseStandardOAEI();
		
		return compare(filePairs, refPairs, threshold);	
	}
	
	public static String compare(ArrayList<MatchingPair> toEvaluate, ArrayList<MatchingPair> reference, double threshold){
		int count = 0;
		MatchingPair p1;
		MatchingPair p2;
		
		boolean found;
		for (int i = 0; i < toEvaluate.size(); i++) {
			found = false;
			p1 = toEvaluate.get(i);
			
			for (int j = 0; j < reference.size(); j++) {
				p2 = reference.get(j);
				//System.out.println(p2.getTabString());
				if(p1.sourceURI.equals(p2.sourceURI) && p1.targetURI.equals(p2.targetURI)
						&& p1.relation.equals(p2.relation) && p1.similarity >= threshold){
					count++;
					found = true;
					break;
				}
			}
			if(found == false && printWrongMappings){
				System.out.println("Wrong: " + p1.sourceURI + " " + p1.targetURI);
			}
		}	
		//System.out.println("right mappings: "+count);
		//System.out.println("prec:"+ (float)count/toEvaluate.size() + " rec: " +  (float)count/reference.size());
		float precision = (float)count/toEvaluate.size();
		float recall = (float)count/reference.size();
		float fmeasure = 2 * precision * recall / (precision + recall);
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		String retValue = df.format(threshold) + "\t" + precision + "\t" + recall + "\t" + fmeasure;
		return retValue;
	}
	
	public static void main(String[] args) throws Exception {
		//evaluate(toEvaluate, reference);
	}
}