package bib;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import papers.Paper;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;

public class BibtexFileWriter {

	public boolean writeFile(String fn, List<Paper> papers){
		BibtexFile bfile = new BibtexFile();
		for(Paper paper : papers){
			BibtexEntry entry = bfile.makeEntry(paper.getType(), paper.getLabel());
			for(String key : paper.getFields()){
				entry.setField(key, bfile.makeString(paper.getField(key)));
			}
			bfile.addEntry(entry);
		}
		PrintWriter out=null;
		try {
			out = new PrintWriter(fn);
			bfile.printBibtex(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
