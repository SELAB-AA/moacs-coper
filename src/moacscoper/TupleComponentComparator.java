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
public class TupleComponentComparator implements Comparator<DeploymentTuple> {
    public int compare(DeploymentTuple t1, DeploymentTuple t2){        
        //ascending order
        return Double.compare(t1.component.componentNumber, t2.component.componentNumber); 
        
	//descending order
	//return Double.compare(solution2.cost, solution1.cost); 
    }
}
