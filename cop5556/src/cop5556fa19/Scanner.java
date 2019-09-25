
/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */
package cop5556fa19;

import static cop5556fa19.Token.Kind.EOF;
import static cop5556fa19.Token.Kind.LCURLY;
import static cop5556fa19.Token.Kind.LSQUARE;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cop5556fa19.Token.Kind;

public class Scanner {

    Reader r;
    StringBuilder sb = new StringBuilder();
    String intLiteral = "";
    int skippedChar = ' ';

    private enum State {
	START, HAS_INTLIT, HAS_STRLIT, HAS_KW, AFTER_XOR, AFTER_DOT, AFTER_EQ, AFTER_GT, AFTER_LT, AFTER_COLON,
	AFTER_DOTDOT, AFTER_DIV, AFTER_NOT, AFTER_CR, AFTER_BACKSLASH, AFTER_HYPHEN
    };

    Map<String, Token.Kind> keywordMap = new HashMap<>();
    int pos = -1;
    int line = -1;
    int ch = 0;

    @SuppressWarnings("serial")
    public static class LexicalException extends Exception {
	public LexicalException(String arg0) {
	    super(arg0);
	}
    }

    public Scanner(Reader r) throws IOException {
	this.r = r;
    }

    public Token getNext() throws Exception {

	initKeywordMap();
	Token token = null;
	State state = State.START;
	sb = new StringBuilder();
	while (token == null) {

	    pos++;
	    if (skippedChar == ' ') {
		ch = r.read();
	    } else {
		ch = skippedChar;
		skippedChar = ' ';
	    }

	    switch (state) {

	    case START:

		// handling other tokens with single character first

		switch (ch) {

		case -1:
		    token = new Token(Kind.EOF, "eof", pos, line);
		    return token;

		case ' ':
		    state = State.START;
		    break;

		case '\t':
		    state = State.START;
		    break;

		case '\f':
		    state = State.START;
		    break;

		case '\n':
		    state = State.START;
		    break;

		case '\r':
		    state = State.AFTER_CR;
		    break;

		case ',':
		    token = new Token(Kind.COMMA, ",", pos, line);
		    skippedChar = ' ';
		    return token;

		case ';':
		    token = new Token(Kind.SEMI, ";", pos, line);

		    skippedChar = ' ';
		    return token;

		case '(':
		    token = new Token(Kind.LPAREN, "(", pos, line);

		    skippedChar = ' ';
		    return token;

		case ')':
		    token = new Token(Kind.RPAREN, ")", pos, line);

		    skippedChar = ' ';
		    return token;

		case '[':
		    token = new Token(LSQUARE, "[", pos, line);

		    skippedChar = ' ';
		    return token;

		case ']':
		    token = new Token(Kind.RSQUARE, "]", pos, line);

		    skippedChar = ' ';
		    return token;
		case '{':
		    token = new Token(LCURLY, "{", pos, line);

		    skippedChar = ' ';
		    return token;

		case '}':
		    token = new Token(Kind.RCURLY, "}", pos, line);

		    skippedChar = ' ';
		    return token;

		case '+':
		    token = new Token(Kind.OP_PLUS, "+", pos, line);

		    skippedChar = ' ';
		    return token;

		case '-':
		    state = State.AFTER_HYPHEN;
		    /*
		     * token = new Token(Kind.OP_MINUS, "-", pos, line); pos++; skippedChar = ' ';
		     * return token;
		     */

		    skippedChar = ' ';
		    break;

		case '*':
		    token = new Token(Kind.OP_TIMES, "*", pos, line);

		    skippedChar = ' ';
		    return token;

		case '^':
		    token = new Token(Kind.OP_POW, "^", pos, line);

		    skippedChar = ' ';
		    return token;

		case '%':
		    token = new Token(Kind.OP_MOD, "%", pos, line);

		    skippedChar = ' ';
		    return token;

		case '#':
		    token = new Token(Kind.OP_HASH, "#", pos, line);

		    skippedChar = ' ';
		    return token;

		case '&':
		    token = new Token(Kind.BIT_AMP, "&", pos, line);

		    skippedChar = ' ';
		    return token;

		case '|':
		    token = new Token(Kind.BIT_OR, "|", pos, line);

		    skippedChar = ' ';
		    return token;

		case '0':
		    token = new Token(Kind.INTLIT, "0", pos, line);
		    skippedChar = ' ';
		    return token;

		// checking for tokens that can have multiple characters
		case '.':
		    state = State.AFTER_DOT;

		    skippedChar = ' ';
		    break;

		case ':':
		    state = State.AFTER_COLON;

		    skippedChar = ' ';
		    break;

		case '=':
		    state = State.AFTER_EQ;

		    skippedChar = ' ';
		    break;

		case '>':
		    state = State.AFTER_GT;

		    skippedChar = ' ';
		    break;

		case '<':
		    state = State.AFTER_LT;

		    skippedChar = ' ';
		    break;

		case '/':
		    state = State.AFTER_DIV;

		    skippedChar = ' ';
		    break;

		case '~':
		    state = State.AFTER_NOT;

		    skippedChar = ' ';
		    break;

		// if not any of the above handled characters
		default:
		    if (Character.isDigit(ch)) {
			int currPos = pos;
			sb.append((char) ch);
			while (Character.isDigit(ch)) {
			    ch = r.read();
			    currPos++;
			    if (Character.getNumericValue(ch) != -1 && Character.getNumericValue(ch) >= 0
				    && Character.getNumericValue(ch) <= 9)
				sb.append((char) ch);
			    else {
				skippedChar = ch;
				String intStr = sb.toString();
				int intVal = 0;
				try {
				    intVal = Integer.parseInt(intStr);
				} catch (NumberFormatException e) {
				    throw new LexicalException("Integer out of range");
				}
				if (intVal <= Integer.MAX_VALUE || intVal >= Integer.MIN_VALUE)
				    token = new Token(Kind.INTLIT, sb.toString(), pos, line);

			    }
			}
			pos = currPos;
			return token;

		    } else if (Character.isJavaIdentifierStart(ch) && ch != '$' && ch != '_') {
			int currPos = pos;
			sb.append((char) ch);
			while ((ch = r.read()) != -1 && Character.isJavaIdentifierPart(ch)) {
			    currPos++;
			    sb.append((char) ch);
			}
			if (keywordMap.containsKey(sb.toString())) {
			    Token.Kind value = keywordMap.get(sb.toString());
			    token = new Token(value, sb.toString(), pos, line);
			} else {
			    token = new Token(Kind.NAME, sb.toString(), currPos, line);
			}
			skippedChar = ch;
			pos = currPos;

			return token;

		    } else {
			pos++;
			if (ch == '\'' || ch == '"') {
			    int start = ch;
			    sb.append((char) ch);

			    while ((ch = r.read()) != -1 && (ch != '"' && ch != '\'')) {
				pos++;
				if (ch != 34 && ch != 39 && ch != 92) {
				    sb.append((char) ch);
				}
				// sb.append((char) ch);
				else if (ch == '\\') {
				    // checking for escape sequences in string literal
				    ch = r.read();
				    if (ch == 'a') {
					sb.append((char) 7);
				    } else if (ch == 'b') {
					sb.append((char) 8);
				    } else if (ch == 'f') {
					sb.append((char) 12);
				    } else if (ch == 'v') {
					sb.append((char) 11);
				    } else if (ch == 'n') {
					sb.append((char) 10);
				    } else if (ch == 't') {
					sb.append((char) 9);
				    } else if (ch == 'r') {
					sb.append((char) 13);
				    } else if (ch == '\\') {
					sb.append((char) ch);
				    } else if (ch == '\'') {
					sb.append((char) ch);
				    } else if (ch == '\"') {
					sb.append((char) ch);
				    } else {
					skippedChar = ch;
					throw new LexicalException("Invalid token \\ not allowed in string literal");

				    }
				} else {
				    skippedChar = ch;
				}
			    }
			    if (!(ch == -1) && ch == start) {
				sb.append((char) ch);
			    } else {
				throw new LexicalException("No closing quotes in string literal");
			    }
			    token = new Token(Kind.STRINGLIT, sb.toString(), pos, line);

			    return token;

			}

		    }
		    throw new LexicalException("Invalid token");

		}
		break;
	    // Checking for tokens .. and ...
	    case AFTER_DOT:

		if (ch == '.') {
		    state = State.AFTER_DOTDOT;
		    break;
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.DOT, ".", pos, line);
		}

	    case AFTER_DOTDOT:

		if (ch == '.') {
		    return new Token(Kind.DOTDOTDOT, "...", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.DOTDOT, "..", pos, line);
		}

	    case AFTER_COLON:

		if (ch == ':') {
		    return new Token(Kind.COLONCOLON, "::", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.COLON, ":", pos, line);
		}

	    case AFTER_EQ:

		if (ch == '=') {
		    return new Token(Kind.REL_EQEQ, "==", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.ASSIGN, "=", pos, line);
		}

	    case AFTER_GT:

		if (ch == '=') {
		    return new Token(Kind.REL_GE, ">=", pos, line);
		} else if (ch == '>') {
		    return new Token(Kind.BIT_SHIFTR, ">>", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.REL_GT, ">", pos, line);
		}

	    case AFTER_LT:

		if (ch == '=') {
		    return new Token(Kind.REL_LE, "<=", pos, line);
		} else if (ch == '<') {
		    return new Token(Kind.BIT_SHIFTL, "<<", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.REL_LT, "<", pos, line);
		}

	    case AFTER_DIV:

		if (ch == '/') {
		    return new Token(Kind.OP_DIVDIV, "//", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.OP_DIV, "/", pos, line);
		}

	    case AFTER_NOT:

		if (ch == '=') {
		    return new Token(Kind.REL_NOTEQ, "~=", pos, line);
		} else {
		    skippedChar = ch;
		    state = State.START;
		    return new Token(Kind.BIT_XOR, "~", pos, line);
		}

	    case AFTER_CR:

		ch = r.read();
		if (ch == '\n') {
		    state = State.START;
		    break;
		}

	    case AFTER_BACKSLASH:
		ch = r.read();
		if (ch == 'a' || ch == 'b' || ch == 'f' || ch == 'n' || ch == 'r' || ch == 't' || ch == 'v'
			|| ch == '\'') {

		}
		break;

	    case AFTER_HYPHEN:
		if (ch == '-') {
		    while (ch != -1 && ch != 10 && ch != 13) {
			ch = r.read();

		    }
		    if (ch == '\n') {
			ch = r.read();
			if (ch == '\r') {
			    state = State.START;
			    break;
			} else {
			    skippedChar = ch;
			    state = State.START;
			    break;
			}
		    } else if (ch == '\r') {
			state = State.START;
			break;
		    } else if (ch == -1) {
			throw new LexicalException("No comment terminator found");
		    }
		} else {
		    skippedChar = ch;
		    return new Token(Kind.OP_MINUS, "-", pos, line);
		}
		break;
	    }

	}
	return new Token(EOF, "eof", pos, line);
    }

    private void initKeywordMap() {
	keywordMap.put("and", Kind.KW_and);
	keywordMap.put("break", Kind.KW_break);
	keywordMap.put("do", Kind.KW_do);
	keywordMap.put("else", Kind.KW_else);
	keywordMap.put("elseif", Kind.KW_elseif);
	keywordMap.put("end", Kind.KW_end);
	keywordMap.put("false", Kind.KW_false);
	keywordMap.put("for", Kind.KW_for);
	keywordMap.put("function", Kind.KW_function);
	keywordMap.put("goto", Kind.KW_goto);
	keywordMap.put("if", Kind.KW_if);
	keywordMap.put("in", Kind.KW_in);
	keywordMap.put("local", Kind.KW_local);
	keywordMap.put("nil", Kind.KW_nil);
	keywordMap.put("not", Kind.KW_not);
	keywordMap.put("or", Kind.KW_or);
	keywordMap.put("repeat", Kind.KW_repeat);
	keywordMap.put("return", Kind.KW_return);
	keywordMap.put("then", Kind.KW_then);
	keywordMap.put("true", Kind.KW_true);
	keywordMap.put("until", Kind.KW_until);
	keywordMap.put("while", Kind.KW_while);

    }

    private boolean containsEscapeSequence() {

	return false;
    }

}
