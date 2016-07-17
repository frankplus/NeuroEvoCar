package jneat;

import java.util.*;
import java.text.*;
import jNeatCommon.*;

public class Evolution extends Neat {
    
    /**
     * this is a standard experiment for XOR emulation; is passed a name of a
     * started genome and a number of times can be execute this experiment;
     */
    
    public static void Experiment1(String xFileName, int gens) {
        
        String fname_prefix = "c:\\jneat\\dati\\population.natural";
        Population pop = null;
        StringTokenizer st;
        String curword;
        String xline;
        String fnamebuf;
        int gen;
        IOseq xFile;
        int id;
        int expcount = 0;
        String mask6 = "000000";
        DecimalFormat fmt6 = new DecimalFormat(mask6);
        
        System.out.println("------ Start experiment 1 -------");
        
        xFile = new IOseq(xFileName);
        boolean ret = xFile.IOseqOpenRfromFile();
        if (ret)
            
        {
            
            try {
                
                System.out.println(" Start XOR experiment");
                System.out.println("  .read start genome..");
                
                xline = xFile.IOseqRead();
                
                st = new StringTokenizer(xline);
                // skip
                curword = st.nextToken();
                // id of genome can be readed
                curword = st.nextToken();
                id = Integer.parseInt(curword);
                
                System.out.println("  .create genome id " + id);
                
                Genome start_genome = new Genome(id, xFile);
                // backup this 'initial' genome (is only for test
                // if the read & write are correct
                start_genome.print_to_filename("c:\\jneat\\dati\\genome.readed");
                
                for (expcount = 0; expcount < Neat.p_num_runs; expcount++) {
                    System.out.println(" Spawned population off genome");
                    pop = new Population(start_genome, Neat.p_pop_size);
                    System.out.print("\n\n Verifying Spawned Pop");
                    pop.verify();
                    
                    System.out.print("\n");
                    
                    for (gen = 1; gen <= gens; gen++) {
                        System.out.print("\n---------------- E P O C H  < " + gen + " >--------------");
                        
                        fnamebuf = "g_" + fmt6.format(gen);
                        xor_epoch(pop, gen, fnamebuf);
                        
                    }
                    
                    System.out.print("\n  Population : innov num   = " + pop.getCur_innov_num());
                    System.out.print("\n             : cur_node_id = " + pop.getCur_node_id());
                    
                    pop.print_to_filename(fname_prefix);
                    
                }
            } catch (Throwable e) {
                System.err.println(e + " : error during read " + xFileName);
            }
            
            xFile.IOseqCloseR();
            
        }
        
        else
            System.err.print("\n : error during open " + xFileName);
        
        System.out.println("\n\n End of experiment");
    }
    
    public static boolean xor_epoch(Population pop, int generation, String filename) {
        
        boolean esito = false;
        // Evaluate each organism if exist the winner.........
        boolean win = false;
        
        Iterator<Organism> itr_organism;
        itr_organism = pop.organisms.iterator();
        while (itr_organism.hasNext()) {
            // point to organism
            Organism _organism = ((Organism) itr_organism.next());
            // evaluate
            esito = xor_evaluate(_organism);
            // if is a winner , store a flag
            if (esito)
                win = true;
        }
        
        // compute average and max fitness for each species
        Iterator<Species> itr_specie;
        itr_specie = pop.species.iterator();
        while (itr_specie.hasNext()) {
            Species _specie = ((Species) itr_specie.next());
            _specie.compute_average_fitness();
            _specie.compute_max_fitness();
        }
        // Only print to file every print_every generations
        
        if (win || (generation % Neat.p_print_every) == 0)
            pop.print_to_file_by_species("c:\\jneat\\dati\\" + filename);
        
        // if exist a winner write to file
        if (win) {
            int cnt = 0;
            itr_organism = pop.getOrganisms().iterator();
            while (itr_organism.hasNext()) {
                Organism _organism = ((Organism) itr_organism.next());
                if (_organism.winner) {
                    System.out.print("\n   -WINNER IS #" + _organism.genome.genome_id);
                    _organism.getGenome().print_to_filename("c:\\jneat\\dati\\xor_win" + cnt);
                    cnt++;
                }
            }
        }
        // wait an epoch and make a reproductionof the best species
        pop.epoch(generation);
        if (win) {
            System.out.print("\t\t** I HAVE FOUND A CHAMPION **");
            return true;
        } else
            return false;
    }
    
