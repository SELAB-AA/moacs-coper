/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

import java.util.Comparator;

/**
 *
 * @author aashraf
 */
public class SolutionCostComparator implements Comparator<DeploymentSolution> {
    public int compare(DeploymentSolution solution1, DeploymentSolution solution2){        
        //ascending order
        return Double.compare(solution1.cost, solution2.cost); 
        
	//descending order
	//return Double.compare(solution2.cost, solution1.cost); 
    }
}


    
