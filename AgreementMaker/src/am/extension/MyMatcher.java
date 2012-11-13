package am.extension;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import simpack.measure.external.alignapi.Hamming;
import simpack.measure.external.alignapi.JaroWinkler;
import simpack.measure.external.alignapi.Levenshtein;
import am.app.mappingEngine.AbstractMatcher;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.DefaultMatcherParameters;
import am.app.mappingEngine.Mapping;
import am.app.mappingEngine.MatcherFactory;
import am.app.mappingEngine.MatchersRegistry;
import am.app.mappingEngine.SimilarityMatrix;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentMatcher;
import am.app.mappingEngine.referenceAlignment.ReferenceAlignmentParameters;
import am.app.mappingEngine.referenceAlignment.ReferenceEvaluationData;
import am.app.mappingEngine.referenceAlignment.ReferenceEvaluator;
import am.app.ontology.Node;
import am.app.ontology.Ontology;
import am.app.ontology.ontologyParser.OntoTreeBuilder;
import am.utility.FromWordNetUtils;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class MyMatcher extends AbstractMatcher {

    private static final long serialVersionUID = -3772449944360959530L;

    private static Logger log = Logger.getLogger(MyMatcher.class);

    public MyMatcher() {
        super();
        setName("My Matcher"); // change this to something else if you want
    }

    @Override
    protected Mapping alignTwoNodes(Node source, Node target, alignType typeOfNodes, SimilarityMatrix matrix) throws Exception {

        FromWordNetUtils wordNetUtils = new FromWordNetUtils();

        Map<String, String> sourceMap = getInputs(source);
        Map<String, String> targetMap = getInputs(target);

        double stringSimilarity = findStringSimilarity(sourceMap, targetMap);

        double finalSimilarity = 0;


//        double dividedSynonymSimilarity = findDividedSynonymSimilarity(source, target);

        /*
         * Checking if values already map exactly
         */
        if (    (sourceMap.containsKey("name") && targetMap.containsKey("name") && sourceMap.get("name").equals(targetMap.get("name"))) || 
                (sourceMap.containsKey("name") && targetMap.containsKey("label") && sourceMap.get("name").equals(targetMap.get("label"))) ||
                (sourceMap.containsKey("name") && targetMap.containsKey("comment") && sourceMap.get("name").equals(targetMap.get("comment"))  ) ){
            finalSimilarity = 1.0;
            
        }

        else if((sourceMap.containsKey("label") && targetMap.containsKey("label") && sourceMap.get("label").equals(targetMap.get("label"))) ||
                (sourceMap.containsKey("label") && targetMap.containsKey("comment") && sourceMap.get("label").equals(targetMap.get("comment"))) ||
                (sourceMap.containsKey("label") && targetMap.containsKey("name") && sourceMap.get("label").equals(targetMap.get("name"))) ||
                
                (sourceMap.containsKey("comment") && targetMap.containsKey("label") && sourceMap.get("comment").equals(targetMap.get("label"))) ||
                (sourceMap.containsKey("comment") && targetMap.containsKey("name") && sourceMap.get("comment").equals(targetMap.get("name")))  ||
                (sourceMap.containsKey("comment") &&targetMap.containsKey("comment") && sourceMap.get("comment").equals(targetMap.get("comment")))) {
                finalSimilarity = 0.9;
        } else
            /*
             * Computing Synonym Similarity using Wordnet
             */
            if (wordNetUtils.areSynonyms(source.getLabel(), target.getLabel()) || 
                wordNetUtils.areSynonyms(source.getLocalName(), target.getLocalName()) || 
                wordNetUtils.areSynonyms(source.getComment(), target.getComment()) ||
                wordNetUtils.areSynonyms(source.getLabel(), target.getLocalName()) || 
                wordNetUtils.areSynonyms(source.getLabel(), target.getComment()) ||
                wordNetUtils.areSynonyms(source.getComment(), target.getLocalName()) || 
                wordNetUtils.areSynonyms(source.getComment(), target.getLabel()) ||
                wordNetUtils.areSynonyms(source.getLocalName(), target.getLabel())|| 
                wordNetUtils.areSynonyms(source.getLocalName(), target.getComment())) {
            finalSimilarity = (.70);
        } else {
            finalSimilarity = stringSimilarity;
        }

        return new Mapping(source, target, finalSimilarity);
    }
    
    /**
     * Essential combination algorithm to compute the basic string similarity between the source and target. 
     * Three Algorithms are combined- Hamming, Levenshtein and Jaro-Winkler.
     * @param sourceMap
     * @param targetMap
     * @return
     */

    private double findStringSimilarity(Map<String, String> sourceMap, Map<String, String> targetMap) {
        double hammingSimilarity = 0;
        double levenshteinSimilarity = 0;
        double jarowinglerSimilarity = 0;
        int divisor = 0;

        /*
         * Checking simple string similarity
         * 
         * Hamming, Levenshtein and Jaro-Winkler are 3 of the very widely used
         * algorithms for string comparison. Here, all possible combinations
         * between label, comment and localName are found for every algorithm,
         * and then the weighted average is found. localName=localName and
         * label=label is given more weightage.
         */

        if (sourceMap.containsKey("name") && targetMap.containsKey("name")) {
            hammingSimilarity += (2 * hammingStringSimilarity(sourceMap.get("name"), targetMap.get("name")));
            divisor += 2;
        }
        if (sourceMap.containsKey("label") && targetMap.containsKey("label")) {
            hammingSimilarity += (2 * hammingStringSimilarity(sourceMap.get("label"), targetMap.get("label")));
            divisor += 2;
        }
        if (sourceMap.containsKey("comment") && targetMap.containsKey("comment")) {
            hammingSimilarity += (2 * hammingStringSimilarity(sourceMap.get("comment"), targetMap.get("comment")));
            divisor += 2;
        }

        if (sourceMap.containsKey("name") && targetMap.containsKey("label")) {
            hammingSimilarity += hammingStringSimilarity(sourceMap.get("name"), targetMap.get("label"));
            divisor++;
        }
        if (sourceMap.containsKey("name") && targetMap.containsKey("comment")) {
            hammingSimilarity += hammingStringSimilarity(sourceMap.get("name"), targetMap.get("comment"));
            divisor++;
        }

        if (sourceMap.containsKey("label") && targetMap.containsKey("comment")) {
            hammingSimilarity += hammingStringSimilarity(sourceMap.get("label"), targetMap.get("comment"));
            divisor++;
        }
        if (sourceMap.containsKey("label") && targetMap.containsKey("name")) {
            hammingSimilarity += hammingStringSimilarity(sourceMap.get("label"), targetMap.get("name"));
            divisor++;
        }

        if (sourceMap.containsKey("comment") && targetMap.containsKey("name")) {
            hammingSimilarity += hammingStringSimilarity(sourceMap.get("comment"), targetMap.get("name"));
            divisor++;
        }
        if (sourceMap.containsKey("comment") && targetMap.containsKey("label")) {
            hammingSimilarity += hammingStringSimilarity(sourceMap.get("comment"), targetMap.get("label"));
            divisor++;
        }

        hammingSimilarity = hammingSimilarity / divisor;

        divisor = 0;
        if (sourceMap.containsKey("name") && targetMap.containsKey("name")) {
            levenshteinSimilarity += 2 * levenshteinStringSimilarity(sourceMap.get("name"), targetMap.get("name"));
            divisor += 2;
        }
        if (sourceMap.containsKey("label") && targetMap.containsKey("label")) {
            levenshteinSimilarity += 2 * levenshteinStringSimilarity(sourceMap.get("label"), targetMap.get("label"));
            divisor += 2;
        }
        if (sourceMap.containsKey("comment") && targetMap.containsKey("comment")) {
            levenshteinSimilarity += 2 * levenshteinStringSimilarity(sourceMap.get("comment"), targetMap.get("comment"));
            divisor += 2;
        }

        if (sourceMap.containsKey("name") && targetMap.containsKey("label")) {
            levenshteinSimilarity += levenshteinStringSimilarity(sourceMap.get("name"), targetMap.get("label"));
            divisor++;
        }
        if (sourceMap.containsKey("name") && targetMap.containsKey("comment")) {
            levenshteinSimilarity += levenshteinStringSimilarity(sourceMap.get("name"), targetMap.get("comment"));
            divisor++;
        }

        if (sourceMap.containsKey("label") && targetMap.containsKey("comment")) {
            levenshteinSimilarity += levenshteinStringSimilarity(sourceMap.get("label"), targetMap.get("comment"));
            divisor++;
        }
        if (sourceMap.containsKey("label") && targetMap.containsKey("name")) {
            levenshteinSimilarity += levenshteinStringSimilarity(sourceMap.get("label"), targetMap.get("name"));
            divisor++;
        }

        if (sourceMap.containsKey("comment") && targetMap.containsKey("name")) {
            levenshteinSimilarity += levenshteinStringSimilarity(sourceMap.get("comment"), targetMap.get("name"));
            divisor++;
        }
        if (sourceMap.containsKey("comment") && targetMap.containsKey("label")) {
            levenshteinSimilarity += levenshteinStringSimilarity(sourceMap.get("comment"), targetMap.get("label"));
            divisor++;
        }

        levenshteinSimilarity = levenshteinSimilarity / divisor;

        divisor = 0;
        if (sourceMap.containsKey("name") && targetMap.containsKey("name")) {
            jarowinglerSimilarity += 2 * jarowinklerStringSimilarity(sourceMap.get("name"), targetMap.get("name"));
            divisor += 2;
        }
        if (sourceMap.containsKey("label") && targetMap.containsKey("label")) {
            jarowinglerSimilarity += 2 * jarowinklerStringSimilarity(sourceMap.get("label"), targetMap.get("label"));
            divisor += 2;
        }
        if (sourceMap.containsKey("comment") && targetMap.containsKey("comment")) {
            jarowinglerSimilarity += 2 * jarowinklerStringSimilarity(sourceMap.get("comment"), targetMap.get("comment"));
            divisor += 2;
        }

        if (sourceMap.containsKey("name") && targetMap.containsKey("label")) {
            jarowinglerSimilarity += jarowinklerStringSimilarity(sourceMap.get("name"), targetMap.get("label"));
            divisor++;
        }
        if (sourceMap.containsKey("name") && targetMap.containsKey("comment")) {
            jarowinglerSimilarity += jarowinklerStringSimilarity(sourceMap.get("name"), targetMap.get("comment"));
            divisor++;
        }

        if (sourceMap.containsKey("label") && targetMap.containsKey("comment")) {
            jarowinglerSimilarity += jarowinklerStringSimilarity(sourceMap.get("label"), targetMap.get("comment"));
            divisor++;
        }
        if (sourceMap.containsKey("label") && targetMap.containsKey("name")) {
            jarowinglerSimilarity += jarowinklerStringSimilarity(sourceMap.get("label"), targetMap.get("name"));
            divisor++;
        }

        if (sourceMap.containsKey("comment") && targetMap.containsKey("name")) {
            jarowinglerSimilarity += jarowinklerStringSimilarity(sourceMap.get("comment"), targetMap.get("name"));
            divisor++;
        }
        if (sourceMap.containsKey("comment") && targetMap.containsKey("label")) {
            jarowinglerSimilarity += jarowinklerStringSimilarity(sourceMap.get("comment"), targetMap.get("label"));
            divisor++;
        }

        jarowinglerSimilarity = jarowinglerSimilarity / divisor;

        /*
         * Weighted average of String Similarity
         * 
         * Through iterations, I came to a conclusion that the results are given
         * in the ratio of Levenshtein > Hamming > Jaro-Wingler So, the
         * corresponding weightage was given while calculating the mean.
         */
        double finalsimilarity = (2 * hammingSimilarity + 3 * levenshteinSimilarity + jarowinglerSimilarity) / 6;
        return finalsimilarity;
    }

    /**
     * Takes in a node, and returns a Map<String,String> of label, localName and Comment
     */
    private Map<String, String> getInputs(Node node) {
        Map<String, String> stringMap = new HashMap<String, String>();
        if (!node.getLabel().isEmpty())
            stringMap.put("label", node.getLabel().toLowerCase().replaceAll("\\s", ""));
        if (!node.getLocalName().isEmpty())
            stringMap.put("name", node.getLocalName().toLowerCase().replaceAll("\\s", ""));
        if (!node.getComment().isEmpty())
            stringMap.put("comment", node.getComment().toLowerCase().replaceAll("\\s", ""));

        return stringMap;
    }
    
    /**
     * Run MyMatcher on a set of ontologies and print out the Precision, Recall,
     * and F-Measure as compared against the reference alignment.
     * 
     * 
     * to where you saved the Benchmarks dataset.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Ontology source = OntoTreeBuilder
                .loadOWLOntology("/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/101/onto.rdf"); 
        Ontology target1 = OntoTreeBuilder
                .loadOWLOntology("/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/203/onto.rdf"); 
        Ontology target2 = OntoTreeBuilder
                .loadOWLOntology("/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/223/onto.rdf"); 
        Ontology target3 = OntoTreeBuilder
                .loadOWLOntology("/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/205/onto.rdf"); 
        Ontology target4 = OntoTreeBuilder
                .loadOWLOntology("/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/206/onto.rdf"); 
        
        String reference1 = "/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/203/refalign.rdf";
        String reference2 = "/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/223/refalign.rdf";
        String reference3 = "/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/205/refalign.rdf";
        String reference4 = "/home/jeevs/Dropbox/CS586/ExtractedFiles/AgreementMaker/ontologies/OAEI2010_OWL_RDF/BenchmarkTrack/206/refalign.rdf";
        
        
        try{
            ontologyMatcher(source, target1, reference1);
            ontologyMatcher(source, target2, reference2);
            ontologyMatcher(source, target3, reference3);
            ontologyMatcher(source, target4, reference4);
        }catch (Exception e) {
            log.error("Caught exception when running MyMatcher.", e);
        }
    }

    /**
     * 
     * All the ontologyMatching consolidated together under a single function to improve code reuse. 
     */
    private static void ontologyMatcher(Ontology source, Ontology target, String reference) {

        MyMatcher mm = new MyMatcher();

        mm.setSourceOntology(source);
        mm.setTargetOntology(target);

        DefaultMatcherParameters param = new DefaultMatcherParameters();
        param.threshold = 0.6;
        param.maxSourceAlign = 1;
        param.maxTargetAlign = 1;

        mm.setParameters(param);
        try {
            mm.match();
            mm.referenceEvaluation(reference); 
        } catch (Exception e) {
            log.error("Caught exception when running MyMatcher.", e);
        }
        
    }

    /**
     * Levenshtein String Similarity.
     */
    private static double levenshteinStringSimilarity(String sourceString, String targetString) {

        Levenshtein levenshtein = new Levenshtein(sourceString, targetString);
        levenshtein.calculate();
        return levenshtein.getSimilarity().doubleValue();
    }

    /**
     * Hamming String Similarity.
     */
    private static double hammingStringSimilarity(String sourceString, String targetString) {

        Hamming hammingSim = new Hamming(sourceString, targetString);
        hammingSim.calculate();
        return hammingSim.getSimilarity().doubleValue();
    }

    /**
     * Jaro-Winkler String Similarity.
     */
    private static double jarowinklerStringSimilarity(String sourceString, String targetString) {
        JaroWinkler jaro = new JaroWinkler(sourceString, targetString);
        jaro.calculate();
        return jaro.getSimilarity().doubleValue();
    }

    /**
     * Compute the Precision, Recall and F-Measure for MyMatcher given a
     * reference alignment.
     * 
     * @param pathToReferenceAlignment
     *            The path to the reference alignment file. To be safe, you
     *            should provide the absolute file path.
     * @throws Exception
     *             If bad things happen.
     */
    private void referenceEvaluation(String pathToReferenceAlignment) throws Exception {
        // Run the reference alignment matcher to get the list of mappings in
        // the reference alignment file
        ReferenceAlignmentMatcher refMatcher = (ReferenceAlignmentMatcher) MatcherFactory.getMatcherInstance(
                MatchersRegistry.ImportAlignment, 0);

        // these parameters are equivalent to the ones in the graphical
        // interface
        ReferenceAlignmentParameters parameters = new ReferenceAlignmentParameters();
        parameters.fileName = pathToReferenceAlignment;
        parameters.format = ReferenceAlignmentMatcher.OAEI;
        parameters.onlyEquivalence = false;
        parameters.skipClasses = false;
        parameters.skipProperties = false;

        // When working with sub-superclass relations the cardinality is always
        // ANY to ANY
        if (!parameters.onlyEquivalence) {
            parameters.maxSourceAlign = AbstractMatcher.ANY_INT;
            parameters.maxTargetAlign = AbstractMatcher.ANY_INT;
        }

        refMatcher.setParam(parameters);

        // set the source and target ontologies
        refMatcher.setSourceOntology(getSourceOntology());
        refMatcher.setTargetOntology(getTargetOntology());

        // load the reference alignment
        refMatcher.match();

        Alignment<Mapping> referenceSet;
        if (refMatcher.areClassesAligned() && refMatcher.arePropertiesAligned()) {
            referenceSet = refMatcher.getAlignment(); // class + properties
        } else if (refMatcher.areClassesAligned()) {
            referenceSet = refMatcher.getClassAlignmentSet();
        } else if (refMatcher.arePropertiesAligned()) {
            referenceSet = refMatcher.getPropertyAlignmentSet();
        } else {
            // empty set? -- this should not happen
            referenceSet = new Alignment<Mapping>(Ontology.ID_NONE, Ontology.ID_NONE);
        }

        // the alignment which we will evaluate
        Alignment<Mapping> myAlignment;

        if (refMatcher.areClassesAligned() && refMatcher.arePropertiesAligned()) {
            myAlignment = getAlignment();
        } else if (refMatcher.areClassesAligned()) {
            myAlignment = getClassAlignmentSet();
        } else if (refMatcher.arePropertiesAligned()) {
            myAlignment = getPropertyAlignmentSet();
        } else {
            myAlignment = new Alignment<Mapping>(Ontology.ID_NONE, Ontology.ID_NONE); // empty
        }

        // use the ReferenceEvaluator to actually compute the metrics
        ReferenceEvaluationData rd = ReferenceEvaluator.compare(myAlignment, referenceSet);

        // optional
        setRefEvaluation(rd);

        // output the report
        StringBuilder report = new StringBuilder();
        report.append("Reference Evaluation Complete\n\n").append(getName()).append("\n\n").append(rd.getReport()).append("\n")
                .append(rd.getErrorAlignments());

        log.info(report);

        // use system out if you don't see the log4j output
        // System.out.println(report);
    }

    /**
     * Use the Jena API to return the label of a node.
     */
    private String getLabel(Node n) {

        // get the Jena Resource of this node
        Resource res = n.getResource();

        // get the label of this node (returns the whole triple which defines
        // the label)
        Statement labelTriple = res.getProperty(RDFS.label);
        if (labelTriple == null) {
            return null; // no label defined
        }

        // extract the object of the triple (which is the label)
        RDFNode object = labelTriple.getObject();
        Literal label = object.asLiteral();

        // get the string value of the literal
        return label.getString();
    }

}