    /**
     * Insert the method's description here. Creation date: (16/01/2002 9.53.37)
     */
    public static boolean xor_evaluate(Organism organism) {
        
        Network _net = null;
        boolean success = false;
        double errorsum = 0.0;
        double[] out = new double[4]; // The four outputs
        
        // int numnodes = 0;
        int net_depth = 0; // The max depth of the network to be activated
        int count = 0;
        
        // The four possible input combinations to xor
        // The first number is for biasing
        
        double in[][] = { { 1.0, 0.0, 0.0 }, { 1.0, 0.0, 1.0 }, { 1.0, 1.0, 0.0 }, { 1.0, 1.0, 1.0 } };
        
        _net = organism.net;
        // numnodes = organism.genome.nodes.size();
        
        net_depth = _net.max_depth();
        
        // for each example , 'count', propagate signal .... and compute results
        for (count = 0; count <= 3; count++) {
            
            // first activation from sensor to first next levelof neurons
            _net.load_sensors(in[count]);
            success = _net.activate();
            
            // next activation while last level is reached !
            // use depth to ensure relaxation
            
            for (int relax = 0; relax <= net_depth; relax++)
                success = _net.activate();
            
            // ok : the propagation is completed : repeat until all examples are
            // presented
            out[count] = ((NNode) _net.getOutputs().firstElement()).getActivation();
            _net.flush();
        }
        
        // control the result
        if (success) {
            errorsum = (double) (Math.abs(out[0]) + Math.abs(1.0 - out[1]) + Math.abs(1.0 - out[2]) + Math.abs(out[3]));
            organism.setFitness(Math.pow((4.0 - errorsum), 2));
            organism.setError(errorsum);
        } else {
            errorsum = 999.0;
            organism.setFitness(0.001);
            organism.setError(errorsum);
        }
        String mask03 = "0.000";
        new DecimalFormat(mask03);
        
        if ((out[0] < 0.5) && (out[1] >= 0.5) && (out[2] >= 0.5) && (out[3] < 0.5)) {
            organism.setWinner(true);
            return true;
        } else {
            organism.setWinner(false);
            return false;
        }
        
    }
    
    /**
     * This is a test for compute depth of genome and trace all debug
     * information for viewing all signal flowing is not necessary for network
     * simulation
     */
    public static void Experiment2(String xFileName) {
        StringTokenizer st;
        String curword;
        String xline;
        IOseq xFile;
        int id;
        Genome g1 = null;
        Network net = null;
        
        System.out.println("------ Start experiment 2 -------");
        
        xFile = new IOseq(xFileName);
        
        boolean ret = xFile.IOseqOpenRfromFile();
        
        if (ret) {
            
            try {
                
                System.out.println(" Start experiment 2");
                System.out.println(" Read start genome..");
                
                xline = xFile.IOseqRead();
                
                st = new StringTokenizer(xline);
                // skip
                curword = st.nextToken();
                // id of genome can be readed
                curword = st.nextToken();
                id = Integer.parseInt(curword);
                g1 = new Genome(id, xFile);
                
                // generate a link mutation
                g1.mutate_link_weight(Neat.p_weight_mut_power, 1.0, NeatConstant.GAUSSIAN);
                // generate from genome the phenotype
                g1.genesis(id);
                // view genotype
                g1.op_view();
                
                // assign reference to genotype
                net = g1.phenotype;
                
                // compute first the 'teorical' depth
                int lx = net.max_depth();
                
                // compute . after, the 'pratical' depth passing
                // the virtual depth;
                int dx = net.is_stabilized(lx);
                // after reset all value of net
                net.flush();
                
                System.out.print("\n For genome : " + xFileName + " : max depth virtuale=" + lx);
                System.out.print(", max depth reale=" + dx);
                
                if (dx != lx)
                    System.out.print("\n  *ALERT*  This net is   NOT   S T A B L E ");
                
                net.flush();
                
                double in[] = { 1.0, 1.0, 1.0 };
                
                // first activation from sensor to first next level of neurons
                net.load_sensors(in);
                
                net.activate();
                
                // next activation while last level is reached !
                // use depth to ensure relaxation
                
                for (int relax = 1; relax <= dx; relax++) {
                    net.activate();
                }
                
                // ok : the propagation is completed
            } catch (Throwable e) {
                System.err.println(e + " : error during open " + xFileName);
            }
            
            xFile.IOseqCloseR();
            
        } else
            System.err.print("\n : error during open " + xFileName);
        
        System.out.println("\n\n End of experiment");
        
    }
    
