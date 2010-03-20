package applications;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;

import papers.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/** 
 * This is built out of the TableDemo on the swing tutorial site.
 * TODO (High) Implement exit menu item
 * TODO (High) Add importation of bibtex files
 * TODO Add exportation of bibtex files
 * TODO (HIGH) Add tag editor pane
 * TODO (HIGH) Add summary editor pane
 * TODO Separate pane for unclassified PDFs
 * TODO Auto-scan dropbox directory for new pdfs
 * TODO Link to PDFs
 */

public class PaperManager extends JPanel implements ActionListener{
	private boolean DEBUG = true;
	private static final String ADD_CMD = "ADD";
	private static final String RM_CMD = "RM";
	private ArrayList<Paper> paperList=null;
	PaperFileWriter writer = null;
	PaperFileReader pfr = null;
	JTable table;
	MyTableModel tModel;
	
	public PaperManager(String fn) {
		
		super(new BorderLayout());

		// initialize data structures
		pfr = new PaperFileReader(fn);
		paperList = pfr.readFile();
		writer = new PaperFileWriter(fn);
		tModel = new MyTableModel();
		
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
		table = new JTable(tModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		//Add the scroll pane to this panel.
		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
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
		}		
	}
	
	class MyTableModel extends AbstractTableModel{
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

		public MyTableModel(){
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
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col
						+ " to " + value
						+ " (an instance of "
						+ value.getClass() + ")");
			}

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

			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
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


		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i=0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j=0; j < numCols; j++) {
					System.out.print("  " + data[i][j]);
				}
				System.out.println();
			}
			System.out.println("--------------------------");
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
		JMenuItem miLast = new JMenuItem("Exit (unsupported)");
		menu.add(mi1);
		menu.add(mi2);
		menu.add(miLast);
		
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
