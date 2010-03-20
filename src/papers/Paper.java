package papers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import tags.Tag;

import bib.BibEntry;

public class Paper {
	BibEntry bib=null;
	HashSet<Tag> tags=null;
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
	
	public void setField(String key, String val){
		bib.setField(key, val);
	}
	
	public String getField(String key){
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
		out.append("\t<type>" + bib.getType() + "</type>\n");
		out.append("\t<label>" + bib.getLabel() + "</label>\n");
		for(String key : fields.keySet()){
			out.append("\t<" + key + ">" + fields.get(key) + "</" + key + ">\n");
		}
		out.append("\t<taglist>\n");
		for(Tag tag : tags){
			out.append("\t\t<tag>" + tag.getTag() + "</tag>\n");
		}
		out.append("\t</taglist>\n");
		out.append("</paper>");
		return out.toString();
	}
}
