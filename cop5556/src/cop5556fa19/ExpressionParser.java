/**
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa19.AST.Block;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class ExpressionParser {

    @SuppressWarnings("serial")
    class SyntaxException extends Exception {
	Token t;

	public SyntaxException(Token t, String message) {
	    super(t.line + ":" + t.pos + " " + message);
	}
    }

    final Scanner scanner;
    Token t; // invariant: this is the next token
    Kind prec0Op = KW_or;
    Kind prec1Op = KW_and;
    List<Token.Kind> prec2Op = new ArrayList<>();
    Kind prec3Op = BIT_OR;
    Kind prec4Op = BIT_XOR;
    Kind prec5Op = BIT_AMP;
    List<Token.Kind> prec6Op = new ArrayList<>();
    Kind prec7Op = DOTDOT;
    List<Token.Kind> prec8Op = new ArrayList<>();
    List<Token.Kind> prec9Op = new ArrayList<>();
    List<Token.Kind> unaryOp = new ArrayList<>();
    Kind powOp = OP_POW;
    

    ExpressionParser(Scanner s) throws Exception {
	this.scanner = s;
	t = scanner.getNext(); // establish invariant
	initList();
    }

    private void initList() {
	// initializing binary operators list as per precedence
	prec2Op.add(REL_EQEQ);
	prec2Op.add(REL_GE);
	prec2Op.add(REL_GT);
	prec2Op.add(REL_LE);
	prec2Op.add(REL_LT);
	prec2Op.add(REL_NOTEQ); // relational op

	prec6Op.add(BIT_SHIFTL);
	prec6Op.add(BIT_SHIFTR);

	prec8Op.add(OP_MINUS);
	prec8Op.add(OP_PLUS); // + and - with same precedence

	prec9Op.add(OP_DIV);
	prec9Op.add(OP_TIMES);
	prec9Op.add(OP_DIVDIV);
	prec9Op.add(OP_MOD); // *,/,//,% with same precedence

	unaryOp.add(KW_not);
	unaryOp.add(OP_HASH);
	unaryOp.add(OP_MINUS);
	unaryOp.add(BIT_XOR); // unary operators
    }

    Exp exp() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec1();
	while (t.kind == prec0Op) {
	    Token op = t;
	    consume();
	    e1 = prec1();
	    e0 = binaryExp(first, e0, op, e1);
	}

	return e0;
    }

    private Exp prec1() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec2();
	while (t.kind == prec1Op) {
	    Token op = t;
	    consume();
	    e1 = prec2();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;

    }

    private Exp prec2() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec3();
	while (prec2Op.contains(t.kind)) {
	    Token op = t;
	    consume();
	    e1 = prec3();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;

    }

    private Exp prec3() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec4();
	while (t.kind == prec3Op) {
	    Token op = t;
	    consume();
	    e1 = prec4();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }

    private Exp prec4() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec5();
	while (t.kind == prec4Op) {
	    Token op = t;
	    consume();
	    e1 = prec5();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }

    private Exp prec5() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec6();
	while (t.kind == prec5Op) {
	    Token op = t;
	    consume();
	    e1 = prec6();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }

    private Exp prec6() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec7();
	while (prec6Op.contains(t.kind)) {
	    Token op = t;
	    consume();
	    e1 = prec7();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }

    private Exp prec7() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec8();
	while (t.kind == prec7Op) {
	    Token op = t;
	    consume();
	    e1 = prec8();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }

    private Exp prec8() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec9();
	while (prec8Op.contains(t.kind)) {
	    Token op = t;
	    consume();
	    e1 = prec9();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }

    private Exp prec9() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = precUnary();
	while (prec9Op.contains(t.kind)) {
	    Token op = t;
	    consume();
	    e1 = precUnary();
	    e0 = binaryExp(first, e0, op, e1);
	}
	return e0;
    }
    
    private Exp precUnary() throws Exception{
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	e0 = prec10();
	while (unaryOp.contains(t.kind)) {
	    Token op = t;
	    consume();
	    e1 = prec10();
	    e0 = new ExpUnary(first, op.kind, e1);
	}
	return e0;
    }

    private Exp prec10() throws Exception {
	Kind kind = t.kind;
	Exp e0 = null;
	switch (kind) {
	case INTLIT:
	    e0 = new ExpInt(t);
	    consume();
	    break;

	case STRINGLIT:
	    e0 = new ExpString(t);
	    consume();
	    break;

	case KW_false:
	    e0 = new ExpFalse(t);
	    consume();
	    break;

	case KW_true:
	    e0 = new ExpTrue(t);
	    consume();
	    break;

	case KW_nil:
	    e0 = new ExpNil(t);
	    consume();
	    break;

	case DOTDOTDOT:
	    e0 = new ExpVarArgs(t);
	    consume();
	    break;

	case KW_function:
	    e0 = functionBody();
	    break;

	case NAME:
	    e0 = new ExpName(t.text);
	    consume();
	    break;

	case LPAREN:
	    consume();
	    e0 = exp();
	    match(RPAREN);
	    break;

	}
	return e0;
    }

    private Exp prefixExp() {
	// TODO Auto-generated method stub
	return null;
    }

    private Exp functionBody() throws Exception {
	Exp e0 = null;
	Exp e1 = null;
	Token op = consume();
	if (op.kind == Kind.LPAREN) {
	    consume();
	    parList();
	    match(RPAREN);
	} else {
	    throw new SyntaxException(op, "invalid expression");
	}
	return e1;

    }

    private Exp parList() throws Exception {
	Token t = consume();
	if (t.kind == Kind.DOTDOTDOT) {
	    //return new ParList(t, nameList, hasVarArgs);
	}
	nameList();
	return null;

    }

    private void nameList() {

    }

    private Exp tableConstructor() {
	// TODO Auto-generated method stub
	return null;
    }

    private Exp fieldList() {
	// TODO Auto-generated method stub
	return null;
    }

    private Exp field() {
	// TODO Auto-generated method stub
	return null;
    }

    private ExpName expName(String name) {
	ExpName e = new ExpName(name);
	return e;
    }

    private ExpInt expInt(Token first) {
	// TODO Auto-generated method stub
	return new ExpInt(first);
    }

    private Exp andExp() throws Exception {
	// TODO Auto-generated method stub
	throw new UnsupportedOperationException("andExp"); // I find this is a more useful placeholder than returning
							   // null.
    }

    private ExpBinary binaryExp(Token firstToken, Exp e0, Token op, Exp e1) {
	ExpBinary exp = new ExpBinary(firstToken, e0, op, e1);
	return exp;
    }

    private ExpUnary unaryExp() throws Exception {
	throw new UnsupportedOperationException("unaryExp");
    }

    private ExpFalse falseExp(Token token) {
	return new ExpFalse(token);
    }

    private ExpTrue trueExp(Token firstToken) throws Exception {
	return new ExpTrue(firstToken);
    }

    private ExpNil nilExp() throws Exception {
	return new ExpNil(t);

    }

    private ExpInt intExp(Token token) throws Exception {
	ExpInt e = new ExpInt(token);
	return e;
    }

    private ExpString stringExp(Token token) throws Exception {
	ExpString e = new ExpString(token);
	return e;
    }

    private ExpFunction functionDefExp(Token token, FuncBody fnBody) throws Exception {
	ExpFunction e = new ExpFunction(token, fnBody);
	return e;
    }

    private ExpName nameExp() throws Exception {
	throw new UnsupportedOperationException("nameExp");
    }

    private ExpTable tableconstructorExp() throws Exception {
	throw new UnsupportedOperationException("tableconstructorExp");
    }

    private Block block() {
	return new Block(null); // this is OK for Assignment 2
    }

    protected boolean isKind(Kind kind) {
	return t.kind == kind;
    }

    protected boolean isKind(Kind... kinds) {
	for (Kind k : kinds) {
	    if (k == t.kind)
		return true;
	}
	return false;
    }

    /**
     * @param kind
     * @return
     * @throws Exception
     */
    Token match(Kind kind) throws Exception {
	Token tmp = t;
	if (isKind(kind)) {
	    consume();
	    return tmp;
	}
	error(kind);
	return null; // unreachable
    }

    /**
     * @param kind
     * @return
     * @throws Exception
     */
    Token match(Kind... kinds) throws Exception {
	Token tmp = t;
	if (isKind(kinds)) {
	    consume();
	    return tmp;
	}
	StringBuilder sb = new StringBuilder();
	for (Kind kind1 : kinds) {
	    sb.append(kind1).append(kind1).append(" ");
	}
	error(kinds);
	return null; // unreachable
    }

    Token consume() throws Exception {
	Token tmp = t;
	t = scanner.getNext();
	return tmp;
    }

    void error(Kind... expectedKinds) throws SyntaxException {
	String kinds = Arrays.toString(expectedKinds);
	String message;
	if (expectedKinds.length == 1) {
	    message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
	} else {
	    message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
	}
	throw new SyntaxException(t, message);
    }

    void error(Token t, String m) throws SyntaxException {
	String message = m + " at " + t.line + ":" + t.pos;
	throw new SyntaxException(t, message);
    }

}
