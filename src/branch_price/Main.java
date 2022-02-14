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
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;


public class Main {

    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) throws IloException
    {
        // Time for model
        int T = 50;
        
        // Allowable paths to be store per vehicle
        int numPaths = 50000;
        
        //read in network data
        //Network network = new Network("SimpleNetwork");
        Network network = new Network("Braess_Small");
        
        //Create zones, nodes, and links from network data
        Zone[] zones = network.getZones();
        Node[] nodes = network.getNodes();
        Link[] links = network.getLinks();
        Link[] source = network.getSources();
        Link[] sink = network.getSinks();
        
        //Create Time Expanded Graph G
        Time_Expanded_Graph G = new Time_Expanded_Graph(source, sink, links, T);
        G.combineAllNodes();
        G.combineAllLinks();
        ArrayList<Node_TE> TE_AllNodes = G.getAllNodes();
        ArrayList<Link_TE> TE_AllLinks = G.getAllLinks();
        //Test Graph

        
        // Topoligical Sort
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
        
        //Create Nup, Ndown, R_i, S_i arrays
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
        
        
        //Creating Vehicles and Demand
        ArrayList<Vehicle> V = new ArrayList<>();
        int Vehcount = 0;
        for (Zone z: zones)
        {
            for (Zone d: zones)
            {
                double Demand = z.getDemand(d);
                for (int i = 0; i<T; i++)
                {
                    for (int j = 0; j<createDummyNup(Demand, i); j++)
                    {
                        Vehcount++;
                        //Create new vehicle
                        Vehicle v = new Vehicle(z,d,i,0, Vehcount);
                        V.add(v);
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
                        Path pi = G.trace(start, G.destinationNodeTE(dest));
                        pi.createDelta(c);
                        v.addPath(pi);
                        pi.CalculateReducedCost(v, T);
                    }
                }
            }
        }

        long timer = System.nanoTime();
        System.out.println("Vehicle Loading: "+timer/Math.pow(10,9));
        
        //--------------------------------------------------------
        //Column Generation Loop
        //--------------------------------------------------------
        int x = 0;
        int count = 0;
        while (x < 1)
        {
            x = 1;
            count++;
            System.out.println("iteration: "+count);
            //--------------------------------------------------------
            //Constraints for RMP
            //--------------------------------------------------------
            
            IloRange range1;
            IloRange range2;
            IloRange range3;
            IloRange range4;
            IloRange range5;
            IloRange range6;
            IloRange range7;
            IloRange range8;
            IloRange range9;
            
            //Constraint 17b
            for (Vehicle v: V)
            {
                IloLinearNumExpr const1 = c.linearNumExpr();
                for (Path p: v.getPaths())
                {
                    //System.out.println("add term");
                    const1.addTerm(1,p.getDelta());
                }
                range1 = c.addLe(const1, 1);
                v.createRangeV(range1);
            }

            
            
            //Source Link Constraints
            for (Link l: source)
            {
                for (int t=0; t<T-1; t++)
                {
                    IloLinearNumExpr constA = c.linearNumExpr();
                    IloLinearNumExpr constDem = c.linearNumExpr();
                    IloLinearNumExpr constB = c.linearNumExpr();
                    
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            constDem.addTerm(p.CheckZeta1up(t, l),p.getDelta());
                            constA.addTerm(p.CheckZeta1down(t, l),p.getDelta());
                        }
                    }
                    
                    //Demand Loading Constraint
                    c.addEq(c.sum(l.getNup(t+1),c.prod(-1,l.getNup(t)),c.prod(-1,constDem)),0);
                    
                    //Constraint 16e
                    range2 = c.addLe(c.sum(constA,c.prod(-1,l.getSending(t))),0);
                    
                    if (t>0)
                    {
                        l.getTENodeDown(t).createRangeL(range2); //psi
                    }

                    //Constraint 16g
                    if (t >= (int)(l.getL()/l.getuf())-1 && (t -(int)(l.getL()/l.getuf())) < T-1)
                    {
                        c.addGe(c.sum(l.getNup(t+1-(int)(l.getL()/l.getuf())),c.prod(-1,l.getNdown(t))),l.getSending(t));
                    }

                    
                   //Constraint 16h
                    c.addLe((c.prod(1,l.getSending(t))),l.getCapacity());
                     
                    for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                    {
                        for (Vehicle v: V)
                        {
                            for (Path p: v.getPaths())
                            {
                                constB.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                            }
                        }
                    }
                    //Constraint 16d
                    range3 = c.addEq(c.sum(l.getNdown(t+1),c.prod(-1,l.getNdown(t)),c.prod(-1,constB)),0);
                    if (t>0)
                    {
                       l.getTENodeDown(t).createRangeN(range3); //lambda
                    }  
                }
            }

            //interior link constraints
            for (Link l: links)
            {
                for (int t=0; t<T-1; t++)
                {
                    IloLinearNumExpr const1 = c.linearNumExpr();
                    IloLinearNumExpr const2 = c.linearNumExpr();
                    IloLinearNumExpr const3a = c.linearNumExpr();
                    IloLinearNumExpr const3b = c.linearNumExpr();
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            const1.addTerm(p.CheckZeta1up(t, l),p.getDelta());
                            const2.addTerm(p.CheckZeta1down(t, l),p.getDelta());
                        }
                    }                  
                    //Constraint 16c
                    range4 = c.addEq(c.sum(l.getNup(t+1),c.prod(-1,l.getNup(t)),c.prod(-1,const1)),0);

                    //Constraint 16e
                    range5 = c.addLe(c.sum(const2,c.prod(-1,l.getSending(t))),0);
                    if (t > 0)
                    {
                        l.getTENodeUp(t).createRangeL(range4); //mu
                        l.getTENodeDown(t).createRangeL(range5); //psi
                    }

                    //Constraint 16g
                    if (t >= (int)(l.getL()/l.getuf())-1 && (t -(int)(l.getL()/l.getuf())) < T-1)
                    {
                        c.addGe(c.sum(l.getNup(t+1-(int)(l.getL()/l.getuf())),c.prod(-1,l.getNdown(t))),l.getSending(t));
                    }

                   //Constraint 16h
                    c.addLe((c.prod(1,l.getSending(t))),l.getCapacity());

                    
                    Link dummy = network.findLink(network.findNode(3), network.findNode(4));
                    if (l != dummy)
                    {
                    //Constraint 16i
                    if (t >= (int)(l.getL()/l.getW())-1)
                    {
                        c.addGe(c.sum(c.prod(1,l.getNdown(t+1-(int)(l.getL()/l.getW()))),c.prod(-1, l.getNup(t)),c.prod(-1,l.getReceiving(t))), -1*l.getL()*l.getKJam());
                    }
                    }

                    //Constraint 16j
                    c.addLe((c.prod(1,l.getReceiving(t))),l.getCapacity());
                    
                    //new constraints
                    for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                    {
                        for (Vehicle v: V)
                        {
                            for (Path p: v.getPaths())
                            {
                                const3a.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                            }
                        }
                    }
                    for (Link_TE j: l.getTENodeUp(t).getIncoming())
                    {
                        for (Vehicle v: V)
                        {
                            for (Path p: v.getPaths())
                            {
                                const3b.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                            }
                        }
                    }
                    //Constraint 16d
                    range6 = c.addEq(c.sum(l.getNdown(t+1),c.prod(-1,l.getNdown(t)),c.prod(-1,const3a)),0);

                    //Constraint 16f
                    range7 = c.addLe(c.sum(const3b, c.prod(-1,l.getReceiving(t))),0);

                    if (t>0)
                    {
                        l.getTENodeUp(t).createRangeN(range7);
                        l.getTENodeDown(t).createRangeN(range6);
                    }    
                }
            }

            //Sink Link Constraints
            for (Link l: sink)
            {
                for (int t=0; t<T-1; t++)
                {
                    IloLinearNumExpr const4 = c.linearNumExpr();
                    IloLinearNumExpr const5 = c.linearNumExpr();
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            const4.addTerm(p.CheckZeta1up(t, l),p.getDelta());
                        }
                    }
                    //Constraint 16c
                    range8 = c.addEq(c.sum(l.getNup(t+1),c.prod(-1,l.getNup(t)),c.prod(-1,const4)),0);
                    
                    if (t>0)
                    {
                        l.getTENodeUp(t).createRangeL(range8);
                    }
                    
                    for (Link_TE j: l.getTENodeUp(t).getIncoming())
                    {
                        for (Vehicle v: V)
                        {
                            for (Path p: v.getPaths())
                            {
                                const5.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                            }
                        }
                    }
                    //Constraint 16f
                    range9 = c.addLe(c.sum(const5,c.prod(-1,l.getReceiving(t))),0);
                    if (t>0)
                    {
                        l.getTENodeUp(t).createRangeN(range9);
                    }
                }
            }
            long consttimer = timer(timer);
            timer = System.nanoTime();

            //--------------------------------------------------------
            //Create objective function
            //--------------------------------------------------------
            
            IloLinearNumExpr obj = c.linearNumExpr();
            IloNumExpr obj3 = null;
            IloLinearNumExpr obj4 = c.linearNumExpr();
            for (Vehicle v: V)
            {
                IloLinearNumExpr obj2 = c.linearNumExpr();
                for (Path p: v.getPaths())
                {
                    obj.addTerm(p.getPathTravelTime(),p.getDelta());
                    
                    obj2.addTerm(-v.getAlpha(T),p.getDelta());
                }
                
                if(obj3 == null)
                {
                    obj3 = c.sum(v.getAlpha(T),obj2);
                }
                else
                {
                    obj3 = c.sum(obj3, c.sum(v.getAlpha(T),obj2));
                }  
            }
            c.addMinimize(c.sum(obj,obj3));

            //Solve Linear Program
            c.solve();
            c.setOut(null);
            
            System.out.print("Objective: ");
            System.out.println(c.getValue(c.sum(obj,obj3)));
            
            long solvetimer = timer(timer);
            timer = System.nanoTime();

            //Calculate number of Vehicles assigned to a path
            int vehOnPath = 0;
            for (Vehicle v: V)
            {
                for (Path p: v.getPaths())
                {
                    vehOnPath += c.getValue(p.getDelta());
                }
            }
            System.out.println("Vehicles assigned a path: "+vehOnPath);
            
            
            //--------------------------------------------------------
            // Update Duals
            //--------------------------------------------------------
            
            System.out.println("Non Zero Dual Variables:");
            
            for (Vehicle v: V)
            {
                v.updateRho(c.getDual(v.getRangeV()));
//                if (v.getRho() != 0)
//                {
//                    System.out.print("Rho dual: ");
//                    System.out.println(-v.getRho());
//                }
            }
            for (Link l: source)
            {
                for (int t=1; t<T-1; t++)
                {
                    //psi dual
                    l.getTENodeDown(t).updatePsi(c.getDual(l.getTENodeDown(t).getRangeL()));
//                    if (l.getTENodeDown(t).getPsi() != 0)
//                    {
//                        l.getTENodeDown(t).printNode();
//                        System.out.print("Psi dual: ");
//                        System.out.println(-l.getTENodeDown(t).getPsi());
//                    }
                    
                    //Lambda dual
                    for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                    {
                        l.getTENodeDown(t).updateLambda(c.getDual(l.getTENodeDown(t).getRangeN()));
//                        if (l.getTENodeDown(t).getLambda() != 0)
//                        {
//                            l.getTENodeDown(t).printNode();
//                            System.out.print("Lambda: ");
//                            System.out.println(l.getTENodeDown(t).getLambda());
//                        }
                    }
                }
            }
            for (Link l: links)
            {
                for (int t=1; t<T-1; t++)
                {
                    //mu dual
                    l.getTENodeUp(t).updateMu(c.getDual(l.getTENodeUp(t).getRangeL()));
//                    if (l.getTENodeUp(t).getMu() != 0)
//                    {
//                        l.getTENodeUp(t).printNode();
//                        System.out.print("Mu dual: ");
//                        System.out.println(l.getTENodeUp(t).getMu());
//                    }
                    
                    //psi dual
                    l.getTENodeDown(t).updatePsi(c.getDual(l.getTENodeDown(t).getRangeL()));
//                    if (l.getTENodeDown(t).getPsi() != 0)
//                    {
//                        l.getTENodeDown(t).printNode();
//                        System.out.print("Psi dual: ");
//                        System.out.println(-l.getTENodeDown(t).getPsi());
//                    }
                    
                    for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                    {
                        //lambda dual
                        l.getTENodeDown(t).updateLambda(c.getDual(l.getTENodeDown(t).getRangeN()));
//                        if (l.getTENodeDown(t).getLambda() != 0)
//                        {
//                            l.getTENodeDown(t).printNode();
//                            System.out.print("Lambda: ");
//                            System.out.println(l.getTENodeDown(t).getLambda());
//                        }
                    }
                    
                    for (Link_TE j: l.getTENodeUp(t).getIncoming())
                    {
                        //theta dual
                        l.getTENodeUp(t).updateTheta(c.getDual(l.getTENodeUp(t).getRangeN()));
//                        if (l.getTENodeUp(t).getTheta() != 0)
//                        {
//                            j.printLink();
//                            System.out.print("Theta: ");
//                            System.out.println(-l.getTENodeUp(t).getTheta());
//                        }
                    }
                }
            }
            for (Link l: sink)
            {
                for (int t=1; t<T-1; t++)
                {
                    //mu dual
                    l.getTENodeUp(t).updateMu(c.getDual(l.getTENodeUp(t).getRangeL()));
//                    if (l.getTENodeUp(t).getMu() != 0)
//                    {
//                        l.getTENodeUp(t).printNode();
//                        System.out.print("Mu dual: ");
//                        System.out.println(l.getTENodeUp(t).getMu());
//                    }
                    
                    for (Link_TE j: l.getTENodeUp(t).getIncoming())
                    {
                        //theta dual
                        l.getTENodeUp(t).updateTheta(c.getDual(l.getTENodeUp(t).getRangeN()));
//                        if (l.getTENodeUp(t).getTheta() != 0)
//                        {
//                            j.printLink();
//                            System.out.print("Theta: ");
//                            System.out.println(-l.getTENodeUp(t).getTheta());
//                        }
                    }
                }
            }
            
            long dualtimer = timer(timer);
            timer = System.nanoTime();
            
            //--------------------------------------------------------
            //Solve New Pricing Problem
            //--------------------------------------------------------
            System.out.println();
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
                    p.CalculateReducedCost(v, T);
                    double c_pi = p.getReducedCost();
                    if (c_pi < 0)
                    {
                        //Column generation need another interation
                        v.addPath(p);
                        x = 0;
                    }
                }
