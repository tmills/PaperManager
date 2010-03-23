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

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import bib.BibtexFileReader;

import papers.*;
import tags.Tag;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

/*
 * This is started based on the TableDemo on the swing tutorial site.
 * TODO (High) Add exportation of bibtex files (need for thesis since I've started using this tool)
 * TODO (High) Have "add paper" button open a dialog to fill in? (use table only for small edits)
 *      Low because most papers will be added w/ bibtex for the time being
 *      High because no way to edit hidden fields
 * TODO Open linked PDFs natively
 * TODO Visual reminder to save summary (grayed out save button when fresh?  greyed text area?)
 * TODO Separate "card" (?) for unclassified PDFs
 * TODO Auto-scan dropbox directory for new pdfs and add to 2nd card
 * TODO (low) Make division between gui elements thicker (prettier)
 */

public class PaperManager extends JPanel implements ActionListener{
	private static final String ADD_CMD = "ADD";
	private static final String RM_CMD = "RM";
	private static final String LINK_CMD = "LINK";
	private static final String OPEN_CMD = "OPEN";
	private static final String IMP_CMD = "IMPORT";
	private static final String EXP_CMD = "EXPORT";
	private static final String WRITE_CMD = "WRITE";
	private static final String NEW_TAG = "NEW_TAG";
	private static final String EXIT = "EXIT";
	private static final String DB_LOC_KEY = "DB_LOC";
	private static String configurationFilename = "/Users/tmill/.papermanager";
	String sorryMsg = "Sorry, this platform does not support " +
	  "opening PDFs from within the table.  " +
	  "Everything else should work fine though!";
	private ArrayList<Paper> paperList=null;
	PaperFileWriter writer = null;
	PaperFileReader pfr = null;
	BibtexFileReader bibReader = null;
	JTable table;
	JTextArea summaryBox=null;
	JTextField tagField=null;
	RefTableModel tModel;
	TagTableModel tagModel;
	String rootDir;
	Desktop desktop=null;
	private int LABEL_COL;
	private int TYPE_COL;
	private int VENUE_COL;
	private int PDF_COL;
	
	public PaperManager(String fn) {
		
		super(new BorderLayout());
		try{
			if(!Desktop.isDesktopSupported()){
				// this is a problem on some platforms
				noDesktop(this);
			}else{
				desktop = Desktop.getDesktop();
				if(!desktop.isSupported(Desktop.Action.OPEN)){
					noDesktop(this);
					desktop = null;
				}
			}
		}catch(UnsupportedOperationException e){
			// apparently this is a possibility with older apis?
			noDesktop(this);
		}
		rootDir = (new File(fn)).getParent();
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
		// put in a spacer so we don't accidentally hit remove!
		toolbar.addSeparator();
		
		button = new JButton();
		button.setText("Link");
		button.addActionListener(this);
		button.setActionCommand(LINK_CMD);
		toolbar.add(button);
		
		button = new JButton();
		button.setText("-->");
		button.addActionListener(this);
		button.setActionCommand(OPEN_CMD);
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
		tagField = new JTextField();
		tagField.setActionCommand(NEW_TAG);
		tagField.addActionListener(this);
		tagPanel.add(new JScrollPane(tagList), BorderLayout.CENTER);
		tagPanel.add(tagField, BorderLayout.SOUTH);
		tagPanel.setPreferredSize(new Dimension(200, 300));
		
		// make a split pane?
		JSplitPane sidePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, summaryPanel, tagPanel); 		
		
		JPanel entryPanel = new JPanel(new BorderLayout());
		
		JTabbedPane mainPane = new JTabbedPane();
		//Add the scroll pane to this panel.
		entryPanel.add(toolbar, BorderLayout.NORTH);
		entryPanel.add(scrollPane, BorderLayout.CENTER);
		entryPanel.add(sidePanel, BorderLayout.EAST);
		mainPane.addTab("Bibtex Entries", entryPanel);
		
