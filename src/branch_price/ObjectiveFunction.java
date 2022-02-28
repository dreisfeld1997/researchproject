/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class ObjectiveFunction 
{
    private IloLinearNumExpr obj1;
    private IloNumExpr obj3;
    public ObjectiveFunction()
    { 
    }
    
    public void CreateObjective(ArrayList<Vehicle> V, IloCplex c, int T) throws IloException
    {
            obj1 = c.linearNumExpr();
            obj3 = null;
            
            for (Vehicle v: V)
            {
                IloLinearNumExpr obj2 = c.linearNumExpr();
                int count = 0;
                for (Path p: v.getPaths())
                {
                    count++;
                    obj1.addTerm(p.getPathTravelTime(),p.getDelta());
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
            c.addMinimize(c.sum(obj1,obj3));
    }
    
    public void PrintObjective(IloCplex c) throws IloException
    {
        System.out.print("Objective: ");
        System.out.println(c.getValue(c.sum(obj1,obj3)));
        //System.out.println(c.getValue(obj1));
        //System.out.println(c.getValue(obj3));
    }
    
    public double getObjective(IloCplex c) throws IloException
    {
        return c.getValue(c.sum(obj1,obj3));
    }
}
