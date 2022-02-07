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
                        Node_TE start = G.findTENode(nodes[0], v.getOrigin(), v.getTime());
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
                        Path pi = G.trace(start, G.destinationNodeTE(v.getDest(), nodes[nodes.length-1]));
                        pi.createDelta(c);
                        v.addPath(pi);
                        pi.CalculateReducedCost(v, T);
                        //v.addRestrictedPath(pi, pi.getReducedCost());
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
                        G.findTENode(l,t,"down").createRangeL(range2); //psi
                    }

                    //Constraint 16g
                    if (t >= (int)(l.getL()/l.getuf())-1 && (t -(int)(l.getL()/l.getuf())) < T-1)
                    {
                        c.addGe(c.sum(l.getNup(t+1-(int)(l.getL()/l.getuf())),c.prod(-1,l.getNdown(t))),l.getSending(t));
                    }

                    
                   //Constraint 16h
                    c.addLe((c.prod(1,l.getSending(t))),l.getCapacity());
                     
                    for (Link_TE j: G.getOutgoing(l, t))
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
                       G.findTENode(l,t,"down").createRangeN(range3); //lambda
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
                        G.findTENode(l,t,"up").createRangeL(range4); //mu
                        G.findTENode(l,t,"down").createRangeL(range5); //psi
                    }

                    //Constraint 16g
                    if (t >= (int)(l.getL()/l.getuf())-1 && (t -(int)(l.getL()/l.getuf())) < T-1)
                    {
                        c.addGe(c.sum(l.getNup(t+1-(int)(l.getL()/l.getuf())),c.prod(-1,l.getNdown(t))),l.getSending(t));
                    }

                   //Constraint 16h
                    c.addLe((c.prod(1,l.getSending(t))),l.getCapacity());

                    //Constraint 16i
                    if (t >= (int)(l.getL()/l.getW())-1)
                    {
                        c.addGe(c.sum(c.prod(1,l.getNdown(t+1-(int)(l.getL()/l.getW()))),c.prod(-1, l.getNup(t)),c.prod(-1,l.getReceiving(t))), -1*l.getL()*l.getKJam());
                    }

                    //Constraint 16j
                    c.addLe((c.prod(1,l.getReceiving(t))),l.getCapacity());
                    
                    //new constraints
                    for (Link_TE j: G.getOutgoing(l, t))
                    {
                        for (Vehicle v: V)
                        {
                            for (Path p: v.getPaths())
                            {
                                const3a.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                            }
                        }
                    }
                    for (Link_TE j: G.getIncoming(l, t))
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
                        G.findTENode(l,t,"up").createRangeN(range7);
                        G.findTENode(l,t,"down").createRangeN(range6);
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
                        G.findTENode(l,t,"up").createRangeL(range8);
                    }
                    
                    for (Link_TE j: G.getIncoming(l, t))
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
                        G.findTENode(l,t,"up").createRangeN(range9);
                    }
                }
            }

            timer = System.nanoTime() - timer;
            long consttimer = timer;

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
            
            timer = System.nanoTime() - timer;
            long solvetimer = timer;
            
            //Print out vehicle paths
//            for (Vehicle v: V)
//            {
//                System.out.println();
//                System.out.println("Vehicle "+v.getId()+":");
//                for (Path p: v.getRestrictedPaths())
//                {
//                    if (c.getValue(p.getDelta()) > 0)
//                    {
//                        p.printPath();
//                        System.out.print("Delta: ");
//                        System.out.println(c.getValue(p.getDelta()));
//                        System.out.println();
//                    }
//                }
//            }

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
            
//            System.out.println("Non Zero Dual Variables:");
            
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
                    G.findTENode(l,t,"down").updatePsi(c.getDual(G.findTENode(l,t,"down").getRangeL()));
