# Minibase
In this repo, I implement the minimization procedure for conjunctive queries and a lightweight database engine for evaluating queries.

[Logic Behind Extracting Join Conditions](#logic-behind-extracting-join-conditions)<br>
[Optimisation Rules](#optimisation-rules)

## Task 1: CQ Minimization
In Task 1, I implement the minimization algorithm for conjunctive queries. A program that reads a query from the specified input file, minimizes the query, and writes a minimized query in the specified output file. To implement the minimization algorithm for conjunctive queries, I consider a simple and straightforward 3-step approach:

1. Compile a list of possible term substitutions within the query body
2. Eliminate conflicting substitutions. 
3. Perform substitutions and get unique RelationalAtoms

Valid query examples:
- Q(x) :- R(x, ’This is a test string’)
- Q(x, z) :- R(4, z), S(x, y), T(12, ’x’)
- Q() :- R(y, z), R(z, x)
- DBSTUDENTNAME(name) :- STUDENT(id, name), ENROLLED(id, ’ADBS’)

### To compile and run

Usage: CQMinimizer input_file output_file

```
mvn clean compile assembly:single

java -cp target/minibase-1.0.0-jar-with-dependencies.jar \ed.inf.adbs.minibase.CQMinimizer \
data/minimization/input/query1.txt \
data/minimization/output/query1.txt
```

### Step 1
To obtain the list of possible term substitutions, I compare the list of terms for relational atoms with the same names. 

Since these atoms use the same schema of terms, if one contains a Variable while the other contains a Constant in the same position, this indicates a possible substitution.

> Example:  R(x,y,x) and R(x,y,3) has possible substitution (z -> 3)

### Step 2
To eliminate conflicting substitutions, I remove any repeated variables being substituted to different constant values. 

> Example: If the substitution list contains (x -> 1) and (x -> 2), both are removed.

### Step 3
To perform substitutions, I iterate over relational atoms and replace all variable terms with their corresponding substitution value.

### Key Relevant Files

- `CQMinimizer.java` - For query parsing and minimization
- `Extract.java` - For extracting lists of RelationalAtoms and etc. from query
- `Substitution.java` - For obtaining substitution list

<br>

## Task 2: Query Evaluation
In Task 2, I implement a simple interpreter for conjunctive queries. A program that takes in a database (a set of files with data) and an input file containing one query. The program will process and evaluate the query on the database and write the query result in the specified output file. The evaluation should be based on the set semantics, meaning no tuple duplicates should appear in the input and the output. To implement the interpreter for conjunctive queries, I construct a tree of Operators for each query. This means that for every query, I initialise a certain _Operator_ object (that could be _SumOperator_, _ProjectOperator_, _SelectOperator_ or _ScanOperator_) with 0 or more child Operators within it. 

### To compile and run

Usage: CQMinimizer input_file output_file

```
mvn clean compile assembly:single

java -cp target/minibase-1.0.0-jar-with-dependencies.jar \
ed.inf.adbs.minibase.Minibase \
data/evaluation/db \
data/evaluation/input/query1.txt \
data/evaluation/output/query1.csv
```

### Operator Classes
To determine the child Operators needed within each class, I consider the functions of each Operator class, as follows:

#### ScanOperator
Function: scans tuples in CSV files and returns them<br>
Child Operators: None

#### SelectOperator
Function: selects tuples from list satisfying selection conditions and returns them<br>
Child Operators: _ScanOperator_

#### JoinOperator
Function: joins tuples satisfying join conditions and returns them<br>
Left Child Operators: _ScanOperator_ or _SelectOperator_ or _JoinOperator_<br>
Right Child Operators: _ScanOperator_ or _SelectOperator_

#### ProjectOperator
Function: projects unique tuples satisfying all join/selection conditions to match order of head variables and returns them<br>
Child Operators: _ScanOperator_ or _SelectOperator_ or _JoinOperator_

#### SumOperator
Function: performs (group-by) aggregation on tuples satisfying all join/selection conditions and returns them<br>
Child Operators: _ProjectOperator_

## Evaluation Process
The process I use for query evaluation is as follows:

1. Determine _Operator_ class needed to perform evaluation
2. Create new instance of chosen _Operator_ class
3. Dump tuples from _Operator_ class into the specified output file

### Step 1
To determine the specific _Operator_ class needed for evaluation, I perform the following conditional checks:

<b>IF query head contains a SumAggregate:</b><br>
If the query contains a SumAggregate, then we need a _SumOperator_

<b>ELSE IF reordering of tuples is needed:</b><br>
If the tuples need reordering, then we need a _ProjectOperator_<br>
Tuples that need reordering include those from queries with:
- more than 1 RelationalAtoms or
- 1 RelationalAtom and terms in a different order from head variables.

<b>ELSE IF there are selection conditions:</b><br>
If the query has ComparisonAtoms or has explicit comparisons, then a _SelectOperator_ is needed

<b>ELSE:</b><br>
If the query does satisfy any of the above conditions, this means that there is only one RelationalAtom and that there are no SumAggregates and no reordering or selection required. Only a simple _ScanOperator_ is needed.

### Step 2
After performing the above conditional checks, we can create the _Operator_ class needed. Tuples can then be obtained from the _Operator_ by continuously call `getNextTuple()` until an empty tuple is returned.

The `getNextTuple()` implementations for each class are as follows:

**ScanOperator**
- Use scanner to get next line from the relation file
- Use schema and line of data obtained to create Tuple
- return Tuple

**SelectOperator**
- Get next Tuple from the _scanOperator_ child
- Check if Tuple satisfies selection conditions
- Selection conditions include explicit ones and those from ComparisonAtoms in query body
- Return Tuple only if conditions satisfied

**JoinOperator**
- Get next left and right Tuples from both left and right child _Operators_
- Check if Tuples satisfy join conditions
- If not satisfied, keep getting next right Tuple from right child _Operator_ until satisfied or no more right Tuples
- If no more right Tuples, get next left Tuple from left child _Operator_ and reset right child _Operator_ to get first right Tuple again
- Repeat until there are no more left Tuples
- Return Tuple only if join conditions satisfied and ensure left and Right Tuple are stored before return
- Tuples are stored so as to keep track of Tuples that have not been checked yet

**ProjectOperator**
- Get next Tuple from child _Operator_
- Reorder Tuple to match the order of head variables (including those in SumAggregate if exists)
- If there are no head variables, SumAggregate must exist so do not reorder because Tuple will be empty
- Return Tuple if SumAggregate exists
- Otherwise, return Tuple only if Tuple has not been returned before and ensure Tuple is stores in list of return Tuples

**SumOperator**
- Create a HashMap containing Tuples and SUM values grouped by the query's head variables
- Tuples should correspond to the group
- If no head variables, label all Tuples with the temporary group name
- Iterate through the list of Tuples in the HashMap and form the next Tuple by joining the Tuple in the group and the IntegerConstant SUM value
- Return the Tuple that has been formed
- If no head variables, return Tuple containing the sum value only

### Step 3
Finally, dump all the Tuples into a list using and use `getDump()` to pass the list of Tuples into the `writeDump()` method that writes data into the output file.

## Logic Behind Extracting Join Conditions

In my implementation, I consider 2 types of join conditions. The first being join conditions found on the ComparisonAtoms and the second being join conditions based on common variables in 2 RelationalAtoms. 

### Join Conditions from ComparisonAtom 
In order to extract join conditions found in ComparisonAtoms, I first ensure that only ComparisonAtoms containing terms in the 2 different RelationalAtoms (from which the 2 Tuples are obtained from) are considered. Thus, if the ComparisonAtom contains other terms, it will not be considered a join condition.

To simplify this process of extracting ComparisonAtoms relevant to a list of terms, I created a `ExtractRelationComparisonAtoms()` method in my _Extract_ class that extracts all ComparisonAtoms containing variables from the relevant list. 

Thereafter, I remove all ComparisonAtoms that contain only terms of one RelationalAtom and not the other as these conditions will already be satisfied beforehand in the _selectOperator_.

Code can be found in [JoinOperator.java](src/main/java/ed/inf/adbs/minibase/base/JoinOperator.java) - Last portion of `JoinOperator()` initialiser involving joinComparisonAtoms variable and 

### Join Conditions from Common Variables
In order to fulfill the second type of join condition which involves identifying common variables in both left and right RelationAtoms and ensuring they both have the same value, we simply just create a new list containing only left RelationalAtom variables and then apply `retainAll(rightVariables)` to keep only common variables.

From there, we can use the list of common variables to identify the index of variables in tuples using RelationalAtom term lists and check to see if common variables have the same value to satisfy the join conditions.

This is different from the first type of join condition that utilises ComparisonAtoms. This condition checks for the existence of common variables.

Code for extracting common variables can be found in [Extract.java](src/main/java/ed/inf/adbs/minibase/base/Extract.java) - `extractCommonVariables()` method and [JoinOperator.java](src/main/java/ed/inf/adbs/minibase/base/JoinOperator.java) - 
`satisfyCommonVariables()` that checks Term equality

<br>

## Task 3: Query Optimisation
To enrich Minibase with query optimisation, I have come up with the following optimisation rules for my query plan.

## Optimisation Rules

### Rule 1 - Always Minimize Query First
Before evaluating any query, I always ensure that the query is first minimised. This not only helps to eliminates the possibility of repeated RelationalAtoms but also reduces the number of RelationalAtoms if possible term substitutions exist. Since each RelationalAtom has at least on Operator being created, this significantly reduces the number of Operators created and makes the evaluation process more efficient.

### Rule 2 - Remove Constant Terms in SelectOperator
When getting tuples from the selectOperator, I always ensure that the list of terms does not contain any Constants. This means that if the RelationalAtom contained any Constants (which correspond to explicit selection conditions), I do not include the values in the Tuple corresponding to those Constants after performing condition checks. 

> Example: If RelationalAtom is R(x, y, 3) and Tuples obtained are (1, 2, 3), (2, 5, 3), remove the IntegerConstant 3 from the Tuple and only return (1, 2) and (2, 5). 

The reason behind this rule is the fact that once explicit selection checks are done, these values in the Tuple no longer matter as all Tuples returned already satisfy the explicit conditions. This reduces the number of terms in each Tuple to be passed to the next Operator and prevents redundant checking. 

### Rule 3 - Handling Repeated Methods

When writing code, it is best to create classes for frequently used methods. In query evaluation, this includes extraction of terms from the query body and checking whether tuples satisfy selection or join conditions. For this, I created the _Extract_ and _Comparison class_ to handle specific methods related to extraction and comparison. _FileManager_ is also another class I have created to handle file specific methods.

Besides that, it is worth noting that most _Operator_ classes also use the same functions, including `setNewAtom()`, `dump()`, `getDump()`. Thus to minimise code congestion, I have made sure to include these in the Abstract _Operator_ class so as to reuse the same function when extending _Operator_.

### Rule 4 - Always Identify Appropriate Operator

When creating a root and child _Operator_, it is extremely important to ensure that the created _Operator_ serves a purpose. For example, if no sum, join, projection or selection is required for evaluating the query, simply create a _scanOperator_. 

The same goes for child Operators contained within the root _Operator_. When we handle each RelationalAtom, if it does not have explicit selection conditions or related ComparisonAtoms, just create a _scanOperator_.

This is a serious issue as creating the wrong _Operator_ could easily result in more than twice the number of Operators needed being created, slowing down the entire evaluation process. Thus, when creating an _Operator_, I always check if the _RelationalAtom_ satisfies specific conditions before proceeding.

### Rule 5 - Don't Repeat Comparisons Done Before

Specifically when joining Tuples from different _Operators_, it may be easy to just check through all the ComparisonAtoms and join/selection conditions, but this results in unneccesary checks and great inefficiency. 

In my implementation, I use my _Extract_ class to extract only ComparisonAtoms related to the specific RelationalAtom or the specific variable terms of the Tuples being joined together, ensuring quick processing.

### Rule 6 - Use a HashMap in SumOperator
Last of all, in order to prevent unnecessary looping through the _projectOperator_ when deriving sum aggregates, I found it very useful to create a HashMap instead to store all the Tuples obtained from the _projectOperator_ in order to efficiently identify Tuples in the same group and calculate the sum aggregate.
