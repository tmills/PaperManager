package bib;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class BibEntry {
	String label="";
	String type="";

	HashMap<String,String> fields = new LinkedHashMap<String,String>();
	/*
	String authors="";
	String title="";
	String booktitle="";
	String journal="";
	int startPage; int endPage;
	String publisher="";
	String institution="";
	int volume;
	String year="";
*/	
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	public void setField(String key, String value){
		fields.put(key, value);
	}
	
	public String getField(String key){
		return fields.get(key);
	}
	
	public HashMap<String,String> getFields(){
		return fields;
	}
}
