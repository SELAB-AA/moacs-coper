/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author SELAB
 */
public class Ant implements Runnable, ExperimentParameters{
    int antNumber;
    ExperimentalSetupAndReporting sim;
    
    DeploymentSolution deploymentSolution; // app migration plan of an ant. An ant makes a migration plan in each iteration of the main loop.
    
    List<DeploymentTuple> traversedTuples; //List of tulpes that have been traversed. NB: it is like ant's internal memory of visited states.
    
    HashMap<Server, Double> server2allocatedPerformance; // Integer because it is in MIPS
    
    final static double Q0=0.9;
    
    List<SoftwareComponent> deployedComponents;
    List<DeploymentTuple> setOfTuplesWhoseComponentNotYetDeployed; // local copy of ant. it contains only those tuples where tuple.component is not yet deployed
    
    HashMap<DeploymentTuple, Double> tuples2heuristic1; //(tuple, heuristic1 value of tuple). 
    HashMap<DeploymentTuple, Double> tuples2heuristic2; //(tuple, heuristic1 value of tuple). 
    
    HashMap<DeploymentTuple, Double> tuples2prob; //(tuple, probability of tuple). 
    
    public Ant(int antNumber, ExperimentalSetupAndReporting sim){
        this.antNumber=antNumber;
        this.sim=sim;
        this.deploymentSolution=new DeploymentSolution();        
        this.traversedTuples=new ArrayList();
        this.deployedComponents=new ArrayList();
        
        this.tuples2heuristic1=new HashMap();
        this.tuples2heuristic2=new HashMap();
        
        this.tuples2prob=new HashMap();
        
    }
    