    /**
     * This is a sample of creating a new Population with 'size_population'
     * organisms , and simulation of XOR example This sample can be started in
     * two modality : -cold : each time the population is re-created from 0;
     * -warm : each time the population re-read last population created and
     * restart from last epoch. (the population backup file is :
     * 'c:\\jneat\\dati\\population.primitive'
     */
    public static void Experiment3(int size_population, int mode, int gens) {
        Population pop = null;
        String fname_prefix = "c:\\jneat\\dati\\population.primitive";
        String fnamebuf;
        int gen;
        int expcount = 0;
        String mask6 = "000000";
        DecimalFormat fmt6 = new DecimalFormat(mask6);
        
        System.out.println("------ Start experiment 3 -------");
        
        for (expcount = 0; expcount < Neat.p_num_runs; expcount++) {
            System.out.println(" Spawned population off genome");
            
            double prb_link = 0.50;
            boolean recurrent = true;
            
            // default cold is : 3 sensor (1 for bias) , 1 out , 5 nodes max, no
            // recurrent
            if (mode == NeatConstant.COLD)
                pop = new Population(size_population, 3, 1, 5, recurrent, prb_link); // cold
            // start-up
            // pop = new Population(size_population, 3, 1, 5, recurrent,
            // prb_link); // cold start-up
            else
                pop = new Population(fname_prefix + ".last"); // warm start-up
            
            pop.verify();
            System.out.print("\n---------------- Generation starting with----------");
            System.out.print("\n  Population : innov num   = " + pop.getCur_innov_num());
            System.out.print("\n             : cur_node_id = " + pop.getCur_node_id());
            System.out.print("\n---------------------------------------------------");
            
            System.out.print("\n");
            for (gen = 1; gen <= gens; gen++) {
                System.out.print("\n---------------- Generation ----------------------" + gen);
                fnamebuf = "g_" + fmt6.format(gen);
                boolean esito = xor_epoch(pop, gen, fnamebuf);
                System.out.print("\n  Population : innov num   = " + pop.getCur_innov_num());
                System.out.print("\n             : cur_node_id = " + pop.getCur_node_id());
                System.out.print("\n   result    : " + esito);
            }
        }
        
        // backup of population for warm startup
        pop.print_to_filename(fname_prefix + ".last");
        
        System.out.println("\n\n End of experiment");
        
    }
    
    /**
     * Insert the method's description here. Creation date: (16/01/2002 9.46.13)
     */
    /**
     * This is a test for viewing the result of mate or other operation can be
     * executed from two genome the first genome is xFileNameA and second genome
     * is xFileNameB
     *
     */
    public static void Experiment4(String xFileNameA, String xFileNameB) {
        StringTokenizer st;
        String curword;
        String xline;
        IOseq xFile;
        int id;
        Genome gA = null;
        Genome gB = null;
        
        System.out.println("------ Start experiment 4 -------");
        // read genome A
        //
        xFile = new IOseq(xFileNameA);
        boolean ret = xFile.IOseqOpenRfromFile();
        
        if (ret) {
            
            try {
                System.out.println(" Read genome-A");
                xline = xFile.IOseqRead();
                st = new StringTokenizer(xline);
                // skip
                curword = st.nextToken();
                // id of genome can be readed
                curword = st.nextToken();
                id = Integer.parseInt(curword);
                gA = new Genome(id, xFile);
                // view genotype A
            } catch (Throwable e) {
                System.err.println(e + " : error during read " + xFileNameA);
            }
            
            xFile.IOseqCloseR();
        }
        
        else
            System.err.print("\n : error during openA " + xFileNameA);
        
        //
        // read genome B
        //
        xFile = new IOseq(xFileNameB);
        ret = xFile.IOseqOpenRfromFile();
        
        if (ret) {
            
            try {
                System.out.println("\n Read genome-B");
                xline = xFile.IOseqRead();
                st = new StringTokenizer(xline);
                // skip
                curword = st.nextToken();
                // id of genome can be readed
                curword = st.nextToken();
                id = Integer.parseInt(curword);
                gB = new Genome(id, xFile);
                // view genotype A
            } catch (Throwable e) {
                System.err.println(e + " : error during open " + xFileNameB);
            }
            
            xFile.IOseqCloseR();
        }
        
        else
            System.err.print("\n : error during openB " + xFileNameB);
        
        // Genome gC = gA.mate_multipoint(gB,3,0.6,0.3);
        // Genome gC = gA.mate_multipoint_avg(gB,3,0.6,0.3);
        
        System.out.println("\n ----genome-A----------");
        gA.op_view();
        System.out.println("\n ----genome-B----------");
        gB.op_view();
        
        // gA.DEBUGmate_singlepoint(gB,3);
        
        System.out.println("\n ----genome-RESULT----------");
        Genome gC = gA.mate_singlepoint(gB, 999);
        
        // this step is for verify if correct genome
        gC.verify();
        // this step is for verify the phenotype created
        gC.genesis(999);
        // the step print the result genome
        gC.op_view();
        
        // for viewing the imagine of two genome input and the genome output
        
        System.out.println("\n *******  D I S P L A Y      G R A P H   *********");
        gA.View_mate_singlepoint(gB, 999);
        System.out.println("\n *************************************************");
        
        System.out.println("\n\n End of experiment");
        
    }
    
