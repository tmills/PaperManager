package papers;

import java.io.IOException;
import java.util.ArrayList;

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
	
	private boolean inAuthors = false;
	private boolean inTitle = false;
	StringBuilder title = null;
	StringBuilder authors = null;
	
	class PaperFileHandler extends DefaultHandler{
		public void startElement(String nsURI, String strippedName,	String tagName, Attributes attributes){
			if(tagName.equalsIgnoreCase("paper")){
				current = new Paper();
				paperList.add(current);
				title = new StringBuilder();
				authors = new StringBuilder();
			}else if(tagName.equalsIgnoreCase("title")){
				inTitle = true;
			}else if(tagName.equalsIgnoreCase("authors")){
				inAuthors = true;
			}
		}
		
		public void endElement(String nsURI, String strippedName,	String tagName){
			if(tagName.equalsIgnoreCase("paper")){
				current = null;
			}else if(tagName.equalsIgnoreCase("title")){
				inTitle = false;
				current.setTitle(title.toString());
			}else if(tagName.equalsIgnoreCase("authors")){
				inAuthors = false;
				current.setAuthors(authors.toString());
			}
		}
		
		public void characters(char[] ch, int start, int length){
			if(inTitle){
				title.append(ch, start, length);
			}
		}
	}
	
	public PaperFileReader(String fn){
		paperList = new ArrayList<Paper>();
		try {
			parser = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			System.err.println("Error building XMLReader: " + e);
			e.printStackTrace();
			System.exit(-1);
		}
		parser.setContentHandler(new PaperFileHandler());
		this.fn = fn;
	}
	
	public ArrayList<Paper> readFile() throws IOException, SAXException{
		parser.parse(fn);
		return paperList;
	}
}
