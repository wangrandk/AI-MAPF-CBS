package searchclient;

import java.util.ArrayList;

public class CBSNode {
        private ArrayList<Solution> solutions = new ArrayList<Solution>();
        private Integer cost;
        private ArrayList<Constraint> constraints = new ArrayList<>();

        public ArrayList<Solution> getSolutions() {
            return this.solutions;
        }
        public void setSolutions(ArrayList<Solution> solutions) {
            this.solutions = solutions;
        }
        public Integer getCost() {
            return cost;
        }
        public void setCost(Integer cost) {
            this.cost = cost;
        }
        public ArrayList<Constraint> getConstraints() {
            return this.constraints;
        }
        public void setConstraints(ArrayList<Constraint> constraints) {
            this.constraints = constraints;
        }
        public void addConstraint(Constraint constraint){
            this.constraints.add(constraint);
        }

        public boolean hasConstraint(Constraint constraint) {
            if (constraint.constraintType == ConstraintType.AGENT) {
                for (Constraint c : this.constraints) {
                        if (c.getAgent() == constraint.getAgent()
                            && c.getPosition().getX() == constraint.getPosition().getX()
                            && c.getPosition().getY() == constraint.getPosition().getY()
                            && c.getTimeStep() == constraint.getTimeStep()
                            && c.getConstraintType() == constraint.getConstraintType()){
                            return true;
                        }
                }
            } else {
                for (Constraint c : this.constraints) {
                    if (c.constraintType == ConstraintType.BOX) {
                        if (c.getAgent() == constraint.getAgent()
                            && c.getBox() == constraint.getBox()
                            && c.getPosition().getX() == constraint.getPosition().getX()
                            && c.getPosition().getY() == constraint.getPosition().getY()
                            && c.getTimeStep() == constraint.getTimeStep()){
                                return true;
                        }
                    }
                }
            }
            return false;
        }

        public void mergeBoxPositions() {
            for (Solution solution1 : this.solutions) {
                if (solution1.getBoxPositions().length <= 1) {
                    continue;
                } else {
                    for (int i = 0; i < solution1.getBoxPositions().length -1; i++) {
                        for (int box = 0; box < solution1.getBoxPositions()[0].length; box++) {
                                // Solution 1 moved a box. We now update this box-movement in every other box positions
                                if (solution1.getBoxPositions()[i][box].getX() != solution1.getBoxPositions()[i+1][box].getX()
                                || solution1.getBoxPositions()[i][box].getY() != solution1.getBoxPositions()[i+1][box].getY()) {
                                    for (Solution solution2 : this.solutions) {
                                        if (solution1 != solution2 && solution2.getBoxPositions().length > 1) {
                                            System.out.println("# Merging box positions for box: " + (char) ('A' + box) + " at time: " + i + " for solution 2: " + 
                                            this.solutions.indexOf(solution2) + " of length " + solution2.getBoxPositions().length + " and solution 1: " + this.solutions.indexOf(solution1) + " of length "+ solution1.getBoxPositions().length +"  respectively");
                                            solution2.setSingleBoxPositions(solution1.getBoxPositions(), box);
                                        }
                                    }
                                continue;
                            }
                        }
                    }
                }
            }
            // for (Solution solution : this.solutions) {
            //     for (int i = 0; i < solution.getBoxPositions().length; i++) {
            //         for (int j = 0; j < solution.getBoxPositions()[i].length; j++) {
            //             System.out.println("#At time: " + i + ", box " + (char) ('A' + j) + " is at position: " + solution.getBoxPositions()[i][j].getX() + ", " + solution.getBoxPositions()[i][j].getY());
            //         }
            //     }
            // }
        }

        /**
         * constructor that takes in input a node and creates another identical node
         * @param node
         * @return
         */
        public CBSNode(CBSNode node) {
            this.solutions = new ArrayList<>();
            for (Solution solution : node.getSolutions()) {
                this.solutions.add(new Solution(solution));  // This requires Solution to have a copy constructor
            }

            this.cost = node.getCost();

            this.constraints = new ArrayList<>();
            for (Constraint constraint : node.getConstraints()) {
                this.constraints.add(new Constraint(constraint));  // This requires Constraint to have a copy constructor
            }
        }

        /**
         * empty constuctor
         * @return
         */
        public CBSNode(){
            this.solutions = new ArrayList<Solution>();
            this.cost = null;
            this.constraints = new ArrayList<Constraint>();
        }

/**
         * this functions iterates over all the plans in all the actions and generates one plan that is a two nested arrays 
         * outer array is the time and inner array is the action that every agent does at time t
         * @param node
         * @return
         */


        public Action[][] getMixedPlan(){
            // int planLength = this.solutions.get(0).getPlan().length;
            int longestSol = CostFunctions.costFunctionMakespan(this.solutions);
            int numAgents = this.solutions.size();
            Action[][] mixedPlan = new Action[longestSol][numAgents];
            //this one crashes if there is less than 2 agents
            // System.out.println("#this is plan length " + this.solutions.get(0).getPlan().toString());
            // System.out.println("#this is plan length " + this.solutions.get(1).getPlan().toString());

            for (int i = 0; i < longestSol; i++) {
                for (int j = 0; j < numAgents; j++) {
                    if( i < this.solutions.get(j).getPlan().length) {
                        // System.out.println("# Mixed plan: " + this.solutions.get(j).getPlan()[i][0] + " for agent: " + j + " at time: " + i);
                        mixedPlan[i][j] = this.solutions.get(j).getPlan()[i][0];
                    }
                    else {
                        mixedPlan[i][j] = Action.NoOp;
                        // System.out.println("# Mixed plan: NoOp for agent: " + j + " at time: " + i);
                    }
                }
            }
            return mixedPlan;
        }

        public ArrayList<Constraint> getSingleAgentConstraints(int agent) {
            ArrayList<Constraint> singleAgentConstraints = new ArrayList<Constraint>();
            for (Constraint constraint : this.constraints) {
                if (constraint.getAgent() == agent) {
                    singleAgentConstraints.add(constraint);
                }
            }
            return singleAgentConstraints;
        }
}


