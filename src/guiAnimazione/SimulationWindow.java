package guiAnimazione;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import jGraph.Edge;
import jGraph.Structure;
import jGraph.Vertex;
import jGraph.chartXY;
import jGraph.code;
import jNeatCommon.CodeConstant;
import jNeatCommon.EnvConstant;
import jNeatCommon.EnvRoutine;
import jNeatCommon.IOseq;
import jneat.*;

/**
 * In this JPanel class there are the implementation of the genetic algorithm,
 * the start of the animation and the printing of the neural networks.
 *
 * In questo pannello viene eseguito la simulazione dell'algoritmo genetico, riprodotto le animazioni
 * e vengono stampati le reti neurali risultanti dall'output dell'algoritmo genetico
 * @author Francesco Pham
 */
public class SimulationWindow extends JPanel implements ActionListener, ItemListener, ListSelectionListener{
    private static final long serialVersionUID = 1L;
    private Population neatPop; //population
    
    //parametri
    //private int popsize = 200;  //dimensione popolazione
    private final int netInputs = Car.NNINPUTS; //dimensione input rete neurale
    private final int netOutputs = 2;  //dimensione output rete neurale
    private final int maxIndexNodes = 9; //numero massimo di nodi
    private final boolean recurrent = false; //rete neurale ricorrente
    private final double probConnection = 0.3; //probabilit� di connettere due nodi
    private final int ngenerations = 75;
    public static final int MAX_MOVEMENTS = 1800;
    
    private BufferedImage background = null;
    private Checkpoints checkpoints;
    private double carstartPosX;
    private double carstartPosY;
    private double carstartAngle;
    
    //pannelli
    private final AnimationPanel animationPanel;
    private final chartXY graphPanel;
    private final JPanel centralPanel;
    private final CardLayout cl;
    
    //bottoni
    private final JButton startButton;
    private final JButton stopButton;
    //private JButton startPreviousGenome;
    private final JButton startFromLast;
    private final JButton exitButton;
    private final JButton mapSelector;
    private final JButton restartAnimButton;
    private final JButton saveGenerationsBtn;
    private final JButton loadGenerationsBtn;
    
    //radio buttons
    private final JRadioButton animateRadio;
    private final JRadioButton animateAllRadio;
    private final JRadioButton graphRadio;
    private final JRadioButton manualRadio;
    private final JPanel commandPanel;
    private HistoryLog logger;
    private final JList<String> list;
    private final DefaultListModel<String> listModel;
    
    private Simulation simulation;
    private Vector<Vector<Organism>> generations;
    
