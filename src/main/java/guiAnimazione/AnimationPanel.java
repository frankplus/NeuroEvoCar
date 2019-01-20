package guiAnimazione;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

import jNeatCommon.EnvConstant;
import jneat.*;

/**
 * This JPanel contains the animation of the car or cars on the selected circuit
 * Questo pannello contiene l'animazione della macchina o delle macchine sul circuito scelto
 * @author Francesco Pham
 */
public class AnimationPanel extends JPanel{
    private static final long serialVersionUID = 1L;
    
    private Car winnerCar; //se una singola macchina animata
    private Car multiplecars[]; //se pi� macchine animate
    private boolean isMultipleCars;
    private int indexWinner;
    private static final int MAXCARSANIM = 20;
    private int animTimePerGen = 20; //tempo di animazione per generazione in secondi
    
    private BufferedImage background;
    private final Font textFont;
    private Timer tim = null;
    private int timerDelay = 15;
    private Checkpoints checkpoints;
    private boolean initialized = false;
    private double carstartx;
    private double carstarty;
    private double carstartangle;
    private BufferedImage steeringwheel;
    private final MyKeyListener keylisten;
    private final SimulationWindow pannelloSimulazione;
    private int remainingTime;
    private int numberGeneration = -1;
    private int winnerOrgFitness;
    
    /**
     * JPanel constructor, here the image of the steering wheel is loaded
     * Costruttore del pannello, qui viene caricato l'immagine del volante
     * @param simulazione questo parametro è il riferimento alla finestra principale, serve successivamente
     *  a selezionare la generazione successiva dopo la fine della corrente generazione per collisione o tempo
     */
    public AnimationPanel(SimulationWindow simulazione) {
        //caricamento dell'immagine del volante
        try {
            steeringwheel = ImageIO.read(getClass().getResource(EnvConstant.RESOURCES_DIR+"/steering_wheel.png"));
        } catch (IOException e) {
            System.err.println("non � possibile caricare l'immagine del volante");
        }
        keylisten = new MyKeyListener();
        addKeyListener(keylisten);
        setFocusable(true);
        pannelloSimulazione = simulazione;
        textFont = new Font("SansSerif", Font.PLAIN, 20);
    }
    
    /**
     * Loading of the track used in the animation
     * Carica la mappa del circuito da animare
     * @param background immagine del circuito da stampare a schermo
     * @param checkpoints elenco dei checkpoints del circuito
     * @param x posizione x iniziale della macchina
     * @param y posizione x iniziale della macchina
     * @param angle angolo iniziale della macchina
     */
    public void loadMap(BufferedImage background,
            Checkpoints checkpoints,
            double x,
            double y,
            double angle) {
        this.background = background;
        this.checkpoints = checkpoints;
        this.carstartx = x;
        this.carstarty = y;
        this.carstartangle = angle;
        stop();
        initialized = false;
    }
    
    /**
     * Initialization of the animation using the brain given in parameter
     * Inizializza l'animazione di una macchina caricata di una rete neurale di un organismo passato
     * @param organism Organismo con la corrispondente la rete neurale della macchina da animare
     */
    public void init(Organism organism){
        stop();
        winnerCar = new Car(organism.getNet());
        winnerCar.init(carstartx,carstarty,carstartangle);
        isMultipleCars = false;
        winnerOrgFitness = (int) organism.getOrig_fitness();
        startTimer();
    }
    
    /**
     * Initialization of the animation without AI using keyboard
     * Inizializza l'animazione di una macchina comandata da tastiera,
     * senza alcuna intelligenza artificiale
     */
    public void init(){
        stop();
        winnerCar = new Car();
        winnerCar.init(carstartx,carstarty,carstartangle);
        isMultipleCars = false;
        startTimer();
    }
    
