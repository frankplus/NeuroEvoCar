package guiAnimazione;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import jNeatCommon.EnvConstant;

/**
 * Classe contenente il main e la creazione della finestra principale
 * @author Francesco Pham
 */
public class MainClass extends JFrame{
    private static final long serialVersionUID = 1L;
    private Parameter a_parameter;
    private SimulationWindow a_simulation;
    private JTabbedPane jtabbedPane1;
    
    public static void main(String[] args) {
        MainClass finestra = new MainClass();
        finestra.setVisible(true);
    }
    
    public MainClass() {
        super("Neuroevolution - Car AI");
        this.setSize(800,600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        a_parameter = new Parameter(this);
        a_simulation = new SimulationWindow();
        
        logger = new HistoryLog();
        
        a_parameter.setLog(logger);
        a_simulation.setLog(logger);
        
        jtabbedPane1 = new JTabbedPane();
        jtabbedPane1.addTab("Simulation", a_simulation);
        jtabbedPane1.addTab("jneat parameter", a_parameter.pmain);
        jtabbedPane1.setSelectedIndex(0);
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JSplitPane paneSplit1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jtabbedPane1, logger);
        paneSplit1.setOneTouchExpandable(true);
        paneSplit1.setContinuousLayout(true);
        paneSplit1.setDividerSize(10);
        jtabbedPane1.setMinimumSize(new Dimension(400, 50));
        logger.setMinimumSize(new Dimension(100, 50));
        
        paneSplit1.setDividerLocation(410);
        
        paneSplit1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        
        contentPane.add(paneSplit1, BorderLayout.CENTER);
        
        EnvConstant.OP_SYSTEM = System.getProperty("os.name");
        EnvConstant.OS_VERSION = System.getProperty("os.version");
        //EnvConstant.OS_FILE_SEP = System.getProperty("file.separator");
        EnvConstant.USERAPP_DIR = System.getProperty("user.dir")+EnvConstant.OS_FILE_SEP+"save_files";
    }
    
    protected HistoryLog logger;
}
