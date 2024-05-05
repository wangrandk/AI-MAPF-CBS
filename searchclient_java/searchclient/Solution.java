package searchclient;
import java.util.Arrays;
import searchclient.Position;

/**
 * TODO : we have to check if the list of positions 00 corresponds to the positions before or after the move 
 */
public class Solution{
    private Action[][] plan;
    private Position[][] positions;
    private Position[][] boxPositions;

    public Position[][] getPositions(){
        return this.positions;
    }
    public void setPositions(Position[][] positions){
        this.positions = positions;
    }
    public Position[][] getBoxPositions(){
        return this.boxPositions;
    }
    public void setBoxPositions(Position[][] boxPositions){
        this.boxPositions = boxPositions;
    }

    public void setSingleBoxPositions(Position[][] newBoxPositions, int box) {
        if (this.boxPositions[0].length == 0 || this.boxPositions == null) {
            this.boxPositions = Arrays.copyOf(newBoxPositions, newBoxPositions.length);
        } else {
            Position[][] temp = new Position[Math.max(this.boxPositions.length, newBoxPositions.length)][newBoxPositions[0].length];
            for (int i = 0; i < Math.max(this.boxPositions.length, newBoxPositions.length); i++) {
                for (int j = 0; j < newBoxPositions[0].length; j++) {
                    if (j == box) {
                        int index = Math.min(i, newBoxPositions.length - 1);
                        temp[i][j] = newBoxPositions[index][j];
                    } else {
                        int index = Math.min(i, this.boxPositions.length - 1);
                        temp[i][j] = this.boxPositions[index][j];
                    }
                }
            }
            this.boxPositions = temp;
        }
    }

    public Action[][] getPlan(){
        return this.plan;
    }
    public void setPlan(Action[][] plan){
        this.plan = plan;
    }
    //make an empty constructor
    public Solution(){
        this.plan = null;
        this.positions = null;
        this.boxPositions = null;
    }
    public Solution(Solution other) {
        if (other.plan != null) {
            this.plan = new Action[other.plan.length][];
            for (int i = 0; i < other.plan.length; i++) {
                this.plan[i] = Arrays.copyOf(other.plan[i], other.plan[i].length);
            }
        }

        if (other.positions != null) {
            this.positions = new Position[other.positions.length][];
            for (int i = 0; i < other.positions.length; i++) {
                this.positions[i] = Arrays.copyOf(other.positions[i], other.positions[i].length);
            }
        }

        if (other.boxPositions != null) {
            this.boxPositions = new Position[other.boxPositions.length][];
            for (int i = 0; i < other.boxPositions.length; i++) {
                this.boxPositions[i] = Arrays.copyOf(other.boxPositions[i], other.boxPositions[i].length);
            }
        }
    }

    public boolean validBoxPositions(Position[][] newBoxPositions, int box) {
        // If there is an agent at the same position as the box at time i, return false
        for (int i = 0; i < this.positions.length; i++) {
            int index = Math.min(i, newBoxPositions.length - 1);
            if (newBoxPositions[index][box].getX() == this.positions[i][0].getX() && newBoxPositions[index][box].getY() == this.positions[i][0].getY()) {
                return false;
            }
        }

        // If there is a different box at the same position as the box at time i, return false
        for (int i = 0; i < this.boxPositions.length; i++) {
            int index = Math.min(i, newBoxPositions.length - 1);
            for (int k = 0; k < this.boxPositions[i].length; k++) {
                if (k == box) {
                    continue;
                }
                if (newBoxPositions[index][box].getX() == this.boxPositions[i][k].getX() && newBoxPositions[index][box].getY() == this.boxPositions[i][k].getY()) {
                    return false;
                }
            }
        }
        return true;
    }
}