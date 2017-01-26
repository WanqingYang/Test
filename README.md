# Assumption:
1. every event time is accurate at least to hours
2. every order, customer id and order id are complete, without missing

# Documents:
## input
The inputUnordered.txt file in input repository, includes 12 different customer.

The input1user.txt file in sample_input repository, include 1 customer.

## UserData class
In this project, I store events data in UserData class, which is uniquely identified by each customer id.
Because the events are received with no guranteed order, so in the input file,the event time of each user is unordered.
(customer may order first then create their user profile).
Thus track each user's order time is important.

I wrote two functions:

## Ingest(e)
Given event e, create or update UserData class

When there is a incoming event, when there is no matching UserData class, create a new class, and storing data according to the event type. When there is a matching UserData class, update the information in class.

## TopXSimpleLTVCustomers(10)

Return the top 10 customers with the highest Simple Lifetime Value from UserData.

**slv = 52 * a * 10**, where a is the average customer value per week, **a = user_total_expense/weeks**, weeks = week_now - week_start, and week_now and week_start are week identifiers, week_now is the current week from epoch(01-01-1970:00:00:00), week_start is the start week when customers' first event occured. 

## Run the program:
Run the program need download a package: json-simple-1.1.1.jar

In terminal, go to the current directory

```
Compile command: javac -cp ./json-simple-1.1.1.jar SimpleLifetimeValue.java

Run command: java -cp .:./json-simple-1.1.1.jar SimpleLifetimeValue ./inputUnordered.txt
```