		JPanel docPanel = new JPanel(new BorderLayout());
		mainPane.addTab("Unbound Documents", docPanel);
		add(mainPane);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals(ADD_CMD)){
//			System.err.println("Add button has been pressed!");
			paperList.add(new Paper());
			tModel.loadData();
			tModel.fireTableDataChanged();
		}else if(arg0.getActionCommand().equals(RM_CMD)){
//			System.err.println("Remove button has been pressed!");
			int row = table.getSelectedRow();
			paperList.remove(row);
			tModel.loadData();
			tModel.fireTableDataChanged();
			writer.writeFile(paperList);
		}else if(arg0.getActionCommand().equals(OPEN_CMD)){
			System.err.println("Open button has been pressed.");
			if(desktop != null){
				File f = paperList.get(table.getSelectedRow()).getFile();
				if(f != null){
					try {
						System.err.println("Opening: " + f);
						File fullP = new File(rootDir + "/" + f.getName());
						if(fullP.exists()){
							desktop.open(fullP);
						}
					} catch (IOException e) {
						JOptionPane.showMessageDialog(this, "Sorry, could not open file!", "Error", ERROR);
//						e.printStackTrace();
					}
				}else{
					System.err.println("No file associated with that paper!");
				}
			}
		}else if(arg0.getActionCommand().equals(IMP_CMD)){
			System.err.println("Import command triggered");
			String fn="";
			if(bibReader == null) bibReader = new BibtexFileReader();
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = fc.showOpenDialog(table);
			if(ret == JFileChooser.APPROVE_OPTION){
				fn = fc.getSelectedFile().getPath();
				bibReader.readFile(fn, paperList);
				tModel.loadData();
				tModel.fireTableDataChanged();
				writer.writeFile(paperList);
			}
		}else if(arg0.getActionCommand().equals(EXP_CMD)){
			System.err.println("Export command triggered");
		}else if(arg0.getActionCommand().equals(WRITE_CMD)){
			System.err.println("Write command triggered");
			int row = table.getSelectedRow();
			Paper selPaper = paperList.get(row);
//			selPaper.setField("summary", summaryBox.getText());
			selPaper.setSummary(summaryBox.getText());
			writer.writeFile(paperList);
		}else if(arg0.getActionCommand().equals(NEW_TAG)){
//			System.err.println("New tag command triggered");
			int row = table.getSelectedRow();
			tagModel.addTag(row, tagField.getText());
			tagField.setText("");
		}else if(arg0.getActionCommand().equals(LINK_CMD)){
			System.err.println("Link command triggered");
			JFileChooser fc = new JFileChooser(rootDir);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = fc.showOpenDialog(table);
			if(ret == JFileChooser.APPROVE_OPTION){
				int row = table.getSelectedRow();
				paperList.get(row).setFile(fc.getSelectedFile());
				tModel.loadData();
				tModel.fireTableCellUpdated(row, PDF_COL);
				writer.writeFile(paperList);
			}
		}else if(arg0.getActionCommand().equals(EXIT)){
//			System.err.println("Exit pressed!");
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
//				System.err.println("Value changed: " + e);
				DefaultListSelectionModel dlsm = (DefaultListSelectionModel) e.getSource();
				int row = dlsm.getAnchorSelectionIndex();
				if(row >= 0){
					Paper curr = paperList.get(row);
					summaryBox.setText(curr.getSummary());
					tagModel.setData(curr.getTags());
				}
			}
		}
	}
	
	class RefTableModel extends AbstractTableModel{
		private String[] columnNames = {"Label", "Author(s)",
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
				data[i][j++] = paperList.get(i).getField("author");
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
		 * Special case fixes problem of empty cells causing null pointer exceptions
		 */
		public Class getColumnClass(int c) {
			if(getValueAt(0,c)== null){
				return (new String("")).getClass();
			}
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
		
		public void addTag(int row, String text) {
			paperList.get(row).addTag(text);
			setData(paperList.get(row).getTags());
			writer.writeFile(paperList);
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
	
	private void noDesktop(JPanel parent){
		JOptionPane.showMessageDialog(parent, sorryMsg);
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
		
		// first read conf file (or create if doesn't exist)
		configurationFilename = System.getProperty("user.home") + "/.papermanager";
		File confFile = new File(configurationFilename);
		
		// if it doesn't exist create it (have to ask for db_dir location)
		if(!confFile.exists()){
			try {
				confFile.createNewFile();
			} catch (IOException e) {
				fatal(e, "Error creating configuration file: " + confFile.getPath());
			}
		}
		
		// load properties whether or not the file has any (like if we just created it above)
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(configurationFilename));
		} catch (FileNotFoundException e1) {
			fatal(e1, "ERROR: Could not find file: " + configurationFilename);
		} catch (IOException e1) {
			fatal(e1, "ERROR: Could not handle file: " + configurationFilename);
		}
		
		// if we have no properties (only property right now is path to folder w/ pdfs and papers.xml)
		// then we bring up a file dialog... this is tricky across different platforms i guess?
		if(!props.containsKey(DB_LOC_KEY)){
			String dbDir="";
			
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = fileChooser.showOpenDialog(null);
			if(ret == JFileChooser.APPROVE_OPTION){
				dbDir = fileChooser.getSelectedFile().getPath();
			}else{
				JOptionPane.showMessageDialog(null, "Error: No directory selected.  Please figure out what directory you would like and re-run.", "Alert", JOptionPane.WARNING_MESSAGE);
				System.exit(0);
			}
			
/*			// works in os x, not in linux!
			System.setProperty("linux.awt.fileDialogForDirectories", "true");
			FileDialog fileDialog = new FileDialog(frame);
			fileDialog.setDirectory(System.getProperty("user.home"));
			fileDialog.setVisible(true);
			String dir = fileDialog.getDirectory();
			String fn = fileDialog.getFile();
			String dbDir = dir;
			if(fn != null){
				dbDir += fn;
			}
*/
			props.setProperty(DB_LOC_KEY, dbDir);
			try {
				props.store(new FileOutputStream(confFile), null);
			} catch (FileNotFoundException e) {
				fatal(e, "ERROR: Could not find file: " + confFile.getAbsolutePath());
			} catch (IOException e) {
				fatal(e, "ERROR: Cannot store properties in file: " + confFile.getAbsolutePath());
			}
		}
		
		// read configuration file
		String dbDir = props.getProperty(DB_LOC_KEY);
		
		//Create and set up the content pane.
		File dbFile = new File(dbDir + "/papers.xml");
		if(!dbFile.exists()){
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				fatal(e, "ERROR: Could not create file: " + dbFile.getPath());
			}
		}
		
		PaperManager newContentPane = new PaperManager(dbFile.getPath());

		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem miImport = new JMenuItem("Import .bib file");
		miImport.setActionCommand(IMP_CMD);
		miImport.addActionListener(newContentPane);
		JMenuItem miExport = new JMenuItem("Export .bib file (unimplemented)");
		miExport.setActionCommand(EXP_CMD);
		miExport.addActionListener(newContentPane);
		JMenuItem miLast = new JMenuItem("Exit");
		miLast.setActionCommand(EXIT);
		miLast.addActionListener(newContentPane);
		menu.add(miImport);
		menu.add(miExport);
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

	private static void fatal(Exception e, String s){
		e.printStackTrace();
		System.err.println(s);
	}

}