    /**
     * Initialization of the animation of multiple cars given in an array of organisms
     * Inizializza l'animazione delle macchine passate come vettore di organismi,
     * il numero massimo di macchine è definito da maxCarsAnim
     * @param orgs gli organismi da animare
     * @param winner l'indice del vincitore
     */
    public void init(Vector<Organism> orgs, int winner){
        stop();
        isMultipleCars = true;
        indexWinner = winner;
        
        int ncars = orgs.size()<MAXCARSANIM?orgs.size():MAXCARSANIM;
        multiplecars = new Car[ncars];
        if(winner>=ncars) {
            multiplecars[0] = new Car(orgs.elementAt(winner).getNet());
            multiplecars[0].init(carstartx,carstarty,carstartangle);
            indexWinner = 0;
        }else{
            multiplecars[0] = new Car(orgs.elementAt(0).getNet());
            multiplecars[0].init(carstartx,carstarty,carstartangle);
        }
        for(int i=1; i<ncars; i++){
            multiplecars[i] = new Car(orgs.elementAt(i).getNet());
            multiplecars[i].init(carstartx,carstarty,carstartangle);
        }
        winnerOrgFitness = (int) orgs.elementAt(winner).getOrig_fitness();
        startTimer();
    }
    
    /**
     * Function that calculate the rimaining time of the animation
     * Funzione che calcola il tempo rimanente da animare
     * @param nmove il numero di movimenti che ha fatto la macchina
     * @return il tempo rimanente
     */
    private int calculateRemainingTime(int nmove){
        //double time = (FinestraSimulazione.maxMovements-nmove);
        //time = (timerDelay*time/1000)+1;
        double time = animTimePerGen-timerDelay*nmove/1000;
        return (int) time;
    }
    
