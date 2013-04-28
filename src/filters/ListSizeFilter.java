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
			return this.paperList.subList(0, size);
		}catch(Exception e){
			System.err.println("Problem reading string or getting sublist: " + e.getMessage());
		}
		return this.paperList;
	}

}
