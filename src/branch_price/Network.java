/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package branch_price;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.ArrayList;

/**
 *
 * @author dreis
 */
public class Network 
{
    private Node[] nodes;
    private Link[] links;
    private Zone[] zones;
    private Link[] source;
    private Link[] sink;
    private int numnodes;
    private int numlinks;
    private int numzones;
    private int firstThru;
    private int origin;
    private ArrayList <Integer> sourceNodes;
    private ArrayList <Integer> sinkNodes;
          
    
    public Network(String name)
    {
        try 
        {
            //File myObj = new File("data/"+name+"/net.txt");
            File myObj = new File("data/Tests/"+name+"/net.txt");
            Scanner myReader = new Scanner(myObj);
            String data;
            int i = 0;
            while (i == 0) 
            {
                data = myReader.nextLine();
                if (data.contains("<NUMBER OF NODES>"))
                {
                    data = data.substring(data.indexOf("<NUMBER OF NODES>")+17);
                    data = data.trim();
                    numnodes = Integer.parseInt(data);
                    //add 2 for dummy nodes
                    numnodes = numnodes + 2;
                }
                if (data.contains("<NUMBER OF LINKS>"))
                {
                    data = data.substring(data.indexOf("<NUMBER OF LINKS>")+17);
                    data = data.trim();
                    numlinks = Integer.parseInt(data);
                }
                if (data.contains("<NUMBER OF ZONES>"))
                {
                    data = data.substring(data.indexOf("<NUMBER OF ZONES>")+17);
                    data = data.trim();
                    numzones = Integer.parseInt(data);
                }
                if (data.contains("<FIRST THRU NODE>"))
                {
                    data = data.substring(data.indexOf("<FIRST THRU NODE>")+17);
                    data = data.trim();
                    firstThru = Integer.parseInt(data);
                }
                if (data.contains("<END OF METADATA>"))
                {
                    i++;
                }
            }
            myReader.close();
        } 
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        nodes = new Node[numnodes];
        links = new Link[numlinks];
        zones = new Zone[numzones];
        
        sourceNodes = new ArrayList<>();
        sinkNodes = new ArrayList<>();

        int[][] OD = null;
        try
        {
            OD = createSourceSink(new File("data/Tests/"+name+"/trips.txt"));
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        for (int r = 0; r<numzones; r++)
        {
            int sum = 0;
            for (int c = 0; c<numzones; c++)
            {
                sum += OD[r][c];
            }
            if (sum > 0)
            {
                sourceNodes.add(r+1);
            }
        }
        
        for (int c = 0; c<numzones; c++)
        {
            int sum = 0;
            for (int r = 0; r<numzones; r++)
            {
                sum += OD[r][c];
            }
            if (sum > 0)
            {
                sinkNodes.add(c+1);
            }
        }
        //All sources and sinks (doesnt work)
        source = new Link[sourceNodes.size()];
        sink = new Link[sinkNodes.size()];

        //1 source and 1 sink
//        source = new Link[1];
//        sink = new Link [1];

        
        try
        {
            readNetwork(new File("data/Tests/"+name+"/net.txt"));
            readTrips(new File("data/Tests/"+name+"/trips.txt"));
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    
    public Network(Node[] nodes, Link[] links)
    {
        this.nodes = nodes;
        this.links = links;
    }
    
    
    public Link[] getLinks()
    {
        return links;
    }
    
    public Node[] getNodes()
    {
        return nodes;
    }
    
    public Zone[] getZones()
    {
        return zones;
    }
    
    public Link[] getSources()
    {
        return source;
    }
    
    public Link[] getSinks()
    {
        return sink;
    }
    
    
    
    
    public void readNetwork(File netFile) throws IOException
    {      
        for (int x = 0; x<numzones; x++)
        {
            zones[x] = new Zone(x+1); 
            if (x < firstThru-1)
            {
                zones[x].setThruNode(false);
            }
        }
        //source node
        nodes[0] = new Node(0);
        for (int y = 1; y<numnodes;y++)
        {
            if (y-1 < numzones)
            {
                nodes[y] = zones[y-1];
            }
            else
            {
                //sink node
                nodes[y] = new Node(y);
            }
        }
        
        
        //Create Source Nodes
        for (int i = 0; i<sourceNodes.size(); i++) 
        {
            source[i] = new Link(nodes[0], zones[sourceNodes.get(i)-1], 3600, 100000000, 1, 1, 1);
        }
        //Create Sinks Nodes
        for (int i = 0; i<sinkNodes.size(); i++) 
        {
            sink[i] = new Link(zones[sinkNodes.get(i)-1], nodes[numnodes-1], 3600, 100000000, 1, 1, 1);
        }

        //1 source and 1 sink
//        source[0] = new Link(nodes[0], zones[0], 1, 100000000, 1, 1, 1);
//        sink[0] = new Link(zones[numzones-1], nodes[numnodes-1], 1, 100000000, 1, 1, 1);
        

        
        Scanner sc = new Scanner(netFile);
        while (sc.hasNext())
        {
            String line = sc.nextLine();
            //cycle through until reaching link data
            while (!line.contains("~"))
            {
               if (sc.hasNextLine() == false)
               {
                   break;
               }
               else
               {
                   line = sc.nextLine();
               }
            }
            // Stores all network data for each link as an array. Then assigns variables from data
            // needed to make Link. Then creates the link
            
            //Creates array for data
            for (int z=0; z<numlinks; z++ )
            {
                //System.out.println("Line: "+line);
                String[] linkdata = new String[11];
                for (int i=0; i<linkdata.length; i++)
                {
                    linkdata[i] = sc.next(); 
                }

                //Assign Variables n1,n2, t_ff, c, alpha, beta
                System.out.println();
                int nodeStartInt = Integer.parseInt(linkdata[0]);
                int nodeEndInt = Integer.parseInt(linkdata[1]);
                Node n1 = nodes[nodeStartInt];
                Node n2 = nodes[nodeEndInt];
                double length = Double.parseDouble(linkdata[3]);
                double t_ff = Double.parseDouble(linkdata[4]);
                double c = Double.parseDouble(linkdata[2]);
                double alpha = Double.parseDouble(linkdata[5]);
                double beta = Double.parseDouble(linkdata[6]);
                
                //Create link
                links [z] = new Link(n1,n2,t_ff,c,alpha,beta,length);
            }     
        }
        sc.close();
       
        
    }
    
    public void readTrips(File tripsFile) throws IOException
    {
        Scanner sc = new Scanner(tripsFile); 
        String line = sc.next();
        String dest;
        while (!line.contains("Origin"))
        {
           line = sc.next();
        }
        
        //counter variable, this variable makes sure we do not skip a sc.next()
        //so that the first iteration of the while loop (line.contains("Origin")) returns true
        int count = 0; 
        
        //loops through trip data
        while (sc.hasNext())
        {
            //If loop will always generate new line value, except for the first iteration
            if (count > 0)
            {
                line = sc.next();
            }
            //Check if Origin has been reached
            if (line.contains("Origin"))
            {
                //convert from string to integer
                String org = sc.next();
                origin = Integer.parseInt(org);
                //if Origin is created the next value the data reads will be a trip destination
                dest = sc.next();
            }
            else
            {
                //if Origin is not found, the next value the data reads will be a destination
                dest = line;
            }
            //Creates a node variable for the destination
            int destination = Integer.parseInt(dest);
            Node ndest = zones[destination-1];
            //Read next value of data, which will be demand
            String dem = sc.next();
            //Skips semi colon
            if (dem.contains(":"))
            {
                dem = sc.next();
            }
            //Turns demand into a double variable
            dem = dem.replace(";","");
            double demand = Double.parseDouble(dem);
          //System.out.println("Origin: "+ origin + " destination: " +ndest + " Demand: "+demand);

            //Adds demand for specific zone
            zones[origin-1].addDemand(ndest, demand); 
            count++;
        }
        sc.close();
    }
    
    public int[][] createSourceSink(File tripsFile) throws IOException
    {
        Scanner sc = new Scanner(tripsFile); 
        String line = sc.next();
        String dest;
        
        int[][] OD = new int[numzones][numzones];

        while (!line.contains("Origin"))
        {
           line = sc.next();
        }
        
        //counter variable, this variable makes sure we do not skip a sc.next()
        //so that the first iteration of the while loop (line.contains("Origin")) returns true
        int count = 0; 
        
        //loops through trip data
        while (sc.hasNext())
        {
            //If loop will always generate new line value, except for the first iteration
            if (count > 0)
            {
                line = sc.next();
            }
            //Check if Origin has been reached
            if (line.contains("Origin"))
            {
                //convert from string to integer
                String org = sc.next();
                origin = Integer.parseInt(org);
                //if Origin is created the next value the data reads will be a trip destination
                dest = sc.next();
            }
            else
            {
                //if Origin is not found, the next value the data reads will be a destination
                dest = line;
            }
            //Creates a node variable for the destination
            int destination = Integer.parseInt(dest);
            Node ndest = zones[destination-1];
            //Read next value of data, which will be demand
            String dem = sc.next();
            //Skips semi colon
            if (dem.contains(":"))
            {
                dem = sc.next();
            }
            //Turns demand into a double variable
            dem = dem.replace(";","");
            double demand = Double.parseDouble(dem);
            
            //update OD matrix if flow exists
            if (demand > 0)
            {
                OD[origin-1][destination-1] = 1;
            }
            else
            {
                OD[origin-1][destination-1] = 0;
            }

            count++;
        }
        sc.close();
        return OD;
    }
    

    public Node findNode(int id)
    {
        for (Node node : nodes) 
        {
            if (node.getId() == id)
            {
                return node;
            }
        }
        return null;
    }
    
    public Link findLink(Node i, Node j)
    {
        for (Link link : links) 
        {
            if (link.getStart() == i && link.getEnd() == j) 
            {
                return link;
            }
        }
        for (Link link : source) 
        {
            if (link.getStart() == i && link.getEnd() == j) 
            {
                return link;
            }
        }
        for (Link link : sink) 
        {
            if (link.getStart() == i && link.getEnd() == j) 
            {
                return link;
            }
        }
        return null;
    }
    
    
    

}

