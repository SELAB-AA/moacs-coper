/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author SELAB
 */
public class AntController implements Runnable, ExperimentParameters{
    ExperimentalSetupAndReporting sim;
    
    static double PHEROMONE_0=0.0; 
    final static double ALPHA=0.1;
    final static double BETA=2.0; 
    final static double RHO=0.1;
    final static int N_CYCLES=100; 
    final static int N_ANTS=10;
    static int nTotalAntThredas=0; 
        
    List<DeploymentTuple> setOfAllTuples; // a list of all tuples. all app deployments (s, app) with all possible destination servers (d)
    
    // a Hashtable of all tuples. all app deployments (s, app) with all possible destination servers (d)
    HashMap<DeploymentTuple, Double> tuples2pheromone; //(tuple, pheromone value of tuple). 
    
    List<DeploymentSolution> setOfAllAntSpecificDeploymentSolutionsForReporting; // NB: this one is only for reporting. So, we don't clear it or remove anything from it.
    
    List<DeploymentSolution> setOfParetoOptimalDeploymentSolutionsFinal;
    List<DeploymentSolution> setOfBestDeploymentSolutions;
    
    static long millisAtStart=0;
    static long millisAtEnd=0;
    
    public AntController(ExperimentalSetupAndReporting sim){
        this.sim=sim;
        this.setOfAllTuples=new ArrayList();        
        this.tuples2pheromone=new HashMap();
        
        this.setOfAllAntSpecificDeploymentSolutionsForReporting=new ArrayList();
        
        this.setOfParetoOptimalDeploymentSolutionsFinal=new ArrayList(); //new CopyOnWriteArrayList();
        
        this.setOfBestDeploymentSolutions=new ArrayList();    
    }
    
    @Override
    public void run(){
        millisAtStart = System.currentTimeMillis();
        
        this.runOptimizationProcess(); // generate global best migration plan
                
        this.sim.printServersAndComponents();
        this.sim.report(); // report the results after pareto frontier is found
        this.sim.closeFile();
        this.sim.closeDetailedFiles();
        
        this.sim.generateVARAndFUNFiles();
    }
        
    public void runOptimizationProcess(){        
        this.initializeAllTuples();                 
        for(int i=0; i<N_CYCLES;i++){
            List<Ant> ants=new ArrayList();
            List<Thread> antThreads=new ArrayList();        
            for(int j=0; j<N_ANTS;j++){
                Ant ant=new Ant(j+1,this.sim);                
                ants.add(ant);
                
                Thread antThread=new Thread(ant);
                antThreads.add(antThread);
                antThread.start();
                nTotalAntThredas+=1;
            }
            
            for(Thread t:antThreads){
                try{
                    t.join(); // wait for the ants to complete their task of making a migration plan
                }
                catch(InterruptedException e){
                    System.out.println(e);
                    e.printStackTrace();                
                }
            }
            // at this point, the ants are supposed to complete their solutions.
            
            for(Ant ant:ants){
                if(this.isDeploymentSolutionUnique(ant.deploymentSolution, this.setOfAllAntSpecificDeploymentSolutionsForReporting)){
                    this.setOfAllAntSpecificDeploymentSolutionsForReporting.add(ant.deploymentSolution);
                }                
            }
            
            this.findBestSolutions(); 
            
            this.applyGlobalPheromoneTrailEvaporationRule();
            
            ants.clear();
            antThreads.clear();
        }//end of nI
        
        millisAtEnd = System.currentTimeMillis();
        
        this.findParetoSet(); 
        
    }
    
    public void findBestSolutions(){
        this.setOfBestDeploymentSolutions.clear(); 
        DeploymentSolution minCostSolution=this.setOfAllAntSpecificDeploymentSolutionsForReporting.get(0);
        DeploymentSolution maxPerformanceSolution=this.setOfAllAntSpecificDeploymentSolutionsForReporting.get(0);
        DeploymentSolution maxReliabilitySolution=this.setOfAllAntSpecificDeploymentSolutionsForReporting.get(0);
        
        for(DeploymentSolution s: this.setOfAllAntSpecificDeploymentSolutionsForReporting){                
            if(s.cost<minCostSolution.cost){
                minCostSolution=s;
            }
            if(s.performance>maxPerformanceSolution.performance){
                maxPerformanceSolution=s;
            }
            if(s.reliability>maxReliabilitySolution.reliability){
                maxReliabilitySolution=s;
            }
        }
        
        this.setOfBestDeploymentSolutions.add(minCostSolution);
        if(this.isDeploymentSolutionUnique(maxPerformanceSolution, this.setOfBestDeploymentSolutions)){
            this.setOfBestDeploymentSolutions.add(maxPerformanceSolution);
        }
        if(this.isDeploymentSolutionUnique(maxReliabilitySolution, this.setOfBestDeploymentSolutions)){
            this.setOfBestDeploymentSolutions.add(maxReliabilitySolution);
        }
    }
        
