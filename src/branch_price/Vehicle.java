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
public class Vehicle 
{
    private ArrayList<Path> Rp;
    private ArrayList<Path> p;
    private Node origin, dest;
    private int time, alpha;
    public double rho;
    public IloRange rangeV;
    
    public Vehicle(Node origin, Node dest, int time, double rho)
    {
        this.origin = origin;
        this.dest = dest;
        this.time = time;
        this.rho = rho;
        Rp = new ArrayList<>();
        p = new ArrayList<>();
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
    
    public void addRestrictedPath(Path pi)
    {
        Rp.add(pi);
    }
    
    public void addPath(Path pi)
    {
        p.add(pi);
    }
    
    public void updateRho(double d)
    {
        rho = d;
    }
    
    public double getRho()
    {
        return rho;
    }
    
    public ArrayList<Path> getRestrictedPaths()
    {
        return Rp;
    }
    
    public ArrayList<Path> getPaths()
    {
        return p;
    }
    
    public int countPaths()
    {
        return p.size();
    }
    
    public void createRangeV(IloRange R)
    {
        rangeV = R;
    }
    
    public IloRange getRangeV()
    {
        return rangeV;
    }
    
    public int getAlpha(int T)
    {
        return T - time;
    }
    
//    public void createAllPaths(Node_TE[] n, Time_Expanded_Graph G, Node_TE start, Node end)
//    {
//        int index = -1;
//        Node_TE d = null;
//        for (int i = 0; i<n.length; i++)
//        {
//            if (n[i] == start)
//            {
//                index = i;
//                break;
//            }
//        }
//        for (int i = index+1; i <n.length; i++)
//        {
//            if (n[i].getLink().getStart() == dest && n[i].getLink().getEnd() == end && n[i].getDirection().equals("down"))
//            {
//                d = n[i];
//                Path pi = new Path();
//                findPath(start, d, pi, n, index, G); 
//            }
//        }
//    }
    
//    public void findPath(Node_TE s, Node_TE d, Path pi, Node_TE[] n, int index, Time_Expanded_Graph G)
//    {
//        if (s == d)
//        {
//            p.add(pi);
//        }
//        else
//        {
//            for (int i = index+1; i<n.length; i++)
//            {
//                if (n[index].direction.equals("up"))
//                {
//                    if (n[index].getLink() == n[i].getLink() && n[i].getDirection().equals("down"))
//                    {
//                        Node_TE A = n[i];
//                        pi.add(G.findLink(s, A));
//                        findPath(A, d, pi, n, i, G);
//                    }
//                }
//                else
//                {
//                    if (n[index].getLink().getEnd() == n[i].getLink().getStart() && n[i].getDirection().equals("up") && n[index].getTime() == n[i].getTime())
//                    {
//                        Node_TE A = n[i];
//                        pi.add(G.findLink(s, A));
//                        findPath(A, d, pi, n, i, G);
//                    }
//                }
//            }
//        }
//    }
    
}
