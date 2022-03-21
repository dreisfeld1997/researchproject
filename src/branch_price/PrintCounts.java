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
public class PrintCounts 
{
    public PrintCounts()
    { 
    }
    
    public void print(Link[] links, Link[] source, Link[] sink, IloCplex c, int dt, int duration) throws IloException
    {
        for (Link l: source)
        {
            System.out.println("Link: "+l);
            for (int i = 0; i<duration; i += dt)
            {
                System.out.println();
                System.out.println("time: "+i);
                System.out.println("Nup: "+c.getValue(l.getNup(i/dt)));
                System.out.println("Ndown: "+c.getValue(l.getNdown(i/dt)));
                System.out.println("Sending: "+c.getValue(l.getSending(i/dt)));
                //System.out.println("Receiving: "+c.getValue(l.getReceiving(i/dt)));
            }
        }
                
        for (Link l: links)
        {
            System.out.println("Link: "+l);
            for (int i = 0; i<duration; i += dt)
            {
                System.out.println();
                System.out.println("time: "+i);
                System.out.println("Nup: "+c.getValue(l.getNup(i/dt)));
                System.out.println("Ndown: "+c.getValue(l.getNdown(i/dt)));
                System.out.println("Sending: "+c.getValue(l.getSending(i/dt)));
                System.out.println("Receiving: "+c.getValue(l.getReceiving(i/dt)));
                System.out.println("Capacity (veh/hr): "+l.getCapacity());
            }
        }
        for (Link l: sink)
        {
            System.out.println("Link: "+l);
            for (int i = 0; i<duration; i+= dt)
            {
                System.out.println();
                System.out.println("time: "+i);
                System.out.println("Nup: "+c.getValue(l.getNup(i/dt)));
                System.out.println("Receiving: "+c.getValue(l.getReceiving(i/dt)));
            }
        }
    }
    
    public void printTimer(long consttimer, Long solvetimer, Long dualtimer, Long sptimer)
    {
            System.out.println();
            System.out.println("Running Times: ");
            System.out.println("RPM Constraints: "+consttimer/Math.pow(10,9));
            System.out.println("Solve Model: "+solvetimer/Math.pow(10,9));
            System.out.println("Update Duals: "+dualtimer/Math.pow(10,9));
            System.out.println("Shortest path: "+sptimer/Math.pow(10,9));
            System.out.println();
    }
    
    public void printPaths(IloCplex c, Vehicle v) throws IloException
    {
        System.out.println("Vehicle: "+v.getId());
        for (Path paths: v.getPaths())
        {
            if (c.getValue(paths.getDelta()) > 0)
            {
                paths.printPath();
                System.out.print("Delta: ");
                System.out.println(c.getValue(paths.getDelta()));
                System.out.println();
            }
        }
    }
    
     public void printUnassigned(IloCplex c, Vehicle v) throws IloException
    {
        System.out.println("Vehicle: "+v.getId());
        if (v.assignment < 1)
        {
            for (Path paths: v.getPaths())
            {
                paths.printPath();
                System.out.println();
                paths.printRCComponents(v);
                System.out.println();
            }
        }
    }
}
