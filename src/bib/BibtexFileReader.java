package bib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import papers.Paper;
import bibtex.dom.BibtexAbstractValue;
import bibtex.dom.BibtexEntry;
import bibtex.dom.BibtexFile;
import bibtex.parser.BibtexMultipleFieldValuesPolicy;
import bibtex.parser.BibtexParser;
import bibtex.parser.ParseException;

public class BibtexFileReader {
	public void readBibtext(String text, ArrayList<Paper> papers){
//		System.err.println("Attempting to import file: " + fn);
		BibtexFile bibtexFile = new BibtexFile();
		BibtexParser parser = new BibtexParser(false);
		parser.setMultipleFieldValuesPolicy(BibtexMultipleFieldValuesPolicy.KEEP_LAST);
		
		try {
//			parser.parse(bibtexFile, new FileReader(fn));
			parser.parse(bibtexFile, new StringReader(text));
		} catch (FileNotFoundException e) {
			System.err.println("Error finding bib file");
			return;
		} catch (ParseException e) {
			System.err.println("Error parsing bib file");
			return;
		} catch (IOException e) {
			System.err.println("Error reading bib file");
			return;
		}
		
		Paper newPaper = null;
		for(Object entry : bibtexFile.getEntries()){
			if(entry instanceof BibtexEntry){
//				System.err.println("entry");
				newPaper = new Paper();
				newPaper.setType(((BibtexEntry) entry).getEntryType());
				newPaper.setLabel(((BibtexEntry) entry).getEntryKey());
				Map<String,BibtexAbstractValue> fields = ((BibtexEntry) entry).getFields();
				for(Map.Entry<String, BibtexAbstractValue> mapping : fields.entrySet()){
					String value = mapping.getValue().toString();
					while(value.startsWith("{") && value.endsWith("}")){
						value = value.substring(1, value.length()-1);
					}
					newPaper.setField(mapping.getKey(), value);
				}
				papers.add(newPaper);
			}
		}
		System.err.println("Done parsing file... examining bibtex file...");
	}
}
