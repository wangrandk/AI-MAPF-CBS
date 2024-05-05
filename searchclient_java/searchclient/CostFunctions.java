package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CostFunctions {
    
    /**
     * This function calculates the cost of the solution based on the makespan
     * @param solution
     * @return length of the longest plan
     */
    public static Integer costFunctionMakespan(ArrayList<Solution> solution) {
        return solution.stream().map(e -> e.getPlan() == null ? Integer.MAX_VALUE : e.getPlan().length).reduce(0, Integer::max);
    }

    /**
     * This function calculates the cost of the solution based on the sum of the plans
     * @param solution
     * @return
     */
    public static Integer costFunctionSum(ArrayList<Solution> solution) {
        return solution.stream().map(e -> e.getPlan() == null ? Integer.MAX_VALUE : e.getPlan().length).reduce(0, Integer::sum);
    }


}
