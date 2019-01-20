package guiAnimazione;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import jNeatCommon.*;
import jneat.*;

public class Parameter extends JPanel implements ActionListener, ListSelectionListener {
    private static final long serialVersionUID = 1L;
    
    private JFrame f1;
    private JPanel p2; // pannello comandi
    private JPanel p3; // pannello grafico
    
    private JButton b1;
    private JButton b2;
    private JButton b3;
    private JButton b4;
    private JButton b5;
    
    public JPanel pmain;
    
    private JTextArea textArea;
    
    private vectTableModel modello;
    private JTable jtable1;
    private JScrollPane paneScroll1;
    private JScrollPane paneScroll2;
    
    private Container contentPane;
    protected HistoryLog logger;
    
    public Parameter(JFrame _f) {
        
        logger = new HistoryLog();
        
        f1 = _f;
        
        p2 = new JPanel();
        p3 = new JPanel();
        
        b1 = new JButton("Load parameters");
        b1.addActionListener(this);
        
        b2 = new JButton("Load file");
        b2.addActionListener(this);
        
        b3 = new JButton("Write");
        b3.addActionListener(this);
        
        b4 = new JButton("Write file");
        b4.addActionListener(this);
        
        b5 = new JButton("E X I T");
        b5.addActionListener(this);
        
        Font fc = new Font("Dialog", Font.BOLD, 12);
        b1.setFont(fc);
        b2.setFont(fc);
        b3.setFont(fc);
        b4.setFont(fc);
        b5.setFont(fc);
        
        //
        // definizione layout del pannello comandi
        //
        GridBagLayout gbl_p2 = new GridBagLayout();
        GridBagConstraints gbc_p2 = new GridBagConstraints();
        p2.setLayout(gbl_p2);
        
        p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Command options"),
                BorderFactory.createEmptyBorder(10, 10, 2, 2)));
        
        p3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(" j n e a t    parameter's "),
                BorderFactory.createEmptyBorder(10, 10, 2, 2)));
        
        gbc_p2.anchor = GridBagConstraints.NORTH;
        gbc_p2.fill = GridBagConstraints.BOTH;
        gbc_p2.gridheight = 2;
        gbc_p2.gridwidth = 1;
        gbc_p2.gridx = 0;
        gbc_p2.gridy = 1;
        gbc_p2.insets = new Insets(1, 2, 1, 2);
        gbc_p2.ipadx = 0;
        gbc_p2.ipady = 0;
        gbc_p2.weightx = 0.0;
        gbc_p2.weighty = .5;
        p2.add(b1);
        gbl_p2.setConstraints(b1, gbc_p2);
        
        gbc_p2.gridy = 3;
        p2.add(b2);
        gbl_p2.setConstraints(b2, gbc_p2);
        
        gbc_p2.gridy = 5;
        p2.add(b3);
        gbl_p2.setConstraints(b3, gbc_p2);
        
        gbc_p2.gridy = 7;
        p2.add(b4);
        gbl_p2.setConstraints(b4, gbc_p2);
        
        gbc_p2.anchor = GridBagConstraints.SOUTH;
        gbc_p2.fill = GridBagConstraints.HORIZONTAL;
        
        gbc_p2.gridheight = 2;
        gbc_p2.gridy = 10;
        gbc_p2.weighty = 5;
        
        p2.add(b5);
        gbl_p2.setConstraints(b5, gbc_p2);
        
        modello = new vectTableModel(new Vector<ParamValue>());
        jtable1 = new JTable(modello);
        
        paneScroll1 = new JScrollPane(jtable1);
        
        TableColumn column = null;
        for (int i = 0; i < 2; i++) {
            column = jtable1.getColumnModel().getColumn(i);
            if (i == 0)
                column.setPreferredWidth(400);
            if (i == 1) {
                column.setPreferredWidth(100);
                
            }
        }
        
        jtable1.setCellSelectionEnabled(true);
        
        jtable1.setBackground(new Color(255, 252, 242));
        jtable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        GridBagLayout gbl_p3 = new GridBagLayout();
        GridBagConstraints limiti = new GridBagConstraints();
        p3.setLayout(gbl_p3);
        
        buildConstraints(limiti, 0, 0, 1, 4, 35, 90);
        limiti.fill = GridBagConstraints.BOTH;
        gbl_p3.setConstraints(paneScroll1, limiti);
        p3.add(paneScroll1);
        
        buildConstraints(limiti, 1, 0, 2, 4, 55, 0);
        limiti.fill = GridBagConstraints.BOTH;
        limiti.anchor = GridBagConstraints.CENTER;
        
        textArea = new JTextArea("", 10, 60);
        textArea.setFont(getFont());
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        textArea.setVisible(true);
        
        textArea.setBackground(new Color(255, 242, 232));
        
        paneScroll2 = new JScrollPane(textArea);
        paneScroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        gbl_p3.setConstraints(paneScroll2, limiti);
        p3.add(paneScroll2);
        
        pmain = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        pmain.setLayout(gbl);
        
        limiti = new GridBagConstraints();
        buildConstraints(limiti, 0, 0, 1, 5, 0, 100);
        limiti.anchor = GridBagConstraints.WEST;
        limiti.fill = GridBagConstraints.VERTICAL;
        pmain.add(p2);
        gbl.setConstraints(p2, limiti);
        
        limiti = new GridBagConstraints();
        buildConstraints(limiti, 1, 0, 2, 5, 100, 0);
        limiti.anchor = GridBagConstraints.WEST;
        limiti.fill = GridBagConstraints.BOTH;
        pmain.add(p3);
        gbl.setConstraints(p3, limiti);
        
        // interface to main method of this class
        
        contentPane = f1.getContentPane();
        BorderLayout bl = new BorderLayout();
        contentPane.setLayout(bl);
        contentPane.add(pmain, BorderLayout.CENTER);
        contentPane.add(logger, BorderLayout.SOUTH);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int irow = 0;
        Object s_descr = null;
        Object s2 = null;
        ParamValue ox = null;
        String r2 = null;
        String r3 = null;
        String tipo = null;
        
        if (e.getValueIsAdjusting()) {
            return;
        }
        
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        
        if (!lsm.isSelectionEmpty()) {
            irow = lsm.getMinSelectionIndex();
            ox = (ParamValue) modello.data.elementAt(irow);
            s_descr = Neat.getDescription((String) ox.o1);
            s2 = ox.o2;
            
            if (s2 instanceof Integer) {
                tipo = new String(" integer ");
            }
            if (s2 instanceof Double) {
                tipo = new String(" double");
            }
            
            r2 = "\n Current setting is " + s2;
            r3 = s_descr + r2 + tipo;
            textArea.setText(r3);
            
            paneScroll2.revalidate();
            paneScroll2.validate();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        String xret = null;
        String path;
        String tmp1;
        String tmp2;
        boolean rc = false;
        
        if (e.getActionCommand().equals("E X I T")) {
            System.exit(0);
        }
        
        else if (e.getActionCommand().equals("Load parameters")) {
            logger.sendToLog("loading file parameter...");
            Neat.initbase();
            
            rc = EnvRoutine.readParameters();
            
            modello.data.clear();
            modello.rows = -1;
            Neat.getParam(modello);
            modello.fireTableDataChanged();
            ListSelectionModel lsm = jtable1.getSelectionModel();
            lsm.addListSelectionListener(this);
            
            if(rc) logger.sendToLog("Parameters loaded");
            else logger.sendToLog("Failed loading parameters");
            
            logger.sendToStatus("READY");
        }
        
        else if (e.getActionCommand().equals("Load file")) {
            logger.sendToLog("loading file parameter...");
            Neat.initbase();
            
            FileDialog fd = new FileDialog(f1, "load file parameter", FileDialog.LOAD);
            fd.setVisible(true);
            
            tmp1 = fd.getDirectory();
            tmp2 = fd.getFile();
            
            if (tmp1 != null && tmp2 != null) {
                path = tmp1 + tmp2;
                rc = Neat.readParam(path,IOseq.TYPE_FILE);
                
                if (rc)
                    xret = new String("  ok");
                else
                    xret = new String("  *ERROR*");
                
                modello.data.clear();
                modello.rows = -1;
                Neat.getParam(modello);
                modello.fireTableDataChanged();
                ListSelectionModel lsm = jtable1.getSelectionModel();
                lsm.addListSelectionListener(this);
                logger.sendToLog(" read of file parameter " + path + xret);
                logger.sendToStatus("READY");
                
            }
        }
        
        else if (e.getActionCommand().equals("Write")) {
            path = EnvRoutine.getUserFileData(EnvConstant.NAME_PARAMETER);
            logger.sendToLog(" writing file parameter " + path + "...");
            Neat.updateParam(modello);
            Neat.writeParam(path);
            logger.sendToLog(" okay : file writed");
            logger.sendToStatus("READY");
            
        }
        
        else if (e.getActionCommand().equals("Write file")) {
            
            FileDialog fd = new FileDialog(f1, "load file parameter", FileDialog.SAVE);
            fd.setVisible(true);
            
            tmp1 = fd.getDirectory();
            tmp2 = fd.getFile();
            
            if (tmp1 != null && tmp2 != null) {
                
                path = tmp1 + tmp2;
                logger.sendToLog(" writing file parameter " + path + "...");
                Neat.updateParam(modello);
                Neat.writeParam(path);
                logger.sendToLog(" okay : file writed");
                logger.sendToStatus("READY");
            }
        }
    }
    
    public void setLog(HistoryLog _log) {
        logger = _log;
    }
    
    public void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
}