/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author dreis
 */
public class Vehicle 
{
    private ArrayList<Path> Rp; 
    private Node origin, dest;
    private int time, alpha, id;
    public double rho, assignment;
    public IloRange rangeV;
    private IloNumVar P;
    
    public Vehicle(Node origin, Node dest, int time, double rho, int id)
    {
        this.origin = origin;
        this.dest = dest;
        this.time = time;
        this.rho = rho;
        Rp = new ArrayList<>();
        this.id = id;
    }
    
    public Node getOrigin()
    {
        return origin;
    }
    
    public Node getDest()
    {
        return dest;
    }
    
    public int getTime()
    {
        return time;
    }
    
    public int getId()
    {
        return id;
    }
    
    public void addPath(Path pi)
    {
        Rp.add(pi);
    }
    
    public void updateRho(double d)
    {
        rho = d;
    }
    
    public double getRho()
    {
        return rho;
    }
    
    public ArrayList<Path> getPaths()
    {
        return Rp;
    }  
    
    public void createRangeV(IloRange R)
    {
        rangeV = R;
    }
    
    public IloRange getRangeV()
    {
        return rangeV;
    }
    
    public void CreateP(IloCplex cplex) throws IloException
    {
        P = cplex.numVar(0,1);
    }
    
    public IloNumVar getP()
    {
        return P;
    }
    
    public double getAlpha(int T)
    {
        //return T - time;
        return (1-assignment)*(T - time);
    }
    
    public void assign(double a)
    {
        assignment = a;
    }
    
    public void reassign()
    {
        assignment = 0;
    }
}
