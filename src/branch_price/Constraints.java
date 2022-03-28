/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class Constraints 
{
    public Constraints()
    {
    }
    
    public void setVehConstraints(ArrayList<Vehicle> V, IloCplex c) throws IloException
    {
        IloRange range1, range2;
        for (Vehicle v: V)
        {
            IloLinearNumExpr const1 = c.linearNumExpr();
            for (Path p: v.getPaths())
            {
                const1.addTerm(1,p.getDelta());
            }
            range1 = c.addLe(const1, 1);
            range2 = c.addEq(c.sum(const1, v.getP()),1);
            v.createRangeV(range1);
            v.createRangeP(range2);
        }
    }
    
    public void setSourceConstraints(ArrayList<Vehicle> V, Link[] source, IloCplex c, int dt, int duration) throws IloException
    {
        IloRange range1, range2;
        for (Link l: source)
        {
            for (int t=0; t<duration; t += dt)
            {
                IloLinearNumExpr Dem = c.linearNumExpr();
                IloLinearNumExpr flowOut = c.linearNumExpr();
                for (Vehicle v: V)
                {
                    for (Path p: v.getPaths())
                    {
                        Dem.addTerm(p.CheckZeta1up(t, l, v),p.getDelta());
                        flowOut.addTerm(p.CheckZeta1down(t, l, v), p.getDelta());
                    }
                }

                //Demand Loading Constraint
                c.addEq(c.sum(l.getNup((t+dt)/dt),c.prod(-1,l.getNup(t/dt)),c.prod(-1,Dem)),0);

                //Constraint 16d
                range2 = c.addEq(c.sum(l.getNdown((t+dt)/dt),c.prod(-1,l.getNdown(t/dt)),c.prod(-1,flowOut)),0);
                l.getTENodeDown(t/dt).createRangeN(range2); //lambda dual 
                
                //Constraint 16e
                range1 = c.addLe(c.sum(flowOut,c.prod(-1,l.getSending(t/dt))),0);
                l.getTENodeDown(t/dt).createRangeL(range1); //psi dual
                
                if (t >= l.gettf()-dt && (t + dt - l.gettf()) <= duration)
                {
                    c.addGe(c.sum(l.getNup((t+dt-(int)(l.gettf()))/dt),c.prod(-1,l.getNdown(t/dt))),l.getSending(t/dt));
                }

               //Constraint 16h
                c.addLe((c.prod(1,l.getSending(t/dt))),l.getCapacity()*dt/3600);
            }
        }
    }
    
    public void setLinkConstraints(ArrayList<Vehicle> V, Link[] links, IloCplex c, int dt, int duration, Network network) throws IloException
    {
        IloRange range1, range2, range3, range4;
        for (Link l: links)
        {
            for (int t=0; t<duration; t += dt)
            {
                IloLinearNumExpr flowIn = c.linearNumExpr();
                IloLinearNumExpr flowOut = c.linearNumExpr();
                for (Vehicle v: V)
                {
                    for (Path p: v.getPaths())
                    {
                        flowIn.addTerm(p.CheckZeta1up(t, l, v),p.getDelta());
                        flowOut.addTerm(p.CheckZeta1down(t, l, v), p.getDelta());
                    }
                }                  
                //Constraint 16c
                range1 = c.addEq(c.sum(l.getNup((t+dt)/dt),c.prod(-1,l.getNup(t/dt)),c.prod(-1,flowIn)),0);
                l.getTENodeUp(t/dt).createRangeL(range1); //mu
                
                //Constraint 16d
                range3 = c.addEq(c.sum(l.getNdown((t+dt)/dt),c.prod(-1,l.getNdown(t/dt)),c.prod(-1,flowOut)),0);
                l.getTENodeDown(t/dt).createRangeN(range3); //lambda dual
                
                //Constraint 16e
                range2 = c.addLe(c.sum(flowOut,c.prod(-1,l.getSending(t/dt))),0);
                l.getTENodeDown(t/dt).createRangeL(range2); //psi
                
                
                //constraint 16f
                range4 = c.addLe(c.sum(flowIn, c.prod(-1,l.getReceiving(t/dt))),0);
                l.getTENodeUp(t/dt).createRangeN(range4);

                //Constraint 16g
                if (t >= l.gettf()-dt && (t + dt - l.gettf()) <= duration)
                {
                    c.addGe(c.sum(l.getNup((t+dt-(int)(l.gettf()))/dt),c.prod(-1,l.getNdown(t/dt))),l.getSending(t/dt));
                }
    
               //Constraint 16h
                c.addLe((c.prod(1,l.getSending(t/dt))),l.getCapacity()*dt/3600);
                 
                //Constraint 16i
                if (t >= (int)(l.getL()/l.getW())-dt && (t+dt - (l.getL()/l.getW())) <= duration)
                {
                    c.addGe(c.sum(c.prod(1,l.getNdown((t+dt-(int)(l.getL()/l.getW()))/dt)),c.prod(-1, l.getNup(t/dt)),c.prod(-1,l.getReceiving(t/dt))), -1*l.getL()*l.getKJam());
                }

                //Constraint 16j
                c.addLe(l.getReceiving(t/dt),l.getCapacity()*dt/3600);

                

            }
        }
    }
    
    public void setSinkConstraints(ArrayList<Vehicle> V, Link[] sink, IloCplex c, int dt, int duration) throws IloException
    {
        IloRange range1, range2;
        for (Link l: sink)
            {
                for (int t=0; t<duration; t += dt)
                {
                    IloLinearNumExpr flowIn = c.linearNumExpr();
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            flowIn.addTerm(p.CheckZeta1up(t, l, v),p.getDelta());
                        }
                    }
                    //Constraint 16c
                    range1 = c.addEq(c.sum(l.getNup((t+dt)/dt),c.prod(-1,l.getNup(t/dt)),c.prod(-1,flowIn)),0);
                    l.getTENodeUp(t/dt).createRangeL(range1);
                    
                    //Constraint 16f
                    range2 = c.addLe(c.sum(flowIn,c.prod(-1,l.getReceiving(t/dt))),0);
                    l.getTENodeUp(t/dt).createRangeN(range2);
                    
                }
            }
    }
    
}
