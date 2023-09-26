# Multi Stage Multi Agent

Set up a multi stage jenkins pipeline where each stage is run on a unique agent. This is a very useful approach when you have multi language application
or application that has conflicting dependencies.


                            Point to know about Multi-stages and Multi-Agent in the jenkinsfile


//we may have question that ok since we are making use of containers as Agent in our Jenkins pipeline so 

//  what if we have different application written in different languages { java, Nodejs,  Some DataBase querrys that we need to do } So

//  how do we configure a Multi-stage/ Multi-agent  pipeline to handle this applications using Docker containers?

// As we can see below is and example on how to do that all we need is to present the container that acn host the kind of application we want to run
// 

// The Advantage of using Docker as an Agent it will save cost and time and configurations errors as well  Because if we were to use VM as our Agents:

// we have to make the number of Vm needed to run the Various application and scale the up and down  trying to manage cost and we have to configure them anytime there

//  And updates in the software BUT with Docker all we need is just to change the Image version an everything will good to go.

// And the container created as and agent as soon it done with the execution it will get deleted.

