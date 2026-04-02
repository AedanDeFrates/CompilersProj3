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

   @Override
   public T visitFunDecl(FunDecl node) {
	   Scope prevscope = currentscope;
	   currentscope = node.scope;

	   visit(node.params);
	   visit(node.body);

	   currentscope=prevscope;
	   return null;
   }

   @Override
	public T visitStructDecl(StructDecl node)
		Scope prevscope = currentscope;
		currentscope = node.scope;

		visit(node.body);

		currentscope=prevscope;
		return null;

	}

	@Override
	public T visitUnionDecl(UnionDecl node) {

		Scope prevscope = currentscope;
		currentscope = node.scope;

		visit(node.body);

		currentscope=prevscope;
		return null;
	}

	@Override
	public T visitIfStmt(IfStmt node) {

		Scope prevscope = currentscope;
		currentscope = node.scope;

		visit(node.expression);
		visit(node.if_statement);
		visit(node.else_statement);

		currentscope=prevscope;
		return null;
	}

   @Override
	public T visitWhileStmt(WhileStmt node) {
	   Scope prevscope = currentscope;
	   currentscope = node.scope;

	   visit(node.expression);
	   visit(node.statement);

	   currentscope=prevscope;
	   return null;
	}

}
