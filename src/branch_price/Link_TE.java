/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

/**
 *
 * @author dreis
 */
public class Link_TE
{
    private Node_TE start, end;
    private double occupancy;
    private double capacity;
    private boolean full;
    
    public Link_TE(Node_TE start, Node_TE end)
    {
        this.start = start;
        this.end = end;
        occupancy = 0;
        capacity = 0;
        full = false;
        
        start.addOutgoingLink(this);
        end.addIncomingLink(this);
    }   
    
    public Node_TE getStart()
    {
        return start;
    } 
    
    public Node_TE getEnd()
    {
        return end;
    }
    
    public double getLinkMu()
    {
        return start.getMu();
    }
    
    public double getLinkPsi()
    {
        return end.getPsi();
    }
    
    public double getNodeLambda()
    {
        return start.getLambda();
    }

    public double getNodeTheta()
    {
        return end.getTheta();
    }
    
    public double getLinkTT()
    {
        return end.getTime() - start.getTime();
    }
    
    public void printLink()
    {
        System.out.println("["+start.getLink()+start.getDirection()+" t="+start.getTime()+","+end.getLink()+end.getDirection()+" t="+end.getTime()+"]");
    }
    
    
    //reservation hueristic
    public void updateOccupancy(double volume)
    {
        occupancy += volume;
    }
    
    public double getOccupancy()
    {
        return occupancy;
    }
    
    public double getCapacity()
    {
        return capacity;
    }
    
    public double getRemainingCapacity()
    {
        return capacity - occupancy;
    }
    
    public void getCapacity(int dt)
    {
        double c1 = start.getLink().getCapacity()*dt/3600;
        double c2 = end.getLink().getCapacity()*dt/3600;
        if (c1 <= c2)
        {
            capacity = c1;
        }
        else
        {
            capacity = c2;
        }
    }
    
    public void checkCapacity()
    {
        if (occupancy+1 > capacity)
        {
            full = true;
        }
    }
    
    public boolean getFull()
    {
        return full;
    }
    
    public void removeLink()
    {
        start.RemoveOutgoingLink(this);
        end.RemoveIncomingLink(this);
    }
    
    public void restoreLink()
    {
        start.addOutgoingLink(this);
        end.addIncomingLink(this);
    }
}
