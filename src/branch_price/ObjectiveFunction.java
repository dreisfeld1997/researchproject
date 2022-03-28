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
    private IloLinearNumExpr obj1, obj2;
    public ObjectiveFunction()
    { 
    }
    
    public void CreateObjective(ArrayList<Vehicle> V, IloCplex c, int duration) throws IloException
    {
            obj1 = c.linearNumExpr();
            obj2 = c.linearNumExpr();
            
            for (Vehicle v: V)
            {
                for (Path p: v.getPaths())
                {
                    double tt = v.getQuantity()*p.getPathTravelTime();
                    obj1.addTerm(tt,p.getDelta());
                }
                
                double alpha = v.getAlpha(duration)*v.getQuantity();
                obj2.addTerm(v.getP(),alpha);
            }
            c.addMinimize(c.sum(obj1,obj2));
    }
    
    public void PrintObjective(IloCplex c) throws IloException
    {
        System.out.print("Objective: ");
        System.out.println(c.getValue(c.sum(obj1,obj2)));
        //System.out.println(c.getValue(obj1));
        //System.out.println(c.getValue(obj3));
    }
    
    public double getObjective(IloCplex c) throws IloException
    {
        return c.getValue(c.sum(obj1,obj2));
    }
}
