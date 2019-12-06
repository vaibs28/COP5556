package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import cop5556fa19.Token;
import cop5556fa19.Token.Kind;
import cop5556fa19.AST.ASTVisitor;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
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
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldList;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.FuncName;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.RetStat;
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

//Traverses the AST and finds labels and get the names for jumping to that block
public class StaticAnalysis implements ASTVisitor {

    private final Stack<Integer> stack = new Stack<>();
    private final Map<String, List<Scope>> map = new HashMap<>();
    private boolean flag = true;
    private int current, next;

    public StaticAnalysis() {
	current = 0;
	next = 1;
	stack.push(0);
    }

    @Override
    public Object visitExpNil(ExpNil expNil, Object arg) throws Exception {
	return LuaNil.nil;
    }

    @Override
    public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpInt(ExpInt expInt, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpString(ExpString expString, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpList(ExpList expList, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitParList(ParList parList, Object arg) throws Exception {

	return null;
    }

    @Override
    public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitName(Name name, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
	initScope();
	for (int i = 0; i < block.stats.size(); i++) {
	    block.stats.get(i).visit(this, new TableKV(block, i));
	}
	leaveScope();
	return null;
    }

    @Override
    public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
	return null;
    }

    @Override
    public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
	if (flag)
	    return null;
	statGoto.label = lookup(statGoto);
	return null;
    }

    @Override
    public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
	statDo.b.visit(this, arg);
	return null;
    }

    @Override
    public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
	statWhile.b.visit(this, arg);
	return null;
    }

    @Override
    public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
	statRepeat.b.visit(this, arg);
	return null;
    }

    @Override
    public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
	for (Block b : statIf.bs) {
	    b.visit(this, arg);
	}
	return null;
    }

    @Override
    public Object visitStatFor(StatFor statFor, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitFuncName(FuncName funcName, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitChunk(Chunk chunk, Object arg) throws Exception {
	chunk.block.visit(this, arg);
	flag = false;
	resetScope();
	return null;
    }

    @Override
    public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpTrue(ExpTrue expTrue, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpFalse(ExpFalse expFalse, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {

	return null;
    }

    @Override
    public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {

	return null;
    }

    @Override
    public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Object visitLabel(StatLabel statLabel, Object ar) throws Exception {
	if (!flag)
	    return null;

	TableKV kv = (TableKV) ar;
	System.out.println(kv.key);
	System.out.println(kv.value);
	statLabel.enclosingBlock = (Block) kv.getKey();
	statLabel.index = (int) kv.getValue();
	insert(statLabel);
	return null;
    }

    private void insert(StatLabel stat) throws StaticSemanticException {
	Scope data = new Scope(current, stat);
	map.putIfAbsent(stat.label.name, new ArrayList<>());
	if (map.get(stat.label.name).contains(data)) {
	    throw new StaticSemanticException(null, "");
	}
	map.get(stat.label.name).add(data);
    }

    private StatLabel lookup(StatGoto sGoto) throws StaticSemanticException {
	if (!map.containsKey(sGoto.name.name))
	    throw new StaticSemanticException(new Token(Kind.EOF, null, 0, 0), "");

	Scope data = null;
	List<Scope> labels = map.get(sGoto.name.name);
	for (int i = labels.size() - 1; i >= 0; i--) {
	    int depth = stack.search(labels.get(i).scope);
	    if (depth > -1 && (data == null || depth < stack.search(data.scope))) {
		data = labels.get(i);
	    }
	}
	if (data != null)
	    return data.label;
	throw new StaticSemanticException(new Token(Kind.EOF, "", 0, 0), "");
    }

    @Override
    public Object visitFieldList(FieldList fieldList, Object arg) throws Exception {
	return null;
    }

    @Override
    public Object visitExpName(ExpName expName, Object arg) throws Exception {
	return null;
    }

    private void resetScope() {
	current = 0;
	next = 1;
	stack.clear();
	stack.push(0);
    }

    private void initScope() {
	current = next++;
	stack.push(current);
    }

    private void leaveScope() {
	stack.pop();
	current = stack.peek();
    }

}

class Scope {
    public final int scope;
    public final StatLabel label;

    public Scope(int scope, StatLabel label) {
	this.scope = scope;
	this.label = label;
    }

    @Override
    public boolean equals(Object o) {
	if (this == o)
	    return true;
	if (o == null || getClass() != o.getClass())
	    return false;
	Scope data = (Scope) o;
	return scope == data.scope && Objects.equals(label, data.label);
    }

    @Override
    public int hashCode() {
	return Objects.hash(scope, label);
    }
}
