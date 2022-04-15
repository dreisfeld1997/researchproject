/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import ilog.concert.IloRange;
import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class Node_TE 
{
    private Link L;
    private String direction;
    private int time;
    private int toplabel;
    private IloRange rangeL, rangeN;
    private double Mu, Psi, Lambda, Theta;
    
    ArrayList<Link_TE> outgoing = new ArrayList<>();
    ArrayList<Link_TE> incoming = new ArrayList<>();
    ArrayList<Link_TE> AllLinkTE = new ArrayList<>();
    
    public Node_TE(){}
    
    public Node_TE(Link L, String direction, int time, double Mu, double Psi, double Lambda, double Theta)
    {
        this.L = L;
        this.direction = direction;
        this.time = time;
        this.Mu = Mu;
        this.Psi = Psi;
        this.Lambda = Lambda;
        this.Theta = Theta;
    }
    
    public Link getLink()
    {
       return L;
    }
    
    public String getDirection()
    {
       return direction;
    }
    
    public int getTime()
    {
       return time;
    }
    
    //Return mu or psi dual variable
    public double getMu()
    {
        return Mu;
    }
    
    public double getPsi()
    {
        return Psi;
    }
    
    public double getLambda()
    {
        return Lambda;
    }
    
    public double getTheta()
    {
        return Theta;
    }
//    public double getLinkcost()
//    {
//       return Lcost;
//    }
    
    public void updateMu(double d)
    {
        Mu = d;
    }
    
    public void updatePsi(double d)
    {
        Psi = d;
    }
    
    public void updateLambda(double d)
    {
        Lambda = d;
    }
    
    public void updateTheta(double d)
    {
        Theta = d;
    }
//    public void updateLinkcost(double d)
//    {
//        Lcost = d;
//    }
   
    
    public void printNode()
    {
        System.out.println("Node: "+L+direction+" t="+time);
    }
    
        //returns all outgoing links from this node
    public ArrayList<Link_TE> getOutgoing()
    {
        return this.outgoing;
    }
    
    //adds link l to the list of outgoing links from this node
    public void addOutgoingLink(Link_TE l)
    {
        this.outgoing.add(l);
    }
    
    //returns all incoming links to this node
    public ArrayList<Link_TE> getIncoming()
    {
        return this.incoming;
    }
    
    //adds link l to the list of incoming links to this node
    public void addIncomingLink(Link_TE l)
    {
        this.incoming.add(l);
    }
    
    //returns all links for this node
    public ArrayList<Link_TE> getAllLinksTE()
    {
        return this.AllLinkTE;
    }
    
    //adds link l to the list of incoming links to this node
    public void addAllTELinks(Link_TE l)
    {
        this.AllLinkTE.add(l);
    }
    
    public void createRangeL(IloRange R)
    {
        rangeL = R;
    }
    
    public IloRange getRangeL()
    {
        return rangeL;
    }
    
    public void createRangeN(IloRange R)
    {
        rangeN = R;
    }
    
    public IloRange getRangeN()
    {
        return rangeN;
    }
    
    //Reservation Huersitic Methods
    //adds link l to the list of outgoing links from this node
    public void RemoveOutgoingLink(Link_TE l)
    {
        int i = this.outgoing.indexOf(l);
        this.outgoing.remove(i);
    }
    
    //adds link l to the list of incoming links to this node
    public void RemoveIncomingLink(Link_TE l)
    {
        int i = this.incoming.indexOf(l);
        this.incoming.remove(i);
    }
    
    protected double cost;
    protected Node_TE predecessor;
}
