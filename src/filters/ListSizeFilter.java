package filters;

import java.util.List;

import papers.Paper;

public class ListSizeFilter extends PaperStringFilter {

	public ListSizeFilter(List<Paper> paperList) {
		super(paperList);
	}

	@Override
	public List<Paper> filterList(String keyword) {
//		ArrayList<Paper>
		try{
			int size = Integer.parseInt(keyword);
			if(size >= paperList.size()) size = paperList.size();
			return this.paperList.subList(0, size-1);
		}catch(Exception e){
			System.err.println("Problem reading string or getting sublist: " + e.getMessage());
		}
		return this.paperList;
	}

	@Override
	public void papersAdded() {
		// don't need to do anything
	}

	@Override
	public void papersRemoved() {
		// don't need to do anything
	}

}
