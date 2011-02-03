package am.app.userfeedbackloop.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import am.app.mappingEngine.AbstractMatcher.alignType;
import am.app.mappingEngine.Alignment;
import am.app.mappingEngine.Mapping;
import am.app.userfeedbackloop.CandidateSelection;
import am.app.userfeedbackloop.CandidateSelectionEvaluation;

public class PrecisionRecallPlot extends CandidateSelectionEvaluation {

	private int correct;// number of correct mappings found
	//private int found;//number of mappings in rankedList  TODO: is this needed???
	String filename = "/home/cosmin/evaluation.data";//name of the file to output the data
	
	public PrecisionRecallPlot() {
		super();
	}

	@Override
	public void evaluate(CandidateSelection cs, Alignment<Mapping> reference ) {
		// This method is called to create the 'table' and calculate the points
		
		List<Mapping> rankedList = cs.getRankedMappings(alignType.aligningClasses);
		
		correct=0;
		float precision;
		float recall;
		int isCorrect;
	
		// open the output file
		PrintStream out = null;
		try
		{
			FileOutputStream outputfile = null;
			if( filename == null ) 
				outputfile = new FileOutputStream(File.createTempFile("AgreementMaker", "gnuplot"));
			else 
				outputfile = new FileOutputStream(filename);
			out = new PrintStream(outputfile);	
		}
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		// do the evaluation
		for(int i=0;i<rankedList.size();i++)
		{
			isCorrect=0;
			
			Mapping currentMapping=rankedList.get(i);
			
			if(reference.contains(currentMapping))//increase the number of correct and set isCorrect to true
			{
				correct++;
				isCorrect=1;
			}
			
			precision=(float)correct/(float)(i+1);
			recall=(float)correct/(float)reference.size();
			
			//write the data to a file
			out.println(precision+", "+recall+", "+currentMapping.toString()+", "+isCorrect);
		}//end for loop
	}
	
}
