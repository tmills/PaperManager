/*
 * Created on Mar 21, 2003
 *
 * @author henkel@cs.colorado.edu
 * 
 */
package bibtex;

import java.io.FileReader;
import java.io.PrintWriter;

import bibtex.dom.BibtexFile;
import bibtex.expansions.CrossReferenceExpander;
import bibtex.expansions.ExpansionException;
import bibtex.expansions.MacroReferenceExpander;
import bibtex.expansions.PersonListExpander;
import bibtex.parser.BibtexParser;

/**
 * This is a simple driver for the bibtex parser and the expansions. Have a look at the code
 * to figure out how to use this parser. Also, you may just run this over your bibtex file
 * to find out whether it can be parsed.
 * 
 * @author henkel
 */
public final class Main {
 
	public static void usage() {
		System.err.println(
			"\nUsage: bibtex.Main [-expandStringDefinitions]\n"
				+ "         [-expandAndDropMacroDefinitions] [-expandCrossReferences]\n"
				+ "         [-expandPersonLists] [-noOutput] <file.bib>\n"
				+ "\nNote: Selecting -expandCrossReferences implies that we will\n"
				+ "      expand the string definitions as well (for consistency).\n"
				+ "\nNote: Selecting -expandPersonLists implies that we will expand\n"
				+ "      the string definitions as well (for consistency)."
				+ "\nThe output will be given on stdout, errors and messages will be printed to stderr.\n\n");
	}

	public static void main(String[] args) {
		//long startTime = System.currentTimeMillis();
		if (args.length < 1) {
			usage();
			return;
		}
		BibtexFile bibtexFile = new BibtexFile();
		BibtexParser parser = new BibtexParser(false);
		//parser.setMultipleFieldValuesPolicy(BibtexMultipleFieldValuesPolicy.KEEP_ALL);
		boolean expandMacros = false;
		boolean dropMacros = false;
		boolean expandCrossrefs = false;
		boolean expandPersonLists = false;
		boolean noOutput = false;
		for (int argsIndex = 0; argsIndex < args.length - 1; argsIndex++) {
			String argument = args[argsIndex];
			if (argument.equals("-expandStringDefinitions")) {
				expandMacros = true;
			} else if (argument.equals("-expandAndDropStringDefinitions")) {
				expandMacros = dropMacros = true;
			} else if (argument.equals("-expandCrossReferences")) {
				expandCrossrefs = expandMacros = true;
			} else if (argument.equals("-expandPersonLists")) {
				expandPersonLists = expandMacros = true;
			} else if(argument.equals("-noOutput")){
				noOutput = true;
			} else {
				System.err.println("Illegal argument: " + argument);
				usage();
			}
		}

		try {
			String filename = args[args.length - 1];
			System.err.println("Parsing \"" + filename + "\" ... ");
			parser.parse(bibtexFile, new FileReader(args[args.length - 1]));
		} catch (Exception e) {
			System.err.println("Fatal exception: ");
			e.printStackTrace();
			return;
		} finally {
			printNonFatalExceptions(parser.getExceptions());
		}
		try {
			if (expandMacros) {
				System.err.println("\n\nExpanding macros ...");
				MacroReferenceExpander expander =
					new MacroReferenceExpander(true, true, dropMacros,false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
				
			}
			if (expandCrossrefs) {
				System.err.println("\n\nExpanding crossrefs ...");
				CrossReferenceExpander expander = new CrossReferenceExpander(false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
			}
			if (expandPersonLists) {
				System.err.println("\n\nExpanding person lists ...");
				PersonListExpander expander = new PersonListExpander(true, true, false);
				expander.expand(bibtexFile);
				printNonFatalExceptions(expander.getExceptions());
			}
		} catch (ExpansionException e1) {
			e1.printStackTrace();
			return;
		}
		if(noOutput) return;
		System.err.println("\n\nGenerating output ...");
		PrintWriter out = new PrintWriter(System.out);
		bibtexFile.printBibtex(out);
		out.flush();
		
		//System.gc();
		//System.err.println("Memory used:"+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
		//System.err.println("This run took "+(System.currentTimeMillis()-startTime)+" ms.");
		//System.out.println("Press any key to exit.");
		//try { System.in.read(); } catch(Exception e){ e.printStackTrace();}
	}

	private static void printNonFatalExceptions(Exception[] exceptions) {
		if (exceptions.length > 0) {
			System.err.println("Non-fatal exceptions: ");
			for (int i = 0; i < exceptions.length; i++) {
				exceptions[i].printStackTrace();
				System.err.println("===================");
			}
		}
	}
}
