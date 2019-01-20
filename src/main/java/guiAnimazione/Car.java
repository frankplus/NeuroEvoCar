package guiAnimazione;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import jneat.NNode;
import jneat.Network;

/**
 * This class represent the car with position, velocity, accelerations...
 * Questa classe rappresenta una macchina attribuendone la posizione, velocit�, accelerazione...
 * La macchina genera i movimenti sfruttando la rete neurale che gli viene assegnato
 * @author Francesco Pham
 */
public class Car{
    private double positionX;
    private double positionY;
    private double velocity;
    private double rotationRad;//angolo della macchina rispetto asse delle y
    
    public static BufferedImage carSprite;
    public static BufferedImage carSpriteFaded;
    public static BufferedImage track;
    public static Checkpoints checkpoints;
    
    private static final boolean DRAWRAYS = true;
    private static final double CAR_RADIUS = 17;
    //private static final double maxVelocity = carRadius-5;
    private static final double MAX_VELOCITY = 20;
    public static final double MIN_VELOCITY = 2;
    public static final double MAX_ACCELERATION = 0.06;
    private static final double MAX_MOVE_ANGLE = 0.05;
    private static final double VISION_DEPTH = 200;
    public static final int NSENSORS = 7;
    public static final int NNINPUTS = NSENSORS+1;
    
    private Network brain;
    private double fitness;
    private final boolean useNeuralNetworks; //false = manual mode
    
    //numero di movimenti per valutare il tempo percorso dalla macchina
    private int nmovements;
    private double initDistNextChkpnt; //to evaluate fitness
    private int indexcheckpoint;  //n checkpoints passed since start
    private double currentAcceleration=0;
    private double currentSteering=0; //current angle of steering wheel, for drawing
    private double avgMoveAngle = 0;
    private boolean collided = false;
    //private double uncertainty = 0;
    
    /**
     * Initialization of the car with a neural network brain 
     * Costruttore dell macchina che lo inizializza con una rete neurale
     * @param brain la rete neurale da caricare
     */
    public Car(Network brain){
        this.brain = brain;
        useNeuralNetworks = true;
    }
    
    /**
     * Costruttore senza utilizzo di reti neurali (principalmente a scopo di debug)
     */
    public Car(){
        useNeuralNetworks = false;
    }
    
    /**
     * Initialization of the car with position and angle
     * Inizializza la macchina nella posizione e angolo indicato
     * @param x posizione X
     * @param y posizione Y
     * @param rotation rotazione in radiali
     */
    public void init(double x, double y, double rotation){
        positionX = x;
        positionY = y;
        velocity = MIN_VELOCITY;
        rotationRad = rotation+Math.PI/2;
        nmovements = 1;
        fitness = 0;
        indexcheckpoint = 0;
        currentAcceleration=0;
        currentSteering=0;
        avgMoveAngle = 0;
        collided=false;
        //uncertainty = 0;
        
        Point2D Pcar = new Point2D.Double(positionX,positionY);
        initDistNextChkpnt = checkpoints.getDistanceNextChkpnt(Pcar,indexcheckpoint);
    }
    
    /**
     * The movement is calculated based on a movement angle and acceleration 
     * which change the car car angle and velocity
     * Il movimento è calcolato in base alla velocità e lo sterzo della macchina,
     * questa funzione permette di variare velocità e sterzo passando i due parametri.
     * @param movementangle valore da zero a uno che indica quanto sterzare 0=sinistra, 1=destra
     * @param acceleration valore da zero a uno che indica quanto accelerare/decelerare 0=decelera, 1=accelera
     */
    public void move(double movementangle, double acceleration){
        if(collided) return; //se colliso non muovere
        
        acceleration -= 0.5;
        acceleration *= MAX_ACCELERATION/0.5;
        movementangle -= 0.5;
        movementangle *= MAX_MOVE_ANGLE/0.5;
        
        currentAcceleration = (acceleration+currentAcceleration)/2;
        //double newAvgMoveAngle = (movementangle+avgMoveAngle)/2; //calcolo quanto effettivamente sterzare
        double newAvgMoveAngle = avgMoveAngle+(movementangle-avgMoveAngle)/5;
        
        //calcolo l'angolo del volante da disegnare
        currentSteering += (newAvgMoveAngle*3/MAX_MOVE_ANGLE-currentSteering)/10;
        
        //sommo la differenza di sterzata per indicare l'indeterminatezza dell'organismo
        //uncertainty += Math.abs(newAvgMoveAngle-avgMoveAngle);
        avgMoveAngle = newAvgMoveAngle;
        
        //sommo la velocit� con l'accelerazione indicato
        velocity += currentAcceleration;
        
        //controllo i limiti imposti
        if(velocity>MAX_VELOCITY) velocity=MAX_VELOCITY;
        if(velocity<MIN_VELOCITY) velocity=MIN_VELOCITY;
        
        //if(velocity>0 && Math.abs(currentSteering)>Math.PI/6) rotationRad += currentSteering/50;
        double rot = avgMoveAngle;
        int rotThres = 3;
        if(velocity>rotThres) rotationRad += rot;
        else rotationRad += rot*(velocity/rotThres);
        
        //DEBUG
        //System.out.println("tl: "+turnLeft);
        //System.out.println("tr: "+turnRight);
        //System.out.println("acc:"+currentAcceleration);
        //System.out.println("move angle: "+movementangle);
        //System.out.println("velocity: "+velocity);
        
        //spostamento
        double xshift = Math.cos(rotationRad-Math.PI/2) * velocity;
        double yshift = Math.sin(rotationRad-Math.PI/2) * velocity;
        positionX += xshift;
        positionY += yshift;
        
        updateFitness();
        checkCollision();
    }
    
