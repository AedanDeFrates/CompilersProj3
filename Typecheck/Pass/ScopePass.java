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

	   return null;
   }

   @Override
	public T visitStructDecl(StructDecl node) {
	   return null;
	}

	@Override
	public T visitUnionDecl(UnionDecl node) {
		return null;
	}

	@Override
	public T visitIfStmt(IfStmt node) {
		return null;
	}

   @Override
	public T visitWhileStmt(WhileStmt node) {
	   return null;
	}

}
