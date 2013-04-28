package filters;

import java.util.ArrayList;
import java.util.List;

import papers.Paper;

public class TitleNameFilter extends PaperStringFilter {

	private ArrayList<String> titleStrings = null;

	public TitleNameFilter(List<Paper> paperList) {
		super(paperList);
		initStrings();
	}

	@Override
	public List<Paper> filterList(String keyword) {
		String query = keyword.toLowerCase();
		ArrayList<Paper> subList = new ArrayList<Paper>();
		for(int i = 0; i < this.paperList.size(); i++){
			if(titleStrings.get(i).contains(query)){
				subList.add(paperList.get(i));
			}
		}
		return subList;
	}

	@Override
	public void papersAdded() {
		int newestPaper = titleStrings.size();
		for(int i = newestPaper; i < paperList.size(); i++){
			String title = paperList.get(i).getField("title");
			if(title == null){
				titleStrings.add("");
			}else{
				titleStrings.add(title.toLowerCase());
			}
		}
	}

	@Override
	public void papersRemoved() {
		initStrings();
	}

	private void initStrings(){
		titleStrings = new ArrayList<String>();
		for(int i = 0; i < paperList.size(); i++){
			Paper paper = paperList.get(i);
			String title = paper.getField("title");
			if(title == null){
				titleStrings.add("");
			}else{
				titleStrings.add(title.toLowerCase());
			}
		}		
	}
}
