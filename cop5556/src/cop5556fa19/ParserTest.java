package cop5556fa19;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cop5556fa19.Parser;
import cop5556fa19.Parser.SyntaxException;
import cop5556fa19.AST.ASTNode;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Expressions;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.Scanner;
import cop5556fa19.Token;

import static cop5556fa19.Token.Kind.*;

class ParserTest {

    // To make it easy to print objects and turn this output on and off
    static final boolean doPrint = true;
//	static final boolean doPrint = false;

    private void show(Object input) {
	if (doPrint) {
	    System.out.println(input.toString());
	}
    }

    // creates a scanner, parser, and parses the input by calling exp().
    Exp parseExpAndShow(String input) throws Exception {
	show("parser input:\n" + input); // Display the input
	Reader r = new StringReader(input);
	Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
	Parser parser = new Parser(scanner);
	Exp e = parser.exp();
	show("e=" + e);
	return e;
    }

    // creates a scanner, parser, and parses the input by calling block()
    Block parseBlockAndShow(String input) throws Exception {
	show("parser input:\n" + input); // Display the input
	Reader r = new StringReader(input);
	Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
	Parser parser = new Parser(scanner);
	Method method = Parser.class.getDeclaredMethod("block");
	method.setAccessible(true);
	Block b = (Block) method.invoke(parser);
	show("b=" + b);
	return b;
    }

    // creates a scanner, parser, and parses the input by calling parse()
    // this corresponds to the actual use case of the parser
    Chunk parseAndShow(String input) throws Exception {
	show("parser input:\n" + input); // Display the input
	Reader r = new StringReader(input);
	Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
	Parser parser = new Parser(scanner);
	Chunk c = parser.parse();
	show("c=" + c);
	return c;
    }

    @Test
    void testEmpty1() throws Exception {
	String input = "";
	Block b = parseBlockAndShow(input);
	Block expected = Expressions.makeBlock();
	assertEquals(expected, b);
    }

    @Test
    void testEmpty2() throws Exception {
	String input = "";
	ASTNode n = parseAndShow(input);
	Block b = Expressions.makeBlock();
	Chunk expected = new Chunk(b.firstToken, b);
	assertEquals(expected, n);
    }

