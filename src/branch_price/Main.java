/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

/**
 *
 * @author dreis
 */
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;


public class Main {

    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) throws IloException, FileNotFoundException
    {
        // Time for model
        int dt = 6;
        int duration = 1200;
        int T = duration/dt + 1;
        
        // Allowable paths to be store per vehicle
        //int numPaths = 70;
        
        // Creating a File object that represents the disk file.
        PrintStream o = new PrintStream(new File("log.txt"));
  
        // Store current System.out before assigning a new value
        PrintStream console = System.out;
        
        long timer = System.nanoTime();
        ArrayList<Double> objectives = new ArrayList<>();
        ArrayList<Integer> iterations = new ArrayList<>();
        int iter = 0;
        for (int num=44; num<45; num++)
        {
            int numPaths = num;

        //read in network data
        Network network = new Network("ColumnTest1");
        
        //Creates printer class
        PrintCounts printer = new PrintCounts();
        
        //Create zones, nodes, and links from network data
        Zone[] zones = network.getZones();
        Node[] nodes = network.getNodes();
        Link[] links = network.getLinks();
        Link[] source = network.getSources();
        Link[] sink = network.getSinks();
        
        //--------------------------------------------------------------------------------------------------------------
        //Create Time Expanded Graph G
        //--------------------------------------------------------------------------------------------------------------
        
        Time_Expanded_Graph G = new Time_Expanded_Graph(source, sink, links, dt, duration);
        G.combineAllNodes();
        G.combineAllLinks();
        ArrayList<Node_TE> TE_AllNodes = G.getAllNodes();

        //--------------------------------------------------------------------------------------------------------------
        // Topoligical Sort
        //--------------------------------------------------------------------------------------------------------------
        TopologicalSort.Graph graph = new TopologicalSort.Graph(TE_AllNodes);
        
        for (Link_TE L: G.getTELinks())
        {
            graph.addLink(L.getStart(), L.getEnd());
        }
        for (Link_TE L: G.getTENodeLinks())
        {
            graph.addLink(L.getStart(), L.getEnd());
        }
        Node_TE[] TopoSort = graph.topologicalSorting();
        
        
     
        //Create Linear Program
        IloCplex c = new IloCplex();
        
        //--------------------------------------------------------------------------------------------------------------
        //Create Nup, Ndown, R_i, S_i arrays
        //--------------------------------------------------------------------------------------------------------------
        for (Link l : links)
        {
            l.createNup(T, c);
            l.createNdown(T, c);
            l.createReceiving(T, c);
            l.createSending( T, c);
        }
        
        for (Link l: source)
        {
            l.createNup(T, c);
            l.createNdown(T, c);
            l.createSending(T, c);
        }
        
        for (Link l: sink)
        {
            l.createNup(T, c);
            l.createNdown(T, c);
            l.createReceiving(T, c);
        }
        //--------------------------------------------------------------------------------------------------------------
        //Load Vehicle Demand
        //--------------------------------------------------------------------------------------------------------------
        
            
        ArrayList<Vehicle> V = new ArrayList<>();
        int Vehcount = 0;
        for (Zone z: zones)
        {
            for (Zone d: zones)
            {
                double Demand = z.getDemand(d);
                for (int i = 0; i<duration; i += dt)
                {
                    for (int j = 0; j<createDummyNup(Demand, i); j++)
                    {
                        Vehcount++;
                        //Create new vehicle
                        Vehicle v = new Vehicle(z,d,i,0,0,Vehcount);
                        V.add(v);
                        Node_TE start = findLink(source, v.getOrigin().getId()).getTENodeUp(v.getTime()/dt);
                        for (Node_TE SortedNode : TopoSort) 
                        {
                            SortedNode.cost = Double.MAX_VALUE;
                            if (SortedNode == start)
                            {
                                SortedNode.cost = 0;
                            }
                        }
                        for (Node_TE n: TopoSort)
                        {
                            G.relax(n);
                        }
                        Link dest = network.findLink(v.getDest(), nodes[nodes.length-1]);
                        Path pi = G.trace(start, G.destinationNodeTE(dest));
                        pi.createDelta(c);
                        v.CreateP(c);
                        v.addPath(pi);
                        pi.CalculateReducedCost(v);
                    }
                }
            }
        }

        //long timer = System.nanoTime();
        //System.out.println("Vehicle Loading: "+(timer/Math.pow(10,9)));
        
        //--------------------------------------------------------------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        //Column Generation Loop
        //--------------------------------------------------------------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        
            
        Constraints Const = new Constraints();
        Duals D = new Duals();
            
        int x = 0;
        int count = 0;
        while (x < 1)
        {
            // Assign console to output stream
            System.setOut(console);
            x = 1;
            count++;
            System.out.println("Tests: "+numPaths);
            System.out.println("iteration: "+count);
            
            
            //--------------------------------------------------------------------------------------------------------------
            //Constraints for RMP
            //--------------------------------------------------------------------------------------------------------------
            
            Const.setVehConstraints(V, c);
            Const.setSourceConstraints(V, source, c, dt, duration);
            Const.setLinkConstraints(V, links, c, dt, duration, network);
            Const.setSinkConstraints(V, sink, c, dt, duration);
            
            long consttimer = timer(timer);
            timer = System.nanoTime();

            //--------------------------------------------------------------------------------------------------------------
            //Create/Solve objective function
            //--------------------------------------------------------------------------------------------------------------
            
            ObjectiveFunction obj = new ObjectiveFunction();
            
            obj.CreateObjective(V, c, duration);

            //Solve Linear Program
            c.solve();
            c.setOut(null);
            
            obj.PrintObjective(c);
            
            long solvetimer = timer(timer);
            timer = System.nanoTime();

            //Calculate number of Vehicles assigned to a path
            double vehOnPath = 0;
            for (Vehicle v: V)
            {
                for (Path p: v.getPaths())
                {
                    vehOnPath += c.getValue(p.getDelta());
                }
            }
            
            
            //-----------------------------------------------------------------------------------------------------
            // Update Duals
            //-----------------------------------------------------------------------------------------------------
            
            D.updateVehDuals(V, c);
            D.updateSourceDuals(source, c, T);
            D.updateLinkDuals(links, c, T);
            D.updateSinkDuals(sink, c, T);
            
            
            long dualtimer = timer(timer);
            timer = System.nanoTime();
            
            //--------------------------------------------------------------------------------------------------------
            //Solve New Pricing Problem
            //--------------------------------------------------------------------------------------------------------
            for (Vehicle v: V)
            {
                int duplicate = 0;
                Node_TE start = findLink(source, v.getOrigin().getId()).getTENodeUp(v.getTime());
                for (Node_TE SortedNode : TopoSort) 
                {
                    SortedNode.cost = Double.MAX_VALUE;
                    if (SortedNode == start)
                    {
                        SortedNode.cost = 0;
                    }
                }
                
                for (Node_TE n: TopoSort)
                {
                    G.relax(n);
                }
                
                Link dest = network.findLink(v.getDest(), nodes[nodes.length-1]);
                Path p = G.trace(start, G.destinationNodeTE(dest));
                
                p.createDelta(c);
                duplicate = checkPath(p,v);
                if (duplicate == 0)
                {
                    p.CalculateReducedCost(v);
                    double c_pi = p.getReducedCost();
                    
                    if (c_pi < 0)
                    {   
                        v.addPath(p);
                        x = 0;
                    }
                }
                
                // update the reduced costs
                for (Path pi: v.getPaths())
                {
                    pi.CalculateReducedCost(v);
                }
                
                Collections.sort(v.getPaths());
                
                // Only keep certain number of paths in the Restricted Path set
                if (v.getPaths().size() > numPaths)
                {
                    int index = v.getPaths().size()-1;
                    v.getPaths().remove(index);
                }
                
            }
            if (count > 300)
            {
                x = 1;
            }
            
            long sptimer = timer(timer);
            timer = System.nanoTime();
            printer.printTimer(consttimer, solvetimer, dualtimer, sptimer);
            //printer.print(links, source, sink, c, dt, duration);
            
            if (x!= 0)
            {
                obj.PrintObjective(c);
                System.out.println("Vehicles on a Path: "+vehOnPath); 
                objectives.add(obj.getObjective(c));
                iterations.add(count);
                
//                for (Vehicle v: V)
//                {
//                    //printer.printPaths(c, v);
//                }
            }
            //reset model for next iteration
            c.clearModel();
            timer = System.nanoTime();
        }
            long iterationTime  = timer(timer);
            timer = System.nanoTime();
            
            System.setOut(o);
            System.out.println(numPaths+"  "+(iterationTime/Math.pow(10,9))+"   Objective  "+objectives.get(iter)+"    iterations   "+iterations.get(iter));
            iter++;
            System.setOut(console);
        //Bracked for the for loop
        }
    }
        
    
    
    public static int createDummyNup(double demand, int T)
    {
//        int dummyNup;
//        double minute_demand = demand/60;
//        int min_demand_rounded = (int) Math.round(minute_demand);
//        if (T <= 59-1)
//        {
//            dummyNup = min_demand_rounded;
//        }
//        else if (T == 60-1)
//        {
//            dummyNup  = (int)(demand - min_demand_rounded*59);
//        }
//        else
//        {
//            dummyNup = 0;
//        }
//        return dummyNup;
        if (demand > 0)
        {
            if (T < 120)
            {
                return 15;
            }
        }
        return 0;
    }
    
    public static int checkPath(Path pi, Vehicle v)
    {
        for (Path p: v.getPaths())
        {
            if (p.equals(pi))
            {
                return 1;
            }
        }
        return 0;
    }
    
    public static Link findLink(Link[] links, int origin)
    {
       return links[origin-1];
    }
    
    public static Long timer(long t)
    {
        long oldtimer = t;
        long timer = System.nanoTime();
        return timer - oldtimer;
    }  
}
