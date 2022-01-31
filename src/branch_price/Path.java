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

public class Path extends ArrayList<Link_TE>
{
    private IloNumVar delta;
    
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
    
    public int CheckZeta1up(int t, Link i)
    {
        for (Link_TE L: this)
        {
            Link A = L.getStart().getLink();
            if (A == i && L.getStart().getTime() == t && L.getStart().getDirection().equals("up"))
            {
                return 1;
            }
        }
        return 0;
    }
    
    public int CheckZeta1down(int t, Link i)
    {
        for (Link_TE L: this)
        {
            Link A = L.getStart().getLink();
            if (A == i && L.getStart().getTime() == t && L.getStart().getDirection().equals("down"))
            {
                return 1;
            }
        }
        return 0;
    }
    
    public int CheckZeta2(int t, Link i, Link j)
    {
        for (Link_TE L: this)
        {
            //L.printLink();
            //System.out.println("Start Link: "+i+" End Link: "+j+" Time: "+t);
            if (L.getStart().getLink() == i && L.getEnd().getLink() == j && L.getStart().getTime() == t)
            {
                // System.out.println("worked");
                return 1;
            }
        }
        return 0;
    }
    
    
    public double getMuCost()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            //s = s + L.getLinkMu()*CheckZeta1up(L.getStart().getTime(), L.getStart().getLink());
            s += L.getLinkMu();
        }
        return s;
    }

    public double getPsiCost()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            //s = s + L.getLinkPsi()*CheckZeta1down(L.getEnd().getTime(), L.getEnd().getLink());
            s += L.getLinkPsi();
        }
        return s;
    }
    
    public double getPathCostNodeLinks()
    {
        double s = 0;
        for (Link_TE L: this)
        {
            //s = s + L.getNodeLinkDual()*CheckZeta2(L.getEnd().getTime(), L.getStart().getLink(),L.getEnd().getLink());
            s += L.getNodeLinkDual();
        }
        return s;
    }
    
    
    public void printPath()
    {
        System.out.println("Path: ");
        for (Link_TE L: this)
        {
            L.printLink();
        }
    }
}
