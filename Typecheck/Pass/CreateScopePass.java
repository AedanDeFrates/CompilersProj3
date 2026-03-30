package Typecheck.Pass;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import Absyn.*;

public class CreateScopePass extends Pass<Void> {

   protected Scope currentscope;
   public Scope globalscope;

   public CreateScopePass() {
      this.globalscope = new Scope();
      this.currentscope = globalscope;
   }

   // Notes
    // This pass is creates child local scopes for all the funcs,structs,unions,ifs,and whiles
    // These scopes are defined recursively, all empty, will have bindings added in next two passes



// Hint: Functions introduce a new nested scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Temporarily switch currentscope to this new scope.
// 3. Visit the function type, parameters, and body.
// 4. Store the resulting scope in node.scope.
// 5. Restore the previous scope.
   @Override
   public Void visitFunDecl(FunDecl node) {

        Scope newScope = new Scope(currentscope);
        Scope prev = currentscope;
        currentscope = newScope;

        visit(node.type);
        for(Decl paremeter: node.params.list){
            visit(paremeter);
        }
        visit(node.body);

        node.scope = newScope;
        currentscope = prev;

        return null;

   }
// Hint: Struct bodies are evaluated inside their own scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to this new scope.
// 3. Visit the struct body.
// 4. Store this scope in node.scope.
// 5. Restore the previous scope.
   @Override
	public Void visitStructDecl(StructDecl node) {

       Scope structScope = new Scope(currentscope);
       Scope prev = currentscope;
       currentscope = structScope;

       for(Decl paremeter: node.body.list){
           visit(paremeter);
       }

       node.scope = structScope;
       currentscope = prev;
       return null;

	}
// Hint: Union bodies behave like structs for scoping.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to the new scope.
// 3. Visit the union body.
// 4. Store this scope in node.scope.
// 5. Restore the previous scope.
	@Override
	public Void visitUnionDecl(UnionDecl node) {

        Scope unionScope = new Scope(currentscope);
        Scope prev = currentscope;
        currentscope = unionScope;

        for(Decl paremeter: node.body.list){
            visit(paremeter);
        }

        node.scope = unionScope;
        currentscope = prev;
        return null;

	}
// Hint: If statements execute inside a fresh scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to this new scope.
// 3. Visit the condition and both branches.
// 4. Save this scope in node.scope.
// 5. Restore the previous scope.
	@Override
	public Void visitIfStmt(IfStmt node) {

        Scope ifScope = new Scope(currentscope);
        Scope prev = currentscope;
        currentscope = ifScope;

        visit(node.expression);
        visit(node.if_statement);
        visit(node.else_statement);

        node.scope = ifScope;
        currentscope = prev;
        return null;

	}
// Hint: Loops also introduce a nested scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to the new scope.
// 3. Visit the condition and loop body.
// 4. Store the scope in node.scope.
// 5. Restore the previous scope.
   @Override
	public Void visitWhileStmt(WhileStmt node) {
       Scope whileScope = new Scope(currentscope);
       Scope prev = currentscope;
       currentscope = whileScope;

       visit(node.expression);
       visit(node.statement);

       node.scope = whileScope;
       currentscope = prev;
       return null;

	}

}