    /**
     * This is a test for compute depth of genome and compute if has a path from
     * two nodes (is a test for new version of method is_recur())
     * has_a_path(..) is not necessary for network simulation
     */
    public static void Experiment5(String xFileName, int potin, int potout) {
        StringTokenizer st;
        String curword;
        String xline;
        IOseq xFile;
        int id;
        Genome g1 = null;
        Network net = null;
        
        xFile = new IOseq(xFileName);
        boolean ret = xFile.IOseqOpenRfromFile();
        
        if (ret) {
            
            try {
                System.out.println("------ Start experiment 5 -------");
                // read genome A
                
                System.out.println(" Read start genome..");
                
                xline = xFile.IOseqRead();
                
                st = new StringTokenizer(xline);
                // skip
                curword = st.nextToken();
                // id of genome can be readed
                curword = st.nextToken();
                id = Integer.parseInt(curword);
                g1 = new Genome(id, xFile);
                
                // generate a link mutation
                g1.mutate_link_weight(Neat.p_weight_mut_power, 1.0, NeatConstant.GAUSSIAN);
                // generate from genome the phenotype
                g1.genesis(id);
                
                // view genotype
                g1.op_view();
                
                // assign reference to genotype
                net = g1.phenotype;
                
                // compute first the 'teorical' depth
                int lx = net.max_depth();
                // compute . after, the 'pratical' depth passing
                // the virtual depth;
                int dx = net.is_stabilized(lx);
                
                System.out.print("\n Max depth virtuale=" + lx);
                System.out.print(", max depth reale=" + dx);
                
                // search the inode
                NNode inode = null;
                NNode onode = null;
                NNode curnode = null;
                boolean rc = false;
                int cnt = 0;
                
                for (int ix = 0; (ix < net.allnodes.size()) && (cnt < 2); ix++) {
                    curnode = (NNode) net.allnodes.elementAt(ix);
                    if (curnode.node_id == potin) {
                        inode = curnode;
                        cnt++;
                    }
                    
                    if (curnode.node_id == potout) {
                        onode = curnode;
                        cnt++;
                    }
                }
                
                // if exist , point to exitsting version
                if (cnt < 2) {
                    
                    System.out.print("\n ERROR :nodes in e/o out wrong's : retype!");
                } else {
                    
                    net.status = 0;
                    rc = net.has_a_path(inode, onode, 0, 30);
                    System.out.print("\n Result for  example " + xFileName + " for ipotetic path ");
                    System.out.print("\n   inode[" + potin + "] ---> onode[" + potout + "]  is  return code=" + rc);
                    System.out.print(", status = " + net.status);
                    
                }
                
                // after reset all value of net
                net.flush();
                
                // ok : the propagation is completed
            } catch (Throwable e) {
                System.err.println(e + " : error during read " + xFileName);
            }
            
            xFile.IOseqCloseR();
            
        }
        
        else
            System.err.print("\n : error during open " + xFileName);
        
        System.out.println("\n\n End of experiment");
        
    }
}