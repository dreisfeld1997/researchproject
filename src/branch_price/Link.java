/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloModeler;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class Link 
{
   public Link(){}
    // the flow on this link
    private double x;
    
    // parameters for travel time calculation. t_ff is the free flow time, C is the capacity, alpha and beta are the calibration parameters in the BPR function
    private double t_ff, C, alpha, beta;
    
    // the start and end nodes of this link. Links are directed.
    private Node start, end;
    
    // parameters for the LTM model. -w is the speed at congested flow, kjam is the jam density, L is the length of the link, and uf is the free flow speed.
    //private double w, kjam, uf;
    private double L;
    
    private IloNumVar [] N_up;
    private IloNumVar[] N_down;
    private IloNumVar[] S_i;
    private IloNumVar[] R_i;
    
    ArrayList<Node_TE> TE_Nodes_up = new ArrayList<>();
    ArrayList<Node_TE> TE_Nodes_down = new ArrayList<>();
    
    
    // construct this Link with the given parameters
    public Link(Node start, Node end, double t_ff, double C, double alpha, double beta, double L)
    {
        this.start = start;
        this.end = end;
        this.t_ff = t_ff;
        this.C = C;
        this.alpha = alpha;
        this.beta = beta;
        this.L = L;
        
        start.addOutgoingLink(this);
        end.addIncomingLink(this);
        
    }
    
    public double getTravelTime()
    {
        // fill this in
        double t_ij = t_ff*(1+alpha*Math.pow((x/C),beta));
        
        return t_ij;
    }
    
    //returns w
    public double getW()
    {
        return getuf()/2;
    }
    
    //returns jam density
    public double getKJam()
    {
        return 240;
    }
    
    //returns free flow speed
    public double getuf()
    {
        return L/t_ff;
    }
    
    public double gettf()
    {
        return t_ff;
    }
    
    //returns length
    public double getL()
    {
        return L;
    }
    
    //returns Capacity (Qmax)
    public double getCapacity()
    {
        double Cap = C;
        return Cap;
    }
    
    //returns Start node of link
    public Node getStart()
    {
        Node S = start;
        return S;
    }
    
    //returns End node of link
    public Node getEnd()
    {
        Node E = end;
        return E;
    }
    
    
    public String toString()
    {
        String links = "(" + start + ", " + end + ")";
        return links;
    }
    
    //Creates Nup array for each link
    public  void createNup(int t, IloCplex cplex) throws IloException
    {
        N_up = new IloNumVar [t];
        for (int b = 0; b<t; b++)
        {
            if (b == 0)
            {
                N_up[b] = cplex.numVar(0,0);
            }
            else
            {
                N_up[b] = cplex.numVar(0,Integer.MAX_VALUE);
            }
        }
    }
    
    //Creates Ndown array for each link
    public  void createNdown(int t, IloCplex cplex) throws IloException
    {
        N_down = new IloNumVar[t];
        for (int b = 0; b<t; b++)
        {
            if (b == 0)
            {
                N_down[b] = cplex.numVar(0,0);
            }
            else
            {
                N_down[b] = cplex.numVar(0,Integer.MAX_VALUE);
            }
        }
    }
    
    
    //returns specific Nup value
    public IloNumVar getNup(int t)
    {
        //z-1 avoids index out of bounds error (Destination of zone 1 = [0], zone 2 = [1], etc...)
        return N_up[t];
    }
    
    //returns specific Ndown value
    public IloNumVar getNdown(int t)
    {
        //z-1 avoids index out of bounds error (Destination of zone 1 = [0], zone 2 = [1], etc...)
        return N_down[t];
    }
    
    //Creates array of Receiving flows for each link
    public void createReceiving(int t, IloCplex cplex) throws IloException
    {
        R_i = new IloNumVar[t];
        for (int a = 0; a <t; a++)
        {
            R_i[a] = cplex.numVar(0,Integer.MAX_VALUE);
        }
    }
    
    //Creates array of Sending flows for each link
    public void createSending( int t, IloCplex cplex) throws IloException
    {
        S_i = new IloNumVar[t];
        for (int b = 0; b<t; b++)
        {
            if (b == 0)
            {
                S_i[b] = cplex.numVar(0,0);
            }
            else
            {   
            S_i[b] = cplex.numVar(0,Integer.MAX_VALUE);
            }
        }
    }
    
    //Returns Receiving flow for this link at time t
    public IloNumVar getReceiving(int t)
    {
        return R_i[t];
    }
    
    //Returns Sending flow for this link at time t, for destination z
    public IloNumVar getSending(int t)
    {
        //z-1 avoids index out of bounds error (Destination of zone 1 = [0], zone 2 = [1], etc...)
        return S_i[t];
    }
    
    public void addTENodeup(Node_TE n)
    {
        TE_Nodes_up.add(n);
    }
    
    public void addTENodedown(Node_TE n)
    {
        TE_Nodes_down.add(n);
    }
    
    public ArrayList<Node_TE> getAllTENodesUp()
    {
        return TE_Nodes_up;
    }
    
    public ArrayList<Node_TE> getAllTENodesDown()
    {
        return TE_Nodes_down;
    }
    
    public Node_TE getTENodeUp(int t)
    {
        return TE_Nodes_up.get(t);
    }
    
    public Node_TE getTENodeDown(int t)
    {
        return TE_Nodes_down.get(t);
    }
}

    

    

