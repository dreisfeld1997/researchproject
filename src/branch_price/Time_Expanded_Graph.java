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
                TE_Nodes_Up.add(N);
                dir = "down";
                N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                TE_Nodes_Down.add(N);
            }  
        }
        
        for (Link l: links)
        {
            for (int t = 0; t<T; t++)
            {
                String dir = "up";
                Node_TE N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                TE_Nodes_Up.add(N);
                dir = "down";
                N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                TE_Nodes_Down.add(N);
            }  
        }
        
        for (Link l: sink)
        {
            for (int t = 0; t<T; t++)
            {
                String dir = "up";
                Node_TE N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                TE_Nodes_Up.add(N);
                dir = "down";
                N = new Node_TE(l,dir,t, initC, initC, initC, initC);
                TE_Nodes_Down.add(N);
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
        for (Link_TE Links: TE_AllLinks)
        {
            if (Links.getStart() == i)
            {
                Node_TE j = Links.getEnd();
                if (i.getDirection().equals("up"))
                {
                    if (j.cost > (i.cost + Links.getLinkTT()+Links.getLinkMu()+Links.getLinkPsi()))
                    {
                        j.cost = i.cost + Links.getLinkTT()+Links.getLinkMu()+Links.getLinkPsi();
                        j.predecessor = i;
                    }
                }
                else
                {
//                    System.out.println("Test: ");
//                    j.printNode();
//                    System.out.println(j.cost);
//                    System.out.println();
                    if (j.cost > (i.cost + Links.getLinkTT()- Links.getNodeLinkDual()))
                    {
                        j.cost = i.cost + Links.getLinkTT() - Links.getNodeLinkDual();
                       j.predecessor = i;
                    }
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
        //pi.printPath();
        return pi;
    }
    
    public Link_TE findLink(Node_TE i, Node_TE j)
    {
        for (Link_TE link : TE_AllLinks) 
        {
            if (link.getStart() == i && link.getEnd() == j) 
            {
                return link;
            }
        }
        return null;
    }
    
    public Node_TE findTENode(Node source, Node origin, int deptime)
    {
        //find starting Time Expanded node
        Node_TE r = null;
        for (Node_TE n: TE_Nodes_Up)
        {
            if (source == n.getLink().getStart())
            {
                if (origin.equals(n.getLink().getEnd()))
                {
                    if (deptime == n.getTime())
                    {
                        r = n;
                    }
                }
            }
        }
        if (r == null)
        {
            System.out.println("error");
        }
        return r;
    }
    
        public Node_TE findTENode(Link l, int t, String dir)
    {
        //find starting Time Expanded node
        Node_TE r = null;
        for (Node_TE n: TE_AllNodes)
        {
            if (l == n.getLink())
            {
                if (t == n.getTime())
                {
                    if (dir.equals(n.getDirection()))
                    {
                        r = n;
                    }
                }
            }
        }
        if (r == null)
        {
            System.out.println("error");
        }
        return r;
    }
    
    //finds destination link with lowest cost
    public Node_TE destinationNodeTE(Node dest, Node sink)
    {
        Node_TE finaldest = null;
        double c = Double.MAX_VALUE;
        for (Node_TE n: TE_AllNodes)
        {
            if (dest == n.getLink().getStart() && sink == n.getLink().getEnd())
            {
                if (n.getDirection().equals("down"))
                {
                    if (n.cost < c)
                    {
                        finaldest = n;
                        c = n.cost;
                    }
                }
            }
        }
        return finaldest;
    }
    
    public ArrayList<Link_TE> getOutgoing(Link l, int t)
    {
        ArrayList<Link_TE> Outgoing = new ArrayList<>();
        
        for (Link_TE j: getTENodeLinks())
        {
            if (j.getStart().getLink().equals(l))
            {
                if ((t == j.getStart().getTime()))
                {
                    Outgoing.add(j);
                }
            }
        }
        return Outgoing;
    }
    
    public ArrayList<Link_TE> getIncoming(Link l, int t)
    {
        ArrayList<Link_TE> Incoming = new ArrayList<>();
        
        for (Link_TE j: getTENodeLinks())
        {
            if (j.getEnd().getLink().equals(l))
            {
                if ((t == j.getEnd().getTime()))
                {
                    Incoming.add(j);
                }
            }
        }
        return Incoming;
    }
    
}
