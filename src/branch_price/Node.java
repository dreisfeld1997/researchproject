/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dreis
 */
public class Node 
{
    private IloIntVar flow;
    public Node(){}
    
    public int id;
    ArrayList<Link> outgoing = new ArrayList<Link>();
    ArrayList<Link> incoming = new ArrayList<Link>();
    
    //Creates a list of Node_Flows objects for this specific node instance. Each Node Flow includes all the
    //paths to flow in and out of this specific node. 
    ArrayList<Node_Flows> y_ij = new ArrayList<Node_Flows>();
    
    
    
    public Node(int id)
    {
        this.id = id;
    }
    
    public int getId()
    {
       int newID = id;
        return newID;
    }

    public boolean isThruNode()
    {
        return true;
    }
    
    public int hashCode()
    {
        return getId();
    }
    
    public String toString()
    {
        String txt = "" + id;
        return txt;
    }
    
    //returns all outgoing links from this node
    public ArrayList<Link> getOutgoing()
    {
        return this.outgoing;
    }
    
    //adds link l to the list of outgoing links from this node
    public void addOutgoingLink(Link l)
    {
        this.outgoing.add(l);
    }
    
    //returns all incoming links to this node
    public ArrayList<Link> getIncoming()
    {
        return this.incoming;
    }
    
    //adds link l to the list of incoming links to this node
    public void addIncomingLink(Link l)
    {
        this.incoming.add(l);
    }
    
    //Creates node_flows object for this node
    public void createFlows(Zone[] zones, int T, IloCplex cplex) throws IloException
    {
        System.out.println(this.getId()+" Incoming "+this.getIncoming());
        System.out.println(this.getId()+" Outgoing "+this.getOutgoing());
        for (Link A: this.getIncoming())
        {

            for (Link B: this.getOutgoing())
            {
                for (Zone z: zones)
                {
                    for (int t = 0; t<T; t++)
                    {
                        flow = cplex.intVar(0,Integer.MAX_VALUE);
                        Node_Flows f = new Node_Flows(A,B,z,flow,t);
                        this.y_ij.add(f);
                    }
                }
            }
        }
    }
    //This method returns an array of all the flows that go through this node and end on
    //Link L with a destination of zone Z at time t
    public IloLinearNumExpr getFlowin(Link L, Zone Z, int t, IloCplex cplex) throws IloException
    {
        IloLinearNumExpr s = cplex.linearNumExpr();
        for (int i = 0; i<y_ij.size(); i++)
        {
            if (L == y_ij.get(i).getOutgoing())
            {
                if (Z == y_ij.get(i).getdestination())
                {
                    if (t == y_ij.get(i).getTime())
                    {
                        s.addTerm(1, y_ij.get(i).getFlow());
                    }
                }
            }
        }
        return s;
    }
    
        //This method returns an array of all the flows that start on link L and go through this node 
    //with a destination of zone Z at time t
    public IloLinearNumExpr getFlowout(Link L, Zone Z, int t, IloCplex cplex) throws IloException
    {
        IloLinearNumExpr s = cplex.linearNumExpr();
        for (int i = 0; i<y_ij.size(); i++)
        {
            if (L == y_ij.get(i).getincoming())
            {
                if (Z == y_ij.get(i).getdestination())
                {
                    if (t == (y_ij.get(i).getTime()))
                    {
                      s.addTerm(1, y_ij.get(i).getFlow());
                    }
                }
            }
        }
       
        return s;
    }
    
    //Returns a specific flow for this node at time t with destination Z
    public IloIntVar getFlow(Zone Z, int t)
    {
        for (int i = 0; i<y_ij.size(); i++)
        { 
            if (Z == y_ij.get(i).getdestination())
            {
                return y_ij.get(i).getFlow();
            }
        }
        return null;
    }
    
  
    
    protected double cost;
    protected Node predecessor;
}
