/* Copyright 2010 Tim Miller
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
package ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;

import bib.BibEntry;

public class BibEditorDialog extends JDialog implements ActionListener {
	BibEntry bib = null;
	private static final String CANCEL_EDIT = "CANCEL";
	private static final String SAVE_EDIT = "SAVE";
	private static final String ADD_FIELD = "ADD_FIELD";
	private boolean dirty = false;
	private HashMap<String,JTextField> tfields = null;
	int fieldWidth = 20;
	JPanel editPanel;
	JTextField labelField; JTextField typeField;
	JTextField newField; JTextField newValue;
	JSplitPane pane;
	public BibEditorDialog(BibEntry b){
		bib = b;
	
		tfields = new LinkedHashMap<String,JTextField>();
		
//		editPanel = new JPanel(new SpringLayout());
		editPanel = getPanel();
		
//		dialog.setPreferredSize(new Dimension(400, 300));
		
		pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		pane.add(editPanel);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand(CANCEL_EDIT);
		cancel.addActionListener(this);
		JButton save = new JButton("Save");
		save.addActionListener(this);
		save.setActionCommand(SAVE_EDIT);
		buttonPanel.add(cancel);
		buttonPanel.add(save);
		pane.add(buttonPanel);
		
		this.setModal(true);
		this.add(pane);
		this.pack();
		
	}

	public boolean isDirty(){
		return dirty;
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.Dialog#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean arg0) {
		if(arg0) dirty = false;
		newField.requestFocus();
		super.setVisible(arg0);
	}

	public JPanel getPanel(){
		Map<String,String> fields = bib.getFields();
		int rows = 2 + fields.size() + 1; //type,label,fields,new
		int cols = 2;
		JPanel editPanel = new JPanel(new SpringLayout());
		JLabel label;
		// label label
		label = new JLabel("Label", JLabel.TRAILING);
		editPanel.add(label);
		// label field
		labelField = new JTextField(bib.getLabel(), fieldWidth);
		labelField.setCaretPosition(0);
		label.setLabelFor(labelField);
		editPanel.add(labelField);
		// type label
		label = new JLabel("Type", JLabel.TRAILING);
		editPanel.add(label);
		// type field
		typeField = new JTextField(bib.getType(), fieldWidth);
		typeField.setCaretPosition(0);
		label.setLabelFor(typeField);
		editPanel.add(typeField);
		JTextField field;
		for(String key : fields.keySet()){
			String value = fields.get(key);
			label = new JLabel(key, JLabel.TRAILING);
			editPanel.add(label);
			field = new JTextField(value, fieldWidth);
			field.setCaretPosition(0);
			label.setLabelFor(field);
			editPanel.add(field);
			tfields.put(key, field);
		}
		
		newField = new JTextField(5);
		newField.addActionListener(this);
		editPanel.add(newField);
		newValue = new JTextField(10);
		newValue.addActionListener(this);
		newValue.setActionCommand(ADD_FIELD);
		editPanel.add(newValue);
		SpringUtilities.makeCompactGrid(editPanel, rows, cols, 5, 5, 5, 5);
		newField.requestFocus();
		return editPanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals(SAVE_EDIT)){
			System.err.println("Save button pressed.");
			setVisible(false);
//			dirty = true;
			String text = labelField.getText();
			if(!text.equals(bib.getLabel())){
				bib.setLabel(text);
				dirty = true;
			}
			text = typeField.getText().toLowerCase();
			if(!text.equalsIgnoreCase(bib.getType())){
				bib.setType(text);
				dirty = true;
			}
			for(String key : tfields.keySet()){
				text = tfields.get(key).getText();
				if(!text.equalsIgnoreCase(bib.getField(key))){
					bib.setField(key, text);
					dirty = true;
				}
			}
		}else if(arg0.getActionCommand().equals(CANCEL_EDIT)){
			dirty = false;
			setVisible(false);
		}else if(arg0.getActionCommand().equals(ADD_FIELD)){
			System.err.println("New field is: " + newField.getText() + " with value: " + newValue.getText());
			pane.remove(editPanel);
			if(!labelField.getText().equals("")){
				bib.setLabel(labelField.getText());
			}
			if(!typeField.getText().equals("")){
				bib.setType(typeField.getText().toLowerCase());
			}
			bib.setField(newField.getText(), newValue.getText());
			editPanel = getPanel();
			pane.add(editPanel);
			newField.requestFocus();
			pack();
			repaint();
			dirty = true;
		}
	}
	
	
}
