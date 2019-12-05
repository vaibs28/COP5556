package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cop5556fa19.Token;
import cop5556fa19.Token.Kind;
import cop5556fa19.AST.ASTVisitor;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpList;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldList;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.FuncName;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.RetStat;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatFor;
import cop5556fa19.AST.StatForEach;
import cop5556fa19.AST.StatFunction;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatIf;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.AST.StatLocalAssign;
import cop5556fa19.AST.StatLocalFunc;
import cop5556fa19.AST.StatRepeat;
import cop5556fa19.AST.StatWhile;

public abstract class ASTVisitorAdapter implements ASTVisitor {

    Stack<String> stack = new Stack<String>();
    int count = 0;

    @SuppressWarnings("serial")
    public static class StaticSemanticException extends Exception {

	public StaticSemanticException(Token first, String msg) {
	    super(first.line + ":" + first.pos + " " + msg);
	}
    }

    @SuppressWarnings("serial")
    public static class TypeException extends Exception {

	public TypeException(String msg) {
	    super(msg);
	}

	public TypeException(Token first, String msg) {
	    super(first.line + ":" + first.pos + " " + msg);
	}

    }

    public abstract List<LuaValue> load(Reader r) throws Exception;

    @Override
    public Object visitExpNil(ExpNil expNil, Object arg) {
	return LuaNil.nil;
    }

    @Override
    public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {

	Object exp0 = expBin.e0.visit(this, arg);
	Object exp1 = expBin.e1.visit(this, arg);

	if (expBin.op == Kind.OP_PLUS) {
	    return new LuaInt(toInteger(exp0) + toInteger(exp1));
	} else if (expBin.op == Kind.OP_MINUS) {
	    return new LuaInt(toInteger(exp0) - toInteger(exp1));
	} else if (expBin.op == Kind.OP_TIMES) {
	    return new LuaInt(toInteger(exp0) * toInteger(exp1));
	} else if (expBin.op == Kind.OP_DIV) {
	    return new LuaInt(toInteger(exp0) / toInteger(exp1));
	} else if (expBin.op == Kind.OP_MOD) {
	    return new LuaInt(toInteger(exp0) % toInteger(exp1));
	} else if (expBin.op == Kind.OP_POW) {
	    return new LuaInt((int) Math.pow(toInteger(exp0), toInteger(exp1)));
	} else if (expBin.op == Kind.OP_DIVDIV) {
	    return new LuaInt(Math.floorDiv(toInteger(exp0), toInteger(exp1)));
	} else if (expBin.op == Kind.REL_EQEQ) {
	    return new LuaBoolean(exp0.equals(exp1));
	} else if (expBin.op == Kind.REL_NOTEQ) {
	    return new LuaBoolean(!exp0.equals(exp1));
	} else if (expBin.op == Kind.BIT_OR) {
	    return new LuaInt(toInteger(exp0) | toInteger(exp1));
	} else if (expBin.op == Kind.BIT_AMP) {
	    return new LuaInt(toInteger(exp0) & toInteger(exp1));
	} else if (expBin.op == Kind.BIT_XOR) {
	    return new LuaInt(toInteger(exp0) ^ toInteger(exp1));
	} else if (expBin.op == Kind.BIT_SHIFTL) {
	    return new LuaInt(toInteger(exp0) << toInteger(exp1));
	} else if (expBin.op == Kind.BIT_SHIFTR) {
	    return new LuaInt(toInteger(exp0) >> toInteger(exp1));
	} else if (expBin.op == Kind.KW_and) {
	    return new LuaBoolean(toBoolean(exp0) && toBoolean(exp1));
	} else if (expBin.op == Kind.KW_or) {
	    return new LuaBoolean(toBoolean(exp0) || toBoolean(exp1));
	} else if (expBin.op == Kind.REL_EQEQ) {
	    return new LuaBoolean(toBoolean(exp0) == toBoolean(exp1));
	} else if (expBin.op == Kind.REL_NOTEQ) {
	    return new LuaBoolean(toBoolean(exp0) != toBoolean(exp1));
	} else if (expBin.op == Kind.REL_GE) {
	    if (areIntegers(exp0, exp1)) {
		return new LuaBoolean(toInteger(exp0) >= toInteger(exp1));
	    } else if (areStrings(exp0, exp1)) {
		return new LuaBoolean(exp0.toString().compareTo(exp1.toString()) >= 0 ? true : false);
	    } else {
		throw new TypeException("Invald type");
	    }

	} else if (expBin.op == Kind.REL_GT) {
	    if (areIntegers(exp0, exp1)) {
		return new LuaBoolean(toInteger(exp0) > toInteger(exp1));
	    } else if (areStrings(exp0, exp1)) {
		return new LuaBoolean(exp0.toString().compareTo(exp1.toString()) > 0 ? true : false);
	    } else {
		throw new TypeException("Invald type");
	    }

	} else if (expBin.op == Kind.REL_LE) {
	    if (areIntegers(exp0, exp1)) {
		return new LuaBoolean(toInteger(exp0) <= toInteger(exp1));
	    } else if (areStrings(exp0, exp1)) {
		return new LuaBoolean(exp0.toString().compareTo(exp1.toString()) <= 0 ? true : false);
	    } else {
		throw new TypeException("Invald type");
	    }

	} else if (expBin.op == Kind.REL_LT) {
	    if (areIntegers(exp0, exp1)) {
		return new LuaBoolean(toInteger(exp0) < toInteger(exp1));
	    } else if (areStrings(exp0, exp1)) {
		return new LuaBoolean(exp0.toString().compareTo(exp1.toString()) < 0 ? true : false);
	    } else {
		throw new TypeException("Invald type");
	    }

	} else {
	    throw new IllegalArgumentException();
	}

    }

