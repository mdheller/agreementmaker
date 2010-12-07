/**
 * 
 */
package am.app.mappingEngine.structuralMatchers.similarityFlooding.utils;

import am.utility.Pair;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author Michele Caci
 *
 */
public class PCGVertexData extends StructMatchVertexData {

	/**
	 * 
	 */
	public PCGVertexData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param stCouple
	 * @param propagationCoefficient
	 */
	public PCGVertexData(Pair<RDFNode, RDFNode> stCouple) {
		super(stCouple);
	}

	/**
	 * @param stCouple
	 * @param propagationCoefficient
	 */
	public PCGVertexData(Pair<RDFNode, RDFNode> stCouple, double propagationCoefficient) {
		super(stCouple, propagationCoefficient);
	}

	/**
	 * @param stCouple
	 * @param propagationCoefficient
	 */
	public PCGVertexData toPCGVertexData(IPGVertexData ipgData) {
		return new PCGVertexData(getStCouple(), getNewSimilarityValue());
	}

}