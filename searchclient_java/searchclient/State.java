package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

public class State
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public static Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).

        this.boxes and this.char are two-dimensional arrays of chars. 
        this.boxes[1][2]='A' means there is an A box at (1,2). 
        If there is no box at (1,2), we have this.boxes[1][2]=0 (null character).
        Similarly for goals. 

    */
    public static boolean[][] walls;
    public char[][] boxes;
    public static char[][] goals;

    public char[][] subgoals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes, 
        this.boxColor[1] is the color of B boxes, etc.
    */
    public static Color[] boxColors;
 
    public final State parent;
    public final Action[] jointAction;
    private final int g;

    private int hash = 0;
    private int numAgents;

    private Integer isSubProblemForAgent = null;

    //Stuff added to help divide the problem into subproblems

    //goalPosition is a map where the key is the letter of the goal (where the box with that letter should go?) and the value is the list of position that have that goal
    public static Map<Character, ArrayList<Position>> goalsPosition = new java.util.HashMap<Character, ArrayList<Position>>();

    //boxesPosition is a map where the key is the letter of the box and the value is the list of position that have that box
    //TODO: DO WE NEED THIS?
    public static Map<Character, ArrayList<Position>> boxesPosition = new java.util.HashMap<Character, ArrayList<Position>>();


    public Integer getIsSubProblemForAgent() {
        return isSubProblemForAgent;
    }

    public void setIsSubProblemForAgent(Integer isSubProblemForAgent) {
        this.isSubProblemForAgent = isSubProblemForAgent;
    }

    /** hashmap that connects with each new letter the original letter of the box */
    private HashMap<Character, Character> originalBoxMap = new HashMap<Character, Character>();
    
    public HashMap<Character, Character> getOriginalBoxMap() {
        return originalBoxMap;
    }

    /**
     * sets for the state an hash map where the key is the new box index and the value is the old box index
     * @param boxes
     */
    public void initializeOriginalBoxMap(char[][] boxes) {
        /** why the fuck to use a char if its use ad an integer index */
        Character boxIndex = 'A';
        HashMap<Character, Character> boxMap = new HashMap<Character, Character>();
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes[i].length; j++) {
                if (boxes[i][j] != 0) {
                    boxMap.put(boxIndex, boxes[i][j]);
                    boxIndex++;
                }
            }
        }
        this.originalBoxMap = boxMap;
    }

    public void setOriginalBoxMap(HashMap<Character, Character> originalBoxMap) {
        this.originalBoxMap = originalBoxMap;
    }

    public char getOriginalBoxChar(char renamedBox) {
        return this.originalBoxMap.get(renamedBox);
    }
    

    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    )
    {
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.agentColors = agentColors;
        this.numAgents = agentRows.length;
        this.walls = walls;
        this.boxes = boxes;
        this.boxColors = boxColors;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.g = 0;

    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction)
    {
        // Copy parent
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxes = new char[parent.boxes.length][];
        for (int i = 0; i < parent.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
        }

        //Passing the "subproblem for agent" from parent to children
        this.isSubProblemForAgent = parent.isSubProblemForAgent;
        this.originalBoxMap = parent.originalBoxMap;

        if(isSubProblemForAgent != null){
            //copy the subgoals
            this.subgoals = new char[parent.subgoals.length][];
            for (int i = 0; i < parent.subgoals.length; i++)
            {
                this.subgoals[i] = Arrays.copyOf(parent.subgoals[i], parent.subgoals[i].length);
            }
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;
        this.numAgents = this.agentRows.length;

        // Apply each action
        for (int agent = 0; agent < this.numAgents; ++agent)
        {
            Action action = jointAction[agent];
            char box;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

                case Push:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    box = this.boxes[this.agentRows[agent]][this.agentCols[agent]];
                    this.boxes[this.agentRows[agent] + action.boxRowDelta][this.agentCols[agent] + action.boxColDelta] = box;
                    this.boxes[this.agentRows[agent]][this.agentCols[agent]] = 0;
                    break;

                case Pull:
                    box = this.boxes[this.agentRows[agent] - action.boxRowDelta][this.agentCols[agent] - action.boxColDelta];
                    this.boxes[this.agentRows[agent]][this.agentCols[agent]] = box;
                    this.boxes[this.agentRows[agent] - action.boxRowDelta][this.agentCols[agent] - action.boxColDelta] = 0;
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;
            }
        }
    }

    public int g()
    {
        return this.g;
    }

    public boolean agentHasGoal(int agent) {
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                char goal = this.goals[row][col];
                if (goal - '0' == agent) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGoalState()
    {
        if(isSubProblemForAgent == null) {
            for (int row = 1; row < this.goals.length - 1; row++)
            {
                for (int col = 1; col < this.goals[row].length - 1; col++)
                {
                    char goal = this.goals[row][col];
                    char box = this.boxes[row][col];

    
                    if ('A' <= goal && goal <= 'Z') {
                        if ('A' <= box && box <= 'Z') {
                            if (getOriginalBoxChar(box) != goal) {
                                System.out.println("Goal " + goal + " is not achieved, but box " + box + " is at the goal");
                                return false;
                            }
                        } else {
                            System.out.println("Goal " + goal + " is not achieved");
                            return false;
                        }
                    }
                    else if ('0' <= goal && goal <= '9' &&
                             !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col))
                    {
                        return false;
                    }
                }
            }
        }
        else {
            for (int row = 1; row < this.subgoals.length - 1; row++)
            {
                for (int col = 1; col < this.subgoals[row].length - 1; col++)
                {
                    char goal = this.subgoals[row][col];
                    char box = this.boxes[row][col];
    
                    if ('A' <= goal && goal <= 'Z' && agentBoxColorMatch(isSubProblemForAgent, goal)) {
                        // System.out.println("#Goal: " + goal + " at position: " + row + ", " + col);
                        for (int i = 0; i < this.boxes.length; i++) {
                            for (int j = 0; j < this.boxes[i].length; j++) {
                                char box2 = this.boxes[i][j];
                                if (box2 != 0) {
                                    // System.out.println("#Box " + box2 + " is at position: " + i + ", " + j);
                                }
                            }
                        }
                        if ('A' <= box && box <= 'Z') {
                            if (getOriginalBoxChar(box) != goal) {
                                // System.out.println("#Goal " + goal + " is not achieved, but box " + box + " is at the goal");
                                return false;
                            }
                            // System.out.println("# Box " + box + " is at the goal " + goal);
                        } else {
                            return false;
                        }
                    }
                    else if ('0'+isSubProblemForAgent == goal &&
                             !(this.agentRows[0] == row && this.agentCols[0] == col))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public boolean agentBoxColorMatch(int agent, char box) {
        return this.boxColors[box - 'A'] == this.agentColors[agent];
    }

    public boolean adheresConstraints(ArrayList<Constraint> constraints, int timeStep){
        for (Constraint constraint : constraints) {
            //constraint.printConstraint();
            // Checks:
            // 1. If time of the conflict equals the current time
            // 2. If the agent of the constraint is in the current state at the position of the constrain
            if (constraint.getTimeStep() == timeStep){
                if (constraint.getConstraintType() == ConstraintType.AGENT && this.getIsSubProblemForAgent() == null && this.agentAt(constraint.getPosition().getY(), constraint.getPosition().getX()) - '0' == constraint.getAgent()) {
                    return false;
                } else if (constraint.getConstraintType() == ConstraintType.AGENT && this.getIsSubProblemForAgent() == constraint.getAgent() && this.agentAt(constraint.getPosition().getY(), constraint.getPosition().getX()) - '0' == 0){
                    return false;
                } else if (constraint.getConstraintType() == ConstraintType.BOX){ // the constraint is on a box
                    if (this.boxes[constraint.getPosition().getY()][constraint.getPosition().getX()] == constraint.getBox() // the box is at the position of the constraint
                        && (constraint.getAgent() == this.getIsSubProblemForAgent() || this.getIsSubProblemForAgent() == null)) { // the box needs to be moved by this agent
                            return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean adheresFinalConstraints(ArrayList<Constraint> constraints, int timeStep){
        for (Constraint constraint : constraints) {
            if (timeStep <= constraint.getTimeStep()){
                if (constraint.getConstraintType() == ConstraintType.AGENT && this.getIsSubProblemForAgent() == null && this.agentAt(constraint.getPosition().getY(), constraint.getPosition().getX()) - '0' == constraint.getAgent()) {
                    return false;
                } else if (constraint.getConstraintType() == ConstraintType.AGENT && this.getIsSubProblemForAgent() == constraint.getAgent() && this.agentAt(constraint.getPosition().getY(), constraint.getPosition().getX()) - '0' == 0){
                    return false;
                } else if (constraint.getConstraintType() == ConstraintType.BOX){ // the constraint is on a box
                    if (this.boxes[constraint.getPosition().getY()][constraint.getPosition().getX()] == constraint.getBox() // the box is at the position of the constraint
                        && (constraint.getAgent() == this.getIsSubProblemForAgent() || this.getIsSubProblemForAgent() == null)) { // the box needs to be moved by this agent
                            return false;
                    }
                }
            }
        }
        return true;
    }



    public ArrayList<State> getExpandedStates()
    {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        for (int agent = 0; agent < numAgents; ++agent)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent)
            {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction))
            {    
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1)
                {
                    ++actionsPermutation[agent];
                    break;
                }
                else
                {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1)
                    {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done)
            {
                break;
            }
        }

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action)
    {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = this.agentColors[agent];
        int boxRow;
        int boxCol;
        char box;
        int destinationRow;
        int destinationCol;
        int destinationRowBox;
        int destinationColBox;
        switch (action.type)
        {
            case NoOp:
                return true;

            case Move:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                return this.cellIsFree(destinationRow, destinationCol);

            case Push:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;

                boxRow = agentRow + action.agentRowDelta;
                boxCol = agentCol + action.agentColDelta;

                destinationRowBox = boxRow + action.boxRowDelta;
                destinationColBox = boxCol + action.boxColDelta;

                box = this.boxes[boxRow][boxCol];

                if ('A' <= box && box <= 'Z')
                {
                    if (this.boxColors[(int) (getOriginalBoxChar(box) - 'A')] == agentColor)
                    {
                        if (this.cellIsFree(destinationRowBox, destinationColBox))
                        {
                            if (Math.abs(destinationRowBox - destinationRow) + Math.abs (destinationColBox - destinationCol) == 1)
                            {
                                if (boxRow == destinationRow && boxCol == destinationCol) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;

            case Pull:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;

                boxRow = agentRow - action.boxRowDelta;
                boxCol = agentCol - action.boxColDelta;

                destinationRowBox = agentRow;
                destinationColBox = agentCol;

                box = this.boxes[boxRow][boxCol];

                if ('A' <= box && box <= 'Z')
                {                    
                    if (this.boxColors[(int) (getOriginalBoxChar(box) - 'A')] == agentColor)
                    {
                        if (this.cellIsFree(destinationRow, destinationCol))
                        {
                            if (Math.abs(destinationRowBox - destinationRow) + Math.abs (destinationColBox - destinationCol) == 1)
                            {
                                if (agentRow == destinationRowBox && agentCol == destinationColBox) {
                                    // System.out.println("# Agent goes from position " + agentRow + ", " + agentCol + " to position " + destinationRow + ", " + destinationCol);
                                    // System.out.println("# Pushing box " + box + " from position " + boxRow +" ," + boxCol + " to position " + destinationRowBox + ", " + destinationColBox);
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
        }

        // Unreachable:
        return false;
    }

    private boolean isConflicting(Action[] jointAction)
    {
        int numAgents = this.agentRows.length;

        int[] destinationRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] destinationCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action

        // Collect cells to be occupied and boxes to be moved
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];
            int boxRow;
            int boxCol;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    destinationRows[agent] = agentRow + action.agentRowDelta;
                    destinationCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = agentRow; // Distinct dummy value
                    boxCols[agent] = agentCol; // Distinct dummy value
                    break;
           }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction[a1] == Action.NoOp)
            {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction[a2] == Action.NoOp)
                {
                    continue;
                }

                // Moving into same cell?
                if (destinationRows[a1] == destinationRows[a2] && destinationCols[a1] == destinationCols[a2])
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    public char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.length; i++)
        {
            if (this.agentRows[i] == row && this.agentCols[i] == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan()
    {
        if (this.jointAction == null)
        {
            return new Action[0][0];
        } else {
            Action[][] plan = new Action[this.g][];
            State state = this;
            while (state.jointAction != null)
            // g = parent.g + 1 ->  g close to the source is lower       this.g = parent.g + 1;
            // state = state.parent -> state.parent.g  = state.g - 1 (i.e. we are backtracking the actions)
            {
                plan[state.g - 1] = state.jointAction;
                state = state.parent;
            }
            return plan;
            
        }
    }

    public Position[][] extractBoxPositions() {
        Position[][] boxPositions = new Position[this.g + 1][this.getNumBoxes()];
        State state = this;
        while (state.jointAction != null) {
            for (int row = 0; row < state.boxes.length; row++) {
                for (int col = 0; col < state.boxes[row].length; col++) {
                    char box = state.boxes[row][col];
                    
                    if (box != 0) {
                        Position pos = new Position(col, row);
                        boxPositions[state.g][(int) (box - 'A')] = pos;
                    }
                }
            }
            state = state.parent;
        }
        for (int row = 0; row < state.boxes.length; row++) {
            for (int col = 0; col < state.boxes[row].length; col++) {
                char box = state.boxes[row][col];
                
                if (box != 0) {
                    Position pos = new Position(col, row);
                    boxPositions[state.g][(int) (box - 'A')] = pos;
                }
            }
        }
        // boxPositions.length equals the length of the solution (= state.jointAction.length)
        // boxPositions[0].length equals the number of boxes
        // for (int i = 0; i < boxPositions.length; i++) {
        //     for (int j = 0; j < boxPositions[i].length; j++) {

        //         System.out.println("#At time: " + i + ", box " + (char) ('A' + j) + " is at position: " + boxPositions[i][j].getX() + ", " + boxPositions[i][j].getY());
        //     }
        // }
        return boxPositions;
    }

    public Position[][] extractPositions()
    {
        Position[][] positions = new Position[this.g + 1][];
        State state = this;
        while (state.jointAction != null)
        {   
            for (int i = 0; i < state.getAgentRows().length ; i++) { // Loop over all agents
                // Create a new position with the position of the agent in the current state
                Position pos = new Position(state.getAgentCols()[i], state.getAgentRows()[i]);

                // If we have not initialized the state yet, we initialize it with the position of the agent
                if(positions[state.g] == null) {
                    positions[state.g] = new Position[]{pos};
                } else {
                    Position[] oldArray = positions[state.g];
                    positions[state.g] = new Position[oldArray.length + 1];
                    System.arraycopy(oldArray, 0, positions[state.g], 0, oldArray.length);
                    positions[state.g][oldArray.length] = pos;
                }
            }
            state = state.parent;
        }
        for (int i = 0; i < state.getAgentRows().length ; i++) {
            Position pos = new Position(state.getAgentCols()[i], state.getAgentRows()[i]);
            if (positions[0] == null) {
                positions[0] = new Position[]{pos};
            } else {
                Position[] oldArray = positions[0];
                positions[0] = new Position[oldArray.length + 1];
                System.arraycopy(oldArray, 0, positions[0], 0, oldArray.length);
                positions[0][oldArray.length] = pos;
            }
        }
        return positions;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row)
            {
                for (int col = 0; col < this.boxes[row].length; ++col)
                {
                    char c = this.boxes[row][col];
                    if (c != 0)
                    {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
               Arrays.equals(this.agentCols, other.agentCols) &&
               Arrays.equals(this.agentColors, other.agentColors) &&
               Arrays.deepEquals(this.walls, other.walls) &&
               Arrays.deepEquals(this.boxes, other.boxes) &&
               Arrays.equals(this.boxColors, other.boxColors) &&
               Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < this.walls.length; row++)
        {
            for (int col = 0; col < this.walls[row].length; col++)
            {
                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (this.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.agentAt(row, col) != 0)
                {
                    s.append(this.agentAt(row, col));
                }
                else
                {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }

    /**
     * function that takes in input a state and returns an arraylist of states
     * that are identical to the input state but each of the new states contains only one
     * of the original agents
     */
    public ArrayList<State> getSingleAgentProblems() {
        ArrayList<State> singleAgentStates = new ArrayList<State>();
        //Estract the position of all the goals in a Map with the key the letter of the goal and the value the list of position that have that goals
        getGoalsPosition();
        //Get for each agent(key) the goals that are closer to it
        System.out.println("#boxColors.length: " + boxColors.length);
        Map<Integer, ArrayList<Position>> separatedGoals = pairAgentsWithGoals();
        for (int agent = 0; agent < this.numAgents; agent++) {
            int[] agentRows = new int[]{this.agentRows[agent]};
            int[] agentCols = new int[]{this.agentCols[agent]};
            Color[] colors = this.agentColors;
            boolean[][] walls = this.walls;
            char[][] boxes = getSingleAgentBoxes(agent);
            Color[] boxColors = this.boxColors;
            char[][] goals = this.goals;
            HashMap<Character, Character> originalBoxMap = this.originalBoxMap;
            // Alternative: char[][] goals = this.getSingleAgentGoals(agent);
            State singleProblemState = new State(agentRows, agentCols, colors, walls, boxes, boxColors, goals);
            singleProblemState.setIsSubProblemForAgent(agent);
            singleProblemState.setOriginalBoxMap(this.originalBoxMap);
 
            singleProblemState.separateGoalsAndBoxes(separatedGoals.get(agent));
            System.out.println("#separatedGoals for agent: " + agent);

            for (Position goal : separatedGoals.get(agent)) {
                System.out.println("Goal position: (" + goal.getX() + ", " + goal.getY() + ")");
            }
            singleAgentStates.add(singleProblemState);

            // System.out.println("#old walls:" + this.walls);
            // printWallsAndGoals(walls, goals);
            // System.out.println("#new walls:" + walls);
            // System.out.println("#old walls:" + this.walls);
            // System.out.println("#new walls:" + walls);

        }
        return singleAgentStates;
    }

    /**
     * 
     * @return this function returns a map where the key is the index of the agent and the value is the list of goals that are closer to that agent
     */
    private Map<Integer, ArrayList<Position>> pairAgentsWithGoals() {
        //This bad boy contains the position of all agents
        List<Position> agentPositions = new ArrayList<Position>();

        //This bad boy contains key: id of the agent, value: list of goals that are closer to that agent
        Map<Integer, ArrayList<Position>> agentGoals = new java.util.HashMap<Integer, ArrayList<Position>>();
        
        //initialize agentGoals and agentPositions
        for (int i = 0; i < this.agentRows.length; i++) {
            agentPositions.add(new Position(this.agentRows[i], this.agentCols[i]));
            agentGoals.put(i, new ArrayList<Position>());
        }

        //How can we split equally the goals among the agents?
        //We can calculate the number of goals and divide them by the number of agents
        //Then we can calculate the distance between each agent and each goal and assign the goal to the agent that is closer to it
        //but only if the agent is not already assigned to the limit of goals that we have calculated before
        
        // Integer goalsCount = goalsPosition.entrySet().stream().reduce(0, (sum, entry) -> sum + entry.getValue().size(), Integer::sum);
        // Integer numberOfAgents = agentPositions.size();

        //TODO: we have a problem here bc if all the goals are closer to one agent, that agent will get all the goals instead of getting split between the agents

        //calculate for each agent the distance to each goal contained into the goalsPosition map
        for (Map.Entry<Character, ArrayList<Position>> entry : goalsPosition.entrySet()) {
            Iterator<Position> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                Position goalPosition = iterator.next();
                System.out.println("#Analizing the best fit for the Goal at position "+entry.getValue().indexOf(goalPosition));
                float minDistance = Float.MAX_VALUE;
                int nearestAgent = -1;
                System.out.println("#boxColors.length: " + boxColors.length);
                for (Position agentPosition : agentPositions) {
                    //get the index of the current agentPosition
                    int agentIndex = agentPositions.indexOf(agentPosition);
                    //         this.boxColor[1] is the color of B boxes, etc.
                // if(this.boxColors.length > 0){
                //     if (this.agentColors[agentIndex] != this.boxColors[(int) (entry.getKey() - 'A')]) {
                //         continue;
                //     }
                // }
                // else if ( entry.getKey() != (char) ('0' + agentIndex)){
                //             System.out.println("#When No boxes, Agent index: "+agentIndex+" not equal to  num as the goal: "+entry.getKey());
                //             continue;
                //         }
                
                // else if (this.boxColors.length>0 && this.agentRows.length > 0)
                //         if ((this.agentColors[agentIndex] != this.boxColors[(int) (entry.getKey() - 'A')])   || entry.getKey() != (char) ('0' + agentIndex) ) {
                //             continue;
                        

                // }

                

                   

                    int distance = Math.abs(agentPosition.getX() - goalPosition.getX()) + Math.abs(agentPosition.getY() - goalPosition.getY());
                    
                    System.out.println("#Agent "+agentIndex+" (" + agentPosition.getX() + "," + agentPosition.getY() + ") has distance: " + distance + " from goal: (" + goalPosition.getX() + ", " + goalPosition.getY()+")");
                    
                    if (distance < minDistance){
                        minDistance = distance;
                        nearestAgent = agentIndex;
                    }
                }
                agentGoals.get(nearestAgent).add(goalPosition);

                //remove from the map goalsPosition the goal that has been assigned to the agent
                iterator.remove();
            }
        }

        //at the end of this loop we have a map where the key is the index of the agent and the value is the list of goals that are closer to that agent
       
        agentGoals.entrySet().forEach(entry -> {
            System.out.println("#Agent: " + entry.getKey() + " has goals: ");
            for (Position goalPosition : entry.getValue()) {
                System.out.println("#Goal: " + goalPosition.getX() + " " + goalPosition.getY());
            }
        });

        //print the final goalsPosition map
        System.out.println("#Remaining goals: ");
        for (Map.Entry<Character, ArrayList<Position>> entry : goalsPosition.entrySet()) {
            for (Position goalPosition : entry.getValue()) {
                System.out.println("#goalsPosition Goal: " + goalPosition.getX() + " " + goalPosition.getY());
            }
        }

        return agentGoals;
    }

    /**
     * it literally 3 fucking bubttons can we put doc comments
     * fucking doc comments
     * @param singleProblemState
     * @param goalsOfTheAgent
     */
    private void separateGoalsAndBoxes(ArrayList<Position> goalsOfTheAgent) {
        Position agentPosition = new Position(this.getAgentRows()[0], this.getAgentCols()[0]);
        int agent = this.getIsSubProblemForAgent();
        System.out.println("#Agent: " + agent);
        //get a copy of this.boxes
        char[][] boxes = new char[this.boxes.length][];
        for (int i = 0; i < this.boxes.length; i++) {
            boxes[i] = Arrays.copyOf(this.boxes[i], this.boxes[i].length);
        }

        //get a copy of goals in a variable called subgoals
        //Why do we need that? because goals is static and if we modifiy it for one agent we modify it for every agent
        char[][] subgoals = new char[this.goals.length][];
        System.out.println("#1 GoalsOfTheAgent size: " + goalsOfTheAgent.size() );
        printGoalMap();
        for (int i = 0; i < this.goals.length; i++) {
            subgoals[i] = Arrays.copyOf(this.goals[i], this.goals[i].length);
            // System.out.println("# 1 subgoals row: " + this.goals[i] + ", subgoals col: " + this.goals[i].length);
        }
        
        //Estract the position of all the boxes in a Map with the key the letter of the box and the value the list of position that have that boxes
        Map<Character, ArrayList<Position>> boxesPosition = getBoxesPosition(boxes);

        //find the position of the nearest box for the agent
        // Position nearestBox = null;
        // int minDistance = Integer.MAX_VALUE;
        // for (Map.Entry<Character, ArrayList<Position>> entry : boxesPosition.entrySet()) {
        //     for (Position boxPosition : entry.getValue()) {
        //         int distance = Math.abs(agentPosition.getX() - boxPosition.getX()) + Math.abs(agentPosition.getY() - boxPosition.getY());
        //         if (distance < minDistance) {
        //             minDistance = distance;
        //             nearestBox = boxPosition;
        //         }
        //     }
        // }

        // //find the position of the nearest goal for the box in position nearestBox
        // Position nearestGoal = null;
        // minDistance = Integer.MAX_VALUE;
        // for (Map.Entry<Character, ArrayList<Position>> entry : goalsPosition.entrySet()) {
        //     for (Position goalPosition : entry.getValue()) {
        //         int distance = Math.abs(nearestBox.getX() - goalPosition.getX()) + Math.abs(nearestBox.getY() - goalPosition.getY());
        //         if (distance < minDistance) {
        //             minDistance = distance;
        //             nearestGoal = goalPosition;
        //         }
        //     }
        // }

        // System.out.println("#Nearest box for the agent " + agent + " is in position: " + nearestBox.getX() + " " + nearestBox.getY()+ " and the nearest goal is in position: " + nearestGoal.getX() + " " + nearestGoal.getY());

        //TODO: Should we remove only the goals for each agent instad of the boxes?
        //remove from `boxes` the boxes that aren't in position nearestBox
        // for (int row = 0; row < boxes.length; row++) {
        //     for (int col = 0; col < boxes[row].length; col++) {
        //         if (row != nearestBox.getX() || col != nearestBox.getY()) {
        //             boxes[row][col] = 0;
        //         }
        //     }
        // }

        //remove from `goals` the goals that aren't in position nearestGoal
        // for (int row = 0; row < goals.length; row++) {
        //     for (int col = 0; col < goals[row].length; col++) {
        //         if (row != nearestGoal.getX() || col != nearestGoal.getY()) {
        //             goals[row][col] = 0;
        //         }
        //     }
        // }
       
        // if(isGoalNum() != true){
            if(goalsOfTheAgent.size() == 0 ){
                for (int row = 0; row < subgoals.length; row++) {
                    for (int col = 0; col < subgoals[row].length; col++) {
                            subgoals[row][col] = ' ';
                            // System.out.println("#goalsOfTheAgent: "+ agent +" is empty: " + goalsOfTheAgent.size() + ", COZ isGoalNum is "+ isGoalNum() );

                    }
                }
            } else {
                    for (int row = 0; row < subgoals.length; row++) {
                        for (int col = 0; col < subgoals[row].length; col++) {
                            boolean toChange = true;
                            for(Position nearestGoal : goalsOfTheAgent){
                                if (row == nearestGoal.getX() && col == nearestGoal.getY()) {
                                    toChange = false;
                                    // System.out.println("#toChange is false. "+ nearestGoal.getX() +", " + nearestGoal.getY());
                                }
                            }
                            
                            if (toChange) {
                                subgoals[row][col] = ' ';
                                // System.out.println("#toChange is true");
                            }
                        }
                    }
                }
        // }
        //set the boxes and goals of the singleProblemState
        this.boxes = boxes;
        this.subgoals = subgoals;    
        
        

        System.out.println("#Boxes :");
        //print the new boxes
        for (int row = 0; row < boxes.length; row++) {
            System.out.print("#");
            for (int col = 0; col < boxes[row].length; col++) {
                System.out.print((boxes[row][col] == 0 ? " " : boxes[row][col]));
            }
            System.out.println(" ");
        }


        System.out.println("#Subgoals :");
        //print the new goals
        for (int row = 0; row < subgoals.length; row++) {
            System.out.print("#");
            for (int col = 0; col < subgoals[row].length; col++) {
                System.out.print((subgoals[row][col] == 0 ? " " : subgoals[row][col]));
            }
            System.out.println(" ");
        }

        System.out.println("#Goals :");
        //print the new goals
        for (int row = 0; row < this.goals.length; row++) {
            System.out.print("#");
            for (int col = 0; col < this.goals[row].length; col++) {
                System.out.print((this.goals[row][col] == 0 ? " " : this.goals[row][col]));
            }
            System.out.println(" ");
        }
        
    }
    public boolean isGoalNum() {
        for (int row = 0; row < this.goals.length; row++) {
            for (int col = 0; col < this.goals[row].length; col++) {
                int goal = this.goals[row][col];
                if (goal <= '0' || goal >= '9') {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Returns a map where the key is the index of the goal and the value is the name of the goal.
     * The goal name can be a digit from 0 to 9 or a letter from A to Z.
     * @return a map where the key is the index of the goal and the value is the name of the goal
     */
    public Map<Integer, Character> getGoalMap() {
        Map<Integer, Character> goalMap = new HashMap<>();
        int index = 0;
        for (int row = 0; row < goals.length; row++) {
            for (int col = 0; col < goals[row].length; col++) {
                char goal = goals[row][col];
                if ((goal >= '0' && goal <= '9') || (goal >= 'A' && goal <= 'Z')) {
                    goalMap.put(index++, goal);
                }
            }
        }
        return goalMap;
    }
    public void printGoalMap() {
        Map<Integer, Character> goalMap = getGoalMap();
        System.out.println("#Goal Map:");
        for (Map.Entry<Integer, Character> entry : goalMap.entrySet()) {
            System.out.println("Index: " + entry.getKey() + ", Goal: " + entry.getValue());
        }
    }
    /**
     * function that iterates over the box structure and returns a map where the key is the letter of the box and the value is the list of position of the boxes with that letter
     * @param boxes
     * @return 
     */
    private Map<Character, ArrayList<Position>> getBoxesPosition(char[][] boxes) {
        Map<Character, ArrayList<Position>> boxesPosition = new java.util.HashMap<Character, ArrayList<Position>>();
        for (int row = 0; row < boxes.length; row++) {
            for (int col = 0; col < boxes[row].length; col++) {
                if (Character.isLetter(boxes[row][col])) {
                    if (boxesPosition.containsKey(boxes[row][col])) {
                        boxesPosition.get(boxes[row][col]).add(new Position(row, col));
                    } else {
                        ArrayList<Position> positions = new ArrayList<Position>();
                        positions.add(new Position(row, col));
                        boxesPosition.put(boxes[row][col], positions);
                    }
                }
            }
        }
        return boxesPosition;
    }

    public void printBoxes() {
        System.out.println("#Boxes :");
        for (int row = 0; row < this.boxes.length; row++) {
            System.out.print("#");
            for (int col = 0; col < this.boxes[row].length; col++) {
                System.out.print((this.boxes[row][col] == 0 ? " " : this.boxes[row][col]));
            }
            System.out.println(" ");
        }
    }

    /**
     * function that iterates over the goals structure and returns a map where the key is the letter of the goal and the value is the list of position that have that goal
     * Since we could have different goals for boxes that share the same letter, we need to store all the positions of the goals with the same letter
     * @return a map where the key is the letter of the goal and the value is the list of position that have that goal
     */
    private Map<Character, ArrayList<Position>> getGoalsPosition() {
        
        char[][] goals = State.goals;
        
        //estract the position of the goals in a map where the key is the letter of the goal and the value is the list of position that have that goal
        for (int row = 0; row < goals.length; row++) {
            for (int col = 0; col < goals[row].length; col++) {
                if (Character.isLetter(goals[row][col]) || Character.isDigit(goals[row][col])) {
                    if (goalsPosition.containsKey(goals[row][col])) {
                        goalsPosition.get(goals[row][col]).add(new Position(row, col));
                    } else {
                        ArrayList<Position> positions = new ArrayList<Position>();
                        positions.add(new Position(row, col));
                        goalsPosition.put(goals[row][col], positions);
                    }
                }
            }
        }

        return goalsPosition;
    }

    private char[][] getSingleAgentBoxes(int agent) {
        char[][] singleAgentBoxes = new char[this.boxes.length][this.boxes[0].length];
        for (int row = 0; row < this.boxes.length -1; row++) {
            for (int col = 0; col < this.boxes[row].length -1; col++) {
                // if the box color is the same as the agents, then  we keep the box
                if (this.boxes[row][col] != 0 && this.boxColors[this.boxes[row][col] - 'A'] == this.agentColors[agent]) {
                    System.out.println("#Box: " + this.boxes[row][col] + " Agent color: " + this.agentColors[agent]);
                    singleAgentBoxes[row][col] = this.boxes[row][col];
                } else {
                    singleAgentBoxes[row][col] = 0;
                }
            }
        }
        // print singleAgentBoxes
        for (int row = 0; row < singleAgentBoxes.length; row++) {
            for (int col = 0; col < singleAgentBoxes[row].length; col++) {
                if (col==0){
                    System.out.print("# ");
                }
                if (singleAgentBoxes[row][col] > 0) {
                    System.out.print(singleAgentBoxes[row][col]);
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        return singleAgentBoxes;
    }
    /**
     * function that can iterate over the walls structure and print an ascii representation of it
     * if the cell is true it prints a + sign, otherwise it prints a space
     * 
     */
    public void printWalls(boolean[][] walls) {
        for (int row = 0; row < walls.length; row++) {
            for (int col = 0; col < walls[row].length; col++) {
                if (col==0){
                    System.out.print("# ");
                }
                if (walls[row][col]) {
                    System.out.print("+");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println("");

        }
    }

    /**
     * function that iterates over the goals and walls structure and prints weather there is a 
     * wall or a goals in a specific square
     */
    public void printWallsAndGoals(boolean[][] walls, char[][] goals){
        for (int row = 0; row < walls.length; row++) {
            for (int col = 0; col < walls[row].length; col++) {
                if (col==0){
                    System.out.print("# ");
                }
                if (walls[row][col]) {
                    System.out.print("+");
                } else if (Character.isDigit(goals[row][col]) || Character.isLetter(goals[row][col])){
                    System.out.print(goals[row][col]);
                }  else {
                    System.out.print(" ");
                }
            }
            System.out.println("");

        }  
    }


    /**function that reads the position of the walls, and of the agents and prints 
     * an shi representation of the grid with agents represents by numbers and walls 
     * represented by + signs
     */
    public void printState() {
        for (int row = 0; row < this.walls.length; row++) {
            for (int col = 0; col < this.walls[row].length; col++) {
                if (this.boxes[row][col] > 0) {
                    System.out.print(this.boxes[row][col]);
                } else if (this.walls[row][col]) {
                    System.out.print("+");
                } else if (this.agentAt(row, col) != 0) {
                    System.out.print(this.agentAt(row, col));
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }


    public static Random getRng() {
        return RNG;
    }


    public int[] getAgentRows() {
        return agentRows;
    }

    public int getAgentRow(int agent) {
        return agentRows[agent];
    }


    public void setAgentRows(int[] agentRows) {
        this.agentRows = agentRows;
    }


    public int[] getAgentCols() {
        return agentCols;
    }

    public int getAgentCol(int agent) {
        return agentCols[agent];
    }

    public void setAgentCols(int[] agentCols) {
        this.agentCols = agentCols;
    }


    public static Color[] getAgentColors() {
        return agentColors;
    }


    public static void setAgentColors(Color[] agentColors) {
        State.agentColors = agentColors;
    }


    public static boolean[][] getWalls() {
        return walls;
    }


    public static void setWalls(boolean[][] walls) {
        State.walls = walls;
    }


    public char[][] getBoxes() {
        return boxes;
    }


    public void setBoxes(char[][] boxes) {
        this.boxes = boxes;
    }


    public static char[][] getGoals() {
        return goals;
    }


    public static void setGoals(char[][] goals) {
        State.goals = goals;
    }


    public static Color[] getBoxColors() {
        return boxColors;
    }


    public static void setBoxColors(Color[] boxColors) {
        State.boxColors = boxColors;
    }


    public State getParent() {
        return parent;
    }


    public Action[] getJointAction() {
        return jointAction;
    }


    public int getG() {
        return g;
    }


    public int getHash() {
        return hash;
    }


    public void setHash(int hash) {
        this.hash = hash;
    }


    public int getNumAgents() {
        return numAgents;
    }


    public void setNumAgents(int numAgents) {
        this.numAgents = numAgents;
    }

    public char[][] getRenamedBoxes() {
        char boxIndex = 'A';
        char[][] renamedBoxes = new char[this.boxes.length][this.boxes[0].length];
        for (int i = 0; i < this.boxes.length; i++) {
            for (int j = 0; j < this.boxes[i].length; j++) {
                if (this.boxes[i][j] != 0) {
                    renamedBoxes[i][j] = boxIndex;
                    boxIndex++;
                }
            }
        }
        return renamedBoxes;
    }

    // TO CHECK: not sure if we need this function, I think the solution only looks at joint actions so this would not be relevant.
    public void setOriginalBoxes() {
        for (int i = 0; i < this.boxes.length; i++) {
            for (int j = 0; j < this.boxes[i].length; j++) {
                if (this.boxes[i][j] != 0) {
                    this.boxes[i][j] = getOriginalBoxChar(this.boxes[i][j]);
                }
            }
        }
    }

    public int getNumBoxes() {
        int numBoxes = 0;
        for (int i = 0; i < this.boxes.length; i++) {
            for (int j = 0; j < this.boxes[i].length; j++) {
                if (this.boxes[i][j] != 0) {
                    numBoxes++;
                }
            }
        }
        return numBoxes;
    }

    public Color[] getRenamedColors() {
        int boxColorIndex = 0;
        Color[] renamedBoxColors = new Color[this.getNumBoxes()];
        for (int i = 0; i < this.boxes.length; i++) {
            for (int j = 0; j < this.boxes[i].length; j++) {
                if (this.boxes[i][j] != 0) {
                    renamedBoxColors[boxColorIndex] = this.boxColors[(int) this.boxes[i][j] - (int) 'A'];
                    boxColorIndex++;
                }
            }
        }
        return renamedBoxColors;

    }

        /**
     * explore all of the maze to see if there are disconnected parts
     * will be even more powerfull once we implement non movable boxes changing to walls
     */
    public void searchSubMazes(){
        // boolean[][] visited = new boolean[this.walls.length][this.walls[0].length];
        // for (int row = 0; row < visited.length; row++) {
        //     for (int col = 0; col < visited[row].length; col++) {
        //         visited[row][col] = false;
        //     }
        // }
        // ArrayList<boolean[][]> list_of_mazes = new ArrayList<>();
        // this.walls
    }

}
