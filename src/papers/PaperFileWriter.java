package papers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/*
 * class PaperFileWriter
 * Purpose: Output xml file corresponding to data in tables of main application
 * Input format: ArrayList<Paper> variable
 * Design goal: Add method to Paper class to write its own xml and just surround that
 * with paperlist tag.
 */
public class PaperFileWriter {

	String outputFile = null;
	
	public PaperFileWriter(String fn){
		outputFile = fn;
	}
	
	public boolean writeFile(ArrayList<Paper> papers){
		PrintWriter out=null;
		try {
			out = new PrintWriter(outputFile);
		} catch (FileNotFoundException e) {
			System.err.println("Output file not found!");
			return false;
		}
		out.println("<paperlist>");
		for(Paper paper : papers){
			out.println(paper.toXML());
		}
		out.println("</paperlist>\n");
//		System.err.println("If this were a real method I'd be printing the file right now.");
		out.flush();
		return true;
	}
}
