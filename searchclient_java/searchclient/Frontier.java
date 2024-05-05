package searchclient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface Frontier
{
    void add(State state, int timeStep);
    State pop();
    boolean isEmpty();
    int size();
    boolean contains(State state);
    String getName();
    int getTimeForState(State state);
}

class FrontierBFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);
    private final HashMap<State, Integer> stateTime = new HashMap<>(65536);
    
    @Override
    public int getTimeForState(State state) {
        return stateTime.get(state);
    }

    @Override
    public void add(State state, int timeStep)
    {
        this.queue.addLast(state);
        this.set.add(state);        
        this.stateTime.put(state, timeStep);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "breadth-first search";
    }
}

class FrontierDFS
        implements Frontier
{
    private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);
    private final HashMap<State, Integer> stateTime = new HashMap<>(65536);

    @Override
    public int getTimeForState(State state) {
        return stateTime.get(state);
    }

    @Override
    public void add(State state, int timeStep)
    {
        this.queue.addFirst(state);
        this.set.add(state);
        this.stateTime.put(state, timeStep);
    }

    @Override
    public State pop()
    {
        State state = this.queue.pollFirst();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return "depth-first search";
    }
}

class FrontierBestFirst
        implements Frontier
{
    private final List<State> queue = new ArrayList<>(65536);
    private final HashSet<State> set = new HashSet<>(65536);
    private Heuristic heuristic;
    private final HashMap<State, Integer> stateTime = new HashMap<>(65536);

    @Override
    public int getTimeForState(State state) {
        return stateTime.get(state);
    }

    public FrontierBestFirst(Heuristic h)
    {
        this.heuristic = h;
        // System.out.println("Heuristic: " + this.heuristic.choice);
    }

    @Override
    public void add(State state, int timeStep)
    {
        this.queue.add(state);
        this.set.add(state);
        this.stateTime.put(state, timeStep);
    }

    @Override
    public State pop()
    {
        Collections.sort(this.queue, new Comparator<State>() {
            @Override
            public int compare(State state1, State state2) {
                return Double.compare(heuristic.f(state1), heuristic.f(state2));
            }
        });

        State state = queue.remove(0);
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    @Override
    public int size()
    {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state)
    {
        return this.set.contains(state);
    }

    @Override
    public String getName()
    {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}
