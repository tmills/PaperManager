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
