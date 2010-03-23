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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import tags.Tag;

import bib.BibEntry;

public class Paper {
	BibEntry bib=null;
	HashSet<Tag> tags=null;
	String summary=null;
	File fp=null;
	
	public Paper(){
		bib = new BibEntry();
		tags = new LinkedHashSet<Tag>();
	}

	public String getType(){
		return bib.getType();
	}
	public void setType(String t){
		bib.setType(t);
	}

	public String getLabel(){
		return bib.getLabel();
	}
	
	public void setLabel(String l){
		bib.setLabel(l);
	}
	
	public void addTag(String t){
		tags.add(new Tag(t));
	}
	
	public Set<Tag> getTags(){
		return tags;
	}
	
	public void setSummary(String s){
		summary = s;
	}
	
	public String getSummary(){
		return summary;
	}
	
	public void setField(String key, String val){
		bib.setField(key, val);
	}
	
	public String getField(String key){
		String ret = bib.getField(key);
		if(ret == null) return "";
		return bib.getField(key);
	}
	
	public String getVenue(){
		if(bib.getType().equalsIgnoreCase("article")){
			return bib.getField("journal");
		}else if(bib.getType().equalsIgnoreCase("book")){
			return bib.getField("publisher");
		}else if(bib.getType().equalsIgnoreCase("inproceedings")){
			return bib.getField("booktitle");
		}else if(bib.getType().equalsIgnoreCase("phdthesis")){
			return bib.getField("institution");
		}else if(bib.getType().equalsIgnoreCase("techreport")){
			return bib.getField("techreport");
		}else{
			return "";
		}
	}
	
	public void setVenue(String v){
		if(bib.getType().equalsIgnoreCase("article")){
			bib.setField("journal",v);
		}else if(bib.getType().equalsIgnoreCase("book")){
			bib.setField("publisher",v);
		}else if(bib.getType().equalsIgnoreCase("inproceedings")){
			bib.setField("booktitle",v);
		}else if(bib.getType().equalsIgnoreCase("phdthesis")){
			bib.setField("institution",v);
		}else if(bib.getType().equalsIgnoreCase("techreport")){
			bib.setField("techreport",v);
		}
	}
	
	public File getFile(){
		return fp;
	}
	
	public void setFile(File f){
		fp = f;
	}
	
	public String toXML(){
		StringBuilder out = new StringBuilder();
		HashMap<String,String> fields = bib.getFields();
		out.append("<paper>\n");
		out.append("\t<bibentry>\n");
		out.append("\t\t<type>" + bib.getType() + "</type>\n");
		out.append("\t\t<label>" + bib.getLabel() + "</label>\n");
		for(String key : fields.keySet()){
			out.append("\t\t<" + key + ">" + fields.get(key) + "</" + key + ">\n");
		}
		out.append("\t</bibentry>\n");
		if(summary != null){
			out.append("\t<summary>" + summary + "</summary>\n");
		}
		if(tags.size() > 0){
			out.append("\t<taglist>\n");
			for(Tag tag : tags){
				out.append("\t\t<tag>" + tag.getTag() + "</tag>\n");
			}
			out.append("\t</taglist>\n");
		}
		if(fp != null){
			out.append("\t<filename>" + fp.getName() + "</filename>\n");
		}
		out.append("</paper>");
		return out.toString();
	}
}