    /**
     * Start the animation of the car/cars
     * Funzione che avvia l'animazione della macchina vincitrice o di tutte le macchine o della macchina
     * controllata manualmente da tastiera
     */
    private void startTimer(){
        requestFocusInWindow();
        //se animazione di una macchina singola
        if(!isMultipleCars){
            tim = new Timer(timerDelay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(keylisten.isDownPressed()) winnerCar.move(0.5, 0);
                    else if(keylisten.isUpPressed()) winnerCar.move(0.5, 1);
                    else if(keylisten.isRightPressed()) winnerCar.move(1, 0.5);
                    else if(keylisten.isLeftPressed()) winnerCar.move(0, 0.5);
                    else winnerCar.generateMovement();
                    if(winnerCar.isCollided()){
                        pannelloSimulazione.collisionAnimation();
                    }
                    remainingTime = calculateRemainingTime(winnerCar.getNmovements());
                    repaint();
                    if(remainingTime<=0) pannelloSimulazione.nextAnimation();
                }
            });
        }
        //altrimenti se animazione di pi� macchine
        else{
            tim = new Timer(timerDelay, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int i=0; i<multiplecars.length; i++){
                        multiplecars[i].generateMovement();
                        
                        //se il vincitore ha fatto collisione (quindi anche tutte le altre macchine)
                        if(i==indexWinner && multiplecars[i].isCollided()){
                            pannelloSimulazione.collisionAnimation();
                        }
                    }
                    remainingTime = calculateRemainingTime(multiplecars[indexWinner].getNmovements());
                    repaint();
                    if(remainingTime<=0) pannelloSimulazione.nextAnimation();
                }
            });
        }
        
        tim.start();
        initialized = true;
    }
    
    /**
     * Restart the animation of the current generation
     * Riavvio dell'animazione della corrente generazione
     */
    public void restart(){
        stop();
        
        if(isMultipleCars)
            for (Car multiplecar : multiplecars) {
                multiplecar.init(carstartx, carstarty, carstartangle);
            }
        else
            winnerCar.init(carstartx,carstarty,carstartangle);
        
        startTimer();
    }
    
    /**
     * Stop animation
     * Funzione che ferma l'animazione
     */
    public void stop(){
        if(initialized) tim.stop();
    }
    
    /**
     * Paint the screen
     * Funzione che stampa la schermata della corrente animazione
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(initialized){
            if(isMultipleCars) winnerCar = multiplecars[indexWinner];
            
            //stampo lo sfondo con la macchina centrata in mezzo al pannello
            Color bgcolor = new Color(0, 0, 0);
            Point2D carPos = winnerCar.getPosition();
            int panelWidth = this.getWidth();
            int panelHeight = this.getHeight();
            if(panelHeight>background.getHeight()) panelHeight = background.getHeight();
            if(panelWidth>background.getWidth()) panelWidth = background.getWidth();
            int drawRegionX = (int)carPos.getX()-panelWidth/2;
            int drawRegionY = (int)carPos.getY()-panelHeight/2;
            int rightlimit = background.getWidth()-panelWidth;
            int bottomlimit = background.getHeight()-panelHeight;
            if(drawRegionX>rightlimit) drawRegionX=rightlimit;
            if(drawRegionY>bottomlimit) drawRegionY=bottomlimit;
            if(drawRegionX<0) drawRegionX=0;
            if(drawRegionY<0) drawRegionY=0;
            BufferedImage subBgImage = background.getSubimage(drawRegionX, drawRegionY, panelWidth, panelHeight);
            g.drawImage(subBgImage, 0, 0,bgcolor, null);
            
            //disegno checkpoints
            Line2D[] segments = checkpoints.getCheckpoints();
            int nchkpnts = segments.length;
            for(int i=0; i<nchkpnts; i++){
                Line2D s = segments[i];
                if(i==nchkpnts-1) g.setColor(Color.WHITE);
                else if(i<winnerCar.getIndexcheckpoint()%checkpoints.getNcheckpoints()){
                    g.setColor(Color.GREEN);
                }
                else g.setColor(Color.GRAY);
                
                int x1 = (int)s.getX1()-drawRegionX;
                int y1 = (int)s.getY1()-drawRegionY;
                int x2 = (int)s.getX2()-drawRegionX;
                int y2 = (int)s.getY2()-drawRegionY;
                g.drawLine(x1, y1, x2, y2);
                //g.drawString(Integer.toString(i), x1, y1);
            }
            
            //se macchine multiple allora stampo le macchine secondarie
            if(isMultipleCars){
                for(int i=0; i<multiplecars.length; i++){
                    if(i!=indexWinner)
                        multiplecars[i].paint(g,drawRegionX,drawRegionY,false);
                }
            }
            
            //stampo la macchina vincitrice
            winnerCar.paint(g,drawRegionX,drawRegionY,true);
            
            //stampo punti, fitness e numero movimenti attuale
            g.setColor(Color.BLACK);
            String points = "Points: "+Integer.toString((int)winnerCar.getFitness());
            String fitness = "  Winner fitness: "+Integer.toString(winnerOrgFitness);
            g.drawString(points+fitness, 10, panelHeight-10);
            
            //stampo il volante
            AffineTransform at = new AffineTransform();
            at.translate(30,40);
            at.rotate(winnerCar.getCurrentSteering());
            at.scale(0.2, 0.2);
            at.translate(-steeringwheel.getWidth()/2, -steeringwheel.getHeight()/2);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(steeringwheel, at, null);
            
            //stampo gas/brake
            double acc = winnerCar.getCurrentAcceleration()/Car.MAX_ACCELERATION;
            if(acc>0.02){
                g.setColor(Color.GREEN);
                g.drawString("GAS", 60, 45);
            }else if(acc<-0.02 && winnerCar.getVelocity()>Car.MIN_VELOCITY){
                g.setColor(Color.RED);
                g.drawString("BREAK", 60, 45);
            }
            
            //stampo tempo trascorso
            g.setFont(textFont);
            g.setColor(Color.BLACK);
            if(remainingTime>0)
                g.drawString("Time: "+remainingTime+" sec", panelWidth-150, 45);
            
            //stampo il numero della generazione
            if(numberGeneration!=-1)
                g.drawString("Generation: "+numberGeneration, panelWidth-150, panelHeight-20);
        }
    }
    
    /**
     * Set the number of generation to print on screen
     * Per impostare il numero della generazione corrente da mostrare a schermo
     * @param numberGeneration il numero della generazione
     */
    public void setNumberGeneration(int numberGeneration) {
        this.numberGeneration = numberGeneration;
    }
}
