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
    
    public Link_TE(Node_TE start, Node_TE end)
    {
        this.start = start;
        this.end = end;
        
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

    public double getNodeLinkDual()
    {
        return start.getLambda() + end.getTheta();
    }
    
    public double getLinkTT()
    {
        return end.getTime() - start.getTime();
    }
    
    public void printLink()
    {
        System.out.println("["+start.getLink()+start.getDirection()+" t="+start.getTime()+","+end.getLink()+end.getDirection()+" t="+end.getTime()+"]");
    }
}
