package applications;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

public class TestPDFBox {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PDDocument document;
		try {
			document = PDDocument.load("test.pdf");
			PDDocumentInformation info = document.getDocumentInformation();
			System.out.println( "Page Count=" + document.getNumberOfPages() );
			System.out.println( "Title=" + info.getTitle() );
			System.out.println( "Author=" + info.getAuthor() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
