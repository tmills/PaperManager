package filters;

import java.util.ArrayList;
import java.util.List;

import papers.Paper;

public class AuthorNameFilter extends PaperStringFilter {

	private ArrayList<String> authorStrings = null;
	
	public AuthorNameFilter(List<Paper> paperList) {
		super(paperList);
		authorStrings = new ArrayList<String>();
		for(int i = 0; i < paperList.size(); i++){
			Paper paper = paperList.get(i);
			String author = paper.getField("author");
			if(author == null){
				authorStrings.add("");
			}else{
				authorStrings.add(author.toLowerCase());
			}
		}
	}

	@Override
	public List<Paper> filterList(String keyword) {
		String query = keyword.toLowerCase();
		ArrayList<Paper> subList = new ArrayList<Paper>();
		for(int i = 0; i < this.paperList.size(); i++){
			if(authorStrings.get(i).contains(query)){
				subList.add(paperList.get(i));
			}
		}
		return subList;
	}

}
