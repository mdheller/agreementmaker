package am.app.ontology.ontologyParser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

import am.Utility;
import am.app.Core;
import am.app.mappingEngine.utility.MatchingPair;
import am.app.ontology.Node;
import am.app.ontology.Ontology;
import am.app.ontology.Ontology.DatasetType;
import am.app.ontology.instance.FreebaseInstanceDataset;
import am.app.ontology.instance.GeoNamesInstanceDataset;
import am.app.ontology.instance.InstanceDataset;
import am.app.ontology.instance.OntologyInstanceDataset;
import am.app.ontology.instance.SeparateFileInstanceDataset;
import am.app.ontology.instance.SparqlInstanceDataset;
import am.app.ontology.instance.endpoint.EndpointRegistry;
import am.app.ontology.instance.endpoint.FreebaseEndpoint;
import am.app.ontology.instance.endpoint.GeoNamesEndpoint;
import am.app.ontology.instance.endpoint.SparqlEndpoint;
import am.app.ontology.ontologyParser.OntologyDefinition.OntologyLanguage;
import am.app.ontology.ontologyParser.OntologyDefinition.OntologySyntax;
import am.output.alignment.oaei.OAEIAlignmentFormat;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public abstract class TreeBuilder<T extends OntologyDefinition> extends SwingWorker<Void, Void> {

	/**
	 * This is the name of a progress change event. The "ONTOLOGY LOADED" event
	 * is fired when the ontology is done loading.
	 */
	public final static String PROGRESS_CHANGE_ONTOLOGY_LOADED = "PROGRESS_CHANGE_ONTOLOGY_LOADED";
	
	/**
	 * This is meant to tell the progress listeners to clear their log.
	 */
	public final static String PROGRESS_COMMAND_CLEAR_LOG = "PROGRESS_COMMAND_CLEAR_LOG";
	
	/**
	 * Tell the progress listeners to add a line to their log.
	 */
	public final static String PROGRESS_COMMAND_APPEND_LINE = "PROGRESS_COMMAND_APPEND_LINE";

	
	// instance variables 
	protected int treeCount;  // this variable is used in the Canvas visualization.  ( it is the total number of Vertices in the Classes and Properties trees )
	protected Node treeRoot;
	protected Ontology ontology;  
	protected int uniqueKey = 0;
	
	// Progress Monitor Variables
	/**
	 * Progress events are broadcast via this object.
	 */
	protected final PropertyChangeSupport listeners;
	protected int stepsTotal; // Used by the ProgressDialog.  This is a rough estimate of the number of steps to be done before we finish the matching.
	protected int stepsDone;  // Used by the ProgressDialog.  This is how many of the total steps we have completed.
	protected String report = "";
	
	protected T ontDefinition; // All the information needed to load the ontology.

	protected InstanceDataset instances;
	
	public TreeBuilder( T def ) {
		this.ontDefinition = def;
		ontology = new Ontology(null);
		ontology.setDefinition(def);
		ontology.setIndex( Core.getInstance().numOntologies() );
		ontology.setID( Core.getInstance().getNextOntologyID() );  // get an unique ID for this ontology
		if( def.loadOntology ) {
			ontology.setFilename(def.ontologyURI);
			ontology.setLanguage(def.ontologyLanguage);
			ontology.setFormat(def.ontologySyntax);
	        File f = new File(def.ontologyURI);
	        ontology.setTitle(f.getName()); 
		}
		else if( def.loadInstances ) {
			if( def.instanceSourceType == DatasetType.DATASET ) {
				ontology.setFilename(def.instanceSourceFile);
				ontology.setLanguage(OntologyLanguage.OWL);
				ontology.setFormat(OntologySyntax.RDFXML);
				File f = new File(def.instanceSourceFile);
		        ontology.setTitle(f.getName()); 
			}
			else if( def.instanceSourceType == DatasetType.ENDPOINT ){
				ontology.setFilename(def.instanceSourceFile);
				ontology.setLanguage(OntologyLanguage.OWL);
				ontology.setFormat(OntologySyntax.RDFXML);
				ontology.setTitle("Semantic Web Endpoint");
			}
		}
		else {
			throw new RuntimeException("Load ontology or Load instances must be checked.");
		}
		
		listeners = new PropertyChangeSupport(this);
		
		treeCount = 0;
	}
	
	/**
	 * FIXME: Remove this method? Replace with a better mechanism. -- Cosmin, Oct. 22, 2013
	 */
	public static TreeBuilder<OntologyDefinition> buildTreeBuilder(
			OntologyDefinition odef) {
		// TODO: Not sure if this method is supposed to take implementation
		// specific variables (ex. DB).

		if (odef.ontologyLanguage == OntologyLanguage.XML) {
			return new XmlTreeBuilder(odef);
		} 
		else if (odef.ontologyLanguage == OntologyLanguage.RDFS) {
			if (odef.onDiskStorage)
				return new TDBOntoTreeBuilder(odef);
			else
				return new RdfsTreeBuilder(odef);
		} 
		else if (odef.ontologyLanguage == OntologyLanguage.TABBEDTEXT) {
			return new TabbedTextBuilder(odef);
		}
		else if (odef.ontologyLanguage == OntologyLanguage.OWL) {
			if (odef.onDiskStorage) {
				return new TDBOntoTreeBuilder(odef);
			}
			else {
				return new OntoTreeBuilder(odef);
			}
		}
		else {
			return null;
		}
	}
	
	public void build() throws Exception{
		buildTree();//Instantiated in the subclasses
		
		if( ontDefinition != null ) loadInstances();
		
		// TODO: Remove these calls?
		ontology.setDeepRoot(getTreeRoot());
		ontology.setTreeCount(getTreeCount());
		
		report = "Ontology loaded succesfully\n\n";
        report += "Total number of classes: "+ontology.getClassesList().size()+"\n";
        report += "Total number of properties: "+ontology.getPropertiesList().size()+"\n";
        report += "Instances source: ";
        if( ontDefinition != null ) {
        	if( !ontDefinition.loadInstances )
        		report += "none.\n\n";
        	else if( ontDefinition.instanceSourceType == DatasetType.DATASET )
        		report += "dataset.\n\n";
        	else if( ontDefinition.instanceSourceType == DatasetType.ONTOLOGY )
        		report += "ontology.\n\n";
        	else if( ontDefinition.instanceSourceType == DatasetType.ENDPOINT )
        		report += "endpoint.\n\n";
        }
        report += "Select the 'Ontology Details' function in the 'Ontology' menu\nfor additional informations.\n";
        report += "The 'Hierarchy Visualization' can be disabled from the 'View' menu\nto improve system performances.\n";
	}
	
	protected void buildTree() throws Exception {
		throw new RuntimeException("This method has to be implemented in the subclass");
	}

	protected void loadInstances() {
		
		if( ontDefinition == null ) return;
		
		if( !ontDefinition.loadInstances ) return;
		
		if( ontDefinition.instanceSourceType == DatasetType.ONTOLOGY ) {
			instances = new OntologyInstanceDataset(ontology);
		}
		else if ( ontDefinition.instanceSourceType == DatasetType.DATASET ) {
			
			OntModel instancesModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
			
			if( !(ontDefinition.instanceSourceFile.startsWith("file:///") || 
					ontDefinition.instanceSourceFile.startsWith("http://")) ) {
				ontDefinition.instanceSourceFile = "file:///" + ontDefinition.instanceSourceFile;
			}
			
			instancesModel.read( ontDefinition.instanceSourceFile, null, ontology.getFormat().toString() );
			
			instances = new SeparateFileInstanceDataset(instancesModel);
		}
		else if ( ontDefinition.instanceSourceType == DatasetType.ENDPOINT &&
				  ontDefinition.instanceEndpointType.equals( EndpointRegistry.FREEBASE ) ) {
			
			FreebaseEndpoint freebase = new FreebaseEndpoint();
			instances = new FreebaseInstanceDataset(freebase);
		}
		else if ( ontDefinition.instanceSourceType == DatasetType.ENDPOINT &&
				  ontDefinition.instanceEndpointType.equals( EndpointRegistry.GEONAMES ) ) {
			
			GeoNamesEndpoint geoNames = new GeoNamesEndpoint();
			instances = new GeoNamesInstanceDataset(geoNames);
		}
		else if ( ontDefinition.instanceSourceType == DatasetType.ENDPOINT &&
				  ontDefinition.instanceEndpointType.equals( EndpointRegistry.SPARQL ) ) {
			
			SparqlEndpoint endpoint = new SparqlEndpoint(ontDefinition.instanceSourceFile);
			instances = new SparqlInstanceDataset(endpoint);
		}

		
		ontology.setInstances(instances); // save the instances with this ontology
		
		// load the mapping file
		
		if( ontDefinition.loadSchemaAlignment ) {
			if( ontDefinition.schemaAlignmentFormat == 0 ) { // RDF
				try {
					File file = new File( ontDefinition.schemaAlignmentURI );
					FileReader fr = new FileReader(file);
					HashMap<String,List<MatchingPair>> map = OAEIAlignmentFormat.readAlignment(fr);
					ontology.setInstanceTypeMappings(map);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public InstanceDataset getInstances() { return instances; }
	
	/**
	 * This function returns the number of nodes created by the tree
	 * @return int the number of nodes created by the tree
	 */
	public int getTreeCount()
	{
		return treeCount;
	}  
	/**
	 * This function returns the tree root
	 * @return treeRoot	root of the tree
	 */
	public Node getTreeRoot() { return treeRoot; }


	/********************************************************************************************/
	/**
	 * This function sets the tree root
	 *
	 * @param root root of the tree
	 */

	public void setTreeRoot(Node root) { treeRoot = root; }
	
	/********************************************************************************************/	
	public Ontology getOntology() {
		return ontology;
	}
	
	//****************** PROGRESS DIALOG METHODS *************************8
	
	
    /**
     * This function is used by the Progress Dialog, in order to invoke the the treebuilder.
     * It's just a wrapper. 
     */
	public Void doInBackground() throws Exception {
		try {
			//without the try catch, the exception got lost in this thread, and we can't debug
			build();
		}
		catch(java.lang.OutOfMemoryError ex2){
			ex2.printStackTrace();
			report = Utility.OUT_OF_MEMORY;
			this.cancel(true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			report = Utility.UNEXPECTED_ERROR;
			this.cancel(true);
		}
		return null;
	}
    
    /**
     * Function called by the worker thread when the matcher finishes the algorithm.
     */
    public void done() {
    	listeners.firePropertyChange(PROGRESS_CHANGE_ONTOLOGY_LOADED, null, null);
    }
	
    public void addProgressListener(PropertyChangeListener listener) {
    	listeners.addPropertyChangeListener(listener);
    }
    
    public void removeProgressListeners(PropertyChangeListener listener) {
    	listeners.removePropertyChangeListener(listener);
    }
    
	public String getReport() {
		return report;
	}
	
}


	
