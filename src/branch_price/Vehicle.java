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
    private int time, alpha, id, quantity;
    public double rho, eta, assignment;
    public IloRange rangeV, rangeP;
    private IloNumVar P;
    
    public Vehicle(Node origin, Node dest, int time, double rho, double eta, int quantity)
    {
        this.origin = origin;
        this.dest = dest;
        this.time = time;
        this.rho = rho;
        this.eta = eta;
        Rp = new ArrayList<>();
        //this.id = id;
        this.quantity = quantity;
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
    
    public int getQuantity()
    {
        return quantity;
    }
    
    public void addPath(Path pi)
    {
        Rp.add(pi);
    }
        
    public ArrayList<Path> getPaths()
    {
        return Rp;
    }  
    
    //update dual variables
    public void updateRho(double d)
    {
        rho = d;
    }
    
    public void updateEta(double d)
    {
        eta = d;
    }
    
    //get dual variables
    public double getRho()
    {
        return rho;
    }
    
    public double getEta()
    {
        return eta;
    }
    
    //create ranges for variables
    public void createRangeV(IloRange R)
    {
        rangeV = R;
    }
    
        public void createRangeP(IloRange R)
    {
        rangeP = R;
    }
        
    //get ranges
    public IloRange getRangeV()
    {
        return rangeV;
    }
    
    public IloRange getRangeP()
    {
        return rangeP;
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
        return T - time;
    }
    
    public void printVehicle()
    {
        System.out.println("Vehicle     Origin: "+origin+" Destination: "+dest+" Time: "+time+" Quantity: "+quantity);
    }
}
