package Typecheck.Pass;
import Typecheck.Types.*;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import java.util.ArrayList;

// This pass implements the type rules.
// Some of the logic has been implemented for you in the Types.
// Check out the "canAccept" functions.
public class JudgementsPass extends ScopePass<Void> {
   public JudgementsPass(Scope s) {
      super(s);
   }

   // Recursively resolve ALIAS types by looking them up in the scope.
   // After this, ALIAS.canAccept() will work because setType() has been called.
   private void resolveAliases(Type t) {
      if (t instanceof ALIAS) {
         ALIAS alias = (ALIAS) t;
         if (alias.name != null && currentscope.hasType(alias.name)) {
            Type resolved = currentscope.getType(alias.name).type;
            alias.setType(resolved);
            // Recursively resolve the inner type too
            resolveAliases(resolved);
         } else {
            throw new TypeCheckException("Unknown type: " + (t instanceof ALIAS ? ((ALIAS)t).name : t));
         }
      } else if (t instanceof ARRAY) {
         resolveAliases(((ARRAY) t).type);
      } else if (t instanceof POINTER) {
         resolveAliases(((POINTER) t).type);
      } else if (t instanceof LIST) {
         for (Type elem : ((LIST) t).typelist) {
            resolveAliases(elem);
         }
      } else if (t instanceof OR) {
         for (Type opt : ((OR) t).options) {
            resolveAliases(opt);
         }
      }
      // INT, STRING, VOID need no resolution
   }

   //check if the type is a number
   // first checks if type is null and then uses resolveAliases to check what type it is if it isn't null
   // returns true if int or pointer (pointers count as numbers)
   private boolean isNumber(Type t){
      if (t == null) {
         return false;
      } else {
         resolveAliases(t);
         if (t instanceof INT || t instanceof POINTER){
            return true;
         }
         else {
            return false;
         }
      }
   }

   // Compute the Typecheck.Types.Type of an expression.
   private Type typeOf(Absyn.Exp e) {
      if (e instanceof Absyn.EmptyExp) return null;
      if (e instanceof Absyn.DecLit)   return new INT();
      if (e instanceof Absyn.StrLit)   return new STRING();
      if (e instanceof Absyn.ExpList) {
         ArrayList<Type> types = new ArrayList<>();
         for (Absyn.Exp sub : ((Absyn.ExpList) e).list) {
            types.add(typeOf(sub));
         }
         return new LIST(types);
      }
      if (e instanceof Absyn.ID) {
         String name = ((Absyn.ID) e).value;
         if (currentscope.hasVar(name)) {
            return currentscope.getVar(name).type;
         }
         throw new TypeCheckException("Variable '" + name + "' not found in scope");
      }

      // Rule 6: unions can be assigned any value that type checks with one of its members
      if (e instanceof Absyn.AssignExp){
         Absyn.AssignExp a = (Absyn.AssignExp) e;
         Type leftT = typeOf(a.left);
         Type rightT = typeOf(a.right);

         if (leftT == null || rightT == null){
            throw new TypeCheckException("Couldn't determine assignment types.");
         }
         resolveAliases(leftT);
         resolveAliases(rightT);
         if (!leftT.canAccept(rightT)) {
            throw new TypeCheckException("Invalid assignment");
         }
         return leftT;
      }

      // Rule 8: math operation can only accept numbers
      // checks both right and left factors to see what type they are
      // returns new number if both sides are numeric values
      // returns TypeCheckException if left or right is not a number
      if(e instanceof Absyn.BinOp){
         Absyn.BinOp b = (Absyn.BinOp) e;
         Type leftT = typeOf(b.left);
         Type rightT = typeOf(b.right);

         if (!isNumber(leftT) || !isNumber(rightT)) {
            throw new TypeCheckException("Binary operator '" + b.oper + "' requires both factors to be a numeric value.");
         }
         return new INT();
      }

      //Rule 9: function application must match parameter type and evaluate to expression of the return type
      if (e instanceof Absyn.FunExp){
         Absyn.FunExp f = (Absyn.FunExp) e;
         //check if the function name is an identifier
         if (!(f.name instanceof Absyn.ID)){
            throw new TypeCheckException("Function name must be an identifier.");
         }
         //isolate the function name and check if a function with that name exists
         String fname = ((Absyn.ID) f.name).value;
         if(!currentscope.hasFun(fname)){
            throw new TypeCheckException("Function '" + fname + "' not found");
         }
         //get the function symbol (stores the parameters types and return type)
         FunSymbol fs = currentscope.getFun(fname);
         //loop through to figure out all arguments
         ArrayList<Type> arTypes = new ArrayList<>();
         for (Absyn.Exp ar : f.params.list) {
            Type t = typeOf(ar);
            if (t == null) {
               throw new TypeCheckException(
                       "Could not determine type of argument for '" + fname + "'"
               );
            }
            resolveAliases(t);
            arTypes.add(t);
         }

         //check the list of actual arguments vs the list of expected arguments to make sure they match
         LIST actuals = new LIST(arTypes);
         LIST formals = fs.params;
         Type retType = fs.returnType;

         resolveAliases(formals);
         resolveAliases(retType);

         if (!formals.canAccept(actuals)) {
            throw new TypeCheckException(
                    "Function call arguments don't match the parameters for '" + fname + "'"
            );
         }

         return retType;
      }

      // Other expression types will be handled when implementing later rules
      return null;
   }


