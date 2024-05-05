package searchclient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class CBSFrontier
{
    private PriorityQueue<CBSNode> queue;
    private HashSet<ArrayList<Solution>> set = new HashSet<>(65536);
                                  
    public CBSFrontier()
    {
        this.queue = new PriorityQueue<>(65536, Comparator.comparingInt(CBSNode::getCost));
    }

    public void add(CBSNode node)
    {
        queue.add(node);
        set.add(node.getSolutions());
    }

    public CBSNode pop()
    {
        return this.queue.poll();
    }

    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    public int size()
    {
        return this.queue.size();
    }

    public boolean contains(ArrayList<Action [][]> solutions)
    {
        return this.set.contains(solutions);
    }

    public String getName()
    {
        return String.format("CBS Frontier");
    }
}