    public void findParetoSet(){
        for(DeploymentSolution s: this.setOfAllAntSpecificDeploymentSolutionsForReporting){
            if(this.setOfParetoOptimalDeploymentSolutionsFinal.isEmpty()){
                this.setOfParetoOptimalDeploymentSolutionsFinal.add(s);
            }
            else{
                Boolean nonDominated=true;
                for(DeploymentSolution otherSolution: this.setOfParetoOptimalDeploymentSolutionsFinal){
                    if(this.dominates(otherSolution, s)){
                        nonDominated=false;
                        break;
                    }
                }
                if(nonDominated){
                    // add the soluiton to the Pareto front and remove dominated solutions from it
                    this.setOfParetoOptimalDeploymentSolutionsFinal.add(s);
                    // the front method will return the new Pareto front, which contains only the non-dominated solutions. So, the dominated solutions are removed.
                    this.setOfParetoOptimalDeploymentSolutionsFinal=this.front(this.setOfParetoOptimalDeploymentSolutionsFinal);
                }
            }
        }
    }
    
    public List<DeploymentSolution> front(List<DeploymentSolution> solutions){
        int i=0;
        List<DeploymentSolution> output=new ArrayList();
        while(i<solutions.size()){        
            int j=0;
            Boolean dominated=false;
            while(j<solutions.size()){
                if(j!=i){
                    if(this.dominates(solutions.get(j), solutions.get(i))){ // NB: it checks weak dominance                        
                        dominated=true;
                        break;
                    }                    
                }
                j=j+1;                    
            } // end while
            if(!dominated){
                if(output.isEmpty()){
                    output.add(solutions.get(i));
                }
                else if(this.isDeploymentSolutionUnique(solutions.get(i), output)){
                    output.add(solutions.get(i));
                }                
            }
            i=i+1;
        } // end while        
        return output;        
    }
    
    
    // there are 2 conditions.
    //  1. solution1 is no worse than solution2 in all objectives
    //  2. solution1 is strictly better than solution2 in at least one objective
    public Boolean dominates(DeploymentSolution solution1, DeploymentSolution solution2){
        if(solution1.cost<=solution2.cost && solution1.performance>=solution2.performance && solution1.reliability>=solution2.reliability){
            if(solution1.cost<solution2.cost || solution1.performance>solution2.performance || solution1.reliability>solution2.reliability){ // weak dominance            
                return true;
            }
        }
        return false;
    }
    
    
    public void initializeAllTuples(){
        this.tuples2pheromone.clear();        
        this.setOfAllTuples.clear();        
        // we compute PHEROMONE_0 and then initialize all tuples in tuples2pheromone        
        this.computePHEROMONE_0();
        for(Server server:this.sim.servers){
            for(SoftwareComponent component:this.sim.components){
                if(component.requiredPerformance<=server.performance && component.requiredReliability<=server.reliability){
                    DeploymentTuple aNewTuple = new DeploymentTuple(component, server);
                    this.setOfAllTuples.add(aNewTuple);                
                    
                    this.tuples2pheromone.put(aNewTuple, PHEROMONE_0); 
                    
                }                
            }
        }
    }
    
    public void computePHEROMONE_0(){
        int nServers=this.sim.servers.size(); 
        PHEROMONE_0=1.0/(nServers*ExperimentalSetupAndReporting.N_COMPONENTS);
    }
            
    public void applyGlobalPheromoneTrailEvaporationRule(){
        for(DeploymentSolution solution: this.setOfBestDeploymentSolutions){
            for(DeploymentTuple tupleInLoop:this.setOfAllTuples){                
                if(solution.tuples.contains(tupleInLoop)){
                    this.tuples2pheromone.put(tupleInLoop, ((1-ALPHA)*this.tuples2pheromone.get(tupleInLoop)) + (ALPHA*PHEROMONE_0) );
                }
            }            
        }
    }   
    
    public boolean isDeploymentSolutionUnique(DeploymentSolution solution, List<DeploymentSolution> solutions){
        boolean isUnique=true;
        for(DeploymentSolution otherSolution: solutions){
            List<DeploymentTuple> tuplesSolution=new ArrayList(solution.tuples);
            List<DeploymentTuple> tuplesOtherSolution=new ArrayList(otherSolution.tuples);
            
            tuplesSolution.removeAll(tuplesOtherSolution);
            if(tuplesSolution.isEmpty()){    
                // it means that the solutions were same in terms of tuples/configuration, i.e, not unique                
                isUnique=false;
                break;
            }
        }
        return isUnique;
    }

}