//                    if (G.findTENode(l,t,"down").getPsi() != 0)
//                    {
//                        G.findTENode(l,t,"down").printNode();
//                        System.out.print("Psi dual: ");
//                        System.out.println(G.findTENode(l,t,"down").getPsi());
//                    }
                    
                    //Lambda dual
                    for (Link_TE j: G.getOutgoing(l, t))
                    {
                        G.findTENode(l,t,"down").updateLambda(c.getDual(G.findTENode(l,t,"down").getRangeN()));
//                        if (G.findTENode(l,t,"down").getLambda() != 0)
//                        {
//                            G.findTENode(l,t,"down").printNode();
//                            System.out.print("Lambda: ");
//                            System.out.println(G.findTENode(l,t,"down").getLambda());
//                        }
                    }
                }
            }
            for (Link l: links)
            {
                for (int t=1; t<T-1; t++)
                {
                    //mu dual
                    G.findTENode(l,t,"up").updateMu(c.getDual(G.findTENode(l,t,"up").getRangeL()));
//                    if (G.findTENode(l,t,"up").getMu() != 0)
//                    {
//                        G.findTENode(l,t,"up").printNode();
//                        System.out.print("Mu dual: ");
//                        System.out.println(G.findTENode(l,t,"up").getMu());
//                    }
                    
                    //psi dual
                    G.findTENode(l,t,"down").updatePsi(c.getDual(G.findTENode(l,t,"down").getRangeL()));
//                    if (G.findTENode(l,t,"down").getPsi() != 0)
//                    {
//                        G.findTENode(l,t,"down").printNode();
//                        System.out.print("Psi dual: ");
//                        System.out.println(G.findTENode(l,t,"down").getPsi());
//                    }
                    
                    for (Link_TE j: G.getOutgoing(l, t))
                    {
                        //lambda dual
                        G.findTENode(l,t,"down").updateLambda(c.getDual(G.findTENode(l,t,"down").getRangeN()));
//                        if (G.findTENode(l,t,"down").getLambda() != 0)
//                        {
//                            G.findTENode(l,t,"down").printNode();
//                            System.out.print("Lambda: ");
//                            System.out.println(G.findTENode(l,t,"down").getLambda());
//                        }
                    }
                    
                    for (Link_TE j: G.getIncoming(l, t))
                    {
                        //theta dual
                        G.findTENode(j.getEnd().getLink(),t,"up").updateTheta(c.getDual(G.findTENode(l,t,"up").getRangeN()));
//                        if (G.findTENode(j.getEnd().getLink(),t,"up").getTheta() != 0)
//                        {
//                            j.printLink();
//                            System.out.print("Theta: ");
//                            System.out.println(-G.findTENode(j.getEnd().getLink(),t,"up").getTheta());
//                        }
                    }
                }
            }
            for (Link l: sink)
            {
                for (int t=1; t<T-1; t++)
                {
                    //mu dual
                    G.findTENode(l,t,"up").updateMu(c.getDual(G.findTENode(l,t,"up").getRangeL()));
//                    if (G.findTENode(l,t,"up").getMu() != 0)
//                    {
//                        G.findTENode(l,t,"up").printNode();
//                        System.out.print("Mu dual: ");
//                        System.out.println(G.findTENode(l,t,"up").getMu());
//                    }
                    
                    for (Link_TE j: G.getIncoming(l, t))
                    {
                        //theta dual
                        G.findTENode(j.getEnd().getLink(),t,"up").updateTheta(c.getDual(G.findTENode(l,t,"up").getRangeN()));
//                        if (G.findTENode(j.getEnd().getLink(),t,"up").getTheta() != 0)
//                        {
//                            j.printLink();
//                            System.out.print("Theta: ");
//                            System.out.println(-G.findTENode(j.getEnd().getLink(),t,"up").getTheta());
//                        }
                    }
                }
            }
            
            timer = System.nanoTime() - timer;
            long dualtimer = timer;
            
            //--------------------------------------------------------
            //Solve New Pricing Problem
            //--------------------------------------------------------
            System.out.println();
            for (Vehicle v: V)
            {
                int duplicate = 0;
                Node_TE start = G.findTENode(nodes[0], v.getOrigin(), v.getTime());
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
                Path p = G.trace(start, G.destinationNodeTE(v.getDest(), nodes[nodes.length-1]));
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
                        //v.addRestrictedPath(p, c_pi);
                        x = 0;
                    }
                }
                // update the reduced costs
                for (Path pi: v.getPaths())
                {
                    pi.CalculateReducedCost(v, T);
                }
                
                // Only keep certain number of paths in the Restricted Path set
                if (v.getPaths().size() > numPaths)
                {
                    int index = -1;
                    int counter = 0;
                    double large = Double.NEGATIVE_INFINITY;
                    for (Path pi: v.getPaths())
                    {
                        if (pi.getReducedCost() > large)
                        {
                            large = pi.getReducedCost();
                            index = counter;
                        }
                        counter++;
                    }
                    v.getPaths().remove(index);
                    //System.out.println("RC Removed: "+large);
                }
            }
            timer = System.nanoTime() - timer;
            long sptimer = timer;
            
            
            System.out.println();
            System.out.println("Running Times: ");
            System.out.println("RPM Constraints: "+consttimer/Math.pow(10,9));
            System.out.println("Solve Model: "+solvetimer/Math.pow(10,9));
            System.out.println("Update Duals: "+dualtimer/Math.pow(10,9));
            System.out.println("Shortest path: "+sptimer/Math.pow(10,9));
            System.out.println();

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
            if (T < 10)
            {
                return 10;
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
    
   
    

    
}
