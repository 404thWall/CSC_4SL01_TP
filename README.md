# CSC_4SL01_TP
Factory project for Distributed Systems at Télécom Paris. This repo proposes an implementation of the exercises proposed on the Moodle. Do note that the code proposed here was made using Intellij as an IDE, so I'm not 100% sure that it would work properly on eclipse for instance, but only when building everything from scratch. For general use, runnable jars are provided so that it should work without any compatibility issue.

In order to launch the simulation you only need to launch all these scripts in differents terminals : 
- `start_zoo.sh` that launches ZooKeeper
- `start_kafka.sh` that launches the Kafka server
- `start_webserver.sh` that launches the remote persistance server
- `start_microservice.sh` that launches the microservice
- `start_simulator.sh` that launches the simulator application

You can now use the functionalities of the application inside the dedicated window that is created by `start_simulator.sh`.
