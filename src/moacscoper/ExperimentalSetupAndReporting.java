/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

import java.util.Random;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author aashraf
 */
public class ExperimentalSetupAndReporting implements ExperimentParameters {
    List<Server> servers;    
    List<SoftwareComponent> components;
    
    Random random;    
    
    BufferedWriter bufferedWriter;
    BufferedWriter bufferedWriterDetailed;
    BufferedWriter bufferedWriterDetailed2;
    BufferedWriter bufferedWriterDetailed3;
    
    BufferedWriter bufferedWriterDetailed4;
    BufferedWriter bufferedWriterDetailed5;
    
    AntController antController;
    
    static int N_COMPONENTS=7;    
    
    Parser parser;
        
    public ExperimentalSetupAndReporting(){
        this.servers=new ArrayList();
        this.components=new ArrayList();
        
        this.random=new Random();
        random.setSeed(RANDOM_SEED); 
        
        this.antController=new AntController(this);
        this.parser=new Parser(this);        
    }
    
    //@Override
    public void run(){ 
        // make a scenario with x servers and y software components 
        this.addNewServers();                            
        
        // create software components
        this.addNewComponents();
                
        // record some results
        this.createNewReportFile("pareto-set.dat");
        this.createNewDetailedReportFiles("all-solutions.dat", "servers-and-components.dat", "best-solutions.dat");
        
        Thread antControllerThread=new Thread(this.antController);
        Main.threads.add(antControllerThread);
        
        antControllerThread.start(); 
        // here we start the AntController thread. it runs the complete algorithm including creation and management of ant threads.
        
    }
    
    public void generateVARAndFUNFiles(){
        this.createNewVAR_FUNFiles("FUNACS", "VARACS");
        // FUNACS file
        for(DeploymentSolution solution: this.antController.setOfParetoOptimalDeploymentSolutionsFinal){
            this.writeNewLineToDetailedFile4(solution.performance+" "+solution.reliability+" "+solution.cost);
        }
        // VARACS file
        for(DeploymentSolution solution: this.antController.setOfParetoOptimalDeploymentSolutionsFinal){
            String solutionVector="";
            // sort all tuples in the ascending order of componentNumber
            List<DeploymentTuple> tuplesLocal=new ArrayList(solution.tuples);
            Collections.sort(tuplesLocal, new TupleComponentComparator());
            for(DeploymentTuple tuple: tuplesLocal){
                solutionVector+=tuple.destination.name+" ";
            }
            this.writeNewLineToDetailedFile5(solutionVector);
        }
        this.closeDetailedFiles45();        
    }
    
