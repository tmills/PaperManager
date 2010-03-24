package ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class MyFileChooser extends JFileChooser {
	public void approveSelection(){
		File f = this.getSelectedFile(); 
		if(f.exists()){ 
			int result = JOptionPane.showConfirmDialog(this,"Overwrite existing file?","Overwrite?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); 
			if (result != JOptionPane.OK_OPTION) 
				return ; 
			else{ 
				this.setSelectedFile(f); 
			} 
		}
		super.approveSelection();
	} 
}
