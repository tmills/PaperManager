package ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

public class FileDropHandler extends TransferHandler {
	String rootDir=null;
	
	public FileDropHandler(String rootDir){
		super();
		this.rootDir = rootDir;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
	 */
	@Override
	public boolean canImport(TransferSupport support) {
		if(support.isDrop()){
//			System.err.println("can import: ");
			if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
	 */
	@Override
	public boolean importData(TransferSupport support) {
		if(!support.isDrop()){
			return false;
		}
		System.err.println("Dropped into here!");
		Transferable t = support.getTransferable();
		List<File> files=null;
		try {
			files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(File f : files){
			System.out.println("Got file named: " + f.getName());
			if(f.getName().toLowerCase().endsWith("pdf")){
				// move to dropbox directory
				f.renameTo(new File(rootDir + "/" + f.getName()));
			}else{
				return false;
			}
		}
		return super.importData(support);
	}
	
}
