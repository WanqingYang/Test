Assumption:

1.every event time is accurate at least to hours
2.every order, customer id and order id are complete, without missing

In this project, I store events data in class, which is identified by each customer id
Because the events are received with no guranteed order, so the event time of each user is unordered.
(In the input file, customer may order first then create profile).
Thus track each user's order time is important.

I write two functions:
Ingest(e)

Given event e, update data D

TopXSimpleLTVCustomers(x)

Return the top x customers with the highest Simple Lifetime Value from data D.



Run the program need download a package: json-simple-1.1.1.jar

In terminal, go to the current repository.
Compile command: javac -cp ./json-simple-1.1.1.jar SimpleLifetimeValue.java
Run command: java -cp .:./json-simple-1.1.1.jar SimpleLifetimeValue ./inputUnordered.txt 
