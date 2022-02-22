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
        IloRange range;
        for (Vehicle v: V)
        {
            IloLinearNumExpr const1 = c.linearNumExpr();
            for (Path p: v.getPaths())
            {
                const1.addTerm(1,p.getDelta());
            }
            range = c.addLe(const1, 1);
            v.createRangeV(range);
        }
    }
    
    public void setSourceConstraints(ArrayList<Vehicle> V, Link[] source, IloCplex c, int T) throws IloException
    {
        IloRange range1, range2;
        for (Link l: source)
        {
            for (int t=0; t<T-1; t++)
            {
                IloLinearNumExpr A = c.linearNumExpr();
                IloLinearNumExpr constDem = c.linearNumExpr();
                IloLinearNumExpr B = c.linearNumExpr();

                for (Vehicle v: V)
                {
                    for (Path p: v.getPaths())
                    {
                        constDem.addTerm(p.CheckZeta1up(t, l),p.getDelta());
                        A.addTerm(p.CheckZeta1down(t, l),p.getDelta());
                    }
                }

                //Demand Loading Constraint
                c.addEq(c.sum(l.getNup(t+1),c.prod(-1,l.getNup(t)),c.prod(-1,constDem)),0);

                //Constraint 16e
                range1 = c.addLe(c.sum(A,c.prod(-1,l.getSending(t))),0);

                if (t>0)
                {
                    l.getTENodeDown(t).createRangeL(range1); //psi dual
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
                            B.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                        }
                    }
                }
                //Constraint 16d
                range2 = c.addEq(c.sum(l.getNdown(t+1),c.prod(-1,l.getNdown(t)),c.prod(-1,B)),0);
                if (t>0)
                {
                   l.getTENodeDown(t).createRangeN(range2); //lambda dual
                }  
            }
        }
    }
    
    public void setLinkConstraints(ArrayList<Vehicle> V, Link[] links, IloCplex c, int T, Network network) throws IloException
    {
        IloRange range1, range2, range3, range4;
        for (Link l: links)
        {
            for (int t=0; t<T-1; t++)
            {
                IloLinearNumExpr A = c.linearNumExpr();
                IloLinearNumExpr B = c.linearNumExpr();
                for (Vehicle v: V)
                {
                    for (Path p: v.getPaths())
                    {
                        A.addTerm(p.CheckZeta1up(t, l),p.getDelta());
                        B.addTerm(p.CheckZeta1down(t, l),p.getDelta());
                    }
                }                  
                //Constraint 16c
                range1 = c.addEq(c.sum(l.getNup(t+1),c.prod(-1,l.getNup(t)),c.prod(-1,A)),0);

                //Constraint 16e
                range2 = c.addLe(c.sum(B,c.prod(-1,l.getSending(t))),0);
                if (t > 0)
                {
                    l.getTENodeUp(t).createRangeL(range1); //mu
                    l.getTENodeDown(t).createRangeL(range2); //psi
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
                c.addLe(l.getReceiving(t),l.getCapacity());

                //new constraints
                IloLinearNumExpr C = c.linearNumExpr();
                for (Link_TE j: l.getTENodeDown(t).getOutgoing())
                {
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            C.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                        }
                    }
                }
                //Constraint 16d
                range3 = c.addEq(c.sum(l.getNdown(t+1),c.prod(-1,l.getNdown(t)),c.prod(-1,C)),0);
                if (t>0)
                {
                    l.getTENodeDown(t).createRangeN(range3);
                }
                for (Link_TE j: l.getTENodeUp(t).getIncoming())
                {
                    IloLinearNumExpr D = c.linearNumExpr();
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            D.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                        }
                    }
                    //constraint 16f
                    range4 = c.addLe(c.sum(D, c.prod(-1,l.getReceiving(t))),0);
                    if (t>0)
                    {
                        l.getTENodeUp(t).createRangeN(range4);
                    }   
                } 
            }
        }
    }
    
    public void setSinkConstraints(ArrayList<Vehicle> V, Link[] sink, IloCplex c, int T) throws IloException
    {
        IloRange range1, range2;
        for (Link l: sink)
            {
                for (int t=0; t<T-1; t++)
                {
                    IloLinearNumExpr A = c.linearNumExpr();
                    //IloLinearNumExpr B = c.linearNumExpr();
                    for (Vehicle v: V)
                    {
                        for (Path p: v.getPaths())
                        {
                            A.addTerm(p.CheckZeta1up(t, l),p.getDelta());
                        }
                    }
                    //Constraint 16c
                    range1 = c.addEq(c.sum(l.getNup(t+1),c.prod(-1,l.getNup(t)),c.prod(-1,A)),0);
                    
                    if (t>0)
                    {
                        l.getTENodeUp(t).createRangeL(range1);
                    }
                    
                    for (Link_TE j: l.getTENodeUp(t).getIncoming())
                    {
                        IloLinearNumExpr B = c.linearNumExpr();
                        for (Vehicle v: V)
                        {
                            for (Path p: v.getPaths())
                            {
                                B.addTerm(p.CheckZeta2(t,l,j.getEnd().getLink()),p.getDelta());
                            }
                        }
                        //Constraint 16f
                        range2 = c.addLe(c.sum(B,c.prod(-1,l.getReceiving(t))),0);
                        if (t>0)
                        {
                            l.getTENodeUp(t).createRangeN(range2);
                        }
                    }
                }
            }
    }
    
}
