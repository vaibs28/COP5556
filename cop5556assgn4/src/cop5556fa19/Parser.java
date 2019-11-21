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

import cop5556fa19.AST.ASTVisitor;
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
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
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
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class Parser {

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

    public Parser(Scanner s) throws Exception {
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
	List<Exp> concatExpList = new ArrayList<>();
	concatExpList.add(e0);
	while (t.kind == prec7Op) {
	    Token op = t;
	    consume();
	    e1 = prec8();
	    concatExpList.add(e1);
	    int index = 0;
	    // reading from right to left
	    if (concatExpList != null && concatExpList.size() != 0) {
		e0 = concatExpList.get(concatExpList.size() - 1);
		index = concatExpList.size() - 2;
	    }
	    while (index >= 0 && e0 != null) {
		Exp exp1 = concatExpList.get(index);

		e0 = new ExpBinary(first, exp1, Kind.DOTDOT, e0);
		index--;
	    }

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

    private Exp precUnary() throws Exception {
	Token first = t;
	Exp e0 = null;
	Exp e1 = null;
	if (!unaryOp.contains(t.kind))
	    e0 = precPow();
	if (unaryOp.contains(t.kind)) {
	    Token op = t;
	    consume();
	    e1 = precPow();
	    e0 = new ExpUnary(first, op.kind, e1);
	}
	return e0;

    }

    private Exp precPow() throws Exception {
	Token first = t;
	List<Exp> powerExpList = new ArrayList<>();
	powerExpList.add(prec10());
	while (t.kind == powOp) {
	    consume();
	    powerExpList.add(prec10());
	}
	Exp e0 = null;
	int index = 0;
	if (powerExpList != null && powerExpList.size() != 0) {
	    e0 = powerExpList.get(powerExpList.size() - 1);
	    index = powerExpList.size() - 2;
	}
	while (index >= 0 && e0 != null) {
	    Exp expLS = powerExpList.get(index);

	    e0 = new ExpBinary(first, expLS, Kind.OP_POW, e0);
	    index--;
	}
	return e0;
    }

    private Exp prec10() throws Exception {
	Token first = t;
	Kind kind = t.kind;
	Exp e0 = null;
	switch (kind) {
	case INTLIT:
	    e0 = new ExpInt(first);
	    consume();
	    break;

	case STRINGLIT:
	    e0 = new ExpString(first);
	    consume();
	    break;

	case KW_false:
	    e0 = new ExpFalse(first);
	    consume();
	    break;

	case KW_true:
	    e0 = new ExpTrue(first);
	    consume();
	    break;

	case KW_nil:
	    e0 = new ExpNil(first);
	    consume();
	    break;

	case DOTDOTDOT:
	    e0 = new ExpVarArgs(first);
	    consume();
	    break;

	case KW_function:
	    FuncBody fnBody = functionBody();
	    e0 = new ExpFunction(first, fnBody);
	    match(KW_end);
	    // consume();
	    break;

	case NAME:
	    // e0 = new ExpName(first.text); replacing with prefixexp for assgn 3
	    e0 = prefixexp();
	    break;

	case LPAREN:
	    consume();
	    e0 = exp();
	    match(RPAREN);
	    break;

	case LCURLY:
	    consume();
	    List<Field> fieldList = fieldList();
	    e0 = tableConstructor(first, fieldList);
	    match(Kind.RCURLY);
	    break;

	case OP_MINUS:
	    consume();
	    Exp e1 = exp();
	    e0 = new ExpUnary(first, Kind.OP_MINUS, e1);
	    break;

	case KW_not:
	    consume();
	    e1 = exp();
	    e0 = new ExpUnary(first, KW_not, e1);
	    break;

	case OP_HASH:
	    consume();
	    e1 = exp();
	    e0 = new ExpUnary(first, Kind.OP_HASH, e1);
	    consume();
	    break;

	/*
	 * case BIT_XOR: consume(); e1 = exp(); e0 = new ExpUnary(first, Kind.BIT_XOR,
	 * e1); break;
	 */
	default:
	    throw new SyntaxException(first, "invalid token");

	}
	return e0;
    }

    private ExpTable tableConstructor(Token first, List<Field> fieldList) {
	return new ExpTable(first, fieldList);
    }

    private List<Field> fieldList() throws Exception {
	List<Field> fieldList = new ArrayList<>();
	if (isKind(RCURLY)) {
	    return fieldList;
	}
	Field f = field();
	fieldList.add(f);
	while (isKind(COMMA, SEMI)) {
	    consume();
	    if (isKind(RCURLY)) {
		return fieldList;
	    }
	    fieldList.add(field());
	}
	return fieldList;
    }

    private Field field() throws Exception {
	Token first = t;
	Field f;
	if (isKind(Kind.LSQUARE)) {
	    consume();
	    Exp key = exp();
	    match(Kind.RSQUARE);
	    match(ASSIGN);
	    Exp value = exp();
	    f = new FieldExpKey(first, key, value);
	} else if (isKind(NAME)) {
	    Name name = new Name(t, t.text);
	    if (isKind(ASSIGN)) {
		Exp exp = exp();
		f = new FieldNameKey(first, name, exp);
	    } else {
		Exp exp = exp();
		f = new FieldImplicitKey(first, exp);
	    }
	} else {
	    Exp exp = exp();
	    f = new FieldImplicitKey(first, exp);
	}
	return f;
    }

    private FuncBody functionBody() throws Exception {
	Token first = t;
	FuncBody fnBody = null;
	ParList pList = null;
	if (!isKind(LPAREN)) // removed commented code
	    consume();
	match(LPAREN);
	pList = parList();
	match(RPAREN);
	Block b = block();
	fnBody = new FuncBody(first, pList, b);
	return fnBody;
    }

    private ParList parList() throws Exception {
	Token first = t;
	ParList pList = null;
	List<Name> nList = null;
	boolean hasVarArgs = true;

	if (t.kind == Kind.RPAREN) {
	    hasVarArgs = false;
	    pList = new ParList(t, nList, hasVarArgs);
	} else {
	    while (t.kind != RPAREN) {
		if (t.kind == NAME) {
		    hasVarArgs = false;
		    nList = nameList();
		    pList = new ParList(t, nList, hasVarArgs);
		} else if (t.kind == Kind.DOTDOTDOT) {
		    pList = new ParList(t, nList, hasVarArgs);
		    consume();
		}
	    }
	}
	return pList;

    }

    private List<Name> nameList() throws Exception {
	List<Name> nameList = new ArrayList<>();
	nameList.add(new Name(t, t.text));
	consume();
	while (isKind(COMMA)) {
	    consume();
	    if (t.kind == NAME) {
		nameList.add(new Name(t, t.getName()));
		consume();
	    }

	}

	return nameList;
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

    /******** Assignment 3 start - additional methods for parsing block ******/

    private Block block() throws Exception {
	Token first = t;
	List<Stat> stats = getStatsList();
	// consume(); //test
	Stat ret = retStat();
	if (ret != null)
	    stats.add(ret);
	return new Block(first, stats);
    }

    private Stat retStat() throws Exception {
	Token first = t;
	RetStat rs = null;
	List<Exp> el = null;
	if (isKind(KW_return)) {
	    match(KW_return);
	    if (!isKind(KW_end) && !isKind(SEMI)) {
		el = getExpList();
	    }
	    if (isKind(SEMI))
		match(SEMI);
	    rs = new RetStat(first, el);
	}

	return rs;
    }

    private List<Stat> getStatsList() throws Exception {
	List<Stat> statList = new ArrayList<>();
	Token first = t;
	while (isKind(SEMI, COLONCOLON, KW_break, KW_goto, KW_do, KW_while, KW_repeat, KW_if, KW_for, KW_function,
		KW_local, NAME)) {
	    if (isKind(SEMI)) {
		consume();
	    } else if (isKind(COLONCOLON)) {
		consume();
		StatLabel sl = label();
		statList.add(sl);
		consume();
		match(COLONCOLON);
	    } else if (isKind(KW_break)) {
		consume();
		statList.add(new StatBreak(t));
	    } else if (isKind(KW_goto)) {
		consume();
		Name name = new Name(t, t.text);
		statList.add(new StatGoto(first, name));
		consume();
	    } else if (isKind(KW_do)) {
		consume();
		Block b = block();
		statList.add(new StatDo(first, b));
		if (isKind(RPAREN)) // fix for multi
		    consume();
		match(KW_end);
	    } else if (isKind(KW_while)) {
		consume();
		Exp e0 = exp();
		match(KW_do);
		Block b = block();
		StatWhile sw = new StatWhile(first, e0, b);
		statList.add(sw);
		if (isKind(RPAREN)) // fix for multi
		    consume();
		match(KW_end);
	    } else if (isKind(KW_repeat)) {
		consume();
		Block b = block();
		match(KW_until);
		Exp e0 = exp();
		StatRepeat sr = new StatRepeat(first, b, e0);
		statList.add(sr);
	    } else if (isKind(KW_if)) {
		StatIf sf;
		List<Exp> expList = new ArrayList<>();
		List<Block> blockList = new ArrayList<>();
		consume();
		Exp e0 = exp();
		match(KW_then);
		Block b = block();
		expList.add(e0);
		blockList.add(b);
		while (t.kind == KW_elseif) {
		    consume();
		    Exp e1 = exp();
		    match(KW_then);
		    Block b1 = block();
		    expList.add(e1);
		    blockList.add(b1);
		}
		if (isKind(KW_else)) {
		    consume();
		    Block b1 = block();
		    blockList.add(b1);
		}
		match(KW_end);
		sf = new StatIf(first, expList, blockList);
		statList.add(sf);
	    } else if (isKind(KW_for)) {
		consume();
		StatFor sf = null;
		ExpName name = new ExpName(t.text);
		List<ExpName> names = null;
		names = getExpNameList();
		if (t.kind == Kind.ASSIGN) {
		    consume();
		    Exp e0 = exp();
		    match(Kind.COMMA);
		    Exp e1 = exp();
		    Exp e2 = null;
		    if (isKind(COMMA)) {
			consume();
			e2 = exp();
		    }
		    match(KW_do);
		    Block b0 = block();
		    match(KW_end);
		    sf = new StatFor(first, name, e0, e1, e2, b0);
		    statList.add(sf);
		} else if (t.kind == KW_in) {
		    consume();

		    List<Exp> exps = getExpList();
		    match(KW_do);
		    Block b = block();
		    match(KW_end);
		    StatForEach sfe = new StatForEach(first, names, exps, b);
		    statList.add(sfe);
		}
	    } else if (isKind(Kind.KW_function)) {
		consume();
		FuncName fn = getFuncName();
		FuncBody fb = functionBody();
		StatFunction sf = new StatFunction(first, fn, fb);
		statList.add(sf);
	    } else if (isKind(Kind.KW_local)) {
		consume();
		if (isKind(KW_function)) {
		    consume();
		    // match(Kind.NAME);
		    StatLocalFunc slf = new StatLocalFunc(first, getFuncName(), functionBody());
		    statList.add(slf);
		} else {
		    StatLocalAssign sla = new StatLocalAssign(first, getExpNameList(), getExpList());
		    statList.add(sla);
		}
	    } else if (isKind(NAME)) {
		// varlist = explist
		List<Exp> varList = getVarList();
		match(ASSIGN);
		List<Exp> expList = getExpList();
		StatAssign sa = new StatAssign(first, varList, expList);
		statList.add(sa);
	    }
	}
	return statList;
    }

    private List<Exp> getVarList() throws Exception {
	List<Exp> varList = new ArrayList<>();
	varList.add(var());
	while (isKind(COMMA)) {
	    consume();
	    varList.add(exp());
	}
	return varList;
    }

    private List<ExpName> getExpNameList() throws Exception {
	List<ExpName> names = new ArrayList<>();
	names.add(new ExpName(t.text));
	consume();
	while (isKind(COMMA)) {
	    consume();
	    if (t.kind == NAME) {
		names.add(new ExpName(t.getName()));
		consume();
	    }
	}
	return names;
    }

    private List<Exp> getExpList() throws Exception {
	// if (isKind(COMMA, COLON, LPAREN)) // important
	// consume();
	Token first = t;
	List<Exp> expList = new ArrayList<>();
	if (isKind(LPAREN))
	    consume(); // working for multipletest , not working for f(a)[b]
	expList.add(exp());
	while (isKind(COMMA)) {
	    consume();
	    expList.add(exp());
	}
	return expList;
    }

    public StatLabel label() {
	Token first = t;
	Name label = new Name(t, t.text);
	StatLabel sl = new StatLabel(first, label, new Block(first, null) ,1);
	return sl;

    }

    public FuncName getFuncName() throws Exception {
	Token first = t;
	List<ExpName> names = getFunNameList();
	ExpName nameAfterColon = null;
	if (t.kind == COLON) {
	    consume();
	    nameAfterColon = new ExpName(t);
	}
	FuncName fn = new FuncName(first, names, nameAfterColon);
	return fn;
    }

    private List<ExpName> getFunNameList() throws Exception {
	List<ExpName> names = new ArrayList<>();
	names.add(new ExpName(t.text));
	consume();
	while (isKind(DOT)) {
	    consume();
	    if (t.kind == NAME) {
		names.add(new ExpName(t.getName()));
		consume();
	    }
	}
	return names;
    }

    public Exp var() throws Exception {
	Exp e = null;
	if (isKind(NAME)) {
	    e = prefixexp();

	} else {
	    prefixexp();
	    if (isKind(LSQUARE)) {
		consume();
		e = exp();
		match(RSQUARE);
	    } else if (isKind(DOT)) {
		match(NAME);
		e = exp();
	    }
	}
	return e;
    }

    public Exp prefixexp() throws Exception {
	Exp e = null;
	Token first = t;
	if (isKind(NAME)) {
	    e = prefixexpTail();
	    // e = new ExpName(first.text);
	} else if (isKind(LPAREN)) {
	    e = exp();
	    match(RPAREN);
	    prefixexpTail();
	}
	return e;
    }

    public Exp prefixexpTail() throws Exception {
	Token first = t;
	// ExpFunctionCall fnCall = null;
	Exp tableLookup = null;
	List<Exp> args = null;
	tableLookup = new ExpName(first.text);
	Exp table = new ExpName(first.text);
	consume(); // consume name
	Token prev = null;
	while (isKind(LPAREN, LSQUARE, STRINGLIT, DOT, COLON)) {

	    if (isKind(Kind.LSQUARE)) {
		consume();
		Exp key = exp();
		match(Kind.RSQUARE);
		tableLookup = new ExpTableLookup(first, tableLookup, key);
	    } else if (isKind(DOT)) {
		consume();
		Exp key = new ExpString(t);
		tableLookup = new ExpTableLookup(first, tableLookup, key);
		consume();
	    } else if (isKind(COLON)) {
		prev = consume();
		Exp key = new ExpString(t);
		tableLookup = new ExpTableLookup(first, tableLookup, key);
		consume();
	    } else if (isKind(LPAREN)) {
		Exp f = tableLookup;
		args = args();
		if (prev!=null && prev.kind == COLON) /* for synctactic sugar v:name(args) */
		    args.add(table);
		tableLookup = new ExpFunctionCall(first, f, args);
		consume(); // fix for f(b)[a]
	    } else if (isKind(STRINGLIT)) {
		Exp f = tableLookup;
		args = args();
		tableLookup = new ExpFunctionCall(first, f, args);
		consume();
	    } else {
		tableLookup = new ExpName(first.text);
		consume();

	    }
	}
	return tableLookup;
    }

    public List<Exp> args() throws Exception {
	Token first = t;
	Exp e = null;
	List<Exp> eList = new ArrayList<>();
	if (isKind(Kind.LPAREN)) {
	    eList = getExpList();

	} else if (isKind(LCURLY)) {
	    e = tableConstructor(first, fieldList());
	    // match(RCURLY);
	    eList.add(e);
	} else if (isKind(STRINGLIT)) {
	    e = new ExpString(first);
	    eList.add(e);
	} else if (isKind(DOT)) {
	    consume();
	    e = new ExpString(t);
	    eList.add(e);
	}
	return eList;
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

    public Chunk chunk() throws Exception {
	Token first = t;
	Block b = block();
	return new Chunk(first, b);
    }
    
    public Chunk parse() throws Exception {
	Chunk chunk = chunk();
	if (!isKind(EOF)) 
	    throw new SyntaxException(t, "Parse ended before end of input");
	return chunk;
	}

}