    @Override
    public void run(){
        this.initializeHeuristicsAndProbabilities();
        this.initializeUsedCapacities();
        int numAllocatedComponents=0;
        this.setOfTuplesWhoseComponentNotYetDeployed=new ArrayList(this.sim.antController.setOfAllTuples);
        
        while(numAllocatedComponents<this.sim.components.size()){ // while all components are allocated
            
            this.updateHeuristicValues(); // this should be done in both cases whether q>Q0 or not because heuristic values are used in both cases
            
            DeploymentTuple selectedTuple=null;
            double q=this.sim.random.nextDouble(); 
            
            if(this.setOfTuplesWhoseComponentNotYetDeployed.isEmpty()){
                break;
            }
            
            if(q>Q0){
                // calculate and use probabilities
                this.updateProbabilities();
                int randomIndex=this.sim.random.nextInt(this.setOfTuplesWhoseComponentNotYetDeployed.size());
                DeploymentTuple maxProbTuple=this.setOfTuplesWhoseComponentNotYetDeployed.get(randomIndex); // tuple with max. probability            
                for(DeploymentTuple mt:this.setOfTuplesWhoseComponentNotYetDeployed){
                    if(this.tuples2prob.get(mt)>this.tuples2prob.get(maxProbTuple)){
                        maxProbTuple=mt;                        
                    }
                }
                selectedTuple=maxProbTuple;                
            }
            else{ //when q<=Q0
                // no need to calculate probabilities
                DeploymentTuple argMaxTuple=null;
                double LAMBDA=this.antNumber/(double)this.sim.antController.N_ANTS;
                
                // change selectedTuple according to arg max from !traversedTuples
                for(DeploymentTuple mt:this.setOfTuplesWhoseComponentNotYetDeployed){                    
                        if(argMaxTuple==null){
                            argMaxTuple=mt;                            
                        }
                        else{
                            double pheromone=this.sim.antController.tuples2pheromone.get(mt);
                            double heuristic1=this.tuples2heuristic1.get(mt);
                            double heuristic2=this.tuples2heuristic2.get(mt);
                            double pheromoneIntoHeuristicPowBeta = pheromone * Math.pow( heuristic1, (this.sim.antController.BETA*(LAMBDA)) ) *
                                    Math.pow( heuristic2, (this.sim.antController.BETA * (1-LAMBDA) ) );
                            
                            double pheromoneArgMax=this.sim.antController.tuples2pheromone.get(argMaxTuple);    
                            double heuristic1ArgMax=this.tuples2heuristic1.get(argMaxTuple);
                            double heuristic2ArgMax=this.tuples2heuristic2.get(argMaxTuple);
                            double pheromoneIntoHeuristicPowBetaOfArgMax = pheromoneArgMax * Math.pow(heuristic1ArgMax, (this.sim.antController.BETA*(LAMBDA)) ) *
                                    Math.pow(heuristic2ArgMax, (this.sim.antController.BETA * (1-LAMBDA) ) );
                            if(pheromoneIntoHeuristicPowBeta>pheromoneIntoHeuristicPowBetaOfArgMax){                                
                                argMaxTuple=mt;
                            }
                        }                    
                }
                selectedTuple=argMaxTuple;
            }            
            
            if(selectedTuple==null){
                break; // if no more tuples are left, break the while loop
            }
            
            this.traversedTuples.add(selectedTuple);
            
            this.setOfTuplesWhoseComponentNotYetDeployed.remove(selectedTuple);
            
            // use ACS local pheromone update rule
            if(AntController.PHEROMONE_0==0.0){
                this.sim.antController.computePHEROMONE_0();
            }            
            
            this.sim.antController.tuples2pheromone.put(selectedTuple, (this.sim.antController.tuples2pheromone.get(selectedTuple) - (this.sim.antController.RHO*AntController.PHEROMONE_0) ));
            
            if(!this.deployedComponents.contains(selectedTuple.component)){ 
                // if deployment of c does not overload the destination VM in tuple t
                if((selectedTuple.component.requiredPerformance + this.server2allocatedPerformance.get(selectedTuple.destination)) <= selectedTuple.destination.performance){
                    // if the deployment of component c on VM v satisfies the reliability requirement of c
                    if(selectedTuple.component.requiredReliability <= selectedTuple.destination.reliability){
                        // update the allocated processing speed of VM v
                        double updatedPerformanceAtDestination = this.server2allocatedPerformance.get(selectedTuple.destination)+selectedTuple.component.requiredPerformance;
                        this.server2allocatedPerformance.put(selectedTuple.destination, updatedPerformanceAtDestination);
                        // add selectedTuple to ant-specific deployment solution
                        this.deploymentSolution.tuples.add(selectedTuple);
                        this.deployedComponents.add(selectedTuple.component);
                        
                        for(int i=0; i<this.setOfTuplesWhoseComponentNotYetDeployed.size(); i++){
                            if(selectedTuple.component.equals(this.setOfTuplesWhoseComponentNotYetDeployed.get(i).component)){
                                this.setOfTuplesWhoseComponentNotYetDeployed.remove(this.setOfTuplesWhoseComponentNotYetDeployed.get(i)); 
                                // now, setOfTuplesWhoseComponentNotYetDeployed contains only those tuples whose tuple.component is not yet deployed
                            }
                        }
                        // increment numAllocatedComponents because component c is allocated to VM v
                        numAllocatedComponents+=1;            
                    }
                }
            }
        }//end of while loop
        
        this.deploymentSolution.computeCostOfSolution();
        this.deploymentSolution.computePerformanceOfSolution();
        this.deploymentSolution.computeReliabilityOfSolution();
        
    }

    public void initializeUsedCapacities(){                
        this.server2allocatedPerformance=new HashMap();
        for(Server server:this.sim.servers){ 
            this.server2allocatedPerformance.put(server, 0.0); 
            // initially, the servers have no deployed software components on them. Allocated MIPS=0.
        }        
    }
    
    public void initializeHeuristicsAndProbabilities(){
        for(DeploymentTuple tuple: this.sim.antController.setOfAllTuples){
            this.tuples2heuristic1.put(tuple, 0.0);
            this.tuples2heuristic2.put(tuple, 0.0);                
            this.tuples2prob.put(tuple, 0.0); 
        }
    }
    
