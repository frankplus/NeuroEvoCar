package jNeatCommon;

import java.util.*;
import java.io.*;

public class IOseq {
	private String NomeFile;
	private PrintWriter pW;
	private BufferedReader bR;
	
	public final static int TYPE_FILE = 1;
	public final static int TYPE_RESOURCE = 2;

	public String IOseqRead() {
		String line = "?";

		try {
			line = bR.readLine();
			if (line == null || line == "")
				line = "EOF";
		} catch (Exception evt) {
			line = "EXC";
			evt.printStackTrace();
		}

		return line;

	}

	/**
	 * jSeqFile constructor comment.
	 */
	public IOseq() {
		NomeFile = "Logxxxx.txt";
		pW = null;
		bR = null;
	}

	public IOseq(String xNome) {
		NomeFile = xNome;
		pW = null;
		bR = null;
	}

	public void IOseqOpenW(boolean h) {
		try {
			File resource = new File(NomeFile);
			
			//create folders if not exist
			if(!resource.exists()){
				try{
					resource.getParentFile().mkdirs();
				}
				catch(SecurityException e){
					e.printStackTrace();
				}
			}
			
			pW = new PrintWriter(resource);
			if (h == true) {
				String testata = "Created in date --> " + (new Date());
				pW.println(testata);
			}
		} catch (Exception evt) {
			evt.printStackTrace();
		}
	}

	public void IOseqWrite(String testo) {
		pW.println(testo);
	}

	public void IOseqCloseR() {
		try {
			bR.close();

		} catch (Exception evt) {
			evt.printStackTrace();
		}
	}

	public void IOseqCloseW() {
		try {
			pW.flush();
		} catch (Exception evt) {
			evt.printStackTrace();
		}
	}

	public boolean IOseqOpenRfromResource() {
		try {
			InputStream resource = getClass().getResourceAsStream(NomeFile);
			InputStreamReader isReader = new InputStreamReader(resource);
			bR = new BufferedReader(isReader);
		} catch (Exception evt) {
			evt.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean IOseqOpenRfromFile() {
		try {
			FileInputStream inputstream = new FileInputStream(NomeFile);
			InputStreamReader isReader = new InputStreamReader(inputstream);
			bR = new BufferedReader(isReader);
		} catch (Exception evt) {
			evt.printStackTrace();
			return false;
		}
		return true;
	}

}