    /**
     * Costruttore della finestra simulazione, vengono aggiunti i bottoni e i pannelli della GUI
     */
    public SimulationWindow() {
        loadComponents("http");
        
        //pannello animazione circuito
        animationPanel = new AnimationPanel(this);
        animationPanel.loadMap(background,checkpoints,carstartPosX,carstartPosY,carstartAngle);
        animationPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Animation"),
                BorderFactory.createEmptyBorder(10, 2, 2, 2)));
        
        //pannello disegno grafo
        graphPanel = new chartXY();
        
        //pannello centrale intercambiabile tra animazione e grafo
        centralPanel = new JPanel(new CardLayout());
        centralPanel.add(animationPanel, "Animation");
        centralPanel.add(graphPanel, "Graph");
        cl = (CardLayout)(centralPanel.getLayout());
        cl.show(centralPanel, "Animate winner");
        
        //pannello comandi
        commandPanel = new JPanel();
        
        //creazione dei bottoni
        startButton = new JButton("Start simulation");
        startButton.addActionListener(this);
        stopButton = new JButton("Stop simulation");
        stopButton.addActionListener(this);
        //startPreviousGenome = new JButton("Start from genome");
        //startPreviousGenome.addActionListener(this);
        startFromLast = new JButton("Start from last");
        startFromLast.addActionListener(this);
        exitButton = new JButton("E X I T");
        exitButton.addActionListener(this);
        mapSelector = new JButton("Select map");
        mapSelector.addActionListener(this);
        restartAnimButton = new JButton("Restart animation");
        restartAnimButton.addActionListener(this);
        saveGenerationsBtn = new JButton("Save simulation");
        saveGenerationsBtn.addActionListener(this);
        loadGenerationsBtn = new JButton("Load simulation");
        loadGenerationsBtn.addActionListener(this);
        
        //radio buttons
        ButtonGroup ck_group = new ButtonGroup();
        animateRadio = new JRadioButton("Animate winner", false);
        animateRadio.setActionCommand("animate");
        animateRadio.addItemListener(this);
        
        animateAllRadio = new JRadioButton("Animate All", true);
        animateAllRadio.setActionCommand("animateall");
        animateAllRadio.addItemListener(this);
        
        graphRadio = new JRadioButton("Graph winner", false);
        graphRadio.setActionCommand("graph");
        graphRadio.addItemListener(this);
        
        manualRadio = new JRadioButton("Manual mode", false);
        manualRadio.setActionCommand("manual");
        manualRadio.addItemListener(this);
        
        ck_group.add(animateRadio);
        ck_group.add(animateAllRadio);
        ck_group.add(graphRadio);
        ck_group.add(manualRadio);
        
        commandPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Command options"),
                BorderFactory.createEmptyBorder(10, 2, 2, 2)));
        
        //aggiunta dei bottoni al pannello comandi
        commandPanel.setLayout(new GridLayout(12, 1));
        commandPanel.add(startButton);
        //commandPanel.add(startPreviousGenome);
        commandPanel.add(startFromLast);
        commandPanel.add(mapSelector);
        commandPanel.add(restartAnimButton);
        commandPanel.add(animateRadio);
        commandPanel.add(animateAllRadio);
        commandPanel.add(graphRadio);
        commandPanel.add(manualRadio);
        commandPanel.add(loadGenerationsBtn);
        commandPanel.add(saveGenerationsBtn);
        commandPanel.add(stopButton);
        commandPanel.add(exitButton);
        
        //Pannello di selezione della generazione
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridLayout(1, 1,5,20));
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(100, 100));
        selectionPanel.add(scrollPane);
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Generation"),
                BorderFactory.createEmptyBorder(10, 2, 2, 2)));
        list.addListSelectionListener(this);
        
        //pannello principale
        JPanel pmain = new JPanel();
        pmain.setLayout(new BorderLayout());
        pmain.add(commandPanel, BorderLayout.WEST);
        pmain.add(centralPanel, BorderLayout.CENTER);
        pmain.add(selectionPanel, BorderLayout.EAST);
        
        //JFrame principale
        //Container contentPane = this.getContentPane();
        BorderLayout bl = new BorderLayout();
        this.setLayout(bl);
        this.add(pmain, BorderLayout.CENTER);
        //this.add(logger, BorderLayout.SOUTH);
    }
    
    /**
     * Loading of the images and initialization of the car position and tracks
     * Caricamento delle immagini e inizializzazione della macchina e del circuito
     * @param mapname nome del file con estensione .conf contenente le informazioni della mappa da caricare
     */
    private void loadComponents(String mapname){
        //caricamento delle informazioni sulla mappa, memorizzate nel file .conf in formato JSON
        JSONParser parser = new JSONParser();
        String tracksFolder = "/tracks/";
        String mapfile = mapname+".map";
        
        JSONArray chkpnts;
        String trackbgFile;
        String trackmapFile;
        
        BufferedImage track = null;
        BufferedImage carSprite = null;
        BufferedImage carSpriteFaded = null;
        
        try {
            URL resource = getClass().getResource(tracksFolder+mapfile);
            String out;
            try (Scanner scanner = new Scanner(resource.openStream(), "UTF-8")) {
                out = scanner.useDelimiter("\\A").next();
            }
            
            //Object obj = parser.parse(new FileReader(tracksFolder+mapfile));
            
            Object obj =  parser.parse(out);
            //Object obj = parser.parse(new FileReader(tracksFolder+mapfile));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject startInfo = (JSONObject) jsonObject.get("normal");
            carstartPosX = ((Number)startInfo.get("startX")).doubleValue();
            carstartPosY = ((Number)startInfo.get("startY")).doubleValue();
            carstartAngle = ((Number)startInfo.get("startAngle")).doubleValue();
            chkpnts = (JSONArray) jsonObject.get("checkpoints");
            trackbgFile = (String) jsonObject.get("trackbg");
            trackmapFile = (String) jsonObject.get("trackmap");
        } catch (NullPointerException e) {
            logger.sendToLog("Mappa non esistente");
            return;
        } catch (Exception e) {
            logger.sendToLog("Errore caricamento mappa");
            return;
        }
        
        checkpoints = new Checkpoints(chkpnts);
        
        //caricamento del circuito
        try {
            background = ImageIO.read(getClass().getResource(tracksFolder+trackbgFile));
        } catch (IOException e) {
            System.err.println("non � possibile caricare il circuito");
            System.exit(-1);
        }
        
        //caricamento della mappa del circuito
        try {
            track = ImageIO.read(getClass().getResource(tracksFolder+trackmapFile));
        } catch (IOException e) {
            System.err.println("non � possibile caricare la mappa circuito");
            System.exit(-1);
        }
        
        //caricamento dell'immagine della macchina
        try {
            carSprite = ImageIO.read(getClass().getResource(EnvConstant.RESOURCES_DIR+"/redcar.png"));
            carSpriteFaded = ImageIO.read(getClass().getResource(EnvConstant.RESOURCES_DIR+"/redcarfaded.png"));
        } catch (IOException e) {
            System.err.println("non � possibile caricare l'immagine della macchina");
            System.exit(-1);
        }
        
        Car.carSprite = carSprite;
        Car.carSpriteFaded = carSpriteFaded;
        Car.checkpoints = checkpoints;
        Car.track = track;
    }
    
    public void setLog(HistoryLog _log) {
        logger = _log;
    }
    
    /**
     * disegno del grafo della rete neurale attuale
     * @param _o1 l'organismo contenente la rete neurale da stampare su schermo
     * @param _mappa il pannello dove deve essere stampata la rete neurale
     */
    public void drawGraph(Organism _o1, chartXY _mappa) {
        String mask6d = "0.00000";
        DecimalFormat fmt6d = new DecimalFormat(mask6d);
        
        Genome _g1 = _o1.genome;
        
        Vector<code> v1 = new Vector<>(1, 0);
        Structure sx = new Structure();
        
        _mappa.initAzioni();
        
        sx.LoadGenome(_g1);
        sx.generate_Grafo();
        
        sx.compute_Coordinate(centralPanel.getWidth()+50, centralPanel.getHeight()+50);
        
        String riga_r1 = "Fitness: " + fmt6d.format(_o1.getOrig_fitness());
        
        v1.add(new code(10, centralPanel.getHeight() + 10, riga_r1, 0, CodeConstant.DESCRIPTOR));
        
        Iterator<Vertex> itr_point = sx.vVertex.iterator();
        while (itr_point.hasNext()) {
            Vertex _point = ((Vertex) itr_point.next());
            
            if ((_point.x) != 0 && (_point.y != 0) && (_point.is_real()))
                v1.add(new code(_point, CodeConstant.NODO_N));
            if ((_point.x) != 0 && (_point.y != 0) && (_point.is_recurrent()))
                v1.add(new code(_point, CodeConstant.NODO_R));
            
        }
        
        // store edge for interpreter
        Iterator<Edge> itr_edge = sx.vEdge.iterator();
        while (itr_edge.hasNext()) {
            Edge _edge = ((Edge) itr_edge.next());
            Vertex _inode = _edge.in_node;
            Vertex _onode = _edge.out_node;
            int type = _edge.type;
            
            double weight_edge = _edge.weight;
            int sign_edge;
            if (weight_edge >= 0)
                sign_edge = +1;
            else
                sign_edge = -1;
            
            if ((_inode.x) != 0 && (_inode.y != 0) && (_onode.x) != 0 && (_onode.y != 0) && (_edge.active)) {
                v1.add(new code(_inode, _onode, type, sign_edge));
            }
        }
        
        _mappa.setScale(centralPanel.getWidth(), centralPanel.getHeight());
        _mappa.setAxis(false);
        _mappa.setGrid(false);
        _mappa.setGrafo(v1);
        _mappa.repaint();
        
    }
    
    
    /**
     * Questa funzione viene richiamata quando vengono cliccati i bottoni,
     * vengono eseguiti quindi le azioni dei bottoni cliccati
     * @param e The event to manage
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Start simulation":
                EnvConstant.TYPE_OF_START = EnvConstant.START_FROM_NEW_RANDOM_POPULATION;
                simulation = new Simulation();
                simulation.start();
                break;
            case "Stop simulation":
                EnvConstant.STOP_EPOCH = true;
                logger.sendToLog("Generation: request of *interrupt* ...");
                break;
            case "Start from genome":
                EnvConstant.TYPE_OF_START = EnvConstant.START_FROM_GENOME;
                simulation = new Simulation();
                simulation.start();
                break;
            case "Start from last":
                EnvConstant.TYPE_OF_START = EnvConstant.START_FROM_OLD_POPULATION;
                simulation = new Simulation();
                simulation.start();
                break;
            case "E X I T":
                System.exit(0);
            case "Select map":
                /*JFileChooser fc = new JFileChooser();
                File workingDirectory = new File(System.getProperty("user.dir")+"/tracks");
                FileFilter filter = new FileNameExtensionFilter("Map file", "map");
                fc.setFileFilter(filter);
                fc.setCurrentDirectory(workingDirectory);
                int returnVal = fc.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                loadComponents(file.getName());
                animationPanel.loadMap(background,checkpoints,carstartPosX,carstartPosY,carstartAngle);
                list.clearSelection();
                }*/
                String[] mapnames = {"http","desert","bio","curvy","formula"};
                String mapName = ListDialog.showDialog(
                        this,
                        null,
                        "Select map:",
                        "Map Chooser",
                        mapnames,
                        "http",
                        null);
                if(mapName!=null){
                    loadComponents(mapName);
                    animationPanel.loadMap(background,checkpoints,carstartPosX,carstartPosY,carstartAngle);
                    list.clearSelection();
                }   break;
            case "Restart animation":
                animationPanel.restart();
                break;
            case "Save simulation":
                //serialize generations and save to file
                try{
                    String filePath = EnvRoutine.getUserFileData("generations.ser");
                    FileOutputStream fileOut = new FileOutputStream(filePath);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    
                    //writing to file
                    out.writeInt(generations.size());
                    Iterator<Vector<Organism>> vectiterator = generations.iterator();
                    while(vectiterator.hasNext()){
                        Vector<Organism> organisms = vectiterator.next();
                        out.writeInt(organisms.size());
                        Iterator<Organism> orgiterator = organisms.iterator();
                        while(orgiterator.hasNext()){
                            out.writeObject(orgiterator.next());
                        }
                        //System.out.println("saved one generation");
                    }
                    out.close();
                    fileOut.close();
                }catch(Exception i){
                }   break;
            case "Load simulation":
                //deserialize generations from file
                generations = null;
                try {
                    String filePath = EnvRoutine.getUserFileData("generations.ser");
                    FileInputStream fileIn = new FileInputStream(filePath);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    
                    //reading file
                    int size = in.readInt();
                    generations = new Vector<>(size);
                    for(int i=0; i<size; i++){
                        int size1 = in.readInt();
                        Vector<Organism> temp = new Vector<>(size1);
                        for(int j=0; j<size1; j++){
                            temp.addElement((Organism) in.readObject());
                        }
                        generations.insertElementAt(temp, i);
                    }
                    
                    in.close();
                    fileIn.close();
                    listModel.clear();
                    for(int i=1; i<=generations.size(); i++){
                        listModel.addElement("gen "+i);
                    }
                }catch(IOException i) {
                }catch(ClassNotFoundException c) {
                    System.out.println("class not found");
                }   break;
            default:
                break;
        }
    }
    
    /**
     * Questa funzione viene richiamata quando viene cambiato la selezione delle radio buttons
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        //prendo l'organismo selezionato se � selezionato
        int index = list.getSelectedIndex();
        int currentwinner = -1;
        Vector<Organism> currentGeneration = null;
        
        //trovo il vincitore della generazione corrente
        if(index != -1) {
            currentGeneration = generations.elementAt(index);
            currentwinner = findWinner(currentGeneration);
        }
        
        JRadioButton cb = (JRadioButton) e.getItem();
        String ckx = cb.getActionCommand();
        if (ckx.equalsIgnoreCase("animate")) {
            if(index != -1) animationPanel.init(currentGeneration.elementAt(currentwinner));
            cl.show(centralPanel, "Animation");
            animationPanel.repaint();
        }
        
        else if (ckx.equalsIgnoreCase("animateall")) {
            if(index != -1) animationPanel.init(currentGeneration,currentwinner);
            cl.show(centralPanel, "Animation");
            animationPanel.repaint();
        }
        
        else if (ckx.equalsIgnoreCase("graph")) {
            animationPanel.stop();
            if(index != -1) drawGraph(currentGeneration.elementAt(currentwinner),graphPanel);
            cl.show(centralPanel, "Graph");
            graphPanel.repaint();
        }
        
        else if (ckx.equalsIgnoreCase("manual")) {
            animationPanel.init();
            cl.show(centralPanel, "Animation");
            animationPanel.repaint();
        }
    }
    
    /**
     * richiamato quando viene cambiata la selezione della generazione
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            int index = list.getSelectedIndex();
            if(index != -1){
                Vector<Organism> currentGeneration = generations.elementAt(index);
                //trovo il vincitore della generazione selezionata
                int currentwinner = findWinner(currentGeneration);
                if(graphRadio.isSelected())
                    drawGraph(currentGeneration.elementAt(currentwinner),graphPanel);
                else if(animateRadio.isSelected())
                    animationPanel.init(currentGeneration.elementAt(currentwinner));
                else if(animateAllRadio.isSelected())
                    animationPanel.init(currentGeneration,currentwinner);
                animationPanel.setNumberGeneration(index+1);
            }
        }
    }
    
    /**
     * Funzione per trovare l'organismo vincitore cio� l'organismo con fitness pi� alto
     * @param v il vettore di organismi dove bisogna ricercare il vincitore
     * @return l'organismo vincitore
     */
    private int findWinner(Vector<Organism> v){
        /*double highestfitness = -1;
        int winner = -1;
        for(int i=0; i<v.size(); i++){
        Organism org = v.elementAt(i);
        if(org.getFitness()>highestfitness){
        highestfitness = org.getFitness();
        winner = i;
        }
        }
        System.out.println(highestfitness);
        return winner;*/
        
        for(int i=0; i<v.size(); i++){
            Organism org = v.elementAt(i);
            if(org.getWinner()) return i;
        }
        return -1;
    }
    
    /**
     * Funzione che sceglie la prossima animazione da eseguire
     */
    public void nextAnimation(){
        int currentindex = list.getSelectedIndex();
        if(currentindex==-1) return;
        int nextindex = currentindex+(currentindex/4);
        int ntotalindex = listModel.size();
        
        if(currentindex==nextindex)
            nextindex++;
        if(nextindex<ntotalindex)
            list.setSelectedIndex(nextindex);
        else if(currentindex<ntotalindex-1)
            list.setSelectedIndex(ntotalindex-1);
    }
    
    /**
     * Funzione chiamata nel caso di collisione della macchina, esegue le stesse
     * istruzioni di nextAnimation() ma riesegue l'animazione corrente se la generazione � l'ultima
     */
    public void collisionAnimation(){
        nextAnimation();
        if(list.getSelectedIndex() == ngenerations-1){
            animationPanel.restart();
        }
    }
    
    /**
     * Thread dove viene eseguito la simulazione dell'algoritmo genetico:
     * - vengono caricati i parametri
     * - viene generato la popolazione
     * - viene testato ogni genoma sul circuito per ogni generazione per trovare i genotipi migliori
     * - infine viene salvato la popolazione finale su un file
     * @author Francesco Pham
     */
    class Simulation extends Thread{
        @SuppressWarnings("unchecked")
        public void run(){
            //disable start buttons
            startButton.setEnabled(false);
            //startPreviousGenome.setEnabled(false);
            startFromLast.setEnabled(false);
            mapSelector.setEnabled(false);
            saveGenerationsBtn.setEnabled(false);
            loadGenerationsBtn.setEnabled(false);
            
            generations = new Vector<Vector<Organism>>(ngenerations);
            listModel.clear(); //clear list of generations on left bar
            
            logger.sendToStatus("Started simulation");
            
            //inizializzazione
            Neat.initbase();
            
            boolean res = EnvRoutine.readParameters();
            if (!res) {
                logger.sendToLog("generation: error reading parameters");
                return;
            }
            
            logger.sendToLog("Loaded parameters");
            
            //se avvio simulazione da nuova popolazione casuale
            if (EnvConstant.TYPE_OF_START == EnvConstant.START_FROM_NEW_RANDOM_POPULATION){
                logger.sendToLog("Generating new random population");
                neatPop = new Population(Neat.p_pop_size,netInputs,netOutputs,maxIndexNodes,recurrent,probConnection);
            }
            
            //se avvio da genoma precedentemente salvato su file
            else if (EnvConstant.TYPE_OF_START == EnvConstant.START_FROM_GENOME) {
                logger.sendToLog("Generating population from previous genome");
                
                //lettura genoma da file
                Genome u_genome = null;
                StringTokenizer st;
                String curword;
                String xline;
                IOseq xFile;
                
                xFile = new IOseq(EnvRoutine.getUserFileData(EnvConstant.NAME_GENOMEA));
                res = xFile.IOseqOpenRfromFile();
                if (!res) {
                    logger.sendToLog("generation: error open "
                            + EnvRoutine.getUserFileData(EnvConstant.NAME_GENOMEA));
                    return;
                }
                
                logger.sendToLog("generation: open file genome "
                        + EnvRoutine.getUserFileData(EnvConstant.NAME_GENOMEA) + "...");
                xline = xFile.IOseqRead();
                st = new StringTokenizer(xline);
                // skip
                curword = st.nextToken();
                // id of genome can be readed
                curword = st.nextToken();
                int id = Integer.parseInt(curword);
                u_genome = new Genome(id, xFile);
                
                neatPop = new Population(u_genome, Neat.p_pop_size);
            }
            
            //se avvio da popolazione precedente
            if (EnvConstant.TYPE_OF_START == EnvConstant.START_FROM_OLD_POPULATION) {
                logger.sendToLog("Starting from old population: "+EnvConstant.PREFIX_LAST_POPULATION);
                neatPop = new Population(EnvRoutine.getUserFileData(EnvConstant.PREFIX_LAST_POPULATION));
            }
            
            logger.sendToLog("Population generated");
            
            EnvConstant.STOP_EPOCH = false;
            
            double highestFitness;
            double avgfitness;
            int indexwinner;
            
            //processo iterativo dell'evoluzione delle generazioni
            for(int generation=1; generation<=ngenerations; generation++){
                if(generation>1) {
                    // Evoluzione della generazione
                    neatPop.epoch(generation-1);
                    //System.out.println(EnvConstant.REPORT_SPECIES_TESTA);
                    //System.out.println(EnvConstant.REPORT_SPECIES_CORPO);
                    //System.out.println(EnvConstant.REPORT_SPECIES_CODA);
                }
                highestFitness = -1;
                avgfitness = 0;
                indexwinner = -1;
                
                Vector<Organism> neatOrgs = (Vector<Organism>) neatPop.getOrganisms();
                
                // per ogni generazione eseguo il circuito ad ogni organismo
                for(int i=0;i<neatOrgs.size();i++){
                    Organism org = (Organism)neatOrgs.elementAt(i);
                    Car car = new Car(org.getNet());
                    car.init(carstartPosX,carstartPosY,carstartAngle);
                    
                    //finch� la macchina non fa collisione e fino al termine del tempo
                    //assegnato la macchina si muove autonomamente
                    while(!car.isCollided()){
                        car.generateMovement();
                        /*if(car.getIndexcheckpoint()>checkpoints.getNcheckpoints()){
                        //System.out.println("1 lap ended");
                        break;
                        }*/
                        if(car.getNmovements()>MAX_MOVEMENTS){
                            //System.out.println("too many movements");
                            break;
                        }
                    }
                    
                    //ricavo il punteggio di fitness della macchina
                    double fitness = car.getFitness();
                    avgfitness += fitness;
                    if(fitness>highestFitness){
                        highestFitness = fitness;
                        indexwinner = i;
                    }
                    org.setFitness(fitness); //assegno il punteggio di fitness all'organismo
                    //System.out.println("fitness:"+car.getFitness());
                    //System.out.println("organism index: "+i);
                }
                avgfitness /= neatOrgs.size();
                Organism winner = (Organism)neatOrgs.elementAt(indexwinner); //get winner
                winner.setWinner(true); //set winner
                generations.add((Vector<Organism>) neatOrgs.clone()); //clone organisms and store them
                logger.sendToLog("generation "+generation+" highest fitness: "+highestFitness+" avgfitness: "+avgfitness);
                
                //aggiorno pannello selezione generazione per animazione
                listModel.addElement("gen "+generation);
                if (EnvConstant.STOP_EPOCH){
                    EnvConstant.STOP_EPOCH = false;
                    break;
                }
                
                //avvio animazione della prima generazione
                if(generation==1) list.setSelectedIndex(0);
            }
            
            //salvo la popolazione in un file
            String pathLastPop = EnvRoutine.getUserFileData(EnvConstant.PREFIX_LAST_POPULATION);
            neatPop.print_to_file_by_species(pathLastPop);
            
            logger.sendToLog("Simulation finished");
            
            //enable start buttons
            startButton.setEnabled(true);
            //startPreviousGenome.setEnabled(true);
            startFromLast.setEnabled(true);
            mapSelector.setEnabled(true);
            saveGenerationsBtn.setEnabled(true);
            loadGenerationsBtn.setEnabled(true);
            
            logger.sendToStatus("READY");
        }
    }
}
