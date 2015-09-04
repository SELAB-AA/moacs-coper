/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moacscoper;

/**
 *
 * @author aashraf
 */
public class DeploymentTuple {    
    Server destination; // destination VM or server
    SoftwareComponent component; // component to be deployed
    
    public DeploymentTuple(SoftwareComponent component, Server destination){        
        this.component=component;
        this.destination=destination;        
    }
}