    public void printServersAndComponents(){
        NumberFormat formatter = new DecimalFormat("#.####");   
        
        this.writeNewLineToDetailedFile2("Algorithm execution time in milliseconds: " + (AntController.millisAtEnd - AntController.millisAtStart));
        this.writeNewLineToDetailedFile2("Algorithm execution time in seconds: " + (AntController.millisAtEnd - AntController.millisAtStart)/1000);
        this.writeNewLineToDetailedFile2("---------------------------------------------------------");
        
        this.writeNewLineToDetailedFile2("nServers="+this.servers.size()+", nComponents="+this.components.size()+ 
                ", nTuples="+this.antController.setOfAllTuples.size() + ", nTotalSolutions=" + DeploymentSolution.staticSolutionCounter);    
        this.writeNewLineToDetailedFile2("---------------------------------------------------------");
        
        this.writeNewLineToDetailedFile2("nTotalUniqueSolutions="+this.antController.setOfAllAntSpecificDeploymentSolutionsForReporting.size()+
                ", nTotalSolutionsInTheParetoFront="+this.antController.setOfParetoOptimalDeploymentSolutionsFinal.size());    
        this.writeNewLineToDetailedFile2("---------------------------------------------------------");
        
        for(int i=0; i<this.servers.size(); i++){
            this.writeNewLineToDetailedFile2("--- Server "+this.servers.get(i).name +
                    ", cost="+formatter.format(this.servers.get(i).cost) + ", performance (MIPS)="+formatter.format(this.servers.get(i).performance) + 
                    ", reliability [0,1]="+formatter.format(this.servers.get(i).reliability)+ " ---");                        
        }
        this.writeNewLineToDetailedFile2("---------------------------------------------------------");
        for(int i=0; i<this.components.size(); i++){
            this.writeNewLineToDetailedFile2("Component "+ this.components.get(i).name + ", required performance (MIPS)="+
                    formatter.format(this.components.get(i).requiredPerformance) + ", required reliability [0,1]="+
                    formatter.format(this.components.get(i).requiredReliability) );
        }
        this.writeNewLineToDetailedFile2("---------------------------------------------------------");
        
        // detailed solutions (with tuples) in the Pareto set
        this.writeNewLineToDetailedFile2("Detailed solutions (with tuples) in the Pareto set");
        this.writeNewLineToDetailedFile2("Solution, Cost,\t\tPerformance (MIPS),\t\tReliability, \tnServers, \tTuples");
        for(DeploymentSolution solution: this.antController.setOfParetoOptimalDeploymentSolutionsFinal){
            String tuplesInSolution="";
            for(DeploymentTuple tuple: solution.tuples){
                tuplesInSolution+="("+tuple.component.name+", "+tuple.destination.name+"), ";
            }
            this.writeNewLineToDetailedFile2(solution.solutionNumber + " &\t  " + formatter.format(solution.cost) + " &\t" + solution.performance + " MIPS &\t\t\t" + 
                    formatter.format(solution.reliability) + " & \t" + solution.getServers().size() + " &\t\t" + tuplesInSolution);            
        }        
        this.writeNewLineToDetailedFile2("---------------------------------------------------------");
        
        System.out.println("Algorithm execution time in milliseconds: " + (AntController.millisAtEnd - AntController.millisAtStart));
        System.out.println("Algorithm execution time in seconds: " + (AntController.millisAtEnd - AntController.millisAtStart)/1000);
        System.out.println("---------------------------------------------------------");
                
        System.out.println("nServers="+this.servers.size()+", nComponents="+this.components.size()+ 
                ", nTuples="+this.antController.setOfAllTuples.size() + ", nTotalSolutions=" + DeploymentSolution.staticSolutionCounter);
        System.out.println("---------------------------------------------------------");
        
        System.out.println("nTotalUniqueSolutions="+this.antController.setOfAllAntSpecificDeploymentSolutionsForReporting.size()+
                ", nTotalSolutionsInTheParetoFront="+this.antController.setOfParetoOptimalDeploymentSolutionsFinal.size());
        System.out.println("---------------------------------------------------------");

    }
    
    public void report(){ 
        // Report the main results (i.e., the Pareto set)
        NumberFormat formatter = new DecimalFormat("#.####");
        System.out.println("SolutionNumber, Cost,\tPerformance (MIPS),\tReliability, \tnTuples, \tnServers");
        for(DeploymentSolution solution: this.antController.setOfParetoOptimalDeploymentSolutionsFinal){
            this.writeNewLineToFile(solution.solutionNumber + ",\t\t" + formatter.format(solution.cost) + ",\t" + solution.performance + ",\t\t\t" + formatter.format(solution.reliability) +
                     ", \t\t" + solution.tuples.size() + ", \t\t" + solution.getServers().size());
            
        }
        for(DeploymentSolution solution: this.antController.setOfBestDeploymentSolutions){
            System.out.println(solution.solutionNumber + ",\t\t" + formatter.format(solution.cost) + ",\t" + formatter.format(solution.performance) + ",\t\t\t" + formatter.format(solution.reliability) +
                     ", \t\t" + solution.tuples.size() + ", \t\t" + solution.getServers().size());            
        }
        System.out.println("---------------------------------------------------------");
        
        // print all ant-specific solutions in the detailed report
        this.writeNewLineToDetailedFile("SolutionNumber, Cost,\tPerformance (MIPS),\tReliability, \tnTuples, \tnServers");
        for(DeploymentSolution solution: this.antController.setOfAllAntSpecificDeploymentSolutionsForReporting){
            this.writeNewLineToDetailedFile(solution.solutionNumber + ",\t\t" + formatter.format(solution.cost) + ",\t" + formatter.format(solution.performance) + ",\t\t\t" + formatter.format(solution.reliability) +
                     ", \t\t" + solution.tuples.size() + ", \t\t" + solution.getServers().size());
        }
        
        // print the 3 best solutions in the detailed report
        this.writeNewLineToDetailedFile3("SolutionNumber, Cost,\tPerformance (MIPS),\tReliability, \tnTuples, \tnServers");
        for(DeploymentSolution solution: this.antController.setOfBestDeploymentSolutions){
            this.writeNewLineToDetailedFile3(solution.solutionNumber + ",\t\t" + formatter.format(solution.cost) + ",\t" + formatter.format(solution.performance) + ",\t\t\t" + formatter.format(solution.reliability) +
                     ", \t\t" + solution.tuples.size() + ", \t\t" + solution.getServers().size());
        }        
    }
    
