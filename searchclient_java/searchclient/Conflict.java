package searchclient;

enum ConflictType {
    VERTEX,
    FOLLOW,
    AGENT_BOX,
    BOX
}

/**
 * it has as parameters an integer which is the number of the agent, 
 * a two dimensional coordiantewhich is the position of the conflict on the grid,
 * and another integer which 
 */
public class Conflict {
    public int agent1;
    public int agent2;
    public Position position;
    public int timeStep;
    public ConflictType conflictType;

    public Conflict(int agent1, int agent2, Position position, int timeStep, ConflictType conflictType) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.position = position;
        this.timeStep = timeStep;
        this.conflictType = conflictType;
    }

    public int getAgent1(){
        return this.agent1;
    }

    public int getAgent2(){
        return this.agent2;
    }

    public void setAgent1(int agent){
        this.agent1 = agent;
    }

    public void setAgent2(int agent){
        this.agent2 = agent;
    }

    public void setAgent(int agent1, int agent2) {
        this.agent1 = agent1;
        this.agent2 = agent2;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getTimeStep() {
        return this.timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public void setConflictType(ConflictType conflictType) {
        this.conflictType = conflictType;
    }

    public ConflictType getConflictType() {
        return this.conflictType;
    }
    
    /**
     * This method returns a constraint from a conflict.
     * @param agent
     * @return
     */
    public Constraint getConstraintFromConflict(int agent) {
        if (this.conflictType == ConflictType.VERTEX) {
            return new Constraint(agent, this.getPosition(), this.getTimeStep());
        } else if (this.conflictType == ConflictType.FOLLOW) {
            // Agent 1 follows agent 2 -> either agent 1 cannot be at time i+1 or agent 2 cannot be at time i 
            if (this.getAgent1() == agent || this.getAgent2() == agent) {
                return new Constraint(agent, this.getPosition(), this.getTimeStep() + 1);
            } else {
                if (this.getTimeStep() == 0) {
                    return null;
                } else {
                    return new Constraint(agent, this.getPosition(), this.getTimeStep());
                }
            }
        } else if (this.conflictType == conflictType.AGENT_BOX) {
            if (this.getAgent1() == agent) {
                return new Constraint(agent, this.getPosition(), this.getTimeStep());
            }
        }
        System.out.println("getConstraintFromConflict: Conflict type not recognized.");
        return null;

    }

    public Constraint getConstraintFromConflict(char box) {
        if (this.conflictType == ConflictType.AGENT_BOX) {
            if (this.getAgent2() == (int) (box - 'A')) {
                return new Constraint(box, this.getPosition(), this.getTimeStep());
            }
        } else if (this.conflictType == conflictType.BOX) {
            return new Constraint(box, this.getPosition(), this.getTimeStep());
        } else {
            System.out.println( "getConstraintFromConflict: Conflict type not recognized.");
        }
        return null;
    }

}


