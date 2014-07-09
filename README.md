# EventHub
Event hub is a project that collects various events from clients and reacts to them
based on a particular project is configured.
Clients submit data via REST API. EvenHub service provides client authentication and
authorization and some of the most common action processing implementations.
It's a very generic framework and can be extended to work with various kinds
of events and event processors.

# Installation
This section describes steps required to build, configure and run the application.

## Building EventHub

### Install
Install these applications on your dev machine in order to be able to build the src code:

 * Java Development Kit (JDK) >= 1.7
 * Optionally install SBT, or use one provided with the project

### Run SBT to generate JAR
SBT is a build tool that downloads source code dependencies, compiles code, runs tests,
generates scaladocs, and produces executables.

Start up SBT:

    > sbt

or if it's not on a PATH:

    > ./sbt

In sbt shell type (note semicolons):

    ;clean; assembly

You can also run it as a single command from OS shell:

    > sbt clean assembly

This will run all tests and generate a single jar file named similar to: `rest-assembly-0.1.jar`.

Here is a full list of commands in case you want to generate projects and documentation, etc:

    > sbt clean compile test doc assembly eclipse
    
Look at the output to find where docs, jars, etc goes. You can open projects with Eclipse or IntelliJ
afterwards. New versions of IntelliJ IDEA do not require generation of project files and can open
SBT projects directly using Scala plugin.

## System Requirements
To run compiled JAR file you should have installed:

 * Java Runtime Environment (JRE) >= 1.7
 * MongoDB >= 2.4

## Running EventHub
Run from your OS shell:

    > java -jar rest-assembly-0.1.jar
	
This will run the application. In particular it will start a web server and will be ready to receive
HTTP requests. Please use run scripts in production environment. They take care of runtime settings
and environment, so that you don't get it wrong.

### Advanced Configuration
TODO

### Accessing the Service
You can send requests to the web service with tools like `curl` on Unix systems,
or you can open the site in a browser.

