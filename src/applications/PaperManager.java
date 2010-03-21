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

package applications;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import papers.*;
import tags.Tag;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

/*
 * This is started based on the TableDemo on the swing tutorial site.
 * TODO (High) Fix exception when removing item
 * TODO (High) Related?^ Get remove working (saving)
 * TODO (High) Add importation of bibtex files
 * TODO Add exportation of bibtex files
 * TODO Separate "card" (?) for unclassified PDFs
 * TODO Auto-scan dropbox directory for new pdfs
 * TODO Link to PDFs
 * TODO (low) Make division between gui elements thicker (prettier)
 * TODO (low) Make "tag add" button put you in new tag editing mode (no double click required) 
 */

public class PaperManager extends JPanel implements ActionListener{
	private boolean DEBUG = true;
	private static final String ADD_CMD = "ADD";
	private static final String RM_CMD = "RM";
	private static final String IMP_CMD = "IMPORT";
	private static final String EXP_CMD = "EXPORT";
	private static final String WRITE_CMD = "WRITE";
	private static final String ADD_TAG_CMD = "ADD_TAG";
	private static final String EXIT = "EXIT";
	private ArrayList<Paper> paperList=null;
	PaperFileWriter writer = null;
	PaperFileReader pfr = null;
	JTable table;
	JTextArea summaryBox=null;
	RefTableModel tModel;
	TagTableModel tagModel;
	
