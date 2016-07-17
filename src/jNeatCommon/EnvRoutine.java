package jNeatCommon;

import java.awt.*;
import java.io.File;

import jneat.Neat;

public class EnvRoutine {
	public static String getUserFileData(String filename) {
		//folder in user directory
		String path = EnvConstant.USERAPP_DIR + EnvConstant.OS_FILE_SEP + filename;
		return path;
	}
	
	public static String getResourceFileData(String filename) {
		//local folder
		String path = EnvConstant.RESOURCES_DIR + EnvConstant.OS_FILE_SEP + filename;
		return path;
	}
	
	public static boolean readParameters(){
		//lettura parametri da file controllando l'esistenza di parametri definiti da utente
		boolean res;
		String paramsPath = EnvRoutine.getUserFileData(EnvConstant.NAME_PARAMETER);
		File f = new File(paramsPath);
		if(f.exists() && !f.isDirectory()){
			res = Neat.readParam(paramsPath,IOseq.TYPE_FILE);
		}else{
			paramsPath = EnvRoutine.getResourceFileData(EnvConstant.NAME_PARAMETER);
			res = Neat.readParam(paramsPath,IOseq.TYPE_RESOURCE);
		}
		return res;
	}

	public static Color GetForeColorPlot(int _codeColor) {
		Color c = new Color(0, 0, 0);
		if (_codeColor < 0)
			c = new Color(196, 0, 0);

		if (_codeColor > 0)
			c = new Color(132, 0, 128);

		if (_codeColor == 1)
			c = new Color(0, 32, 128);

		if (_codeColor == 2)
			c = new Color(255, 255, 200);
		return c;
	}
}