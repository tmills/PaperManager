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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;

import papers.Paper;
import papers.PaperFileReader;
import papers.PaperFileWriter;
import tags.Tag;
import ui.BibEditorDialog;
import ui.FileDropHandler;
import ui.MyFileChooser;
import bib.BibtexFileReader;
import bib.BibtexFileWriter;
import filters.AuthorNameFilter;
import filters.PaperStringFilter;
import filters.TitleNameFilter;

/*
 * This is started based on the TableDemo on the swing tutorial site.
 * TODO Search by tag... (large)
 * TODO Improve look on os x (file menu on menu bar instead of on app window)
 * TODO Be able to refresh list when new file is dragged in?
 * TODO Visual reminder to save summary (grayed out save button when fresh?  greyed text area?)
 * TODO (low) Make division between gui elements thicker (prettier)
 */

public class PaperManager extends JPanel implements ActionListener, MouseListener, KeyListener{
	private static final String ADD_CMD = "ADD";
	private static final String RM_CMD = "RM";
	private static final String LINK_CMD = "LINK";
	private static final String OPEN_CMD = "OPEN";
	private static final String IMP_CMD = "IMPORT";
	private static final String EXP_CMD = "EXPORT";
	private static final String WRITE_CMD = "WRITE";
	private static final String NEW_TAG = "NEW_TAG";
	private static final String EDIT_CMD = "EDIT";
//	private static final String CANCEL_EDIT = "CANCEL";
	private static final String SAVE_EDIT = "SAVE_EDIT";
	private static final String EXIT = "EXIT";
	private static final String DB_LOC_KEY = "DB_LOC";
	private static final String EXPORT_DIR = "EXPORT_DIR";
	private static final String SNIP_CMD = "ADD_SNIPPET";
	private static final String FILTER_AUTHOR = "FILTER_AUTHOR";
	private static final String FILTER_TITLE = "FILTER_TITLE";
	
	private static String configurationFilename = "/Users/tmill/.papermanager";
	String sorryMsg = "Sorry, this platform does not support " +
	  "opening PDFs from within the table.  " +
	  "Everything else should work fine though!";
	private List<Paper> fullList=null;
	private List<Paper> displayList=null;
	private HashSet<String> filesLinked=null;
	private static Properties props=null;
	private static File confFile = null;
	PaperFileWriter writer = null;
	PaperFileReader pfr = null;
	BibtexFileReader bibReader = null;
	BibtexFileWriter bibWriter = null;
	private PaperStringFilter paperFilter = null;
	JTable table;
	JTextArea summaryBox=null;
	JTextField tagField=null;
	JTextField filterField = null;
	ButtonGroup filterButtons = null;
	RefTableModel tModel;
	TagTableModel tagModel;
	String rootDir;
	Desktop desktop=null;
	private int LABEL_COL;
	private int TYPE_COL;
	private int VENUE_COL;
	private int PDF_COL;
	private JFrame parent;
	
	public PaperManager(String fn, JFrame p) {
		
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
		parent = p;
		rootDir = (new File(fn)).getParent();
		// initialize data structures
		pfr = new PaperFileReader(fn);
		fullList = pfr.readFile();
		displayList = fullList;
		writer = new PaperFileWriter(fn);
		tModel = new RefTableModel();
		filesLinked = new HashSet();
		
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
		toolbar.addSeparator(new Dimension(100,10));
		
		button = new JButton();
		button.setText("Link");
		button.addActionListener(this);
		button.setActionCommand(LINK_CMD);
		toolbar.add(button);
		
		button = new JButton();
		button.setText("Open PDF");
		button.addActionListener(this);
		button.setActionCommand(OPEN_CMD);
		toolbar.add(button);

		button = new JButton();
		button.setText("Edit");
		button.addActionListener(this);
		button.setActionCommand(EDIT_CMD);
		toolbar.add(button);

		toolbar.addSeparator(new Dimension(50,10));
		toolbar.add(new JLabel("Filter"));
		filterField = new JTextField(20);
//		filterField.addActionListener(this);
		filterField.addKeyListener(this);
		toolbar.add(filterField);
		
		paperFilter = new AuthorNameFilter(fullList);
		filterButtons = new ButtonGroup();
		JRadioButton authorButton = new JRadioButton("Author");
		authorButton.setSelected(true);
		authorButton.addActionListener(this);
		authorButton.setActionCommand(FILTER_AUTHOR);
		JRadioButton titleButton = new JRadioButton("Title");
		titleButton.addActionListener(this);
		titleButton.setActionCommand(FILTER_TITLE);
		
		filterButtons.add(authorButton);
		filterButtons.add(titleButton);
		toolbar.add(authorButton);
		toolbar.add(titleButton);
		
		toolbar.addSeparator(new Dimension(50,10));
		
		button = new JButton();
		button.setText("Export (bib)");
		button.addActionListener(this);
		button.setActionCommand(EXP_CMD);
		toolbar.add(button);
		
		button = new JButton();
		button.setText("Import (bib)");
		button.addActionListener(this);
		button.setActionCommand(IMP_CMD);
		toolbar.add(button);
		
		button = new JButton();
		button.setText("Add bib snippet");
		button.addActionListener(this);
		button.setActionCommand(SNIP_CMD);
		toolbar.add(button);
		
		// add table
		table = new RefTable(tModel);
		table.addMouseListener(this);
		
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
		
		JScrollPane docPanel = buildSecondPanel(); //= new JScrollPane();
//		buildSecondPanel();
		mainPane.addTab("Unbound Documents", docPanel);
		add(mainPane);
	}