	public PaperManager(String fn) {
		
		super(new BorderLayout());

		// initialize data structures
		pfr = new PaperFileReader(fn);
		paperList = pfr.readFile();
		writer = new PaperFileWriter(fn);
		tModel = new RefTableModel();
		
		// add toolbar and buttons
		JToolBar toolbar = new JToolBar("Tool buttons");
		JButton button = new JButton();
		button.setText(" + ");
		button.addActionListener(this);
		button.setActionCommand(ADD_CMD);
		toolbar.add(button);
		
		button = new JButton();
		button.setText(" - ");
		button.addActionListener(this);
		button.setActionCommand(RM_CMD);
		toolbar.add(button);

		// add table
		table = new RefTable(tModel);

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		
		// create the editing box for summaries
		// pane containing text area and button for saving...
		summaryBox = new JTextArea(1,20);
		summaryBox.setLineWrap(true);
		summaryBox.setWrapStyleWord(true);
		JScrollPane summaryPane = new JScrollPane(summaryBox);
		JPanel summaryPanel = new JPanel(new BorderLayout());
		summaryPanel.add(new JLabel("Summary"), BorderLayout.NORTH);
		summaryPanel.add(summaryPane, BorderLayout.CENTER);
		JButton writeButton = new JButton();
		writeButton.setText("Save Summary");
		writeButton.addActionListener(this);
		writeButton.setActionCommand(WRITE_CMD);
		summaryPanel.add(writeButton, BorderLayout.SOUTH);
		summaryPanel.setPreferredSize(new Dimension(200, 300));
		
		tagModel = new TagTableModel();
		JTable tagList = new JTable(tagModel);
		JPanel tagPanel = new JPanel(new BorderLayout());
		JToolBar tagTools = new JToolBar("Tag Tools");
		JButton addTagButton = new JButton();
		addTagButton.setText(" + ");
		addTagButton.addActionListener(this);
		addTagButton.setActionCommand(ADD_TAG_CMD);
		tagTools.add(addTagButton);
		tagPanel.add(new JLabel("Tag Editor"), BorderLayout.NORTH);
		tagPanel.add(new JScrollPane(tagList), BorderLayout.CENTER);
		tagPanel.add(tagTools, BorderLayout.SOUTH);
		tagPanel.setPreferredSize(new Dimension(200, 300));
		
		// make a split pane?
		JSplitPane sidePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, summaryPanel, tagPanel); 
//new GridLayout(2,1));
//		sidePanel.add(summaryPanel, "1");
//		sidePanel.add(tagPanel, "2");
		
		
		//Add the scroll pane to this panel.
		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(sidePanel, BorderLayout.EAST);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand().equals(ADD_CMD)){
			System.err.println("Add button has been pressed!");
			paperList.add(new Paper());
			tModel.loadData();
			tModel.fireTableDataChanged();
		}else if(arg0.getActionCommand().equals(RM_CMD)){
			System.err.println("Remove button has been pressed!");
			int row = table.getSelectedRow();
			paperList.remove(row);
			tModel.loadData();
			tModel.fireTableDataChanged();
			writer.writeFile(paperList);
		}else if(arg0.getActionCommand().equals(IMP_CMD)){
			System.err.println("Import command triggered");
		}else if(arg0.getActionCommand().equals(EXP_CMD)){
			System.err.println("Export command triggered");
		}else if(arg0.getActionCommand().equals(WRITE_CMD)){
			System.err.println("Write command triggered");
			int row = table.getSelectedRow();
			Paper selPaper = paperList.get(row);
			selPaper.setField("summary", summaryBox.getText());
			writer.writeFile(paperList);
		}else if(arg0.getActionCommand().equals(ADD_TAG_CMD)){
			System.err.println("Add tag command triggered");
			int row = table.getSelectedRow();
			tagModel.addTag(row);
		}else if(arg0.getActionCommand().equals(EXIT)){
			System.err.println("Exit pressed!");
			System.exit(0);
		}else{
			System.err.println("Unknown event: " + arg0.getActionCommand());
		}
	}
	
	class RefTable extends JTable{
		public RefTable(TableModel tm){
			super(tm);
		}
		@Override
		public void valueChanged(ListSelectionEvent e) {
			super.valueChanged(e);
			if(!e.getValueIsAdjusting()){
				DefaultListSelectionModel dlsm = (DefaultListSelectionModel) e.getSource();
				int row = dlsm.getAnchorSelectionIndex();
				Paper curr = paperList.get(row);
				summaryBox.setText(curr.getField("summary"));
				tagModel.setData(curr.getTags());
			}
		}
	}
	
	class RefTableModel extends AbstractTableModel{
		private int LABEL_COL;
		private int TYPE_COL;
		private int VENUE_COL;
		private int PDF_COL;
		private String[] columnNames = {"Label", "Authors",
				"Title",
				"Type",
				"Venue",
				"Year",
				"PDF?"};
		private Object[][] data = null;

		public RefTableModel(){
			loadData();
		}
		
		private void loadData(){
			data = new Object[paperList.size()][];
			int j;
			for(int i = 0; i < data.length; i++){
				j = 0;
				data[i] = new Object[columnNames.length];
				LABEL_COL = j;
				data[i][j++] = paperList.get(i).getLabel();
				data[i][j++] = paperList.get(i).getField("authors");
				data[i][j++] = paperList.get(i).getField("title");
				TYPE_COL = j;
				data[i][j++] = paperList.get(i).getType();
				VENUE_COL = j;
				data[i][j++] = paperList.get(i).getVenue();
				data[i][j++] = paperList.get(i).getField("year");
				PDF_COL = j;
				data[i][j++] = (paperList.get(i).getFile() == null) ? "no" : "yes";
			}
		}
		
		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			//if(col == 0 || col == 1 || col == 4){
			if(col == LABEL_COL){
				paperList.get(row).setLabel((String) value);
			}else if(col == TYPE_COL){
				paperList.get(row).setType((String) value);
			}else if(col == VENUE_COL){
				paperList.get(row).setVenue((String) value);
			}else{
				paperList.get(row).setField(columnNames[col].toLowerCase(), (String) value);
			}
			writer.writeFile(paperList);
			fireTableCellUpdated(row, col);
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's
		 * editable.
		 */
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			if(col == PDF_COL) return false;
			return true;
		}
	}

	class TagTableModel extends AbstractTableModel{
		String[] columnNames = {"Current Tags"};
		private Object[][] data = null;
		Set<Tag> tags=null;
		
		public void setData(Set<Tag> t){
			tags = t;
			data = new Object[tags.size()][1];
			Object[] temp = tags.toArray();
			for(int i = 0; i < temp.length; i++){
				data[i][0] = temp[i];
			}
			fireTableDataChanged();
		}
		
		public void addTag(int row) {
			paperList.get(row).addTag("*NEW* Double click to edit");
			setData(paperList.get(row).getTags());
		}

		public void setValueAt(Object newData, int row, int col){
//			System.err.println("Setting row " + row + " to " + newData);
			Tag oldTag = (Tag) data[row][0];
			System.err.println("Old tag: " + oldTag);
			oldTag.setTag(newData.toString());
			data[row][0] = oldTag;
			writer.writeFile(paperList);
		}
		
		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return tags == null ? 0 : tags.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		
		public boolean isCellEditable(int row, int col){
			return true;
		}
		
		public String getColumnName(int col) {
			return columnNames[0];
		}
	}
	
	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("Paper Manager");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create and set up the content pane.
		PaperManager newContentPane = new PaperManager("papers_test.xml");

		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem mi1 = new JMenuItem("Import .bib file (unsupported)");
		JMenuItem mi2 = new JMenuItem("Export .bib file (unsupported)");
		JMenuItem miLast = new JMenuItem("Exit");
		miLast.setActionCommand(EXIT);
		miLast.addActionListener(newContentPane);
		menu.add(mi1);
		menu.add(mi2);
		menu.add(miLast);
		//menu.addActionListener(newContentPane);
		
		menubar.add(menu);
		
		frame.setJMenuBar(menubar);


		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}


}
