package searchclient;

enum ConstraintType {
    AGENT,
    BOX
}

public class Constraint {
    public Integer agent;
    public Character box;
    public Position position;
    public int timeStep;
    public ConstraintType constraintType;

    public Constraint(Constraint other) {
        this.agent = other.agent;
        this.box = other.box;
        this.position = new Position(other.position);  // This requires Position to have a copy constructor
        this.timeStep = other.timeStep;
        this.constraintType = other.constraintType;
    }

    public Constraint(int agent, Position position, int timeStep) {
        this.agent = agent;
        this.box = null;
        this.position = position;
        this.timeStep = timeStep;
        this.constraintType = ConstraintType.AGENT;
    }

    public Constraint(char box, Position position, int timeStep) {
        this.agent = null;
        this.box = box;
        this.position = position;
        this.timeStep = timeStep;
        this.constraintType = ConstraintType.BOX;
    }

    public void printConstraint() {
        if (this.constraintType == ConstraintType.AGENT) {
            System.out.println("#Agent-constraint: " + this.agent + " at position: " + this.position.getX() + ", " + this.position.getY() + " at time: " + this.timeStep);
        } else {
            System.out.println("#Box-constraint: " + this.box + " for agent " + this.agent + "at position: " + this.position.getX() + ", " + this.position.getY() + " at time: " + this.timeStep);
        }
    }

    public int getAgent(){
        return this.agent;
    }

    public ConstraintType getConstraintType(){
        return this.constraintType;
    }

    public void setConstraintType(ConstraintType type){
        this.constraintType = type;
    }

    public void setAgent(int agent){
        this.agent=agent;
    }

    public char getBox(){
        return this.box;
    }

    public void setBox(char box){
        this.box = box;
    }


    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

}