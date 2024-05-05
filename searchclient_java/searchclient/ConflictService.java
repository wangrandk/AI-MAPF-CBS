package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ConflictService {
    

    /**
     * This function checks if the solution is valid and returns the first conficlt that it encounters
     * @param solution
     * @return the first conflict in <i>solution</i> or else null
     */ 
    public static Boolean isValid (ArrayList<Solution> solutions) {
        Conflict firstConflict = findFirstConflict(solutions);
        if(firstConflict == null)
        {
            return true;
        }
        else
        {
            // System.out.println("#ConflictService.IsValid: Conflict found at time " + firstConflict.getTimeStep() + " between agents " + firstConflict.getAgent1() + " and " + firstConflict.getAgent2() + " at position " + firstConflict.getPosition().getX()+", "+firstConflict.getPosition().getY()+", for action: ");
            return false;
        }
    }

    /**
     * This function checks if the conflict between agent1 and agent2 at time is the first occurrence of the conflict
     * @param conflicts
     * @param time
     * @param agent1
     * @param agent2
     * @return true if the conflict is the first occurrence, false otherwise
     */
    private static boolean firstEcountOfTheConflict(ArrayList<Conflict> conflicts, Integer time, Integer agent1, Integer agent2) {
        return conflicts.stream().filter(e -> 
            ((e.getAgent1() == agent1 && e.getAgent2() == agent2) ||
            (e.getAgent1() == agent2 && e.getAgent2() == agent1)) &&
            e.getTimeStep() == time).findFirst().isEmpty();
    }


     /**
     * This function checks if the solution is valid and returns the first conficlt that it encounters
     * @param solutions
     * @return the first conflict in <i>solution</i>
     */
    public static Conflict findFirstConflict(ArrayList<Solution> solutions) {
        System.out.println("#Finding first conflict...");
        int maxLength = 0;
        for (Solution solution : solutions) {
            if (solution.getPositions().length > maxLength) {
                maxLength = solution.getPositions().length;
            }
        }

        // System.out.println("Start looking for the first conflict");
        for (int i = 0; i < maxLength; i++) {
            for (Solution solution1 : solutions) {
                for (Solution solution2: solutions) {
                    // Loop over all possible combinations of solutions
                    if (solutions.indexOf(solution1) != solutions.indexOf(solution2)) { 
                        if (solution1.getPositions().length > i || solution2.getPositions().length > i) {
                            int index1 = Math.min(i, solution1.getPositions().length-1);
                            int index2 = Math.min(i, solution2.getPositions().length-1);

                            // Check if no two agents are at the same position
                            if (solution1.getPositions()[index1][0].getX() == solution2.getPositions()[index2][0].getX()
                                && solution1.getPositions()[index1][0].getY() == solution2.getPositions()[index2][0].getY()) {
                                    Conflict conflict = new Conflict(solutions.indexOf(solution1), solutions.indexOf(solution2), new Position(solution1.getPositions()[index1][0].getX(), solution1.getPositions()[index1][0].getY()), i, ConflictType.VERTEX);
                                    System.out.println("#Timestep: " + i + " Type: Vertex " + "Agents: " + solutions.indexOf(solution1) + ", " + solutions.indexOf(solution2) + " Position: " + solution1.getPositions()[index1][0].getX() + ", " + solution1.getPositions()[index1][0].getY());
                                    return conflict;
                            }

                            // Check if there is no "follow" conflict
                            // Check that agent 1 and agent 2 are moving
                            if (i < solution1.getPositions().length - 1 && i < solution2.getPositions().length - 1
                                && (solution1.getPositions()[i][0].getX() != solution1.getPositions()[i + 1][0].getX() ||
                                    solution1.getPositions()[i][0].getY() != solution1.getPositions()[i + 1][0].getY())
                                && (solution2.getPositions()[i][0].getX() != solution2.getPositions()[i + 1][0].getX() ||
                                    solution2.getPositions()[i][0].getY() != solution2.getPositions()[i + 1][0].getY())) {

                                    // Check if the position of agent 1 at time i+1 is the same as the position of agent 2 at time i => 1 is following 2
                                    if (solution1.getPositions()[i + 1][0].getX() == solution2.getPositions()[index2][0].getX()
                                        && solution1.getPositions()[i + 1][0].getY() == solution2.getPositions()[index2][0].getY()) {
                                            // System.out.println("Agent: " + solutions.indexOf(solution1) + " follow conflict with " + solutions.indexOf(solution2));
                                            Conflict conflict = new Conflict(solutions.indexOf(solution1), solutions.indexOf(solution2), new Position(solution1.getPositions()[i + 1][0].getX(), solution1.getPositions()[i + 1][0].getY()), i, ConflictType.FOLLOW);
                                            System.out.println("#Timestep: " + i + " Type: Follow " + "Agents: " + solutions.indexOf(solution1) + ", " + solutions.indexOf(solution2) + " Position: " + solution1.getPositions()[i][0].getX() + ", " + solution1.getPositions()[i][0].getY());
                                            return conflict;
                                    }
                            }
                            
                            index2 = Math.min(i, solution2.getBoxPositions().length-1);
                            for (int j = 0; j < solution2.getBoxPositions()[index2].length; j++) {
                                if (solution1.getPositions()[index1][0].getX() == solution2.getBoxPositions()[index2][j].getX()
                                && solution1.getPositions()[index1][0].getY() == solution2.getBoxPositions()[index2][j].getY()) {
                                    Conflict conflict = new Conflict(solutions.indexOf(solution1), j, new Position(solution1.getPositions()[index1][0].getX(), solution1.getPositions()[index1][0].getY()), i, ConflictType.AGENT_BOX);
                                    System.out.println("#Timestep: " + i + " Type: Agent-Box " + "Agent: " + solutions.indexOf(solution1) + ", Box: " + (char) (j + 'A') + " Position: " + solution1.getPositions()[index1][0].getX() + ", " + solution1.getPositions()[index1][0].getY());
                                    return conflict;
                                }
                            }
                            
                            index1 = Math.min(i, solution1.getBoxPositions().length-1);
                            for (int j = 0; j < solution1.getBoxPositions()[index1].length; j++) {
                                for (int k = 0; k < solution2.getBoxPositions()[index2].length; k++) {
                                    if (solution1.getBoxPositions()[index1][j].getX() == solution2.getBoxPositions()[index2][k].getX()
                                    && solution1.getBoxPositions()[index1][j].getY() == solution2.getBoxPositions()[index2][k].getY()
                                    && j != k) {
                                        Conflict conflict = new Conflict(j, k, new Position(solution1.getBoxPositions()[index1][j].getX(), solution1.getBoxPositions()[index1][j].getY()), i, ConflictType.BOX);
                                        System.out.println("#Timestep: " + i + " Type: Box, Boxes: " + (char) (j + 'A') + ", " + (char) (k + 'A') + " Position: " + solution1.getBoxPositions()[index1][j].getX() + ", " + solution1.getBoxPositions()[index1][j].getY());
                                        return conflict;
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }

        // //agentposition{key : time, value : {key : agent, value : position}
        // Map<Integer, Map<Integer, Position>> agentPositions = new HashMap<>();

        // // Create a map {key = agent, value = {map {key = time, value = position}}
        // for(int i  = 0; i < solutions.size(); i++){
        //     Solution solutionForAgentI = solutions.get(i);

        //     // For j from 0 to the length of the plan of the agent
        //     for(int j = 0; j < solutionForAgentI.getPlan().length; j++){
        //         if(!agentPositions.containsKey(j)){
        //             agentPositions.put(j, new HashMap<>());
        //         }
        //         agentPositions.get(j).put(i, solutionForAgentI.getPositions()[j][0]);
        //     }
        // }

        // //use the structure of the agentPositions to check if there are conflicts
        // for(Map.Entry<Integer, Map<Integer, Position>> time : agentPositions.entrySet()){
        //     Map<Integer, Position> agents = time.getValue();

        //     // get the previous time step's positions
        //     Map<Integer, Position> previousAgents = agentPositions.get(time.getKey() - 1); 
        //     for(Map.Entry<Integer, Position> agent1 : agents.entrySet()){
        //         for(Map.Entry<Integer, Position> agent2 : agents.entrySet()){
        //             // Checking for a vertex conflict
        //             if (agent1.getKey() != agent2.getKey() && agent1.getValue().getX() == agent2.getValue().getX() && agent1.getValue().getY() == agent2.getValue().getY()){
        //                 Conflict conflict = new Conflict(agent1.getKey(), agent2.getKey(), new Position(agent1.getValue().getX(), agent1.getValue().getY()), time.getKey());
        //                 System.out.println("Found first conflict: " + conflict);
        //                 return conflict;
        //             }
        //         }
        //         if (previousAgents != null) { // check if there was a previous time step
        //             // Iterate over the previous time step's positions
        //             for(Map.Entry<Integer, Position> agent2 : previousAgents.entrySet()){ 
        //                 // Checking follow conflict (edge conflict is a double edge conflict)
        //                 if(agent1.getKey() != agent2.getKey() && agent1.getValue().getX() == agent2.getValue().getX() && agent1.getValue().getY() == agent2.getValue().getY()){
        //                     Conflict conflict = new Conflict(agent1.getKey(), agent2.getKey(), new Position(agent1.getValue().getX(), agent1.getValue().getY()), time.getKey());
        //                     System.out.println("Found first conflict: " + conflict);
        //                     return conflict;
        //                 }
        //             }
        //         }
        //     }
        // }
        
        return null;
    }
}

//     /**
//      * this function returns an arraylist with all the conflict happening at the first time step where a conflict is found
//      * @param solution
//      * @return the list of conflicts in the <i>solution</i>
//      */
//     private static ArrayList<Conflict> findAllEarlyConflicts(ArrayList<Solution> solutions) {
       
//         // agent position{key : time, value : {key : agent, value : position}
//         Map<Integer, Map<Integer, Position>> agentPositions = new HashMap<>();
//         ArrayList<Conflict> conflicts = new ArrayList<>();
//         Integer conflictTime = null;


//         //for every solution object solution add an entry to the map agentPositions and put as value a map the time as key and the position as value
//         for(int i  = 0; i < solutions.size(); i++){
//             Solution solutionForAgentI = solutions.get(i);
//             for(int j = 0; j < solutionForAgentI.getPlan().length; j++){
//                 if(!agentPositions.containsKey(j)){
//                     agentPositions.put(j, new HashMap<>());
//                 }
//                 agentPositions.get(j).put(i, solutionForAgentI.getPositions()[j][0]);
//             }
//         }
 
//         //use the structure of the agentPositions to check if there are conflicts
//         // return all the conflicts that happen at the time where is founded the first conflict
//         for(Map.Entry<Integer, Map<Integer, Position>> time : agentPositions.entrySet()){
//             Map<Integer, Position> agents = time.getValue();
//             Map<Integer, Position> previousAgents = agentPositions.get(time.getKey() - 1); // get the previous time step's positions
//             for(Map.Entry<Integer, Position> agent1 : agents.entrySet()){
//                 for(Map.Entry<Integer, Position> agent2 : agents.entrySet()){
//                     if(agent1.getKey() != agent2.getKey() && agent1.getValue().getX() == agent2.getValue().getX() && agent1.getValue().getY() == agent2.getValue().getY()){ //checking vertex conflict
//                         if (conflictTime == null) {
//                             conflictTime = time.getKey();
//                         }
//                         if (time.getKey().equals(conflictTime)) {
//                             conflicts.add(new Conflict(agent1.getKey(), agent2.getKey(), new Position(agent1.getValue().getX(), agent1.getValue().getY()), time.getKey()));
//                         }
//                     }
//                 }
//                 if (previousAgents != null) { // check if there was a previous time step
//                     for(Map.Entry<Integer, Position> agent2 : previousAgents.entrySet()){ // iterate over the previous time step's positions
//                         if(agent1.getKey() != agent2.getKey() && agent1.getValue().getX() == agent2.getValue().getX() && agent1.getValue().getY() == agent2.getValue().getY()){//checking follow conflict (edge conflict is a double edge conflict)
//                             if (conflictTime == null) {
//                                 conflictTime = time.getKey();
//                             }
//                             if (time.getKey().equals(conflictTime)) {
//                                 conflicts.add(new Conflict(agent1.getKey(), agent2.getKey(), new Position(agent1.getValue().getX(), agent1.getValue().getY()), time.getKey()));
//                             }
//                         }
//                     }
//                 }
//             }
//             if (conflictTime != null) {
//                 break; // stop the loop as soon as we have found all conflicts for the first conflict time
//             }
//         }
        
//         return conflicts;
        
//     }
// }
