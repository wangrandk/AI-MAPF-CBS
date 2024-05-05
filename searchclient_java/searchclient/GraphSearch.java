package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import searchclient.Constraint;

public class GraphSearch {
    /**
     * This function is used to perform a graph search on the given initial state.
     * behaves as A* and timespace A*
     * @param initialState
     * @param frontier
     * @param constraints
     * @return
     */
    public static State graphSearch(State initialState, Frontier frontier, ArrayList<Constraint> constraints) {

        int iterations = 0;
        frontier.add(initialState, 0);
        HashMap<State, ArrayList<Integer>> expanded = new HashMap<>(10536);

        while (!frontier.isEmpty()) {
            iterations += 1;
            //Print a status message every 10000 iteration
            // if (++iterations % 10000 == 0) {
            //      printSearchStatus(expanded, frontier);
            // }

            // Dequeue a state from frontier
            State state = frontier.pop();

            // Initialize the time-step of the current state
            int timeStep = frontier.getTimeForState(state);

            // Mark the state as expanded
            
            if(expanded.containsKey(state)) {
                expanded.get(state).add(timeStep);
            } else {
                expanded.put(state, new ArrayList<Integer>(Arrays.asList(timeStep)));
            }

            // Get all neighbors for the current state
            ArrayList<State> neighbors = state.getExpandedStates();

            // If this state is the goal state, return the actions used to reach it
            if (state.isGoalState() && state.adheresFinalConstraints(constraints, timeStep)) {
                return state;
            }

            // If a neighbor has not been visited, then mark it visited and enqueue it
            for (State neighbor : neighbors) {
                // Check whether the neighbor adheres to all the previously created constraints
                if (neighbor.adheresConstraints(constraints, timeStep + 1)

                    // Either expanded does not contain the state yet
                    && (expanded.get(neighbor) == null)
                        // Or the state has been expanded before, but not at the same time-step and it has not been considered more frequently than 10000 times
                        // || (expanded.get(neighbor) != null
                        //     && !expanded.get(neighbor).contains(timeStep + 1)
                        //     && expanded.get(neighbor).size() < 1000))
                    && !frontier.contains(neighbor)) {
                    frontier.add(neighbor, timeStep + 1);
                }
            }
        }
        System.out.println("# Cannot find a solution");
        return null;
    }


