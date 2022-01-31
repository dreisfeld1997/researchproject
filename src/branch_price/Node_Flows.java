/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloIntVar;

/**
 *
 * @author dreis
 */
public class Node_Flows 
{
    private Link Incoming, Outgoing;
    private Zone Dest;
    //private IloIntVar[] flow;
    private IloIntVar flow;
    private int time;
    
    public Node_Flows(Link Incoming, Link Outgoing, Zone Dest, IloIntVar flow, int time)
    {
        this.Incoming = Incoming;
        this.Outgoing = Outgoing;
        this.Dest = Dest;
        this.flow = flow;
        this.time = time;
    }
    
    public Link getincoming()
    {
        return Incoming;
    }
    public Link getOutgoing()
    {
        return Outgoing;
    }
    public Zone getdestination()
    {
        return Dest;
    }
    public IloIntVar getFlow()
    {
        return flow;
    }    
    
    public int getTime()
    {
        return time;
    } 
    
}

