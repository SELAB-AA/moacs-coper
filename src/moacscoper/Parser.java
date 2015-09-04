/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author bbyholm
 */
public class Parser {
    ExperimentalSetupAndReporting sim;
    
    public Parser(ExperimentalSetupAndReporting sim){
        this.sim=sim;
    }
    
    private List<CSVRecord> parse(String path) throws IOException {
        return CSVParser.parse(new File(path), StandardCharsets.UTF_8, CSVFormat.TDF).getRecords();
    }
    
    public void addServers() throws IOException {
        List<CSVRecord> vms = parse("vm.dat");
        int serverNumber=1;
        int performance;
        double reliability;
        double cost;        
        for(CSVRecord r:vms){
            performance=Integer.parseInt(r.get(0));
            reliability=Double.parseDouble(r.get(1));
            cost=Double.parseDouble(r.get(2));            
            this.sim.servers.add(new Server(serverNumber+"", performance, reliability, cost));
            serverNumber++;
        }        
    }
    
    public int addComponents() throws IOException {
        List<CSVRecord> components = parse("component.dat");
        int componentNumber=0;
        int performance;
        double reliability;
        for(CSVRecord r:components){
            performance=Integer.parseInt(r.get(0));
            reliability=Double.parseDouble(r.get(1));
            this.sim.components.add(new SoftwareComponent(componentNumber+1, "c"+(componentNumber+1), performance, reliability));  
            componentNumber++;
        }
        return componentNumber; // return total number of components
    }

}
