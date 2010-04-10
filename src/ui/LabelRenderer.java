package ui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class LabelRenderer implements TableCellRenderer {
	Map<String,Integer> labels=null;
	
	public LabelRenderer(Map<String,Integer> labelsUsed) {
		labels = labelsUsed;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel myLabel = new JLabel(); //(String) value);
		String label = (String) value;
		myLabel.setText(label);
		
		if(column == 0 && labels.get(label) > 1){
			myLabel.setBorder(BorderFactory.createLineBorder(Color.red));
		}else if(isSelected){
			myLabel.setBorder(BorderFactory.createTitledBorder(label));
		}
		return myLabel;
	}

}
