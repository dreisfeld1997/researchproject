/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class Time_Expanded_Graph 
{
    ArrayList<Node_TE> TE_Nodes_Up = new ArrayList<>();
    ArrayList<Node_TE> TE_Nodes_Down = new ArrayList<>();
    ArrayList<Node_TE> TE_AllNodes = new ArrayList<>();
    ArrayList<Link_TE> TE_Links = new ArrayList<>();
    ArrayList<Link_TE> TE_NodeLinks = new ArrayList<>();
    ArrayList<Link_TE> TE_AllLinks = new ArrayList<>();
    
    public Time_Expanded_Graph(Link[] source, Link[] sink, Link[] links, int T)
    {
        
        //Create Time Expanded Nodes
        double initC = 0;
        for (Link l: source)
        {
            for (int t = 0; t<T; t++)
            {
                String dir = "up";
                Node_TE N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                //Store Node_TE
                TE_Nodes_Up.add(N);
                l.TE_Nodes_up.add(N);
                
                dir = "down";
                N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                //Store Node_TE
                TE_Nodes_Down.add(N);
                l.TE_Nodes_down.add(N);
            }  
        }
        
        for (Link l: links)
        {
            for (int t = 0; t<T; t++)
            {
                String dir = "up";
                Node_TE N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                //Store Node_TE
                TE_Nodes_Up.add(N);
                l.TE_Nodes_up.add(N);
                
                dir = "down";
                N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                //Store Node_TE
                TE_Nodes_Down.add(N);
                l.TE_Nodes_down.add(N);
            }  
        }
        
        for (Link l: sink)
        {
            for (int t = 0; t<T; t++)
            {
                String dir = "up";
                Node_TE N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                //Store Node_TE
                TE_Nodes_Up.add(N);
                l.TE_Nodes_up.add(N);
                
                dir = "down";
                N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                //Store Node_TE
                TE_Nodes_Down.add(N);
                l.TE_Nodes_down.add(N);
            }  
        }
        
        //Create Time Expanded Links
        for (Node_TE m: TE_Nodes_Up)
        {
            for (Node_TE n: TE_Nodes_Down)
            {
                if (m.getLink().equals(n.getLink()))
                {
                    Link l = m.getLink();
                    int minTT = (int)Math.ceil(l.getL()/l.getuf());
                    if (n.getTime() - m.getTime() >= minTT)
                    {
                        Link_TE A = new Link_TE(m,n);
                        TE_Links.add(A);
                        m.addAllTELinks(A);
                    }
                }
            }
        }
        for (Node_TE m: TE_Nodes_Down)
        {
            int time = m.getTime();
            Link l = m.getLink();
            Node end = l.getEnd();
            for (Node_TE n: TE_Nodes_Up)
            {
                if (n.getTime() == time)
                {
                    if (end.getOutgoing().contains(n.getLink()))
                    {
                        Link_TE A = new Link_TE(m,n);
                        TE_NodeLinks.add(A);
                        m.addAllTELinks(A);
                    }
                }
            }
        }
    }
    
    public ArrayList<Link_TE> getTELinks()
    {
        return TE_Links;
    }
    
    public ArrayList<Link_TE> getTENodeLinks()
    {
        return TE_NodeLinks;
    }
    
    public void combineAllNodes()
    {
        for (int i = 0; i<TE_Nodes_Up.size(); i++)
        {
            TE_AllNodes.add(TE_Nodes_Up.get(i));
            TE_AllNodes.add(TE_Nodes_Down.get(i));
        }
    }
    public ArrayList<Node_TE> getAllNodes()
    {
        return TE_AllNodes;
    }
    
    public void combineAllLinks()
    {
        for (int i = 0; i<TE_Links.size(); i++)
        {
            TE_AllLinks.add(TE_Links.get(i));
        }
        for (int i = 0; i<TE_NodeLinks.size(); i++)
        {
            TE_AllLinks.add(TE_NodeLinks.get(i));
        }
    }
    public ArrayList<Link_TE> getAllLinks()
    {
        return TE_AllLinks;
    }
    
    public void relax(Node_TE i)
    {
//        System.out.println();
//        System.out.print("Node: ");
//        i.printNode();
        for (Link_TE Links: i.getOutgoing())
        {
            Node_TE j = Links.getEnd();
            if (i.getDirection().equals("up"))
            {
                if (j.cost > (i.cost + Links.getLinkTT()+Links.getLinkMu() - Links.getLinkPsi()))
                {
                    j.cost = i.cost + Links.getLinkTT()+Links.getLinkMu() - Links.getLinkPsi();
                    j.predecessor = i;
//                    System.out.print("Successor: ");
//                    j.printNode();
                }
            }
            else
            {
                if (j.cost > (i.cost + Links.getLinkTT() - Links.getNodeTheta() - Links.getNodeLambda()))
                {
                    j.cost = i.cost + Links.getLinkTT() - Links.getNodeTheta() - Links.getNodeLambda();
                    j.predecessor = i;
//                    System.out.print("Successor: ");
//                    j.printNode();
                }
            }
        }
        
    }
    
    public Path trace(Node_TE r, Node_TE s)
    {
        Path pi = new Path();
        Node_TE n = s;
        Node_TE pn = s.predecessor;

        while (n != r)
        {
            pi.add(0, findLink(pn, n));
            n = pn;
            pn = n.predecessor;
        }
        return pi;
    }
    
    public Link_TE findLink(Node_TE i, Node_TE j)
    {
        for (Link_TE link : i.getAllLinksTE()) 
        {
            if (link.getEnd() == j) 
            {
                return link;
            }
        }
        return null;
    }
    
    //finds destination link with lowest cost
    public Node_TE destinationNodeTE(Link L)
    {
        Node_TE finaldest = null;
        double c = Double.MAX_VALUE;
        for (Node_TE n: L.getAllTENodesDown())
        {
            if (n.cost < c)
            {
                finaldest = n;
                c = n.cost;
            }
        }
        return finaldest;
    }
    
    
}
