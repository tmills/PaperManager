package papers;

import java.io.File;
import java.util.HashSet;

import tags.Tag;

import bib.BibEntry;

public class Paper {
	BibEntry bib=null;
	HashSet<Tag> tags=null;
	File fp=null;
	
	public Paper(){
		bib = new BibEntry();
		tags = new HashSet<Tag>();
	}
	public void setTitle(String title){
		bib.setTitle(title);
	}
	
	public void setAuthors(String authors){
		bib.setAuthors(authors);
	}
}
