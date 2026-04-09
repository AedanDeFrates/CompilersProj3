

# ***CompilersProj3 - Semantic Analysis***
4/9/2026

Written by Joshua Walther

### Group Members

- Aedan DeFrates
- Alex Hawk
- Lorenzo Jackson
- Joshua Walther

## Theory
For this project we are implementing the semantic analysis of our compiler, taking the AST from the parser and creating an annotated, type-checked AST that is verified to be correct based on our typing rules.

### What is  Semantic Analysis?
Semantic Analysis is the process of verifying the correctness of the our program's meaning, checking if types, operations, and scope are both correct and compatible with each other. Variables types are enforced for assignment and operations, and scope of variables is managed to make sure variable, function, and type names exist and without conflicts. The compiler will throw errors if there are any violations of the typing and semantic rules.


## How to run our project
To run our type checker, use the provided shell script and test files.      
`./run.sh <optional_test_file_name>`

## Code Implementation
Because we are still working with the AST again, we will continue to use the Visitor pattern and visitor methods to navigate and work with the tree. In this project we will be implementing Pass classes that traverse the whole tree and perform operations on the tree when visiting specific nodes. These passes will assign types, create and assign scopes, and enforce our typing rules.


### Symbol Table
The prewritten symbol table classes define our scopes and symbols contained in them. The scope class maps Var, Func, and Type labels to their underlying types, return types, and parameters. The scope class has a reference to its parent scope, and has methods to lookup symbols and add symbols to scope. We will be attaching scope objects to nodes that have there own scopes, like functions. From there we can add symbols to the scope and check what items are defined in the local or parent scopes.

### Type Classes
The prewritten Type classes define our eight underlying types. Each Type class has an accept method that defines determines if an unknown type is the same as that type. We will assign these types to variables, function parameters and returns, and custom types.

Type Classes:
- VOID - matches no types, used as function return type
- INT - numbers
- STRING - strings
- ARRAY -
- LIST -
- OR - represents a union of multiple types
- ALLIAS - defines customs types, made up of existing types
- POINTER - a pointer of another type, counted as number for binary operations
-

### Passes
The Pass classes are what we are implementing for this project. We use visit functions to traverse every type of node in the AST tree and define specific behavior for nodes, like adding types to type definition nodes, and scopes to function nodes. The steps of our type checking are split into multiple passes to improve organize of our code and isolate different functionality. We have six pass classes.

#### TypeAnnotationPass
This pass visits variable, parameter, and struct and union member nodes and assigns them a corresponding Type object. This means first determining the base type, INT, STRING, VOID, or ALLIAS, and then if applicable creating POINTER, LIST, or ARRAY types iteratively from those base types.

#### CreateScopePass
This pass visits function, struct, union, if, and while nodes and creates empty local scopes objects, using the current scope as the parent of the new scope. When visiting each node we save the current scope, create a  new current scope with the previous scope as its parent, visit all children nodes, then restore the previous scope as the current. This allows us to reclusively assign scopes to nodes easily.

#### ScopePass
This is the parent class of the rest of our passes. Like the previous pass. It saves the current scope, switches it to the local scope, then visits all the children nodes, before restoring the previous scope as the current scope. This class makes it easier to implement our other classes and reduce redundant code writing when using multiple passes.

#### FunAndVarScopePass
This pass finds functions and variables, and parameters, and adds them to their corresponding local scopes, making sure there are no conflicts

#### TypeScopePass
This pass finds types, struct, and union declarations and adds them to their local scope

#### PrintPass
This is a custom print class that prints a string representation of all of the bindings in the local scopes. We implemented toString methods for Scope, Type, and Symbol classes and call this for both the global and local scopes.

#### JudgmentPass
This is the path that implements the fourteen typing rules.


## Group Strategy and Issues Encountered

### Group Strategy
We split the tasks up for this project by creating GitHub issues, and grouping the passes into Steps, Step 1 for type annotation and creating scope, Step 2 for assigning bindings to local scopes, and Step 3 for the judgment pass.

We created print statements for every visit function in every pass so that we could clearly see what functions are running in each pass, and if they are the overloaded or inherited methods.

We also created a new PrintPass class to print out all of the bindings in each scope before the Judgement pass, allowing us to easily debug the passes and help implement the rules. To do this we added/modified toString methods for the Scope, ScopeBucket, Symbol, and Type classes. This way we can simply print a Scope object and see all of the bindings in that scope.

### Issues Encountered
The biggest issue for the project is the lack of feedback in comparison to the last two. For this project there are no test cases provided, no print output, and no solution to judge the validity of our project with, and given the nature of this project, bugs can be caused by an uncaught error from any part of the program which made it very difficult to work on. We did appreciate that the passes we needed to implement and the methods to override where clearly defined, which was a great improvement over the previous assignments where the exact items needing to be implemented was not clearly laid out.

There was also what felt like a lot of duplicate code writing, every function overload for the passes means reimplementing the scope switching, with some passes made up of mostly duplicated code. It might have been better if we implemented some helper class for switching state although this would take some reorganizing of the existing code. 