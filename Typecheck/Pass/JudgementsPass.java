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
         if (alias.name != null && currentscope.hasVar(alias.name)) {
            Type resolved = currentscope.getVar(alias.name).type;
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
      // Other expression types will be handled when implementing later rules
      return null;
   }

   // Rule 1: Numbers and strings are different values
   // Rule 2: Any number can be assigned to a pointer of any type (pointers count as numbers)
   // Rule 3: Arrays must be initialized with a list of the correct length
   // Rule 4: Structs must be initialized with a list matching their members
   @Override
   public Void visitVarDecl(Absyn.VarDecl node) {
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
      Type declaredType = node.type.typeAnnotation;
      if (declaredType == null) {
         return null;
      }

      // Resolve any ALIAS types (e.g., struct/union names) before calling canAccept
      resolveAliases(declaredType);

      // Compute the type of the initializer expression
      Type initType = typeOf(init);
      if (initType == null) {
         // Cannot determine type yet (handled by later rules)
         return null;
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
}
