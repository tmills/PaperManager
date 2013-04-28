/*
 * Copyright 2010 Tim Miller
 * This file is part of PaperManager
 * PaperManager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package papers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

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
	
	public boolean writeFile(List<Paper> papers){
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
