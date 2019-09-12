
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
		AFTER_DOTDOT, AFTER_DIV, AFTER_NOT
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

			if (skippedChar == ' ')
				ch = r.read();
			switch (state) {

			case START:

				// handling other tokens with single character first

				switch (ch) {

				case -1:
					token = new Token(Kind.EOF, "eof", pos, line);
					return token;

				case ',':
					token = new Token(Kind.COMMA, ",", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case ';':
					token = new Token(Kind.SEMI, ";", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '(':
					token = new Token(Kind.LPAREN, "(", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case ')':
					token = new Token(Kind.RPAREN, ")", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '[':
					token = new Token(LSQUARE, "[", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '{':
					token = new Token(LCURLY, "{", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '}':
					token = new Token(Kind.RCURLY, "}", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '+':
					token = new Token(Kind.OP_PLUS, "+", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '-':
					token = new Token(Kind.OP_MINUS, "-", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '*':
					token = new Token(Kind.OP_TIMES, "*", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '^':
					token = new Token(Kind.OP_POW, "^", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '%':
					token = new Token(Kind.OP_MOD, "%", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '#':
					token = new Token(Kind.OP_HASH, "#", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '&':
					token = new Token(Kind.BIT_AMP, "&", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '|':
					token = new Token(Kind.BIT_OR, "|", pos, line);
					pos++;
					skippedChar = ' ';
					return token;

				case '0':
					token = new Token(Kind.INTLIT, "0", pos, line);
					return token;

				// checking for tokens that can have multiple characters
				case '.':
					state = State.AFTER_DOT;
					pos++;
					skippedChar = ' ';
					break;

				case ':':
					state = State.AFTER_COLON;
					pos++;
					skippedChar = ' ';
					break;

				case '=':
					state = State.AFTER_EQ;
					pos++;
					skippedChar = ' ';
					break;

				case '>':
					state = State.AFTER_GT;
					pos++;
					skippedChar = ' ';
					break;

				case '<':
					state = State.AFTER_LT;
					pos++;
					skippedChar = ' ';
					break;

				case '/':
					state = State.AFTER_DIV;
					pos++;
					skippedChar = ' ';
					break;

				case '~':
					state = State.AFTER_NOT;
					pos++;
					skippedChar = ' ';
					break;

				case '\n':
					line++;
					skippedChar = ' ';
					break;

				// if not any of the above handled characters
				default:
					if (Character.isDigit(ch)) {
						pos++;
						sb.append((char) ch);
						while (Character.isDigit(ch)) {
							ch = r.read();
							if (Character.getNumericValue(ch) != -1)
								sb.append((char) ch);
							else {
								skippedChar = ch;
								token = new Token(Kind.INTLIT, sb.toString(), pos, line);
							}
						}
						return token;

					} else if (Character.isJavaIdentifierStart(ch)) {
						sb.append((char) ch);
						while ((ch = r.read()) != -1 && Character.isJavaIdentifierPart(ch)) {
							sb.append((char) ch);
						}
						if (keywordMap.containsKey(sb.toString())) {
							Token.Kind value = keywordMap.get(sb.toString());
							token = new Token(value, sb.toString(), pos, line);
						} else {
							token = new Token(Kind.NAME, sb.toString(), pos, line);
						}
						skippedChar = ch;
						return token;

					} else {
						pos++;
						if (ch == '\'' || ch == '"') {
							ch = r.read();
							sb.append((char) ch);

							while ((ch = r.read()) != -1 && (ch != '"' || ch != '\'')) {
								if (ch != 34 && ch != 39)
									sb.append((char) ch);
								else {
									skippedChar = ch;
									token = new Token(Kind.STRINGLIT, sb.toString(), pos, line);
									return token;
								}
							}

						}

					}
					// throw new LexicalException("Useful error message");

				}
				break;
			// Checking for tokens .. and ...
			case AFTER_DOT:
				if (ch == '.') {
					state = State.AFTER_DOTDOT;
					pos++;
					break;
				} else {
					skippedChar = ch;
					state = State.START;
					return new Token(Kind.DOT, ".", pos, line);
				}

			case AFTER_DOTDOT:
				if (ch == '.') {
					pos++;
					return new Token(Kind.DOTDOTDOT, "...", pos, line);
				} else {
					skippedChar = ch;
					state = State.START;
					return new Token(Kind.DOTDOT, "..", pos, line);
				}

			case AFTER_COLON:

				if (ch == ':') {
					pos++;
					return new Token(Kind.COLONCOLON, "::", pos, line);
				} else {
					skippedChar = ch;
					return new Token(Kind.COLON, ":", pos, line);
				}

			case AFTER_EQ:
				
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_EQEQ, "==", pos, line);
				} else {
					skippedChar = ch;
					return new Token(Kind.ASSIGN, "=", pos, line);
				} // check this

			case AFTER_GT:
			
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_GE, ">=", pos, line);
				} else if (ch == '>') {
					pos++;
					return new Token(Kind.BIT_SHIFTR, ">>", pos, line);
				} else {
					skippedChar = ch;
					return new Token(Kind.REL_GT, ">", pos, line);
				}

			case AFTER_LT:
				
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_LE, "<=", pos, line);
				} else if (ch == '<') {
					pos++;
					return new Token(Kind.BIT_SHIFTL, "<<", pos, line);
				} else {
					skippedChar = ch;
					return new Token(Kind.REL_LT, "<", pos, line);
				}

			case AFTER_DIV:
				
				if (ch == '/') {
					pos++;
					return new Token(Kind.OP_DIVDIV, "//", pos, line);
				} else {
					skippedChar = ch;
					return new Token(Kind.OP_DIV, "/", pos, line);
				}

			case AFTER_NOT:
				
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_NOTEQ, "~=", pos, line);
				} else {
					skippedChar = ch;
					return new Token(Kind.BIT_XOR, "~", pos, line);
				}
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

}
