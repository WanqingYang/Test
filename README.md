# Assumption:
1. every event time is accurate at least to hours
2. every order, customer id and order id are complete, without missing

# Documents:

## UserData class
In this project, I store events data in UserData class, which is uniquely identified by each customer id.
Because the events are received with no guranteed order, so in the input file,the event time of each user is unordered.
(customer may order first then create their user profile).
Thus track each user's order time is important.

I wrote two functions:

## Ingest(e)
Given event e, create or update UserData class

When there is a incoming event, when there is no matching UserData class, create a new class, and storing data according to the event type. When there is a matching UserData class, update the information in class.

## TopXSimpleLTVCustomers(x)

Return the top x customers with the highest Simple Lifetime Value from data D.

The inputUnordered.txt file includes 12 different customer.

## Run the program:
Run the program need download a package: json-simple-1.1.1.jar

In terminal, go to the current repository.

Compile command: javac -cp ./json-simple-1.1.1.jar SimpleLifetimeValue.java

Run command: java -cp .:./json-simple-1.1.1.jar SimpleLifetimeValue ./inputUnordered.txt
