package am.app.mappingEngine;

import am.app.feedback.FeedbackLoop;
import am.app.feedback.InitialMatchers;
import am.app.mappingEngine.Combination.CombinationMatcher;
import am.app.mappingEngine.LexicalMatcherJAWS.LexicalMatcherJAWS;
import am.app.mappingEngine.LexicalMatcherJWNL.LexicalMatcherJWNL;
import am.app.mappingEngine.LexicalSynonymMatcher.LexicalSynonymMatcher;
//import am.app.mappingEngine.LexicalMatcherUMLS.LexicalMatcherUMLS;
import am.app.mappingEngine.PRAMatcher.OldPRAMatcher;
import am.app.mappingEngine.PRAMatcher.PRAMatcher;
import am.app.mappingEngine.PRAMatcher.PRAMatcher2;
import am.app.mappingEngine.PRAintegration.PRAintegrationMatcher;
import am.app.mappingEngine.baseSimilarity.BaseSimilarityMatcher;
import am.app.mappingEngine.basicStructureSelector.BasicStructuralSelectorMatcher;
import am.app.mappingEngine.conceptMatcher.ConceptMatcher;
import am.app.mappingEngine.dsi.DescendantsSimilarityInheritanceMatcher;
import am.app.mappingEngine.dsi.OldDescendantsSimilarityInheritanceMatcher;
import am.app.mappingEngine.manualMatcher.UserManualMatcher;
import am.app.mappingEngine.multiWords.MultiWordsMatcher;
import am.app.mappingEngine.oaei2009.OAEI2009matcher;
import am.app.mappingEngine.parametricStringMatcher.ParametricStringMatcher;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentMatcher;
import am.app.mappingEngine.ssc.SiblingsSimilarityContributionMatcher;
import am.app.mappingEngine.testMatchers.AllOneMatcher;
import am.app.mappingEngine.testMatchers.AllZeroMatcher;
import am.app.mappingEngine.testMatchers.CopyMatcher;
import am.app.mappingEngine.testMatchers.EqualsMatcher;
import am.app.mappingEngine.testMatchers.RandomMatcher;
//import am.app.mappingEngine.LexicalMatcherUMLS.LexicalMatcherUMLS;


/**
 * Enum for keeping the current list of matchers in the system, and their class references
 */
public enum MatchersRegistry {
	
	/**
	 * This is where you add your own MATCHER.
	 * 
	 * To add your matcher, add a definition to the enum, using this format
	 * 
	 * 		EnumName	( "Short Name", MatcherClass.class )
	 * 
	 * For example, to add MySuperMatcher, you would add something like this (assuming the class name is MySuperMatcher):
	 *  
	 * 		SuperMatcher   ( "My Super Matcher", MySuperMatcher.class ),
	 * 
	 * And so, if your matcher is has no code errors, it will be incorporated into the AgreementMaker.  - Cosmin
	 */
	//
	SimilarityFlooding  ( "SFM", "Similarity Flooding Matcher", am.app.mappingEngine.structuralMatchers.similarityFlooding.SimilarityFloodingMatcher.class ),
	OAEI2010			( "OAEI-10", "OAEI 2010 Matcher", am.app.mappingEngine.oaei2010.OAEI2010Matcher.class ),
	IterativeMatcher	( "IISM", "Instance-based Iterator", am.app.mappingEngine.instance.IterativeMatcher.class),
	AdvancedSimilarity  ( "ASM", "Advanced Similarity Matcher", am.app.mappingEngine.baseSimilarity.advancedSimilarity.AdvancedSimilarityMatcher.class),
	GroupFinder			( "GFM", "Group Finder Matcher", am.app.mappingEngine.groupFinder.GroupFinderMatcher.class),
	FCM					( "FCM", "Federico Caimi Matcher", am.app.mappingEngine.FedericoCaimiMatcher.FedericoMatcher.class),
	LSM					( "LSM", "Lexical Synonym Matcher", LexicalSynonymMatcher.class ),
	//OFFICIAL MATCHERS
	LexicalJAWS			( "JAWS", "Lexical Matcher: JAWS", LexicalMatcherJAWS.class ),
	BaseSimilarity		( "BSM", "Base Similarity Matcher", BaseSimilarityMatcher.class ),
	ParametricString 	( "PSM", "Parametric String Matcher",	 ParametricStringMatcher.class ),
	MultiWords       	( "VMM", "Vector-based Multi-Words Matcher", MultiWordsMatcher.class),
	WordNetLexical		( "LM-WN", "Lexical Matcher: WordNet", LexicalMatcherJWNL.class),
	DSI					( "DSI", "Descendant's Similarity Inheritance", DescendantsSimilarityInheritanceMatcher.class ),
	BSS					( "BSS", "Basic Structure Selector Matcher", BasicStructuralSelectorMatcher.class ),
	SSC					( "SSC", "Sibling's Similarity Contribution", SiblingsSimilarityContributionMatcher.class ),
	Combination			( "LWC", "Linear Weighted Combination", CombinationMatcher.class ),
	ConceptSimilarity   ( "Concept Similarity", ConceptMatcher.class, false),
	OAEI2009   			( "OAEI-09", "OAEI2009 Matcher", OAEI2009matcher.class),
	//UMLSKSLexical		("Lexical Matcher: UMLSKS", LexicalMatcherUMLS.class, false), //it requires internet connection and the IP to be registered
	
