# COP5556
COP 5556 - Programming Language Principles Assignments

# Part 1
Implement a scanner for a programming language with the following lexical structure:
 
InputCharacter ::=  any 7-bit ASCII character
LineTerminator ::=  LF |  CR  |  CR  LF
          	LF is the ASCII character also known as “newline”.  The Java character literal is ‘\n’.
          	CR is the ASII character also known as “return”, The Java character literal is ‘\r’.
          	CR immediately followed by LF counts as one line terminator, not two.

Input ::= (WhiteSpace | Comment | Token)*

WhiteSpace ::=  SP  | HT | FF | LineTerminator
          	SP is the ASCII character also known as “space”.  The Java char literal is ‘ ‘.
          	HT is the ASCII character also known as “horizontal tab”. The Java char literal is ‘\t’.
          	FF is the ASCII character also known as “form feed”.  The  Java char literal is ‘\f’.

Comment ::=  --  (NOT (LineTerminator)) *  LineTerminator  
	Comments start with two adjacent hypens ‘-’.  
Token ::= Identifier | Keyword | Literal | Separator | Operator
Token ::= Name | Keyword | Literal | OtherToken
Name ::= IdentifierChars but not a Keyword 
IdentifierChars ::= IdentiferStart IdentifierPart*
IdentifierStart ::= A..Z | a..z 
IdentifierPart ::= IdentifierStart |  Digit | _ | $
Literal ::= IntegerLiteral  |  StringLiteral
StringLiteral ::= “ StringCharacter* “  |  ‘ StringCharacter* ‘
StringCharacter ::= InputCharacter but not “ or \  or ‘   |   EscapeSequence
EscapeSequence ::= \a (bell) 
| \b  (backspace)
| \f (form feed)
| \n (newline)
| \r (carriage return)
| \t (horizontal tab) 
| \v (vertical tab)
| \\ (backslash)
| \" (quotation mark [double quote])
| \’ (apostrophe [single quote]). 
IntegerLiteral ::=  0 | NonZeroDigit  Digit*
NonZeroDigit ::= 1 .. 9
Digit ::= NonZeroDigit | 0
OtherToken ::=     +  |   -  |   *  |   /   |  % |    ^   |  #
    |  &   |  ~   |  |   |  <<  |  >>  |  //
    | ==   | ~=  | <= |   >=  |  <  |   >  |   =
    | (   |  )   |  {   |  }  |   [   |  ] |    ::
    | ;  |    :   |  , |    . |    .. |   ...
Keyword ::=      and     |   break   |   do     |    else    |   elseif   |  end 
    |  false   |   for    |    function  | goto   |    if    |     in
    |  local    |  nil     |   not    |    or    |     repeat   |  return
    |  then   |    true    |   until     | while


If an illegal character is encountered, your scanner should throw a LexicalException. The message should contain useful information about the error.  The contents of the message will not be graded, but you will appreciate it later if it is descriptive.  
If a numeric literal is provided that is out of the range of the Java equivalent of that type, then your scanner should throw a Lexical exception. 
 The contents of the error message will not be graded, but you will appreciate it later if it is descriptive.  
Use the provided Scanner.java and ScannerTest.java as starting points.   


# Part 2

