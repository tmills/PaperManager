package filters;

import java.util.ArrayList;
import java.util.List;

import papers.Paper;

public class AuthorNameFilter extends PaperStringFilter {

	private ArrayList<String> authorStrings = null;
	
	public AuthorNameFilter(List<Paper> paperList) {
		super(paperList);
		initStrings();
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

	@Override
	public void papersAdded() {
		int newestPaper = authorStrings.size();
		for(int i = newestPaper; i < paperList.size(); i++){
			String author = paperList.get(i).getField("author");
			if(author == null){
				authorStrings.add("");
			}else{
				authorStrings.add(author.toLowerCase());
			}
		}
	}

	@Override
	public void papersRemoved() {
		initStrings();
	}
	
	private void initStrings(){
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
}