	//Auxiliary matchers created for specific purposes
	InitialMatcher      ("Initial Matcher: LWC (PSM+VMM+BSM)", InitialMatchers.class, true),
	PRAintegration   	( "PRA Integration", PRAintegrationMatcher.class, false), //this works fine
	PRAMatcher			("PRA Matcher", PRAMatcher.class, false),
	PRAMatcher2			("PRA Matcher2", PRAMatcher2.class, false),
	OldPRAMAtcher		("Old PRA Matcher", OldPRAMatcher.class, false),
	
	//WORK IN PROGRESS
		
	//MATCHERS USED BY THE SYSTEM, usually not shown
	UserManual			( "USER", "User Manual Matching", UserManualMatcher.class, false),
	UniqueMatchings		( "Unique Matchings", ReferenceAlignmentMatcher.class, false), // this is used by the "Remove Duplicate Alignments" UIMenu entry
	ImportAlignment		( "IMPORT", "Import Alignments", ReferenceAlignmentMatcher.class, true),
	
	//TEST MATCHERS 
	Equals 				( "Local Name Equivalence Comparison", EqualsMatcher.class , false),
	AllOne 				( "(Test) All One Similarities", AllOneMatcher.class, true ),
	AllZero				( "(Test) All Zero Similarities", AllZeroMatcher.class, true ),
	Copy				( "Copy Matcher", CopyMatcher.class,false ),
	Random 				( "(Test) Random Similarities", RandomMatcher.class, true ),
	DSI2				( "OLD Descendant's Similarity Inheritance (DSI)", OldDescendantsSimilarityInheritanceMatcher.class, false ),
	UserFeedBackLoop 	("User Feedback Loop", FeedbackLoop.class, false );
	
	/* Don't change anything below this line .. unless you intend to. */
	private boolean showInControlPanel;
	private String name;
	private String shortName;
	private String className;  // TODO: this should be Class instead of string
	
	MatchersRegistry( String sn, String n, Class<?> matcherClass ) { shortName = sn; name = n; className = matcherClass.getName(); showInControlPanel = true;}
	MatchersRegistry( String n, Class<?> matcherClass ) { name = n; className = matcherClass.getName(); showInControlPanel = true;}
	MatchersRegistry( String n, Class<?> matcherClass, boolean shown) { name = n; className = matcherClass.getName(); showInControlPanel = shown; }
	MatchersRegistry( String sn, String n, Class<?> matcherClass, boolean shown) { shortName = sn; name = n; className = matcherClass.getName(); showInControlPanel = shown; }
	public String getMatcherName() { return name; }
	public String getMatcherShortName() { return shortName; }
	public String getMatcherClass() { return className; }
	public boolean isShown() { return showInControlPanel; }
	public String toString() { return name; }
	
	/**
	 * Returns the matcher with the given name.
	 * @param matcherName The name of the matcher.
	 * @return The MatchersRegistry representation of the matcher (used with MatcherFactory).  
	 */
/*  // This method duplicates MatcherFactory.getMatchersRegistryEntry( matcherName )
	public static MatchersRegistry getMatcherByName( String matcherName ) {
		
		EnumSet<MatchersRegistry> matchers = EnumSet.allOf(MatchersRegistry.class);
		
		Iterator<MatchersRegistry> entryIter = matchers.iterator();
		while( entryIter.hasNext() ) {
			MatchersRegistry currentEntry = entryIter.next();
			if( currentEntry.getMatcherName().equals(matcherName) ) return currentEntry;
		}
		
		return null;
	}
*/
	
}
