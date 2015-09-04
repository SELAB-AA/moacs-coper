/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aashraf
 */
public class DeploymentSolution {
    List<DeploymentTuple> tuples;
    double cost; // total cost of the entire solution. Cost is also important because the servers are not homogenous in terms of performance and reliability, so the cost can not be the same.
    double performance; // total performance of the entire solution. in terms of MIPS, e.g., 20000 MIPS
    double reliability; // total reliability of the entire solution. we use availability values in decimal range [0,1], e.g., "five nines" 0.99999.   
    int solutionNumber;
    static int staticSolutionCounter=0;    
    
    public DeploymentSolution(){
        this.tuples = new ArrayList();
        this.performance=0.0;
        this.reliability=0.0;
        this.cost=0.0;
        staticSolutionCounter+=1;
        this.solutionNumber=staticSolutionCounter;
    }
    
    public void computeCostOfSolution(){
        this.cost=0.0;
        List<Server> servers=this.getServers();
        for(Server server: servers){            
            this.cost+=server.cost; // every server is counted only once
        }        
    }
    
    public void computePerformanceOfSolution(){
        this.performance=0.0;
        List<Server> servers=this.getServers();
        for(Server server: servers){            
            this.performance+=server.performance;
        }
    }
    
    public void computeReliabilityOfSolution(){
        List<Server> servers=this.getServers();        
        this.reliability=1.0;        
        for(Server server: servers){
            this.reliability*=server.reliability;
        }
    }
    
    public List<Server> getServers(){
        List<Server> servers=new ArrayList();
        for(DeploymentTuple tuple: this.tuples){
            if(!servers.contains(tuple.destination)){
                servers.add(tuple.destination);                
            }
        }
        return servers;
    }
    
}
