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
public class Duals 
{
    private int x;
    public Duals()
    {
        x = 1;
    }
    
    public void updateVehDuals(ArrayList<Vehicle> V, IloCplex c) throws IloException
    {
        for (Vehicle v: V)
        {
            v.updateRho(c.getDual(v.getRangeV()));
            v.updateEta(c.getDual(v.getRangeP()));
            printVehDuals(v);

        }
    }
    
    public void printVehDuals(Vehicle v)
    {
        if (v.getRho() != 0 & x == 0)
        {
            System.out.print("Vehicle: "+v.getId()+" ");
            System.out.print("Rho dual: ");
            System.out.println(-v.getRho());
        }
        
//        if (v.getEta() != 0 & x == 0)
//        {
//            System.out.print("Vehicle: "+v.getId()+" ");
//            System.out.print("Eta dual: ");
//            System.out.println(v.getEta());
//        }
    }
    
    public void printPsiDuals(Link l, int t)
    {
        if (l.getTENodeDown(t).getPsi() != 0 & x == 0)
        {
            l.getTENodeDown(t).printNode();
            System.out.print("Psi dual: ");
            System.out.println(-l.getTENodeDown(t).getPsi());
        }
    }
    
    public void printLambdaDuals(Link l,Link_TE j, int t)
    {
        if (l.getTENodeDown(t).getPsi() != 0 & x == 0)
        {
            j.printLink();
            System.out.print("Lambda dual: ");
            System.out.println(-l.getTENodeDown(t).getPsi());
        }
    }
    
    public void printMuDuals(Link l, int t)
    {
        if (l.getTENodeUp(t).getMu() != 0 & x == 0)
        {
            l.getTENodeUp(t).printNode();
            System.out.print("Mu dual: ");
            System.out.println(l.getTENodeUp(t).getMu());
        }
    }
    
    public void printThetaDuals(Link l, Link_TE j, int t)
    {
        if (l.getTENodeUp(t).getTheta() != 0 & x == 0)
        {
            j.printLink();
            System.out.print("Theta: ");
            System.out.println(-l.getTENodeUp(t).getTheta());
        }
    }
            
            
    public void updateSourceDuals(Link[] source, IloCplex c, int T) throws IloException
    {
        for (Link l: source)
        {
            for (int t=1; t<T-1; t++)
            {
                //Psi duals
                l.getTENodeDown(t).updatePsi(c.getDual(l.getTENodeDown(t).getRangeL()));
                printPsiDuals(l, t);

                //Lambda duals
                for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                {
                    l.getTENodeDown(t).updateLambda(c.getDual(l.getTENodeDown(t).getRangeN()));
                    printLambdaDuals(l, j, t);
                }
            }
        }
    }
    public void updateLinkDuals(Link[] links, IloCplex c, int T) throws IloException
    {
        for (Link l: links)
        {
            for (int t=1; t<T-1; t++)
            {
                //Mu Duals
                l.getTENodeUp(t).updateMu(c.getDual(l.getTENodeUp(t).getRangeL()));
                printMuDuals(l, t);

                //Psi Duals
                l.getTENodeDown(t).updatePsi(c.getDual(l.getTENodeDown(t).getRangeL()));
                printPsiDuals(l, t);

                //Lambda Duals
                for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                {
                    l.getTENodeDown(t).updateLambda(c.getDual(l.getTENodeDown(t).getRangeN()));
                    printLambdaDuals(l, j, t);
                   
                }

                //Theta Duals
                for (Link_TE j: l.getTENodeUp(t).getIncoming())
                {
                    l.getTENodeUp(t).updateTheta(c.getDual(l.getTENodeUp(t).getRangeN()));
                    printThetaDuals(l, j, t);
                }
            }
        }
    }
    public void updateSinkDuals(Link[] sink, IloCplex c, int T) throws IloException
    {
        for (Link l: sink)
        {
            for (int t=1; t<T-1; t++)
            {
                //Mu Duals
                l.getTENodeUp(t).updateMu(c.getDual(l.getTENodeUp(t).getRangeL()));
                printMuDuals(l, t);

                //Theta Duals
                for (Link_TE j: l.getTENodeUp(t).getIncoming())
                {
                    l.getTENodeUp(t).updateTheta(c.getDual(l.getTENodeUp(t).getRangeN()));
                    printThetaDuals(l, j, t);
                }
            }
        }
    }
}
