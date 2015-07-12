Compiling
==========

Using ANT:

    ant compile

Running the app
===============

    java -cp binaries/bin sinalgo.Run

Running the algorithm
======================

Once the app is running:

1. Select desired algorithm:
    * To run the algorithm from Chapter 3.6, select "colorDistribution" 
    * To run the algorithm from Chapter 3.6, select "fasterColoring"
   (On this screen you can also adjust parameters, such as "GeometricNodeCollection rMax" and "UDG rMax" which will affect the density of edges)
2. From the top menu, select "Simulation -> Generate Nodes"
3. Enter the desired nodes number and select the distribution model (random and grid2d work best) and click "Ok".
4. Click the "play" button to start the algorithm. 
5. When the algorithm has finished, you will see (on the standard output) a message saying "Color distribution/faster coloring done!", for all the nodes.