    public void addNewServers(){        
        try{
            parser.addServers();
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
    public void addNewComponents(){
        try{
            N_COMPONENTS=parser.addComponents();
        }
        catch(IOException e){
            System.out.println(e);
        }        
    }
    
    public void createNewReportFile(String fileName){
        try{
            File file=new File(fileName);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
            this.bufferedWriter=new BufferedWriter(fileWriter);
            this.bufferedWriter.write("SolutionNumber, Cost,\tPerformance (MIPS),\tReliability, \tnTuples, \tnServers");
            this.bufferedWriter.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void writeNewLineToFile(String content){
        try{
            this.bufferedWriter.write(content);
            this.bufferedWriter.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        this.flushBufferedWriter();
    }
    
    
    public void flushBufferedWriter(){
        try{
            this.bufferedWriter.flush(); 
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void closeFile(){
        try{
            this.bufferedWriter.close();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }        
    }
    
    // for a detailed sim-report
    public void createNewDetailedReportFiles(String fileName1, String fileName2, String fileName3){
        try{
            File file=new File(fileName1);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
            this.bufferedWriterDetailed=new BufferedWriter(fileWriter);            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        
        try{
            File file=new File(fileName2);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
            this.bufferedWriterDetailed2=new BufferedWriter(fileWriter);            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        
        try{
            File file=new File(fileName3);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
            this.bufferedWriterDetailed3=new BufferedWriter(fileWriter);            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        
    }
    
    
    public void createNewVAR_FUNFiles(String fileName1, String fileName2){
        try{
            File file=new File(fileName1);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
            this.bufferedWriterDetailed4=new BufferedWriter(fileWriter);            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        
        try{
            File file=new File(fileName2);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
            this.bufferedWriterDetailed5=new BufferedWriter(fileWriter);            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void writeNewLineToDetailedFile4(String content){
        try{
            this.bufferedWriterDetailed4.write(content);
            this.bufferedWriterDetailed4.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        this.flushBufferedWriterDetailedFile4();
    }
    
    public void writeNewLineToDetailedFile5(String content){
        try{
            this.bufferedWriterDetailed5.write(content);
            this.bufferedWriterDetailed5.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        this.flushBufferedWriterDetailedFile5();
    }
    
    public void flushBufferedWriterDetailedFile4(){
        try{
            this.bufferedWriterDetailed4.flush(); 
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void flushBufferedWriterDetailedFile5(){
        try{
            this.bufferedWriterDetailed5.flush(); 
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void closeDetailedFiles45(){
        try{
            this.bufferedWriterDetailed4.close();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }        
        
        try{
            this.bufferedWriterDetailed5.close();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    
    public void writeNewLineToDetailedFile(String content){
        try{
            this.bufferedWriterDetailed.write(content);
            this.bufferedWriterDetailed.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        this.flushBufferedWriterDetailedFile();
    }
    
    public void writeNewLineToDetailedFile2(String content){
        try{
            this.bufferedWriterDetailed2.write(content);
            this.bufferedWriterDetailed2.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        this.flushBufferedWriterDetailedFile2();
    }
    
    public void writeNewLineToDetailedFile3(String content){
        try{
            this.bufferedWriterDetailed3.write(content);
            this.bufferedWriterDetailed3.newLine();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
        this.flushBufferedWriterDetailedFile3();
    }

    public void flushBufferedWriterDetailedFile(){
        try{
            this.bufferedWriterDetailed.flush(); 
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void flushBufferedWriterDetailedFile2(){
        try{
            this.bufferedWriterDetailed2.flush(); 
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void flushBufferedWriterDetailedFile3(){
        try{
            this.bufferedWriterDetailed3.flush(); 
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void closeDetailedFiles(){
        try{
            this.bufferedWriterDetailed.close();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        }        
        
        try{
            this.bufferedWriterDetailed2.close();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        } 
        
        try{
            this.bufferedWriterDetailed3.close();            
        }
        catch(IOException e){
            System.out.println(e);
            e.printStackTrace();
        } 
    }
    

}
