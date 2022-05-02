/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class ReservationHueristic 
{
    double volume;
    
    public ReservationHueristic()
    { 
        volume = 0;
    }
    
    public void loadQuantity(int RQ, Node_TE[] TopoSort, Node_TE start, Time_Expanded_Graph G, Vehicle v, IloCplex c, Network network, Node[] nodes, ArrayList<Link_TE> ReservationH) throws IloException
    {
        while (RQ > 0) 
        {
            for (Node_TE SortedNode : TopoSort) 
            {
                SortedNode.cost = Double.MAX_VALUE;
                if (SortedNode == start) 
                {
                    SortedNode.cost = 0;
                }
            }
            for (Node_TE n : TopoSort) 
            {
                G.relax(n);
            }
            
            //Find destination link
            Link dest = network.findLink(v.getDest(), nodes[nodes.length - 1]);
            
            //Find Shortest Path
            Path pi = G.trace(start, G.destinationNodeTE(dest));
            
            //Path Initialization
            pi.createDelta(c);
            v.CreateP(c);
            v.addPath(pi);
            pi.CalculateReducedCost(v);
            //pi.printPath();
            
            //reservation huerstic
            //get amount of vehicles that will fit on the path
            double availCap = getAvailCap(pi);

            //flow to be loaded must be an integer
            int flowAvail = (int)Math.floor(availCap);
            //System.out.println("flow avail: "+flowAvail);
            RQ = UpdateOccupancy(pi, RQ, availCap, flowAvail, ReservationH, G);
            //System.out.println("RQ: "+RQ);
            
        }
    }
    public double getAvailCap(Path pi)
    {
        double availCap = Double.MAX_VALUE;
        for (Link_TE l : pi) 
        {
            //l.printLink();
            if (l.getRemainingCapacity() < availCap) 
            {
                availCap = l.getRemainingCapacity();
            }
        }
        return availCap;
    }
    
    public int UpdateOccupancy(Path pi, int RQ, double availCap, int flowAvail, ArrayList<Link_TE> ReservationH, Time_Expanded_Graph G)
    {
        if (RQ < availCap)
        {
            for (Link_TE l: pi)
            {
                l.updateOccupancy(RQ);
            }
            pi.updateOccupancy(RQ);
            volume += RQ;
            RQ = 0;
        }
        else
        {
            for (Link_TE l: pi)
            {
                l.updateOccupancy(flowAvail);
            }
            pi.updateOccupancy(flowAvail);
            RQ = RQ - flowAvail;
            volume += flowAvail;
        }
        for (Link_TE l : pi) 
        {
            l.checkCapacity();
            
            //removes link and all possible connections from Time Expanded graph if capacity is reached
            if (l.getFull()) 
            {
                int control = l.getLinkController();
                
                //If link reaches capacity due to incoming link
                if (control == 1 || control == 3)
                {
                    Node_TE s = l.getStart();
                    for (Link_TE link : G.getAllLinks())
                    {
                        if (s.equals(link.getStart()))
                        {
                            link.removeLink();
                            ReservationH.add(link);
                        }
                    }
                }
                
                //If link reaches capacity due to outgoing link
                if (control == 2 || control == 3)
                {
                    Node_TE e = l.getEnd();
                    for (Link_TE link : G.getAllLinks())
                    {
                        if (e.equals(link.getEnd()))
                        {
                            link.removeLink();
                            ReservationH.add(link);
                        }
                    }
                }
            }
        }
        return RQ;
    }
    
    public void RestoreLinks(ArrayList<Link_TE> ReservationH)
    {
        for (Link_TE l : ReservationH) 
        {
            l.restoreLink();
        }
    }
}
