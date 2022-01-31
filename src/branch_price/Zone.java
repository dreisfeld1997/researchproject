/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import java.util.HashMap;

/**
 *
 * @author dreis
 */
public class Zone extends Node
{
    HashMap<Node, Double> nodeDemands = new HashMap<Node, Double>();
    boolean isthru = true;
    double dem = 0;
    /* **********
    Exercise 4(a)
    ********** */
    public Zone(int id)
    {
        this.id = id;
        super.getId();
    }
    
    
    
    /* **********
    Exercise 4(b)
    ********** */
    public void addDemand(Node s, double d)
    {
        if (nodeDemands.get(s) == null)
        {
            nodeDemands.put(s, d);
        }
        else
        {
            dem = d + nodeDemands.get(s);
            nodeDemands.put(s, dem);
        }
    }
    
    public double getDemand(Node s)
    {
        if (nodeDemands.get(s) == null)
        {
            return 0;
        }
        else 
        {    
            return nodeDemands.get(s);
        }
    }
    
    
    /* **********
    Exercise 4(c)
    ********** */
    public double getProductions()
    {
        double Production = 0;
        
        for (Node i : nodeDemands.keySet())
        {
           Production += nodeDemands.get(i);
        }
        return Production;
    }
    
    
    
    /* **********
    Exercise 4(d)
    ********** */
    public boolean isThruNode()
    {
       return isthru;
    }
    
    public void setThruNode(boolean thru)
    {
        isthru = thru;
    }
}