For this assignment, we will write a parser for a subset of a simplified version of the Lua grammar.  We’ll add the rest of the language in assignment 3. 
The subset grammar for this assignment is given below almost as it appears in the Lua Manual.  Note that they use different notation than we did in the lecture:  instead of A* (i.e. 0 or more instances of A) , they use {A}, and instead of A+ (0 or 1 instance of A), they use [A].

	exp ::=  nil | false | true | IntLiteral | LiteralString 
		| ‘...’ | functiondef | 
		 prefixexp | tableconstructor | exp binop exp | unop exp 

	binop ::=  ‘+’ | ‘-’ | ‘*’ | ‘/’ | ‘//’ | ‘^’ | ‘%’ | 
		 ‘&’ | ‘~’ | ‘|’ | ‘>>’ | ‘<<’ | ‘..’ | 
		 ‘<’ | ‘<=’ | ‘>’ | ‘>=’ | ‘==’ | ‘~=’ | 
		 and | or

	unop ::= ‘-’ | not | ‘#’ | ‘~’

                   prefixexp ::= Name | ‘(’ exp ‘)’

	functiondef ::= function funcbody

	funcbody ::= ‘(’ [parlist] ‘)’ block end

	parlist ::= namelist [‘,’ ‘...’] | ‘...’
        
        namelist ::= Name {‘,’ Name}

	tableconstructor ::= ‘{’ [fieldlist] ‘}’

	fieldlist ::= field {fieldsep field} [fieldsep]

	field ::= ‘[’ exp ‘]’ ‘=’ exp | Name ‘=’ exp | exp

	fieldsep ::= ‘,’ | ‘;’
        block ::=   ϵ
The above grammar does not encode the precedence and associativity of operators.   The Lua reference manual specifies these separately.  Before implementing your parser, you will need to revise the grammar to encode this information.  You have seen precedence encoded in grammars in the lecture, and it is also discussed in Scott.  Here are the precedence and associativity rules copied from the Lua manual.
Operator precedence in Lua follows the table below, from lower to higher priority: 
     or
     and
     <     >     <=    >=    ~=    ==
     |
     ~
     &
     <<    >>
     ..
     +     -
     *     /     //    %
     unary operators (not   #     -     ~)
     ^
As usual, you can use parentheses to change the precedences of an expression. The concatenation ('..') and exponentiation ('^') operators are right associative. All other binary operators are left associative. 
Finally, rather than simply parsing to find out whether a sentence is legal, the parse routines will also return an abstract syntax tree.  An abstract syntax tree is like a parse tree except that it leaves out irrelevant details like punctuation.  The AST reflects the structure of the expression and we should be able to correctly evaluate the expression by traversing the tree and passing the values of subexpressions up to the top.  To construct the AST, we define an abstract class Exp which is the superclass of all classes representing expressions.  Each different kind of expression is represented by a subclass of Exp.  For example, ExpBinary represents a binary expression and has 3 fields:  two Exp and one for the operator.  The AST classes have been provided for you.  Here is ExpBinary, for example.  The most important part are the three fields, e0,op, and e1 to hold the two subexpressions and the Token with the operator.  
package parser.AST;

import scanner.Token;

public class ExpBinary extends Exp {
	
	public final Exp e0;
	public final Token op;
	public final Exp e1;

	public ExpBinary(Token firstToken, Exp e0, Token op, Exp e1) {
		super(firstToken);
		this.e0 = e0;
		this.op = op;
		this.e1 = e1;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitExpBin(this,arg);
	}

	@Override
	public String toString() {
	return "ExpBinary [e0=" + e0 + ", op=" + op + ", e1=" + e1 + ", firstToken=" + firstToken + "]";
	}

}

Ignore the visit method for now.  It will be needed in later assignments to implement the Visitor Pattern. 
The firstToken field is defined in a superclass.  It should hold the first Token in the construct and will be used to locate the beginning of the construct in error messages. When a syntax error is detected, your parser should throw a SyntaxException, which takes a Token and an error message.  As before, the contents of your error message will not be graded, but you will be much happier later if the information is useful.
Assuming that your grammar (after modifying to handle associativity and precedence) includes something like
exp :: =  andexp { ‘or’ andexp}
You would have a corresponding method that would return an ExpBinary object.  This follows the approach for implementing a recursive descent parser discussed in class.  Recall that {..} means 0 or more instances of.
	
       Exp exp() throws Exception {
		Token first = t;  Always save the current token when you enter a routine. 
		Exp e0 = andExp();
		while (isKind(KW_or)) {
			Token op = consume();  //consume returns the Token that was consumed
			Exp e1 = andExp();     //get the second expression
			e0 = new ExpBinary(first, e0, op, e1);   
		}
		return e0;
	}