    @Test
    void testAssign1() throws Exception {
	String input = "a=b";
	Block b = parseBlockAndShow(input);
	List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpName("a"));
	List<Exp> rhs = Expressions.makeExpList(Expressions.makeExpName("b"));
	StatAssign s = Expressions.makeStatAssign(lhs, rhs);
	Block expected = Expressions.makeBlock(s);
	assertEquals(expected, b);
    }

    @Test
    void testAssignChunk1() throws Exception {
	String input = "a=b";
	ASTNode c = parseAndShow(input);
	List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpName("a"));
	List<Exp> rhs = Expressions.makeExpList(Expressions.makeExpName("b"));
	StatAssign s = Expressions.makeStatAssign(lhs, rhs);
	Block b = Expressions.makeBlock(s);
	Chunk expected = new Chunk(b.firstToken, b);
	assertEquals(expected, c);
    }

    @Test
    void testMultiAssign1() throws Exception {
	String input = "a,c=8,9";
	Block b = parseBlockAndShow(input);
	List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpName("a"), Expressions.makeExpName("c"));
	Exp e1 = Expressions.makeExpInt(8);
	Exp e2 = Expressions.makeExpInt(9);
	List<Exp> rhs = Expressions.makeExpList(e1, e2);
	StatAssign s = Expressions.makeStatAssign(lhs, rhs);
	Block expected = Expressions.makeBlock(s);
	assertEquals(expected, b);
    }

    @Test
    void testMultiAssign3() throws Exception {
	String input = "a,c=8,f(x)";
	Block b = parseBlockAndShow(input);
	List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpName("a"), Expressions.makeExpName("c"));
	Exp e1 = Expressions.makeExpInt(8);
	List<Exp> args = new ArrayList<>();
	args.add(Expressions.makeExpName("x"));
	Exp e2 = Expressions.makeExpFunCall(Expressions.makeExpName("f"), args, null);
	List<Exp> rhs = Expressions.makeExpList(e1, e2);
	StatAssign s = Expressions.makeStatAssign(lhs, rhs);
	Block expected = Expressions.makeBlock(s);
	assertEquals(expected, b);
    }

    @Test
    void testAssignToTable() throws Exception {
	String input = "g.a.b = 3";
	Block bl = parseBlockAndShow(input);
	ExpName g = Expressions.makeExpName("g");
	ExpString a = Expressions.makeExpString("a");
	Exp gtable = Expressions.makeExpTableLookup(g, a);
	ExpString b = Expressions.makeExpString("b");
	Exp v = Expressions.makeExpTableLookup(gtable, b);
	Exp three = Expressions.makeExpInt(3);
	Stat s = Expressions.makeStatAssign(Expressions.makeExpList(v), Expressions.makeExpList(three));
	;
	Block expected = Expressions.makeBlock(s);
	assertEquals(expected, bl);
    }

    @Test
    void testAssignTableToVar() throws Exception {
	String input = "x = g.a.b";
	Block bl = parseBlockAndShow(input);
	ExpName g = Expressions.makeExpName("g");
	ExpString a = Expressions.makeExpString("a");
	Exp gtable = Expressions.makeExpTableLookup(g, a);
	ExpString b = Expressions.makeExpString("b");
	Exp e = Expressions.makeExpTableLookup(gtable, b);
	Exp v = Expressions.makeExpName("x");
	Stat s = Expressions.makeStatAssign(Expressions.makeExpList(v), Expressions.makeExpList(e));
	;
	Block expected = Expressions.makeBlock(s);
	assertEquals(expected, bl);
    }

    @Test
    void testmultistatements6() throws Exception {
	String input = "x = g.a.b ; ::mylabel:: do  y = 2 goto mylabel f=a(0,200) end break"; // same as
											      // testmultistatements0
											      // except ;
	ASTNode c = parseAndShow(input);
	ExpName g = Expressions.makeExpName("g");
	ExpString a = Expressions.makeExpString("a");
	Exp gtable = Expressions.makeExpTableLookup(g, a);
	ExpString b = Expressions.makeExpString("b");
	Exp e = Expressions.makeExpTableLookup(gtable, b);
	Exp v = Expressions.makeExpName("x");
	Stat s0 = Expressions.makeStatAssign(v, e);
	StatLabel s1 = Expressions.makeStatLabel("mylabel");
	Exp y = Expressions.makeExpName("y");
	Exp two = Expressions.makeExpInt(2);
	Stat s2 = Expressions.makeStatAssign(y, two);
	Stat s3 = Expressions.makeStatGoto("mylabel");
	Exp f = Expressions.makeExpName("f");
	Exp ae = Expressions.makeExpName("a");
	Exp zero = Expressions.makeExpInt(0);
	Exp twohundred = Expressions.makeExpInt(200);
	List<Exp> args = Expressions.makeExpList(zero, twohundred);
	ExpFunctionCall fc = Expressions.makeExpFunCall(ae, args, null);
	StatAssign s4 = Expressions.makeStatAssign(f, fc);
	StatDo statdo = Expressions.makeStatDo(s2, s3, s4);
	StatBreak statBreak = Expressions.makeStatBreak();
	Block expectedBlock = Expressions.makeBlock(s0, s1, statdo, statBreak);
	Chunk expectedChunk = new Chunk(expectedBlock.firstToken, expectedBlock);
	assertEquals(expectedChunk, c);
    }

    @Test
    void testIdent0() throws Exception {
	String input = "x";
	Exp e = parseExpAndShow(input);
	assertEquals(ExpName.class, e.getClass());
	assertEquals("x", ((ExpName) e).name);
    }

    @Test
    void testIdent1() throws Exception {
	String input = "(x)";
	Exp e = parseExpAndShow(input);
	assertEquals(ExpName.class, e.getClass());
	assertEquals("x", ((ExpName) e).name);
    }

    @Test
    void testString() throws Exception {
	String input = "\"string\"";
	Exp e = parseExpAndShow(input);
	assertEquals(ExpString.class, e.getClass());
	assertEquals("string", ((ExpString) e).v);
    }

    @Test
    void testBoolean0() throws Exception {
	String input = "true";
	Exp e = parseExpAndShow(input);
	assertEquals(ExpTrue.class, e.getClass());
    }

    @Test
    void testBoolean1() throws Exception {
	String input = "false";
	Exp e = parseExpAndShow(input);
	assertEquals(ExpFalse.class, e.getClass());
    }

    @Test
    void testBinary0() throws Exception {
	String input = "1 + 2";
	Exp e = parseExpAndShow(input);
	Exp expected = Expressions.makeBinary(1, OP_PLUS, 2);
	show(expected);
	assertEquals(expected, e);
    }

    @Test
    void testUnary0() throws Exception {
	String input = "-2";
	Exp e = parseExpAndShow(input);
	Exp expected = Expressions.makeExpUnary(OP_MINUS, 2);
	show("expected=" + expected);
	assertEquals(expected, e);
    }

    @Test
    void testUnary1() throws Exception {
	String input = "-*2\n";
	assertThrows(SyntaxException.class, () -> {
	    Exp e = parseExpAndShow(input);
	});
    }

    @Test
    void testRightAssoc() throws Exception {
	String input = "\"concat\" .. \"is\"..\"right associative\"";
	Exp e = parseExpAndShow(input);
	Exp expected = Expressions.makeBinary(Expressions.makeExpString("concat"), DOTDOT,
		Expressions.makeBinary("is", DOTDOT, "right associative"));
	show("expected=" + expected);
	assertEquals(expected, e);
    }

    @Test
    void testLeftAssoc() throws Exception {
	String input = "\"minus\" - \"is\" - \"left associative\"";
	Exp e = parseExpAndShow(input);
	Exp expected = Expressions.makeBinary(
		Expressions.makeBinary(Expressions.makeExpString("minus"), OP_MINUS, Expressions.makeExpString("is")),
		OP_MINUS, Expressions.makeExpString("left associative"));
	show("expected=" + expected);
	assertEquals(expected, e);

    }

    @Test
    void testPowPrec() throws Exception {
	String input = "2^3^4";
	Exp e = parseExpAndShow(input);
	Exp expected = Expressions.makeBinary(Expressions.makeInt(2), OP_POW, Expressions.makeBinary(3, OP_POW, 4));
	show("expected=" + expected);
	assertEquals(expected, e);

    }

    @Test
    void testFunction() throws Exception {
	String input = "function(a,b,c) end";
	Exp e = parseExpAndShow(input);

    }

    @Test
    void testInvalidFunction() throws Exception {
	String input = "function(...) end";
	Exp e = parseExpAndShow(input);

    }

    @Test
    void testValidFieldList() throws Exception {
	String input = "{[a]=b}";
	Exp e = parseExpAndShow(input);
    }

    @Test
    void testBinaryExp1() throws Exception {
	String input = "-2-1";
	Exp e = parseExpAndShow(input);
    }

    @Test
    void testPrefixExp() throws Exception {
	String input = "{a,b,c}";
	Exp e = parseExpAndShow(input);
    }

    @Test
    void testParenthesesExp() throws Exception {

	parseExpAndShow("#5+4");
    }

    @Test
    void failed1() throws Exception {

	parseExpAndShow("function(xy,zy,...) end");

    }

    @Test
    void failed2() throws Exception {

	parseExpAndShow("{[x + y]= xx * yy,}");

    }

    @Test
    void failed3() throws Exception {
	// still failing
	parseExpAndShow("1 ~ 2 | 3 & 4");
	// expected - ExpBinary[e0=ExpBinary[e0=ExpInt[v=1],op=BIT_XOR,e1=ExpInt[v=2]],
	// op = BIT_OR, e1=ExpBin[eo=ExpInt[v=3],op = BIT_AMP, e1=ExpInt[v=4]]

    }

    @Test
    void failed4() throws Exception {

	parseExpAndShow("function (aa,b) end >> function(test, I, ...) end & function(...) end)");

    }

    @Test
    void failed5() throws Exception {

	parseExpAndShow("function (aa,b) end >> function(test, I, ...) end ");

    }

    @Test
    void failed6() throws Exception {

	parseExpAndShow("function (...) end");

    }

    // tests for block

    @Test
    void blockTest1() throws Exception {
	parseExpAndShow("function() break return a end");
    }

    @Test
    void blockTest2() throws Exception {
	parseExpAndShow("function() goto abc goto def goto xyz break goto x :: abc :: end");
    }

    @Test
    void blockTest3() throws Exception {
	parseExpAndShow("function() do break break break goto x :: abc :: end return a ; end");
    }

    @Test
    void blockTest4() throws Exception {
	parseExpAndShow("function() while true do break end end");
    }

    @Test
    void blockTest5() throws Exception {
	parseExpAndShow("function() repeat break until true end");
    }

    @Test
    void blockTest6() throws Exception {
	parseExpAndShow(
		"function() if true then break elseif false then break elseif abc then goto x else break goto a end end");
    }

    @Test
    void blockTest7() throws Exception {
	parseExpAndShow("function() for a = true , false , nil do break end end");
    }

    @Test
    void blockTest8() throws Exception {
	parseExpAndShow("function() for a,b,c in true,false do break end end");
    }

    @Test
    void blockTest9() throws Exception {
	parseExpAndShow("function() function abc.def:xyz (a,b,c) break end end");
    }

    @Test
    void blockTest10() throws Exception {
	parseExpAndShow("function() function abc (a,b,c) break end end");
    }

    @Test
    void blockTest11() throws Exception {
	parseExpAndShow("function() local function abc (a,b,c) break end end");
    }

    @Test
    void blockTest12() throws Exception {
	parseExpAndShow("function() local function abc (a,b,c) break end end");
    }

    // varlist = explist
    @Test
    void blockTest13() throws Exception {
	parseExpAndShow("function() a = true end");
    }

    // exp = prefixexp
    // prefixexp = var
    // var = Name | prefixexp '[' exp ']' | prefixexp '.' Name
    @Test
    void blockTest14() throws Exception {
	parseExpAndShow("f(a)[b]");
    }

    /*
     * f(a)[b] is an expression
     * 
     * e=ExpTableLookup [table=ExpFunctionCall [f=ExpName [name=f], args=[ExpName
     * [name=a]]], key=ExpName [name=b]]
     * 
     * 
     * 
     * f (a) [b] "g" is also an expression e=ExpFunctionCall [f=ExpTableLookup
     * [table=ExpFunctionCall [f=ExpName [name=f], args=[ExpName [name=a]]],
     * key=ExpName [name=b]], args=[ExpString [v=g]]]
     * 
     */
    @Test
    void blockTest15() throws Exception {
	parseExpAndShow("f (a) [b] \"g\")");
    }

    @Test
    void blockTest16() throws Exception {
	parseExpAndShow("function() break break return a,b,c,true,false ; end");
    }

    @Test
    void blockTest17() throws Exception {
	parseExpAndShow("v.name(v,args)");
	// e=ExpFunctionCall [f=ExpTableLookup [table=ExpName [name=v], key=ExpString
	// [v=name]], args=[ExpName [name=v], ExpName [name=args]]]
    }

    @Test
    void blockTest20() throws Exception {
	parseExpAndShow("v:name(args)");
	// e=ExpFunctionCall [f=ExpTableLookup [table=ExpName [name=v], key=ExpString
	// [v=name]], args=[ExpName [name=v], ExpName [name=args]]]
    }

    @Test
    void blockTest18() throws Exception {
	parseExpAndShow("function() x = g.a.b ; ::mylabel:: while true do y = 2 goto mylabel f=a(0,200) end break end");

    }

    @Test
    void blockTest21() throws Exception {
	parseAndShow("function a.b.c(self, d, e) return true; end");
    }

    @Test
    void blockTest22() throws Exception {
	parseExpAndShow("v:name(args)");
	
	parseExpAndShow("v[\"name\"](v, args)");
	
	parseExpAndShow("a.b:c(d, e)");
	// e=ExpFunctionCall [f=ExpTableLookup [table=ExpName [name=v], key=ExpString
	// [v=name]], args=[ExpName [name=v], ExpName [name=args]]]

    }
}
