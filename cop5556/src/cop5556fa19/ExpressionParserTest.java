/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import static cop5556fa19.Token.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.Expressions;

import cop5556fa19.ExpressionParser.SyntaxException;
import cop5556fa19.Scanner;

class ExpressionParserTest {

    // To make it easy to print objects and turn this output on and off
    static final boolean doPrint = true;

    private void show(Object input) {
	if (doPrint) {
	    System.out.println(input.toString());
	}
    }

    // creates a scanner, parser, and parses the input.
    Exp parseAndShow(String input) throws Exception {
	show("parser input:\n" + input); // Display the input
	Reader r = new StringReader(input);
	Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
	ExpressionParser parser = new ExpressionParser(scanner); // Create a parser
	Exp e = parser.exp(); // Parse and expression
	show("e=" + e); // Show the resulting AST
	return e;
    }

    @Test
    void testIdent0() throws Exception {
	String input = "x";
	Exp e = parseAndShow(input);
	assertEquals(ExpName.class, e.getClass());
	assertEquals("x", ((ExpName) e).name);
    }

    @Test
    void testIdent1() throws Exception {
	String input = "(x)";
	Exp e = parseAndShow(input);
	assertEquals(ExpName.class, e.getClass());
	assertEquals("x", ((ExpName) e).name);
    }

    @Test
    void testString() throws Exception {
	String input = "\"string\"";
	Exp e = parseAndShow(input);
	assertEquals(ExpString.class, e.getClass());
	assertEquals("string", ((ExpString) e).v);
    }

    @Test
    void testBoolean0() throws Exception {
	String input = "true";
	Exp e = parseAndShow(input);
	assertEquals(ExpTrue.class, e.getClass());
    }

    @Test
    void testBoolean1() throws Exception {
	String input = "false";
	Exp e = parseAndShow(input);
	assertEquals(ExpFalse.class, e.getClass());
    }

    @Test
    void testBinary0() throws Exception {
	String input = "1 + 2";
	Exp e = parseAndShow(input);
	Exp expected = Expressions.makeBinary(1, OP_PLUS, 2);
	show(expected);
	assertEquals(expected, e);
    }

    @Test
    void testUnary0() throws Exception {
	String input = "-2";
	Exp e = parseAndShow(input);
	Exp expected = Expressions.makeExpUnary(OP_MINUS, 2);
	show("expected=" + expected);
	assertEquals(expected, e);
    }

    @Test
    void testUnary1() throws Exception {
	String input = "-*2\n";
	assertThrows(SyntaxException.class, () -> {
	    Exp e = parseAndShow(input);
	});
    }

    @Test
    void testRightAssoc() throws Exception {
	String input = "\"concat\" .. \"is\"..\"right associative\"";
	Exp e = parseAndShow(input);
	Exp expected = Expressions.makeBinary(Expressions.makeExpString("concat"), DOTDOT,
		Expressions.makeBinary("is", DOTDOT, "right associative"));
	show("expected=" + expected);
	assertEquals(expected, e);
    }

    @Test
    void testLeftAssoc() throws Exception {
	String input = "\"minus\" - \"is\" - \"left associative\"";
	Exp e = parseAndShow(input);
	Exp expected = Expressions.makeBinary(
		Expressions.makeBinary(Expressions.makeExpString("minus"), OP_MINUS, Expressions.makeExpString("is")),
		OP_MINUS, Expressions.makeExpString("left associative"));
	show("expected=" + expected);
	assertEquals(expected, e);

    }

    @Test
    void testPowPrec() throws Exception {
	String input = "2^3^4";
	Exp e = parseAndShow(input);
	Exp expected = Expressions.makeBinary(Expressions.makeInt(2), OP_POW, Expressions.makeBinary(3, OP_POW, 4));
	show("expected=" + expected);
	assertEquals(expected, e);

    }

    @Test
    void testFunction() throws Exception {
	String input = "function(a,b,c) end";
	Exp e = parseAndShow(input);

    }

    @Test
    void testInvalidFunction() throws Exception {
	String input = "function(...) end";
	Exp e = parseAndShow(input);

    }

    @Test
    void testValidFieldList() throws Exception {
	String input = "{[a]=b}";
	Exp e = parseAndShow(input);
    }

    @Test
    void testBinaryExp1() throws Exception {
	String input = "-2-1";
	Exp e = parseAndShow(input);
    }

    @Test
    void testPrefixExp() throws Exception {
	String input = "{a,b,c}";
	Exp e = parseAndShow(input);
    }

    @Test
    void testParenthesesExp() throws Exception {

	parseAndShow("#5+4");
    }

    @Test
    void failed1() throws Exception {

	parseAndShow("function(xy,zy,...) end");

    }

    @Test
    void failed2() throws Exception {

	parseAndShow("{[x + y]= xx * yy,}");

    }

    @Test
    void failed3() throws Exception {
	// still failing
	parseAndShow("1 ~ 2 | 3 & 4");
	// expected - ExpBinary[e0=ExpBinary[e0=ExpInt[v=1],op=BIT_XOR,e1=ExpInt[v=2]],
	// op = BIT_OR, e1=ExpBin[eo=ExpInt[v=3],op = BIT_AMP, e1=ExpInt[v=4]]

    }

    @Test
    void failed4() throws Exception {

	parseAndShow("function (aa,b) end >> function(test, I, ...) end & function(...) end)");

    }

    @Test
    void failed5() throws Exception {

	parseAndShow("function (aa,b) end >> function(test, I, ...) end ");

    }

    @Test
    void failed6() throws Exception {

	parseAndShow("function (...) end");

    }

    // tests for block

    @Test
    void blockTest1() throws Exception {
	parseAndShow("function() break break break break end");
    }

    @Test
    void blockTest2() throws Exception {
	parseAndShow("function() goto abc goto def goto xyz break goto x :: abc :: end");
    }

    @Test
    void blockTest3() throws Exception {
	parseAndShow("function() do break break break goto x :: abc :: end end");
    }

    @Test
    void blockTest4() throws Exception {
	parseAndShow("function() while true do break end end");
    }

    @Test
    void blockTest5() throws Exception {
	parseAndShow("function() repeat break until true end");
    }

    @Test
    void blockTest6() throws Exception {
	parseAndShow("function() if true then break elseif false then break else break goto a end end");
    }
}
