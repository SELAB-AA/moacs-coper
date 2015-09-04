/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

/**
 *
 * @author aashraf
 */
public class SoftwareComponent{
    int componentNumber; // e.g., 1, 2, 3.
    String name;
    double requiredPerformance; // in terms of MIPS, e.g., 500 MIPS
    double requiredReliability; // we use availability values in decimal range [0,1], e.g., "five nines" 0.99999.
            
    public SoftwareComponent(int componentNumber, String name, double requiredPerformance, double requiredReliability){
        this.componentNumber=componentNumber;
        this.name=name;
        this.requiredPerformance=requiredPerformance;
        this.requiredReliability=requiredReliability;        
    }    
}
