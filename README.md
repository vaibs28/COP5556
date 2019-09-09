# COP5556
COP 5556 - Programming Language Principles Assignments

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

