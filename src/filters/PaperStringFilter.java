package filters;

import java.util.List;

import papers.Paper;

public abstract class PaperStringFilter {

	protected List<Paper> paperList;

	public PaperStringFilter(List<Paper> paperList){
		this.paperList = paperList;
	}
	
	public abstract List<Paper> filterList(String keyword);
}
