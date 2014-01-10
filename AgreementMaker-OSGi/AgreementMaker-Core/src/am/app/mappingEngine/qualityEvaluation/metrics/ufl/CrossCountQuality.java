package am.app.mappingEngine.qualityEvaluation.metrics.ufl;

import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.mappingEngine.Mapping;
import am.app.mappingEngine.qualityEvaluation.AbstractQualityMetric;
import am.app.mappingEngine.similarityMatrix.SimilarityMatrix;
import static am.Utility.IntArray.getMaxValue;

/**
 * A mapping quality metric that counts how many non-zero values are in the row
 * and column of this mapping.
 * 
 * @author Francesco Loprete
 * @author Cosmin Stroe
 */
public class CrossCountQuality extends AbstractQualityMetric {
	
	// weight for the Uncertain Mappings discovered in the system
	private final double weight_um = 0.5d;

	private SimilarityMatrix matrix;
	
	private int[] rowCounts;
	private int[] colCounts;
	
	private int normalizationFactor;
	
	public CrossCountQuality(SimilarityMatrix matrix) {
		super();
		this.matrix = matrix;

		
		// row counts
		rowCounts = new int[matrix.getColumns()];
		for( int i = 0; i < matrix.getRows(); i++ ) {
			rowCounts[i] = countNonzeroMappings(matrix.getRowMaxValues(i, matrix.getColumns()));
		}
		
		// column counts
		colCounts = new int[matrix.getRows()];
		for( int j = 0; j < matrix.getColumns(); j++ ) {
			colCounts[j] = countNonzeroMappings(matrix.getColMaxValues(j, matrix.getRows()));
		}
		
		normalizationFactor = getMaxValue(rowCounts) + getMaxValue(colCounts);
	}
	
	/**
	 * @param type
	 *            This parameter is ignored for this quality metric.
	 */
	@Override
	public double getQuality(alignType type, int i, int j) 
	{		
		return (rowCounts[i] + colCounts[j]) / (double)normalizationFactor;
	}

	private int countNonzeroMappings(Mapping[] map) 
	{
		int count = 0;
		for(Mapping m : map){
			if( m.getSimilarity() > 0.0 ) count++;
		}
		return count;
	}
}
