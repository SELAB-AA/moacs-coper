/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

/**
 *
 * @author aashraf
 */
public interface ExperimentParameters {        
    final static int MIN_USED_CAPACITY=1; 
    final static int MAX_USED_CAPACITY=2; 
    
    final static int APP_ALLOCATION_POLICY= MIN_USED_CAPACITY;
        
    final static long REAL_TO_SIMULATED_TIME_CONVERSION_FACTOR=100; 
    
    final static int RANDOM_SEED = 111113333; // seed for random number generation
    
    final static int LENGTH_RENTING_HOUR_IN_SECONDS=3600; //assuming that renting period = 1 hour (3600s)
    final static int REMAINING_TIME_UPPER_THRESHOLD=900; 
    final static int REMAINING_TIME_LOWER_THRESHOLD=120; 

    final static double TOTAL_CAPACITY=100; 
    
    final static double CPU_UNDERUTILIZATION_THRESHOLD = 90; 
    final static double MEM_UNDERUTILIZATION_THRESHOLD = 90; 
    
    final static double DESIRED_CPU_UTILIZATION_THRESHOLD = 90; 
    final static double DESIRED_MEM_UTILIZATION_THRESHOLD = 90; 
    
    final static double HOW_MUCH_MAX_CPU_OF_A_SERVER_MAY_AN_APP_INSTANCE_CONSUME = 40.0; 
    final static double HOW_MUCH_MIN_CPU_OF_A_SERVER_MAY_AN_APP_INSTANCE_CONSUME = 5.0; 
    
    final static int HOW_MANY_MAX_INSTANCES_OF_AN_APP = 2; 
    
    final static int MAX_APP_CPU_MEM_DIFF_PERCENTAGE = 10; 
}
