package Typecheck.Pass;
import Absyn.*;
import Typecheck.SymbolTable.*;

public class ScopePass<T> extends Pass<T> {

   protected Scope currentscope;
	protected T defaultReturn = null;

    // Hint: Save scope → switch to node.scope → visit children → restore scope.

   public ScopePass(Scope s) {
      this.currentscope = s;
   }

   // HINT FROM LINE 10
   // 1. In new scope, set the scope to the current node scope
   // 2. visit the children (body, params, members, etc) inside the new scope
   // 3. restore the previous scope

   @Override
   public T visitFunDecl(FunDecl node) 
   {
	// 1.
	Scope prevScope = new Scope(currentscope);
	currentscope = node.scope;

	System.out.println("SCOPE_PASS visitFunDecl\n   " + node.name);

	// 2.
	visit(node.params);
	visit(node.body);

	// 3.
	currentscope = prevScope;

	return null;
   }

   //PATTERN REPEATS FOR REMAINING FUNCTIONS
   @Override
	public T visitStructDecl(StructDecl node) 
	{
		Scope prevScope = currentscope;
		currentscope = node.scope;

		System.out.println("SCOPE_PASS visitStructDecl\n   " + node.name);
	   	
		visit(node.body);

		currentscope = prevScope;

		return null;
	}

	@Override
	public T visitUnionDecl(UnionDecl node) 
	{
		Scope prevScope = currentscope;
		currentscope = node.scope;

		visit(node.body);

		currentscope = prevScope;

		return null;
	}

	@Override
	public T visitIfStmt(IfStmt node) 
	{
		Scope prevScope = currentscope;
		currentscope = node.scope;

		visit(node.if_statement);
		visit(node.else_statement);
		visit(node.expression);

		System.out.println("SCOPE_PASS visitIfStmt\n   IF");
	   	
		currentscope = prevScope;

		return null;
	}

   @Override
	public T visitWhileStmt(WhileStmt node) 
	{
		Scope prevScope = currentscope;
		currentscope = node.scope;

		visit(node.expression);
		visit(node.statement);

		System.out.println("SCOPE_PASS visitWhileStmt\n   WHILE");

		currentscope = prevScope;
	   	
	   return null;
	}

}