    // update heuristic1 and heuristic2 values of each tuple
    public void updateHeuristicValues(){    
        for(DeploymentTuple tupleInLoop:this.traversedTuples){ 
            this.tuples2heuristic1.put(tupleInLoop, 0.0); 
            this.tuples2heuristic2.put(tupleInLoop, 0.0); 
        }
                
        for(DeploymentTuple tupleInLoop:this.setOfTuplesWhoseComponentNotYetDeployed){ 
            double heuristicValue1_tuple=0.0; // performance in MIPS
            double heuristicValue2_tuple=0.0; // reliability or availability            
            if(this.server2allocatedPerformance.get(tupleInLoop.destination)+tupleInLoop.component.requiredPerformance<=tupleInLoop.destination.performance){                
                heuristicValue1_tuple= (this.server2allocatedPerformance.get(tupleInLoop.destination)+tupleInLoop.component.requiredPerformance) / (double)tupleInLoop.destination.performance;
            }
            
            if(tupleInLoop.component.requiredReliability<=tupleInLoop.destination.reliability){
                heuristicValue2_tuple=1.0 - (tupleInLoop.destination.reliability - tupleInLoop.component.requiredReliability);
            }                        
            this.tuples2heuristic1.put(tupleInLoop, heuristicValue1_tuple);            
            this.tuples2heuristic2.put(tupleInLoop, heuristicValue2_tuple);            
        }        
    }  
    
    // compute or update probabilities of all tuples based on the pheromone value and the heuristic1 and heuristic2 values
    public void updateProbabilities(){
        for(DeploymentTuple tupleInLoop:this.traversedTuples){ 
            this.tuples2prob.put(tupleInLoop, 0.0); 
        }
        
        for(DeploymentTuple tupleInLoop:this.setOfTuplesWhoseComponentNotYetDeployed){ 
            // if the tupleInLoop is not yet traversed by ant k
            double pheromone=this.sim.antController.tuples2pheromone.get(tupleInLoop);
            double heuristic1=this.tuples2heuristic1.get(tupleInLoop);
            double heuristic2=this.tuples2heuristic2.get(tupleInLoop);
            double sumOfAllAppsPheromoneIntoHeuristicNotYetTraversed=0.0;                 
                
            double pheromoneOtherComponents=0.0;
            double heuristic1OtherComponents=0.0;
            double heuristic2OtherComponents=0.0;
            
            double LAMBDA=this.antNumber/(double)AntController.N_ANTS;                    
            
            for(DeploymentTuple mt:this.setOfTuplesWhoseComponentNotYetDeployed){ 
                if(!this.traversedTuples.contains(mt)){                                     
                    // pheromone of a tuple not already traversed 
                    pheromoneOtherComponents = this.sim.antController.tuples2pheromone.get(mt);
                    heuristic1OtherComponents = Math.pow(this.tuples2heuristic1.get(mt), (AntController.BETA * LAMBDA) );
                    heuristic2OtherComponents = Math.pow(this.tuples2heuristic2.get(mt), (AntController.BETA * (1-LAMBDA) ) );
                    sumOfAllAppsPheromoneIntoHeuristicNotYetTraversed += (pheromoneOtherComponents * heuristic1OtherComponents * heuristic2OtherComponents);
                }                                 
            }                                         
                
            double probability=0.0;
            if(sumOfAllAppsPheromoneIntoHeuristicNotYetTraversed==0){
                probability=0.0;                    
            }
            else{
                probability=( pheromone * Math.pow(heuristic1,(AntController.BETA * (LAMBDA) )) * Math.pow(heuristic2,(AntController.BETA * (1-LAMBDA) )) ) / (sumOfAllAppsPheromoneIntoHeuristicNotYetTraversed); // eq. 2
            }                
            if(Double.isNaN(probability)){
                probability=0.0;
            }
            if(Double.isInfinite(probability)){                
                probability=0.0;
            }
            
            this.tuples2prob.put(tupleInLoop, probability);
        }
    }
}