    /*
    // il movimento della macchina � la somma vettoriale tra turnRight e turnLeft che formano 45�
    // con l'angolazione della macchina
    public void move(double turnLeft, double turnRight){
    turnLeft *= 10;
    turnRight *= 10;
    
    double movementangle = Math.atan(turnRight/turnLeft)-Math.PI/4;
    if(turnLeft==0) movementangle = turnRight-turnLeft;
    double acceleration = Math.sqrt(Math.pow(turnLeft,2)+Math.pow(turnRight,2));
    acceleration /= 70;
    acceleration -= 0.1;
    
    //check boundaries
    if(movementangle<-Math.PI/8) movementangle=-Math.PI/8;
    if(movementangle>Math.PI/8) movementangle=Math.PI/8;
    if(acceleration>maxAcceleration) acceleration=maxAcceleration;
    if(acceleration<minAcceleration) acceleration=minAcceleration;
    
    //limitazione dell'angolo di sterzo in base alla velocit�
    //movementangle /= Math.abs(velocity-minVelocity+1)/2;
    
    velocity += acceleration;
    
    if(velocity>maxVelocity) velocity=maxVelocity;
    if(velocity<minVelocity) velocity=minVelocity;
    
    rotationRad += movementangle/8;
    
    //spostamento
    double xshift = Math.cos(rotationRad-Math.PI/2) * velocity;
    double yshift = Math.sin(rotationRad-Math.PI/2) * velocity;
    
    positionX += xshift;
    positionY += yshift;
    
    updateFitness();
    }
    */
    
    /**
     * Controllo se, nello spazio circostante alla macchina, esce dal circuito
     * @return True se la macchina collide con gli ostacoli, False altrimenti
     */
    private boolean checkCollision(){
        for(double angle=0; angle<=Math.PI*2; angle+=Math.PI/4){
            int x = (int)(positionX+Math.cos(angle)*CAR_RADIUS);
            int y = (int)(positionY+Math.sin(angle)*CAR_RADIUS);
            int dotcolor = track.getRGB(x,y);
            if(dotcolor != -1){ //-1 = white
                collided = true;
                return true;
            }
        }
        return false;
    }
    
    /**
     * calculate the inputs of the neural network using distances from obstacles
     * Funzione per calcolare l'input alla rete neurale calcolato in base alle distanze dagli ostacoli
     * @return elenco degli input
     */
    private double[] getNNInputs(){
        double[] results = new double[NNINPUTS];
        
        double angle=rotationRad-Math.PI;
        for(int i=0; i<NSENSORS; i++){
            results[i] = VISION_DEPTH;
            for(double distance=0; distance<=VISION_DEPTH; distance+=5){
                int x = (int)(positionX+Math.cos(angle)*(distance+CAR_RADIUS/2));
                int y = (int)(positionY+Math.sin(angle)*(distance+CAR_RADIUS/2));
                int dotcolor = track.getRGB(x,y);
                
                if(dotcolor != -1){ //-1 = white
                    //results[i] = distance-(distance*velocity/maxVelocity);
                    results[i] = distance;
                    break;
                }
            }
            
            angle+=Math.PI/(NSENSORS-1); //divide il campo di visuale nel numero di sensori
        }
        
        results[NSENSORS] = velocity; //input velocity
        //results[nsensors+1] = currentSteering+Math.PI/2;
        
        //for(int i=0; i<ninputs; i++)
        //	System.out.println(i+": "+results[i]);
        
        return results;
    }
    
