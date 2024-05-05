package searchclient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public abstract class Heuristic
        implements Comparator<State>
{
    private double[][][] goalDist;
    private Map<Integer, int[]> goalDict;
    public String choice;
    private char[][] goals = State.goals;
    private int[][][][][] distances;

    public Heuristic(State initialState)
    {
        this.goalDict = new HashMap<>();
        this.distances = calculateDistances(initialState);
        this.choice = "h1"; 
        // System.out.println("agentNumb: " + initialState.agentRows.length );
        // System.out.println("goals.row : " + goals[0]);
        // char[][] goals = State.goals;
        for(int row = 0; row < goals.length; row++){
            for(int col = 0; col < goals[row].length; col++){
                char goal = goals[row][col];
                if (('0' <= goal && goal <= '9') || ('A' <= goal && goal <= 'Z')){
                    this.goalDict.put((int)goal, new int[]{row, col});
                }
            }
        }

        if(this.choice.equals("h2")){
            this.goalDist = new double[goals.length][goals[0].length][256];

            for (int row = 0; row < goals.length; row++) {
                for (int col = 0; col < goals[row].length; col++) {
                    for (int goal : this.goalDict.keySet()) {
                        int[] goalCoords = this.goalDict.get(goal);
                        int x = goalCoords[0]; // row number of the goal
                        int y = goalCoords[1]; // col number of the goal
                        double dist = Math.sqrt(Math.pow(row - x, 2) + Math.pow(col - y, 2));
                        this.goalDist[row][col][goal] = dist;
                    }
                }
            }
        }
             
    }

    private int[][][][][] calculateDistances(State state) {
        int numAgents = state.agentRows.length;
        int numRows = state.walls.length;
        int numCols = state.walls[0].length;

        int[][][][][] distances = new int[numAgents][numRows][numCols][numRows][numCols];

        // Initialize distances to maximum value
        for (int a = 0; a < numAgents; a++) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numCols; j++) {
                    for (int k = 0; k < numRows; k++) {
                        Arrays.fill(distances[a][i][j][k], 0);
                    }
                }
            }
        }

        // Dijkstra's Algorithm to calculate distances between all cells
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int agent = 0; agent < numAgents; agent++) {
            for (int row1 = 0; row1 < numRows; row1++) {
                for (int col1 = 0; col1 < numCols; col1++) {
                    PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));
                    pq.offer(new int[]{row1, col1, 0});

                    boolean[][] visited = new boolean[numRows][numCols];
                    visited[row1][col1] = true;

                    while (!pq.isEmpty()) {
                        int[] current = pq.poll();
                        int x = current[0];
                        int y = current[1];
                        int dist = current[2];

                        distances[agent][row1][col1][x][y] = dist;

                        for (int[] dir : directions) {
                            int newX = x + dir[0];
                            int newY = y + dir[1];
                            if (isValid(state, newX, newY) && !visited[newX][newY]) {
                                visited[newX][newY] = true;
                                pq.offer(new int[]{newX, newY, dist + 1});
                            }
                        }
                    }
                }
            }
        }
        // System.out.println("after precalculated dist at 1, 1 : " + Integer.toString(distances[0][0][0][1][1]));
        // System.out.println("after precalculated dist at 3, 3 : " + Arrays.toString(distances[0][0][0][3][3]));    
        // System.out.println("after precalculated dist at 1, 1 : " + Arrays.stream(Integer.toString(distances[0][0][0][1][1]))
        //                 .mapToObj(String::valueOf)
        //                 .collect(Collectors.joining(" ")));
        return distances;
    }

    private boolean isValid(State state, int x, int y) {
        return x >= 0 && x < state.walls.length && y >= 0 && y < state.walls[0].length &&
                !state.walls[x][y];
    }

    
    public int h(State s)
    {
        if(this.choice.equals("h1")){
            int count = s.agentRows.length;

            for (int goal : this.goalDict.keySet()){
                count++;
                if (s.agentAt(this.goalDict.get(goal)[0], this.goalDict.get(goal)[1]) == (char) goal) {
                    count--;
                }
            }
            // System.out.println("h1 : " + count);
            return count;
        }
        else if(this.choice.equals("h2")){
            int count = 0;
            for (int agent = 0; agent < s.agentRows.length; agent++) {
                try {
                    count += this.goalDist[s.agentRows[agent]][s.agentCols[agent]][agent + '0'];
                } catch (ArrayIndexOutOfBoundsException | NullPointerException ignored) {
                }
            }
            // System.out.println("h2 : " + count);
            return count;
        }
        else if (choice.equals("h3")) {
            int count = 0;
            for (int goal : this.goalDict.keySet()) {
                count++;
                if (s.boxes[goalDict.get(goal)[0]][goalDict.get(goal)[1]] == (char) goal) {
                    count--;
                }
            }
            // System.out.println("h3 : " + count);
            return count;
        } 
        else if (choice.equals("h4")) {
            int count = 0;
            for (int row = 0; row < s.boxes.length; row++) {
                for (int col = 0; col < s.boxes[row].length; col++) {
                    char box = s.boxes[row][col];
                    if ('A' <= box && box <= 'Z') {
                        int goalRow = goalDict.get((int) box)[0];
                        int goalCol = goalDict.get((int) box)[1];
                        count += Math.sqrt(Math.pow(row - goalRow, 2) + Math.pow(col - goalCol, 2));
                    }
                }
            }
            // System.out.println("h4 : " + count);
            return count;
        }
        else if (choice.equals("h5")){
            int count = 0;
            for (int agentIndex = 0; agentIndex < this.goals.length; agentIndex++) {
                if (agentIndex < s.agentRows.length) {
                    for (int goal : this.goalDict.keySet()) {
                        int[] goalCoords = this.goalDict.get(goal);
                        int goalRow = goalCoords[0]; // row number of the goal
                        int goalCol = goalCoords[1]; // col number of the goal
                        // int goalRow = this.goals[agentIndex][0];
                        // int goalCol = this.goals[agentIndex][1];
                        int minDistance = this.distances[agentIndex][s.agentRows[agentIndex]][s.agentCols[agentIndex]][goalRow][goalCol];
                        // System.out.println("agent is : " + agentIndex + ", agent row is: " + s.agentRows[agentIndex]  + ", agent column is: " + s.agentCols[agentIndex] );
                        // System.out.println("goal row is: " + goalRow + ", goal column is: " + goalCol + "Dijkstra distance+ is : " + minDistance);
                        // System.out.println("Dijkstra distance+ 1,1 is : " + this.distances[0][0][0][1][1]); 
                        // System.out.println("Dijkstra distance+ 2,2 is : " + this.distances[0][0][0][2][2]);
                        count += minDistance;
                    }
                }
            }     
            
            return count;
        }
        return 0;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
