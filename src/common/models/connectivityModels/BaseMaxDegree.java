package common.models.connectivityModels;

import projects.colorDistribution.CustomGlobal;
import projects.defaultProject.models.connectivityModels.StaticUDG;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;

import java.util.Enumeration;

import common.globals.BaseCustomGlobal;

public class BaseMaxDegree extends StaticUDG {

    private int degree = 0;

    /**
     * The default constructor for this class.
     *
     * @throws CorruptConfigurationEntryException If one of the initialization steps fails.
     */
    public BaseMaxDegree() throws CorruptConfigurationEntryException {
    }

    public boolean updateConnections(Node n) throws WrongConfigurationException {
        boolean edgeAdded = false;
        
        // For the given node n, retrieve only the nodes which are possible neighbor candidates. This
        // is possible because of the rMax filed of the GeometricNodeCollection, which indicates the maximum
        // distance between any two connected points.
        Enumeration<Node> pNE = sinalgo.runtime.Runtime.nodes.getPossibleNeighborsEnumeration(n);
        while( pNE.hasMoreElements() ){
            Node possibleNeighbor = pNE.nextElement();
            if(n.ID != possibleNeighbor.ID){
                // if the possible neighbor is connected with the the node: add the connection to the outgoing connection of n
                if(isConnected(n, possibleNeighbor)){
                    // add it to the outgoing Edges of n. The EdgeCollection itself checks, if the Edge is already contained
                    boolean nodeAlreadyExists = n.outgoingConnections.add(n, possibleNeighbor, true);
                    edgeAdded =  !nodeAlreadyExists || edgeAdded; // note: don't write it the other way round, otherwise, the edge is not added if edgeAdded is true.

                    if (!nodeAlreadyExists) {
                        // update current node's degree
                        degree++;
                        // Update the maximum degree of the graph
                        BaseCustomGlobal.maxDegree = Math.max(BaseCustomGlobal.maxDegree, degree);
                    }
                }
            }
        }
        // loop over all edges again and remove edges that have not been marked 'valid' in this round
        boolean dyingLinks = n.outgoingConnections.removeInvalidLinks();

        return edgeAdded || dyingLinks; // return whether an edge has been added or removed.
    }
}
