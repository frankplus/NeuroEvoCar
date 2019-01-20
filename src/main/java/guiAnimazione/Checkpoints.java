package guiAnimazione;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class manages the checkpoints of the track, it is initialized with a JSON object
 * Questa classe gestisce i checkpoint del circuito, viene inizializzato con l'oggetto json 
 * ricavato dal file .map del circuito e viene caricato un array di tipo Line2D
 * @author Francesco Pham
 */
public class Checkpoints {
	private Line2D checkpoints[];
	private int ncheckpoints;
	
	@SuppressWarnings("unchecked")
	public Checkpoints (JSONArray checkpoints){
		ncheckpoints = checkpoints.size();
		this.checkpoints = new Line2D[ncheckpoints];
		
		//create checkpoints array
		Iterator<JSONObject> iterator = checkpoints.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			JSONObject checkpoint = iterator.next();
			int x1 = Integer.parseInt(checkpoint.get("x1").toString());
			int y1 = Integer.parseInt(checkpoint.get("y1").toString());
			int x2 = Integer.parseInt(checkpoint.get("x2").toString());
			int y2 = Integer.parseInt(checkpoint.get("y2").toString());
			this.checkpoints[i] = new Line2D.Double(x1,y1,x2,y2);
			i++;
		}
	}
	
	/*private double pointToLineDistance(Segment line, Point P) {
		Point A = new Point(line.getX1(),line.getY1());
		Point B = new Point(line.getX2(),line.getY2());
		double normalLength = Math.sqrt((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
		return Math.abs((P.x-A.x)*(B.y-A.y)-(P.y-A.y)*(B.x-A.x))/normalLength;
	}*/
	
	/**
	 * Calcola la distanza del punto della macchina dal checkpoint indicato
	 * @param Pcar punto contenente le coordinate della macchina
	 * @param indexcheckpoint checkpoint di riferimento
	 * @return distanza del checkpoint dal punto
	 */
	public double getDistanceNextChkpnt(Point2D Pcar, int indexcheckpoint){
		//return pointToLineDistance(checkpoints[indexcheckpoint%ncheckpoints], Pcar);
		return checkpoints[indexcheckpoint%ncheckpoints].ptSegDist(Pcar);
	}
	
	/**
	 * Ritorna l'elenco dei checkpoints
	 * @return L'elenco dei checkpoints in un array di tipo Line2D
	 */
	public Line2D[] getCheckpoints(){
		return checkpoints;
	}
	
	/**
	 * Ritorna il numero totale di checkpoints
	 * @return Numero totale di checkpoints
	 */
	public int getNcheckpoints() {
		return ncheckpoints;
	}
}