   // Rule 1: Numbers and strings are different values
   // Rule 2: Any number can be assigned to a pointer of any type (pointers count as numbers)
   // Rule 3: Arrays must be initialized with a list of the correct length
   // Rule 4: Structs must be initialized with a list matching their members
   @Override
   public Void visitVarDecl(Absyn.VarDecl node) {
      Type declaredType = node.type.typeAnnotation;

      if (declaredType == null) {
         return null;
      }

      resolveAliases(declaredType);

      Absyn.Exp init = node.init;

      // No initializer — nothing to check for Rules 1-4
      // (parser may represent "no init" as EmptyExp or a 0-element ExpList)
      if (init instanceof Absyn.EmptyExp) {
         return null;
      }
      if (init instanceof Absyn.ExpList && ((Absyn.ExpList) init).list.isEmpty()) {
         return null;
      }

      // Declared type is set on node.type by TypeAnnotationPass
      if (declaredType == null) {
         return null;
      }

      // Resolve any ALIAS types (e.g., struct/union names) before calling canAccept


      // Compute the type of the initializer expression
      Type initType = typeOf(init);
      if (initType == null) {
         // Cannot determine type yet (handled by later rules)
         return null;
      }
      resolveAliases(initType);

      // Rule 5: unions cannot be initialized with a list
      // unions are initialized with a single value that type-checks as one of its members
      if (declaredType instanceof OR && init instanceof Absyn.ExpList){
         throw new TypeCheckException(
                 "Union '" + node.name +"' cannot be initialized with a list. Must be initialized with a single value."
         );
      }

      // The canAccept methods encode Rules 1-4:
      //   Rule 1: INT.canAccept(STRING) == false, STRING.canAccept(INT) == false
      //   Rule 2: POINTER.canAccept(INT) == true, INT.canAccept(POINTER) == true
      //   Rule 3: LIST.canAccept(LIST) checks size + element types
      //   Rule 4: resolved struct LIST.canAccept(init LIST) checks member types
      if (!declaredType.canAccept(initType)) {
         throw new TypeCheckException(
                 "Type mismatch in declaration of '" + node.name + "': " +
                         "cannot assign " + initType + " to " + declaredType
         );
      }

      return null;
   }

   @Override
   public Void visitAssignExp(Absyn.AssignExp node) {
      typeOf(node);
      return null;
   }
}
