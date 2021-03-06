package am.app.mappingEngine.instanceMatchers;

import java.util.List;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Statement;
import com.ibm.icu.text.DecimalFormat;

import am.app.mappingEngine.StringUtil.StringMetrics;
import am.app.ontology.instance.Instance;

public class StatementsInstanceMatcher extends BaseInstanceMatcher{
	
	private static final long serialVersionUID = 6536746985095481526L;
	
	Logger log = Logger.getLogger(StatementsInstanceMatcher.class);
	
	@Override
	public double instanceSimilarity(Instance source, Instance target)
			throws Exception {
		List<Statement> sourceStmts = source.getStatements();
		List<Statement> targetStmts = target.getStatements();
		
		if(sourceStmts.size() == 0 || targetStmts.size() == 0) return -1;
		
		Statement sourceStmt;
		Statement targetStmt;
		int count = 0;
		double totalSim = 0;
		DecimalFormat df = new DecimalFormat("#.##");
		for (int i = 0; i < sourceStmts.size(); i++) {
			for (int j = 0; j < targetStmts.size(); j++) {
				sourceStmt = sourceStmts.get(i);
				targetStmt = targetStmts.get(j);
				if(sourceStmt.getPredicate().equals(targetStmt.getPredicate())){
					if(!sourceStmt.getPredicate().getURI().contains("type") &&
							!sourceStmt.getPredicate().getURI().contains("label")){
						if(sourceStmt.getObject().isLiteral() && targetStmt.getObject().isLiteral()){
							count++;
							log.debug(sourceStmt + "\n" + targetStmt);
							
							double sim = 0.0;
							
							String s1 = sourceStmt.getObject().asLiteral().getString();
							String s2 = targetStmt.getObject().asLiteral().getString();
							
							log.debug(s1 + " " + s2);
							
							try{
								double d1 = Double.parseDouble(s1);
								double d2 = Double.parseDouble(s2);
								if(d1 == d2) sim = 1;
								else {
									sim = StringMetrics.AMsubstringScore(df.format(d1),df.format(d2));
									//sim = 1 - Math.abs(d1-d2)/50;
									if(sim < 0) sim = 0;
								}
										
							}
							catch (NumberFormatException e) {
								sim = StringMetrics.AMsubstringScore(sourceStmt.getObject().asLiteral().getString(),
										targetStmt.getObject().asLiteral().getString());
							}
							log.debug(sim);
							totalSim += sim;
						}
					}
				}
			}
		}
		if(count == 0) return -1;
		return totalSim / count;
	}

}