    /**
     * performs cbs on multi-agent problems
     * or executes specific actions with 1 as outputFixedSolution
     * @param initialState
     * @param frontier
     * @return
     */
    public static Action[][] search(State initialState, Frontier frontier)
    {
        // 1 : Fixed solution
        // 2 : Graph-Search R&N 3.7
        // 3 : CBS with A* heuristic
        int outputFixedSolution = 3; 

        if (outputFixedSolution == 1) {
            return new Action[][] {
                {Action.MoveE,Action.MoveN},
                {Action.MoveE,Action.MoveN},
                {Action.MoveE,Action.MoveN},
                {Action.MoveE,Action.MoveN},
            };
        }
        else if (outputFixedSolution == 2) {
            State finalState = graphSearch(initialState, frontier, new ArrayList<>());
            Action[][] plan = finalState.extractPlan();
            return plan;

        } else if (outputFixedSolution == 3) {
            // Split up the problem into sub-problems with 1 agent per problem

            State initialState2 = new State(
                initialState.getAgentRows(), 
                initialState.getAgentCols(),
                initialState.getAgentColors(),
                initialState.getWalls(),
                initialState.getRenamedBoxes(),
                initialState.getRenamedColors(),
                initialState.getGoals()
            );

            initialState2.initializeOriginalBoxMap(initialState.getBoxes());
            // System.out.println("#Printing the original box map");
            // for (Map.Entry<Character, Character> entry : initialState2.getOriginalBoxMap().entrySet()) {
            //     System.out.println("#Box: " + entry.getKey() + " Position: X: " + entry.getValue() + " Y: " + entry.getValue());
            // }

            ArrayList<State> singleAgentProblems =  initialState2.getSingleAgentProblems();
            if (singleAgentProblems.size() == 1) {
                Frontier newFrontier = new FrontierBestFirst(new HeuristicAStar(singleAgentProblems.get(0)));
                State finalState = graphSearch(singleAgentProblems.get(0), newFrontier, new ArrayList<>());
                Action[][] plan = finalState.extractPlan();
                // print plan
                for (int i = 0; i < plan.length; i++) {
                    System.out.println("#Time: " + i + " Action: " + plan[i][0]);
                }
                return plan;
            }

            // Initializing the root node
            CBSNode root = new CBSNode();

            for (int i = 0; i < singleAgentProblems.size(); i++) {
                System.out.println("# Getting the solution for single-agent: " + i);
                Frontier newFrontier = new FrontierBestFirst(new HeuristicAStar(singleAgentProblems.get(i)));
                
                State finalState = graphSearch(singleAgentProblems.get(i), newFrontier, root.getConstraints());

                // Add the solution for each agent to the root
                Solution solution = new Solution();
                solution.setPlan(finalState.extractPlan());
                solution.setPositions(finalState.extractPositions());

                Position[][] boxPositions = finalState.extractBoxPositions();
                solution.setBoxPositions(boxPositions);

                root.getSolutions().add(solution);
            }
            root.mergeBoxPositions();
        

            // Calculate the cost of the root solution
            root.setCost(CostFunctions.costFunctionMakespan(root.getSolutions()));

            CBSFrontier cbsFrontier = new CBSFrontier();
            cbsFrontier.add(root);

            int iterations = 0;

            while (!cbsFrontier.isEmpty()) {
                iterations += 1;    

                CBSNode currentNode = cbsFrontier.pop(); // Get lowest cost node


                System.out.println("# Printing the existing constraints on currentNode");
                for (Constraint cons : currentNode.getConstraints()) {
                    if (cons.constraintType == ConstraintType.AGENT) {
                        System.out.println("#Constraint: agent: " + cons.getAgent() + " timeStep "+ cons.getTimeStep() + " position X: "+ cons.getPosition().getX() + " , Y: " + cons.getPosition().getY() + " ");
                    } else {
                        System.out.println("#Constraint: box: " + cons.getBox() + " for agent: " + cons.getAgent() + "timeStep "+ cons.getTimeStep() + " position X: "+ cons.getPosition().getX() + " , Y: " + cons.getPosition().getY() + " ");
                    }
                    }

                System.out.println("# Printing the positions of the agents in solution currentNode");
                for (int i = 0; i < currentNode.getSolutions().size(); i++) {
                    System.out.println("#Printing for agent: " + i);
                    for (int j = 0; j < currentNode.getSolutions().get(i).getPositions().length; j++) {
                        for (int k = 0; k < currentNode.getSolutions().get(i).getPositions()[j].length; k++) {
                            System.out.println("#X: " + currentNode.getSolutions().get(i).getPositions()[j][k].getX() + " Y: " + currentNode.getSolutions().get(i).getPositions()[j][k].getY() + "; ");
                            if (j < currentNode.getSolutions().get(i).getPositions().length - 1) {
                                System.out.println("#Action: " + currentNode.getSolutions().get(i).getPlan()[j][k]);
                            }
                        }
                    }   
                }
       

                
                Conflict conflict = ConflictService.findFirstConflict(currentNode.getSolutions());

                // Checks if the currentNode is a valid solution, if it is, return it
                if (conflict == null) {
                    System.out.println("#No conflict found, returning plan");
                    for (Solution sol : currentNode.getSolutions()) {
                        System.out.println("#Printing solution");
                        for (int i = 0; i < sol.getPlan().length; i++) {
                            System.out.println("#Time: " + i + " Action: " + sol.getPlan()[i][0]);
                        }
                    }
                    Action[][] plan = currentNode.getMixedPlan();
                    return plan;
                }


                //generate 2 new nodes that are the clone of the old one but each one has a new constraint for one of the two conflicting agents
                //in this step the computation of the solution can be made easier for example if the cost is the highest cost
                //then by knowing the old cost we dont need to recompute them all bu just the one of the new solution
                // System.out.println("Initiatlizing for-loop");
                Integer[] agentList = null;
                Integer[] boxList = null;

                if (conflict.getConflictType() == ConflictType.VERTEX || conflict.getConflictType() == ConflictType.FOLLOW) {
                    agentList = new Integer[]{conflict.getAgent1(), conflict.getAgent2()};
                } else if (conflict.getConflictType() == ConflictType.AGENT_BOX) {
                    agentList = new Integer[]{conflict.getAgent1()};
                    boxList = new Integer[]{conflict.getAgent2()};
                } else if (conflict.getConflictType() == ConflictType.BOX) {
                    boxList = new Integer[]{conflict.getAgent1(), conflict.getAgent2()};
                }

                if (agentList != null) {
                    for (int agent : agentList) {
                        System.out.println("# Resolving agent-conflict for agent: " + agent);
                        CBSNode newNode = new CBSNode(currentNode);
                        Constraint constraint = new Constraint(conflict.getConstraintFromConflict(agent));

                        if (newNode.hasConstraint(constraint)) {
                            continue;
                        }

                        newNode.addConstraint(constraint);
                        Frontier newFrontier = new FrontierBestFirst(new HeuristicAStar(singleAgentProblems.get(agent)));
                        State finalState = graphSearch(singleAgentProblems.get(agent), newFrontier, newNode.getSingleAgentConstraints(agent));

                        // If we return null, than an agent cannot find a solution under the constraints, so consider the other side of the constraint-tree.
                        if (finalState == null) {
                            continue;
                        }
                        Solution solution = new Solution();
                        solution.setPlan(finalState.extractPlan());
                        solution.setPositions(finalState.extractPositions());
                        solution.setBoxPositions(finalState.extractBoxPositions());

                        newNode.getSolutions().set(agent, solution); // this graphSearch should be spacetime A* search
                        newNode.setCost(CostFunctions.costFunctionMakespan(newNode.getSolutions()));
                        cbsFrontier.add(newNode);
                        System.out.println("# Found a valid solution, adding it to the Frotnier " + agent);

                    }
                }
                // If we have a new constraint on a box, we try to resolve the constraint for all agents to assess which one can move the box away most efficiently
                if (boxList != null) {
                    for (int box : boxList) {
                        for (int agent = 0; agent < initialState.getNumAgents(); agent++) {
                            if (initialState.agentColors[agent] == initialState.boxColors[box]) {
                                System.out.println("# Resolving box-conflict for box: " + (char) (box + 'A') + " by agent: " + agent);
                                CBSNode newNode = new CBSNode(currentNode);
                                Constraint constraint = new Constraint(conflict.getConstraintFromConflict((char) (box + 'A')));

                                // We set the agent in the constraint, to the agent who will have to resolve the conflict:
                                constraint.setAgent(agent);

                                if (newNode.hasConstraint(constraint)) {
                                    continue;
                                }

                                newNode.addConstraint(constraint);
                                Frontier newFrontier = new FrontierBestFirst(new HeuristicAStar(singleAgentProblems.get(agent)));
                                State finalState = graphSearch(singleAgentProblems.get(agent), newFrontier, newNode.getSingleAgentConstraints(agent));
            
                                // If we return null, than an agent cannot find a solution under the constraints, so consider the other side of the constraint-tree.
                                if (finalState == null) {
                                    continue;
                                }
                                Solution solution = new Solution();
                                solution.setPlan(finalState.extractPlan());
                                solution.setPositions(finalState.extractPositions());
                                
                                Position[][] boxPositions = finalState.extractBoxPositions();
                                solution.setBoxPositions(boxPositions);
            
                                newNode.getSolutions().set(agent, solution); // this graphSearch should be spacetime A* search

                                // Updating the box positions for every other agent, such that they take into account how the agent moved the box
                                boolean validSolution = true;
                                for (int otherAgt = 0; otherAgt < currentNode.getSolutions().size(); otherAgt++) {
                                    if (otherAgt != agent) {
                                        if (newNode.getSolutions().get(otherAgt).validBoxPositions(boxPositions, box)) {
                                            continue;
                                        } else {
                                            System.out.println("#Found a conflict with a different agent their box position plan");
                                            validSolution = false;
                                            break;
                                        }
                                    }
                                }

                                if (!validSolution) {
                                    continue;
                                } else {
                                    newNode.mergeBoxPositions();
                                }

                                newNode.setCost(CostFunctions.costFunctionMakespan(newNode.getSolutions()));
                                cbsFrontier.add(newNode);
                            }
                        }
                    }

                }
            }
            System.out.println("# CBS Frontier is empty");

            // If we get out her it means that the frontier is empty and we have no solution
            return null;
        } else {
            return null;
        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashMap<State, ArrayList<Integer>> expanded, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, expanded.size(), frontier.size(), expanded.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }
}
