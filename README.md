# moacs-coper
A software deployment optimizer using an ant colony system

Requirements:
	- Java 7
	- Apache Commons CSV 1.2

The software tries to optimize a deployment of software components defined
in `components.dat` on servers defined in `vm.dat`. Each row in `components.dat`
corresponds to a software component with a given performance requirement and an
availability requirement, separated by tabs. Each row in `vm.dat` corresponds to
a server with its performance, availability and cost separated by tabs. The sum
of performance requirements for components assigned to a server may not exceed
the available performance of the server. A component may not be assigned to a
server with less availability than the component requires. The software produces
non-dominated solutions that maximize the performance, cost and availability.