    public boolean areIntegers(Object o1, Object o2) {
	if (o1 instanceof LuaInt && o2 instanceof LuaInt)
	    return true;
	else
	    return false;
    }

    public boolean areStrings(Object o1, Object o2) {
	if (o1 instanceof LuaString && o2 instanceof LuaString)
	    return true;
	else
	    return false;
    }

    @Override
    public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
	Object visited = unExp.e.visit(this, arg);
	if (unExp.op == Kind.OP_MINUS) {
	    return new LuaInt(-1 * toInteger(visited));
	} else if (unExp.op == Kind.KW_not) {
	    return new LuaBoolean(!toBoolean(visited));
	} else if (unExp.op == Kind.BIT_XOR) {
	    return new LuaInt(~toInteger(visited));
	} else if (unExp.op == Kind.OP_HASH) {
	    if (visited instanceof LuaString)
		return new LuaInt(((LuaString) visited).value.length());
	    if (visited instanceof LuaTable)
		return new LuaInt(0);
	    throw new TypeException("Invalid type");
	} else {
	    throw new IllegalArgumentException("Invalid unary operator " + unExp.op);
	}
    }

    @Override
    public Object visitExpInt(ExpInt expInt, Object arg) {
	return new LuaInt(expInt.v);
    }

    @Override
    public Object visitExpString(ExpString expString, Object arg) {
	return new LuaString(expString.v);
    }

    @Override
    public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
	LuaTable table = new LuaTable();
	for (Field f : expTableConstr.fields) {
	    Object visited = f.visit(this, arg);
	    if (visited instanceof TableKV) {
		table.put((LuaValue) ((TableKV) visited).getKey(), (LuaValue) ((TableKV) visited).getValue());
	    } else {
		table.putImplicit((LuaValue) visited);
	    }
	}
	return table;
    }

    @Override
    public Object visitExpList(ExpList expList, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitParList(ParList parList, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitName(Name name, Object arg) {
	return new LuaString(name.name);
    }

    // visit block within chunk by visiting List<Stat>
    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
	int statCount = block.stats.size(); // get the count of the statements in a block
	Object visitedStat = null;
	// visit all the statements
	for (int i = 0; i < statCount; i++) {
	    try {
		Stat st = block.stats.get(i);
		visitedStat = st.visit(this, arg);
		if (visitedStat != null)
		    return visitedStat;
	    } catch (GotoException e) {
		if (e.statLabel.enclosingBlock == block)
		    i = e.statLabel.index;
		else
		    throw e;
	    }
	}
	return visitedStat;
    }

    @Override
    public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
	throw new UnsupportedOperationException(statGoto.toString());
    }

    @Override
    public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
	try {
	    return statDo.b.visit(this, arg);
	} catch (Exception e) {
	    throw e;
	}
    }

    @Override
    public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
	Object visitedStatWhile = null;

	while (true) {
	    Object condition = statWhile.e.visit(this, arg);
	    if (condition != null)
		break;
	    try {
		visitedStatWhile = statWhile.b.visit(this, arg);
		if (visitedStatWhile != null) {
		    break;
		}
	    } catch (Exception e) {
		break;
	    }
	}

	return visitedStatWhile;
    }

    @Override
    public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    private boolean asBoolean(Object visit) {
	return visit instanceof LuaBoolean ? ((LuaBoolean) visit).value : !(visit instanceof LuaNil);
    }

    @Override
    public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
	for (int i = 0; i < statIf.es.size(); i++) {
	    stack.push("value");
	    Object visit = statIf.es.get(i).visit(this, arg);
	    stack.pop();
	    if (asBoolean(visit)) {
		Object visit1 = statIf.bs.get(i).visit(this, arg);
		if (visit1 != null)
		    return visit1;
		break;
	    }
	}
	return null;
    }

    @Override
    public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitFuncName(FuncName funcName, Object arg) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
	List<Object> list = new ArrayList<>();
	for (Exp e : retStat.el) {
	    stack.push("value");
	    Object obj = e.visit(this, arg);
	    list.add(obj);
	    stack.pop();
	}
	return list;
    }

    // visit chunk
    @Override
    public Object visitChunk(Chunk chunk, Object arg) throws Exception {
	stack.push("exp");
	Object visitedChunk = chunk.block.visit(this, arg);
	stack.pop();
	return visitedChunk;
    }

    @Override
    public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object object) throws Exception {
	Object key = fieldExpKey.key.visit(this, object);
	Object value = fieldExpKey.value.visit(this, object);
	return new TableKV(key, value);
    }

    @Override
    public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {

	Object key = fieldNameKey.name.visit(this, arg);
	Object value = fieldNameKey.exp.visit(this, arg);
	return new TableKV(key, value);
    }

    @Override
    public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
	return fieldImplicitKey.exp.visit(this, arg);
    }

    @Override
    public Object visitExpTrue(ExpTrue expTrue, Object arg) {
	return new LuaBoolean(true);
    }

    @Override
    public Object visitExpFalse(ExpFalse expFalse, Object arg) {
	return new LuaBoolean(false);
    }

    @Override
    public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
	LuaTable table = (LuaTable) arg;

	for (int i = 0; i < statAssign.varList.size(); i++) {
	    Exp var = statAssign.varList.get(i);
	    Exp exp = null;
	    if (i >= statAssign.expList.size())
		exp = ExpNil.expNilConst;
	    else
		exp = statAssign.expList.get(i);

	    if (var instanceof ExpTableLookup) {
		stack.push("value");
		ExpTableLookup expTableLookup = (ExpTableLookup) var;
		LuaTable internalTable = (LuaTable) expTableLookup.table.visit(this, arg);
		Object key = expTableLookup.key.visit(this, arg);
		Object value = exp.visit(this, arg);
		stack.pop();
		internalTable.put((LuaValue) key, (LuaValue) value);
	    } else {
		Object key = var.visit(this, arg);
		Object value = exp.visit(this, arg);
		table.put((LuaValue) key, (LuaValue) value);
	    }
	}
	return null;
    }

    @Override
    public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {

	Object expTable = expTableLookup.table.visit(this, arg);
	Object key = expTableLookup.key.visit(this, arg);

	if (!(expTable instanceof LuaTable))
	    throw new TypeException("");
	return ((LuaTable) expTable).get((LuaValue) key);
    }

    @Override
    public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
	LuaTable table = (LuaTable) arg;
	Object functionName = expFunctionCall.f.visit(this, arg);
	List<LuaValue> args = new ArrayList<>();
	for (Exp v : expFunctionCall.args) {
	    args.add((LuaValue) v.visit(this, arg));
	}

	((JavaFunction) table.get((LuaString) functionName)).call(args);
	return null;
    }

    @Override
    public Object visitLabel(StatLabel statLabel, Object ar) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitFieldList(FieldList fieldList, Object arg) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object visitExpName(ExpName expName, Object arg) {
	LuaTable table = (LuaTable) arg;
	LuaString l = new LuaString(expName.name);
	return stack.peek().equals("value") ? table.get(l) : l;
    }

    public int toInteger(Object v) throws TypeException {
	if (v instanceof LuaInt) {
	    return ((LuaInt) v).intValue();
	} else if (v instanceof LuaString) {
	    return Integer.parseInt(v.toString());
	} else {
	    throw new TypeException("Cannot convert to int");
	}
    }

    public boolean toBoolean(Object v) throws TypeException {
	if (v instanceof LuaBoolean) {
	    return ((LuaBoolean) v).value;
	} else {
	    throw new TypeException("Not a boolean");
	}
    }

}

class TableKV {
    Object key;
    Object value;

    public TableKV(Object key, Object value) {
	super();
	this.key = key;
	this.value = value;
    }

    public Object getKey() {
	return key;
    }

    public void setKey(Object key) {
	this.key = key;
    }

    public Object getValue() {
	return value;
    }

    public void setValue(Object value) {
	this.value = value;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((key == null) ? 0 : key.hashCode());
	result = prime * result + ((value == null) ? 0 : value.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TableKV other = (TableKV) obj;
	if (key == null) {
	    if (other.key != null)
		return false;
	} else if (!key.equals(other.key))
	    return false;
	if (value == null) {
	    if (other.value != null)
		return false;
	} else if (!value.equals(other.value))
	    return false;
	return true;
    }

}

class GotoException extends Exception {
    final StatLabel statLabel;

    private GotoException(StatLabel statLabel) {
	this.statLabel = statLabel;
    }
}
