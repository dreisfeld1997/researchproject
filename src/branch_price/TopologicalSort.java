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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class TopologicalSort 
{

 static class Graph 
 {
    int nodes;
    LinkedList<Node_TE>[] adjList;
    ArrayList<Node_TE> n;

    Graph(ArrayList<Node_TE> n) 
    {
       this.nodes = n.size();
       this.n = n;
       adjList = new LinkedList[nodes];
       for (int i = 0; i < nodes; i++) 
       {
           adjList[i] = new LinkedList<>();
       }
    }

    public void addLink(Node_TE start, Node_TE end) 
    {
       int index1 = n.indexOf(start);
       adjList[index1].addFirst(end);
    }

    public Node_TE[] topologicalSorting()
    {
       boolean[] visited = new boolean[nodes];
       Stack<Integer> stack = new Stack<>();
       //visit from each node if not already visited
       for (int i = 0; i < nodes; i++) 
       {
           if (!visited[i]) 
           {
               topologicalSortUtil(i, visited, stack);
           }
       }
       int size = stack.size();
       Node_TE[] sortNodes = new Node_TE[size];
       for (int i = 0; i <size ; i++)
       {
           int z = stack.pop();
           Node_TE nextNode = n.get(z);
           sortNodes[i] = nextNode;
       }
       return sortNodes;
    }

    public void topologicalSortUtil(int start, boolean[] visited, Stack<Integer> stack) 
    {
       visited[start] = true;
       for (int i = 0; i < adjList[start].size(); i++) 
       {
           int vertex = n.indexOf(adjList[start].get(i));
           if (!visited[vertex])
           {
                topologicalSortUtil(vertex, visited, stack);
           }
       }
       stack.push(start);
    }
 }

}