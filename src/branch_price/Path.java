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
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.Comparator;

public class Path extends ArrayList<Link_TE> implements Comparable<Path>
{
    private IloNumVar delta;
    private double C_pi;
    double occupancy = 0;
    
    public double getPathTravelTime()
    {
        int start = this.get(0).getStart().getTime();
        int end = this.get(this.size()-1).getStart().getTime();
        double tt = end - start;
        return tt;
    }
    
    public boolean isConnected()
    {
        int count = 0;
        for (int i=0; i<this.size()-1; i++)
        {
            Node_TE n1 = this.get(i).getEnd();
            Node_TE n2 = this.get(i+1).getStart();
            if (n1 != n2)
            {
                count++;
            }
        }
        return count == 0;
    }
    
    public Node_TE getSource()
    {
        return this.get(0).getStart();
    }
    
    public Node_TE getDest()
    {
        return this.get(this.size()-1).getEnd();
    }
    
    public void createDelta(IloCplex cplex) throws IloException
    {
        delta = cplex.numVar(0,1);
    }
    
    public IloNumVar getDelta()
    {
        return delta;
    }
    
    
    public int CheckZeta1up(int t, Link i, Vehicle v)
    {
        for (Link_TE L: this)
        {
            Link A = L.getStart().getLink();
            if (A == i && L.getStart().getTime() == t && L.getStart().getDirection().equals("up"))
            {
                return 1*v.getQuantity();
            }
        }
        return 0;
    }
    
    public int CheckZeta1down(int t, Link i, Vehicle v)
    {
        for (Link_TE L: this)
        {
            Link A = L.getStart().getLink();
            if (A == i && L.getStart().getTime() == t && L.getStart().getDirection().equals("down"))
            {
                return 1*v.getQuantity();
            }
        }
        return 0;
    }
    
    public int CheckZeta2(int t, Link i, Link j, Vehicle v)
    {
        for (Link_TE L: this)
        {
            if (L.getStart().getLink() == i && L.getEnd().getLink() == j && L.getStart().getTime() == t)
            {
                // System.out.println("worked");
                return 1*v.getQuantity();
            }
        }
        return 0;
    }
    
    
    public double getMuCost()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            s += L.getLinkMu();
        }
        return s;
    }

    public double getPsiCost()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            s += L.getLinkPsi();
        }
        return s;
    }
    
    public double getLambdaCost()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            s += L.getNodeLambda();
        }
        return s;
    }
    
    public double getThetaCost()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            s += L.getNodeTheta();
        }
        return s;
    }
    
    public void CalculateReducedCost(Vehicle v)
    {
        C_pi = this.getPathTravelTime() - v.getEta() - v.getRho() + this.getMuCost()  + this.getPsiCost() - this.getThetaCost() - this.getLambdaCost();
        //C_pi = this.getPathTravelTime() - v.getRho() + this.getMuCost()  + this.getPsiCost() - this.getThetaCost() - this.getLambdaCost();
//        System.out.println("travel time: "+this.getPathTravelTime());
//        System.out.println("Eta: "+v.getEta());
//        System.out.println("Rho: "+v.getRho());
//        System.out.println("Mu: "+this.getMuCost());
//        System.out.println("Psi: "+this.getPsiCost());
//        System.out.println("theta: "+this.getThetaCost());
//        System.out.println("lambda: "+this.getLambdaCost());
    }
    
    public double getReducedCost()
    {
        return C_pi;
    }
    
    public void printRCComponents(Vehicle v)
    {
        System.out.println("Travel Time: "+this.getPathTravelTime());
        System.out.println("Assignment: "+v.assignment);
        System.out.println("Eta: "+v.getEta());
        System.out.println("Eta*Assignment: "+(1-v.assignment)*v.getEta());
        System.out.println("Rho: "+v.getRho());
        System.out.println("Mu: "+this.getMuCost());
        System.out.println("Psi: "+this.getPsiCost());
        System.out.println("Theta: "+this.getThetaCost());
        System.out.println("Lambda: "+this.getLambdaCost());
    }
    
    public void printPath()
    {
        System.out.println("Path: ");
        for (Link_TE L: this)
        {
            L.printLink();
        }
        System.out.println("RC: "+getReducedCost());
    }
    
    public int compareTo(Path p)
    {
        int x = 0;
        if (this.getReducedCost() - p.getReducedCost() < 0)
        {
            x = -1;
        }
        else if (this.getReducedCost() - p.getReducedCost() > 0)
        {
            x = 1;
        }    
        return x;
    }
    
    public void updateOccupancy(double volume)
    {
        occupancy += volume;
    }
    
    public double getOccupancy()
    {
        return occupancy;
    }

}