	public void reloadList(){
		// create a new list.  probably just make its own list model
	}
	
	private JScrollPane buildSecondPanel(){
		// fill the set of files linked to papers first...
		loadFilenames();
		
		File dir = new File(rootDir);
		String[] files = dir.list(new FilenameFilter(){
				public boolean accept(File f, String s){
					return (s.toLowerCase().endsWith("pdf")) &&
					       !(filesLinked.contains(s));
				}
			});
		JList list = new JList(files);
		list.setTransferHandler(new FileDropHandler(rootDir, this));
//		for(String fn : files){
//			if(!filesLinked.contains(fn)){
//				list.add(new JLabel(fn));
//			}
//		}
		return new JScrollPane(list);
	}
	
	private void loadFilenames() {
		for(Paper paper : displayList){
			if(paper.getFile() != null && !paper.getFile().getName().equals("")){
				filesLinked.add(paper.getFile().getName());
			}
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals(ADD_CMD)){
//			System.err.println("Add button has been pressed!");
			displayList.add(new Paper());
			editPaper(displayList.size()-1);
//			BibEditorDialog dialog = new BibEditorDialog(
//			tModel.loadData();
//			tModel.fireTableDataChanged();
		}else if(arg0.getActionCommand().equals(RM_CMD)){
			//			System.err.println("Remove button has been pressed!");
			if(displayList == fullList){
				int row = table.getSelectedRow();
				displayList.remove(row);
				tModel.loadData();
				tModel.fireTableDataChanged();
				writer.writeFile(displayList);
			}else{
				JOptionPane.showMessageDialog(parent, "This button does not work in filter mode!", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}else if(arg0.getActionCommand().equals(OPEN_CMD)){
//			System.err.println("Open button has been pressed.");
			if(desktop != null){
				File f = displayList.get(table.getSelectedRow()).getFile();
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
//			System.err.println("Import command triggered");
			String fn="";
			if(bibReader == null) bibReader = new BibtexFileReader();
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = fc.showOpenDialog(table);
			if(ret == JFileChooser.APPROVE_OPTION){
				fn = fc.getSelectedFile().getPath();
				try {
					bibReader.readBibtext(FileUtils.readFileToString(fc.getSelectedFile()), displayList);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tModel.loadData();
				tModel.fireTableDataChanged();
				writer.writeFile(displayList);
			}
		}else if(arg0.getActionCommand().equals(SNIP_CMD)){
			if(bibReader == null) bibReader = new BibtexFileReader();
//			JPanel textPanel = new JPanel();
			JTextArea textArea = new JTextArea(10, 50);
			textArea.setEditable(true);
//			textPanel.add(textArea);
			int ret = JOptionPane.showConfirmDialog(null, textArea, "Paste in bib entries:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); 
			if(ret == JOptionPane.CANCEL_OPTION) return;
			bibReader.readBibtext(textArea.getText(), displayList);
			tModel.loadData();
			tModel.fireTableDataChanged();
			writer.writeFile(displayList);
		}else if(arg0.getActionCommand().equals(EXP_CMD)){
//			System.err.println("Export command triggered");
			String fn="";
			if(bibWriter == null) bibWriter = new BibtexFileWriter();
			MyFileChooser fc = new MyFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(props.containsKey(EXPORT_DIR)){
				fc.setSelectedFile(new File((String)props.get(EXPORT_DIR)));
			}
			int ret = fc.showSaveDialog(table);
			if(ret == JFileChooser.APPROVE_OPTION){
				fn = fc.getSelectedFile().getPath();
				props.setProperty(EXPORT_DIR, fn);
				boolean success = bibWriter.writeFile(fn, displayList);
				if(!success){
					JOptionPane.showMessageDialog(this, "ERROR Exporting bib file: Please see stack trace.");
				}
			}
		}else if(arg0.getActionCommand().equals(WRITE_CMD)){
//			System.err.println("Write command triggered");
			int row = table.getSelectedRow();
			Paper selPaper = displayList.get(row);
//			selPaper.setField("summary", summaryBox.getText());
			selPaper.setSummary(summaryBox.getText());
			writer.writeFile(displayList);
		}else if(arg0.getActionCommand().equals(NEW_TAG)){
//			System.err.println("New tag command triggered");
			int row = table.getSelectedRow();
			tagModel.addTag(row, tagField.getText());
			tagField.setText("");
		}else if(arg0.getActionCommand().equals(LINK_CMD)){
//			System.err.println("Link command triggered");
			JFileChooser fc = new JFileChooser(rootDir);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = fc.showOpenDialog(table);
			if(ret == JFileChooser.APPROVE_OPTION){
				int row = table.getSelectedRow();
				displayList.get(row).setFile(fc.getSelectedFile());
				tModel.loadData();
				tModel.fireTableCellUpdated(row, PDF_COL);
				writer.writeFile(displayList);
			}
		}else if(arg0.getActionCommand().equals(EDIT_CMD)){
//			System.err.println("Edit command triggered");
			int row = table.getSelectedRow();
			if(row >= 0){
//				editBibentry(paperList.get(row));/
				editPaper(row);
			}
//		}else if(arg0.getActionCommand().equals(CANCEL_EDIT)){
//			System.err.println("Cancel pressed.");
//		}else if(arg0.getActionCommand().equals(SAVE_EDIT)){
//			System.err.println("Save pressed.");
		}else if(arg0.getActionCommand().equals(EXIT)){
			try {
				props.store(new FileOutputStream(confFile), null);
			} catch (FileNotFoundException e) {
				fatal(e, "ERROR: Could not find file: " + confFile.getAbsolutePath());
			} catch (IOException e) {
				fatal(e, "ERROR: Cannot store properties in file: " + confFile.getAbsolutePath());
			}			
			System.exit(0);
		}else if(arg0.getActionCommand().equals(FILTER_AUTHOR)){
			if(!(paperFilter instanceof AuthorNameFilter)){
				filterField.setText("");
				paperFilter = new AuthorNameFilter(fullList);
				tModel.loadData();
				tModel.fireTableDataChanged();
			}
		}else if(arg0.getActionCommand().equals(FILTER_TITLE)){
			if(!(paperFilter instanceof TitleNameFilter)){
				filterField.setText("");
				paperFilter = new TitleNameFilter(fullList);
				tModel.loadData();
				tModel.fireTableDataChanged();
			}
		}else{
			System.err.println("Unknown event: " + arg0.getActionCommand());
		}
	}
	
	private void editPaper(int row){
		BibEditorDialog dialog = new BibEditorDialog(displayList.get(row).getEntry(), parent);
		dialog.setVisible(true);
		if(dialog.isDirty()){
			tModel.loadData();
			tModel.fireTableDataChanged();
			writer.writeFile(displayList);
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
					Paper curr = displayList.get(row);
					summaryBox.setText(curr.getSummary());
					tagModel.setData(curr.getTags());
				}
			}
		}
		
		public String getToolTipText(MouseEvent e) {
            String tip = null;
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            tip = tModel.getValueAt(rowIndex, colIndex).toString();
            return tip;
		}
	}
	
	class RefTableModel extends AbstractTableModel{
		private String[] columnNames = {"Label", "Author",
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
			data = new Object[displayList.size()][];
			int j;
			for(int i = 0; i < data.length; i++){
				j = 0;
				data[i] = new Object[columnNames.length];
				LABEL_COL = j;
				data[i][j++] = displayList.get(i).getLabel();
				data[i][j++] = displayList.get(i).getField("author");
				data[i][j++] = displayList.get(i).getField("title");
				TYPE_COL = j;
				data[i][j++] = displayList.get(i).getType();
				VENUE_COL = j;
				data[i][j++] = displayList.get(i).getVenue();
				data[i][j++] = displayList.get(i).getField("year");
				PDF_COL = j;
				data[i][j++] = (displayList.get(i).getFile() == null) ? "no" : "yes";
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
				displayList.get(row).setLabel((String) value);
			}else if(col == TYPE_COL){
				displayList.get(row).setType((String) value);
			}else if(col == VENUE_COL){
				displayList.get(row).setVenue((String) value);
			}else{
				displayList.get(row).setField(columnNames[col].toLowerCase(), (String) value);
			}
			writer.writeFile(displayList);
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
			if(col == PDF_COL || col == TYPE_COL) return false;
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
			displayList.get(row).addTag(text);
			setData(displayList.get(row).getTags());
			writer.writeFile(displayList);
		}

		public void setValueAt(Object newData, int row, int col){
//			System.err.println("Setting row " + row + " to " + newData);
			Tag oldTag = (Tag) data[row][0];
			System.err.println("Old tag: " + oldTag);
			oldTag.setTag(newData.toString());
			data[row][0] = oldTag;
			writer.writeFile(displayList);
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
		confFile = new File(configurationFilename);
		
		// if it doesn't exist create it (have to ask for db_dir location)
		if(!confFile.exists()){
			try {
				confFile.createNewFile();
			} catch (IOException e) {
				fatal(e, "Error creating configuration file: " + confFile.getPath());
			}
		}
		
		// load properties whether or not the file has any (like if we just created it above)
		props = new Properties();
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
//		File dbFile = new File("papers_text.xml");
		if(!dbFile.exists()){
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				fatal(e, "ERROR: Could not create file: " + dbFile.getPath());
			}
		}
		
		PaperManager newContentPane = new PaperManager(dbFile.getPath(), frame);

		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem miImport = new JMenuItem("Import .bib file");
		miImport.setActionCommand(IMP_CMD);
		miImport.addActionListener(newContentPane);
		JMenuItem miExport = new JMenuItem("Export .bib file");
		miExport.setActionCommand(EXP_CMD);
		miExport.addActionListener(newContentPane);
		JMenuItem miLast = new JMenuItem("Exit");
		miLast.setActionCommand(EXIT);
		miLast.addActionListener(newContentPane);
		fileMenu.add(miImport);
		fileMenu.add(miExport);
		fileMenu.add(miLast);
		
		JMenu editMenu = new JMenu("Edit");
		JMenuItem miEdit = new JMenuItem("Edit paper info");
		miEdit.setActionCommand(EDIT_CMD);
		miEdit.addActionListener(newContentPane);
		editMenu.add(miEdit);
		menubar.add(fileMenu);
		menubar.add(editMenu);
		
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

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if(arg0.getButton() == MouseEvent.BUTTON3){
			// right click
			JPopupMenu rcMenu = new JPopupMenu();
			JMenuItem editMenu = new JMenuItem("Edit entry");
			editMenu.addActionListener(this);
			editMenu.setActionCommand(EDIT_CMD);
			
			JMenuItem openMenu = new JMenuItem("Open paper");
			openMenu.addActionListener(this);
			openMenu.setActionCommand(OPEN_CMD);
			
			JMenuItem linkMenu = new JMenuItem("Link paper to PDF");
			linkMenu.addActionListener(this);
			linkMenu.setActionCommand(LINK_CMD);
			
			rcMenu.add(editMenu);
			rcMenu.add(openMenu);
			rcMenu.add(linkMenu);
			rcMenu.setLocation(arg0.getPoint());
			rcMenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}	
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// ignore and wait for "released"
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		String textSoFar = filterField.getText();
		if(textSoFar.length() == 0) displayList = fullList;
		else displayList = paperFilter.filterList(textSoFar);
		tModel.loadData();
		tModel.fireTableDataChanged();	
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// ignore and wait for "released"
	}
}
