/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

/**
 *
 * @author aashraf
 */
public class Server implements ExperimentParameters{    
    String name;
    double performance; // in terms of MIPS, e.g., 2000 MIPS
    double reliability; // we use availability values in decimal range [0,1], e.g., "five nines" 0.99999.   
    double cost; //Cost is also important because the servers are not homogenous in terms of performance and reliability, so the cost can not be the same.
        
    public Server(String name, double performance, double reliability, double cost){        
        this.name=name;                
        this.performance=performance;
        this.reliability=reliability;
        this.cost=cost;
    }    
}
