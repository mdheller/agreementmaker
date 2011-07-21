package am.app.lexicon.subconcept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import am.app.lexicon.GeneralLexicon;
import am.app.mappingEngine.LexiconStore.LexiconRegistry;

/**
 * A lexicon that is able to keep track of words that should be synonyms, based on 
 * looking at common words between synonyms declared in an ontology.
 * 
 * @author cosmin
 */
public class SCSLexicon extends GeneralLexicon implements SubconceptSynonymLexicon {

	protected HashMap<String,List<String>> subconceptSynonymMap = new HashMap<String,List<String>>();
	
	public SCSLexicon(LexiconRegistry lr) {
		super(lr);
	}

	@Override
	public void addSubConceptSynonyms(String synonym1, String synonym2 ) {
		addToMap( synonym1, synonym2);
		addToMap( synonym2, synonym1);
	}

	private void addToMap(String synonym1, String synonym2) {
		if( subconceptSynonymMap.containsKey(synonym1) ) {
			List<String> synonym1List = subconceptSynonymMap.get(synonym1);
			if( !synonym1List.contains(synonym2) ) synonym1List.add(synonym2);
		} else {
			List<String> synonym1List = new LinkedList<String>(); // LinkedList to save space. (if you need to access via index, change to ArrayList).
			synonym1List.add(synonym2);
			subconceptSynonymMap.put(synonym1, synonym1List);
		}
	}
	
	@Override
	public List<String> getSubConceptSynonyms(String synonym) {
		return subconceptSynonymMap.get(synonym);
	}
	
	@Override
	public List<String> getAllSubConceptSynonyms() {
		return new ArrayList<String>(subconceptSynonymMap.keySet());
	}
}
