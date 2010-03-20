package papers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PaperFileReader {
	String fn;
	ArrayList<Paper> paperList;
	XMLReader parser;
	Paper current;
	
	private boolean inPaper = false;
	private boolean inTaglist = false;
	private boolean inTag = false;
	private boolean inLabel = false;
	private boolean inType = false;
	StringBuilder name = null;
	HashMap<String,Boolean> stateTracker = new HashMap<String,Boolean>();
	
	class PaperFileHandler extends DefaultHandler{
		public PaperFileHandler(){
		}
		
		public void startElement(String nsURI, String strippedName,	String tagName, Attributes attributes){
			if(tagName.equalsIgnoreCase("paper")){
				inPaper = true;
				current = new Paper();
				paperList.add(current);
			}else if(tagName.equalsIgnoreCase("type")){
				inType = true;
				name = new StringBuilder();
			}else if(tagName.equalsIgnoreCase("label")){
				inLabel = true;
				name = new StringBuilder();
			}else if(tagName.equalsIgnoreCase("taglist")){
				// not really necessary, but keeps us out of inPaper case...
				inTaglist = true;
			}else if(tagName.equalsIgnoreCase("tag")){
				inTag = true;
				name = new StringBuilder();
			}else if(inPaper){
				stateTracker.put(tagName.toLowerCase(), true);
				name = new StringBuilder();
			}
			
			/*
			}else if(tagName.equalsIgnoreCase("title")){
				inTitle = true;
				name = new StringBuilder();
			}else if(tagName.equalsIgnoreCase("authors")){
				inAuthors = true;
				name = new StringBuilder();
			}else if(tagName.equalsIgnoreCase("type")){
				inType = true;
				name = new StringBuilder();
			}*/
		}
		
		public void endElement(String nsURI, String strippedName,	String tagName){
			if(tagName.equalsIgnoreCase("paper")){
				current = null;
				name = null;
				inPaper = false;
			}else if(tagName.equalsIgnoreCase("type")){
				inType = false;
				current.setType(name.toString());
				name = null;
			}else if(tagName.equalsIgnoreCase("label")){
				inLabel = false;
				current.setLabel(name.toString());
				name = null;
			}else if(tagName.equalsIgnoreCase("taglist")){
				inTaglist = false;
			}else if(tagName.equalsIgnoreCase("tag")){
				current.addTag(name.toString());
				inTag = false;
			}else if(stateTracker.containsKey(tagName.toLowerCase())){
				stateTracker.put(tagName.toLowerCase(), false);
				if(name == null){
					System.err.println("Error in herre.");
				}
				current.setField(tagName.toLowerCase(), name.toString());
				name = null;
			}
			
			/*
			}else if(tagName.equalsIgnoreCase("title")){
				inTitle = false;
				current.setTitle(name.toString());
			}else if(tagName.equalsIgnoreCase("authors")){
				inAuthors = false;
				current.setAuthors(name.toString());
			}else if(tagName.equalsIgnoreCase("type")){
				inType = false;
				current.setType(name.toString());
			}*/
		}
		
		public void characters(char[] ch, int start, int length){
			if(name != null){
				name.append(ch, start, length);
			}
		}
	}
	
	public PaperFileReader(String fn){
		paperList = new ArrayList<Paper>();
		try {
			parser = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			System.err.println("Error building XMLReader: " + e);
			e.printStackTrace();
			System.exit(-1);
		}
		parser.setContentHandler(new PaperFileHandler());
		this.fn = fn;
	}
	
	public ArrayList<Paper> readFile(){
		try {
			parser.parse(fn);
		} catch (SAXException e) {
			e.printStackTrace();
			return new ArrayList<Paper>();
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Paper>();
		}
		return paperList;
	}
}