    /**
     * Paint the car to screen
     * Stampa della macchina
     * @param g Parametro Graphics dove viene disegnato la macchina
     * @param xshift Quanto la visuale � spostata in X per centrare l'animazione nella finestra
     * @param yshift Quanto la visuale � spostata in Y per centrare l'animazione nella finestra
     * @param ismain true se la macchina da animare � quella vincente
     */
    public void paint(Graphics g, int xshift, int yshift, boolean ismain){
        //disegno della macchina
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = new AffineTransform();
        at.translate(positionX-xshift, positionY-yshift);
        at.rotate(rotationRad);
        //at.scale(0.2, 0.2);
        if(ismain){
            at.translate(-carSprite.getWidth()/2, -carSprite.getHeight()/2);
            g2d.drawImage(carSprite, at, null);
        }else{
            at.translate(-carSpriteFaded.getWidth()/2, -carSpriteFaded.getHeight()/2);
            g2d.drawImage(carSpriteFaded, at, null);
        }
        
        //disegno dei raggi dei sensori
        if(DRAWRAYS && ismain){
            double[] inputs = getNNInputs();
            g.setColor(Color.GRAY);
            double angle=rotationRad-Math.PI;
            int x1, y1, x2, y2;
            for(int i=0; i<NSENSORS; i++){
                x1 = (int)(positionX+Math.cos(angle)*15)-xshift;
                y1 = (int)(positionY+Math.sin(angle)*15)-yshift;
                x2 = (int)(positionX+Math.cos(angle)*inputs[i])-xshift;
                y2 = (int)(positionY+Math.sin(angle)*inputs[i])-yshift;
                g.drawLine(x1, y1, x2, y2);
                
                angle+=Math.PI/(NSENSORS-1);
            }
        }
    }
    
    /**
     * Generate the movement activating the neural network
     *  Generazione del movimento attivando la rete neurale
     */
    public void generateMovement() {
        if(collided){
            return;
        }
        if(!useNeuralNetworks){
            move(0.5, 0.5);
            return;
        }
        
        double[] inputs = getNNInputs();
        
        // Caricamento degli input nella rete neurale
        brain.load_sensors(inputs);
        
        int net_depth = brain.max_depth();
        // first activate from sensor to next layer....
        brain.activate();
        
        // next activate each layer until the last level is reached
        for (int relax = 0; relax <= net_depth; relax++)
        {
            brain.activate();
        }
        
        // Retrieve outputs from the final layer.
        double output1 = ((NNode) brain.getOutputs().elementAt(0)).getActivation();
        double output2 = ((NNode) brain.getOutputs().elementAt(1)).getActivation();
        //double output3 = ((NNode) brain.getOutputs().elementAt(2)).getActivation();
        
        //System.out.println("out1: "+output1);
        //System.out.println("out2: "+output2);
        
        move(output1,output2);
        nmovements++;
    }
    
    /**
     * Get fitness of the organism
     * Ritorno del punteggio di fitness dell'organismo attuale
     * @return punteggio di fitness
     */
    public double getFitness(){
        //System.out.println("fitness:"+fitness);
        return fitness;
    }
    
    /**
     * Calculate the fitness of the organism
     * Aggiornamento del punteggio di fitness calcolato in base al numero di checkpoint passati,
     * dalla distanza dal checkpoint successivo e dal tempo percorso dall'inizio
     */
    private void updateFitness(){
        Point2D pos = new Point2D.Double(positionX,positionY);
        double distanceFromCheckpoint = checkpoints.getDistanceNextChkpnt(pos,indexcheckpoint);
        //System.out.println("disfromcheck: "+distanceFromCheckpoint);
        
        if(distanceFromCheckpoint<CAR_RADIUS){
            indexcheckpoint++;
            //System.out.println("checkpoint: "+indexcheckpoint);
            initDistNextChkpnt = checkpoints.getDistanceNextChkpnt(pos,indexcheckpoint);
        }
        
        //System.out.println("nmove: "+nmovements);
        //fitness = (indexcheckpoint+1-distanceFromCheckpoint/initDistNextChkpnt)*100-(nmovements/100)-(uncertainty/10);
        //fitness = (indexcheckpoint+1-distanceFromCheckpoint/initDistNextChkpnt)*100-(nmovements/100);
        fitness = (indexcheckpoint+1-distanceFromCheckpoint/initDistNextChkpnt)*100;
        //System.out.println("fitness:"+fitness);
    }
    
    /**
     * Get the position of the car
     * Restituisce la posizione della macchina
     * @return un oggetto di tipo Point2D che indica la posizione della macchina
     */
    public Point2D getPosition(){
        return new Point2D.Double(positionX,positionY);
    }
    
    /**
     * get number of checkpoints reached by the car
     * Restituisce il numero di checkpoint dalla passati dalla partenza
     * @return un intero che indica i checkpoint passati
     */
    public int getIndexcheckpoint() {
        return indexcheckpoint;
    }
    
    /**
     * Restituisce il numero di volte che � stato chiamato generateMovement()
     * @return how many times the function generateMovement() is called
     */
    public int getNmovements() {
        return nmovements;
    }
    
    public double getCurrentAcceleration() {
        return currentAcceleration;
    }
    
    /**
     * l'angolo del volante in radianti
     * @return the angle of the steering wheel in radiants
     */
    public double getCurrentSteering() {
        return currentSteering;
    }
    
    public boolean isCollided() {
        return collided;
    }
    
    /**
     * Ritorna la velocit� della macchina
     * @return velocity of the car
     */
    public double getVelocity() {
        return velocity;
    }
}