//                else
//                {
//                    System.out.println();
//                    System.out.println("Vehicle "+v.getId()+":");
//                    for (Path paths: v.getPaths())
//                    {
//                        if (c.getValue(paths.getDelta()) > 0)
//                        {
//                            paths.printPath();
//                            System.out.print("Delta: ");
//                            System.out.println(c.getValue(paths.getDelta()));
//                            System.out.println();
//                        }
//                    }
//                }
                
                // update the reduced costs
                for (Path pi: v.getPaths())
                {
                    pi.CalculateReducedCost(v, T);
                }
                
                Collections.sort(v.getPaths());
                
                // Only keep certain number of paths in the Restricted Path set
                if (v.getPaths().size() > numPaths)
                {
                    int index = v.getPaths().size()-1;
                    v.getPaths().remove(index);
                }
                
            }
            long sptimer = timer(timer);
            timer = System.nanoTime();
            
            
            System.out.println();
            System.out.println("Running Times: ");
            System.out.println("RPM Constraints: "+consttimer/Math.pow(10,9));
            System.out.println("Solve Model: "+solvetimer/Math.pow(10,9));
            System.out.println("Update Duals: "+dualtimer/Math.pow(10,9));
            System.out.println("Shortest path: "+sptimer/Math.pow(10,9));
            System.out.println();

//            if (x!= 0)
//            {
//                for (Link l: links)
//                {
//                    System.out.println("Link: "+l);
//                    for (int i = 0; i<T-1; i++)
//                    {
//                        System.out.println();
//                        System.out.println("time: "+i);
//                        System.out.println("Nup: "+c.getValue(l.getNup(i)));
//                        System.out.println("Ndown: "+c.getValue(l.getNdown(i)));
//                        System.out.println("Sending: "+c.getValue(l.getSending(i)));
//                        System.out.println("Receiving: "+c.getValue(l.getReceiving(i)));
//                    }
//                }
//                for (Link l: sink)
//                {
//                    System.out.println("Link: "+l);
//                    for (int i = 0; i<T-1; i++)
//                    {
//                        System.out.println();
//                        System.out.println("time: "+i);
//                        System.out.println("Nup: "+c.getValue(l.getNup(i)));
//                    }
//                }
//            }
            //reset model for next iteration
            c.clearModel();
            
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
            if (T < 6)
            {
                return 7;
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
