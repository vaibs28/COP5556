
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

import static cop5556fa19.Token.Kind.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import cop5556fa19.Token.Kind;

public class Scanner {

	Reader r;
	static StringBuilder sb = new StringBuilder();

	private enum State {
		START, HAVE_EQ, HAVE_KW, AFTER_XOR, AFTER_DOT, AFTER_EQ, AFTER_GT, AFTER_LT, AFTER_COLON, AFTER_DOTDOT,
		AFTER_DIV, AFTER_NOT
	};

	static int pos = 0;
	static int line = 0;

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
		// replace this code. Just for illustration
		// if (r.read() == -1) { return new Token(EOF,"eof",0,0);}
		Token token = null;
		State state = State.START;
		int ch;

		/*
		 * while ((ch = r.read()) != -1) { Character c = new Character((char) ch);
		 * sb.append(Character.toString(c)); }
		 */

		while ((ch = r.read()) != -1) {

			switch (state) {
			case START:

				// handling tokens with single character first
				switch (ch) {

				case ',':
					token = new Token(Kind.COMMA, ",", pos, line);
					pos++;
					return token;

				case ';':
					token = new Token(Kind.SEMI, ";", pos, line);
					pos++;
					return token;

				case '(':
					token = new Token(Kind.LPAREN, "(", pos, line);
					pos++;
					return token;

				case ')':
					token = new Token(Kind.RPAREN, ")", pos, line);
					pos++;
					return token;

				case '[':
					token = new Token(LSQUARE, "[", pos, line);
					pos++;
					return token;

				case '{':
					token = new Token(LCURLY, "{", pos, line);
					pos++;
					return token;

				case '}':
					token = new Token(Kind.RCURLY, "}", pos, line);
					pos++;
					return token;

				case '+':
					token = new Token(Kind.OP_PLUS, "+", pos, line);
					pos++;
					return token;

				case '-':
					token = new Token(Kind.OP_MINUS, "-", pos, line);
					pos++;
					return token;

				case '*':
					token = new Token(Kind.OP_TIMES, "*", pos, line);
					pos++;
					return token;

				case '^':
					token = new Token(Kind.OP_POW, "^", pos, line);
					pos++;
					return token;

				case '%':
					token = new Token(Kind.OP_MOD, "%", pos, line);
					pos++;
					return token;

				case '#':
					token = new Token(Kind.OP_HASH, "#", pos, line);
					pos++;
					return token;

				case '&':
					token = new Token(Kind.BIT_AMP, "&", pos, line);
					pos++;
					return token;

				case '|':
					token = new Token(Kind.BIT_OR, "|", pos, line);
					pos++;
					return token;

				// checking for tokens that can have multiple characters
				case '.':
					state = State.AFTER_DOT;
					pos++;
					break;

				case ':':
					state = State.AFTER_COLON;
					pos++;
					break;

				case '=':
					state = State.AFTER_EQ;
					pos++;
					break;

				case '>':
					state = State.AFTER_GT;
					pos++;
					break;

				case '<':
					state = State.AFTER_LT;
					pos++;
					break;

				case '/':
					state = State.AFTER_DIV;
					pos++;
					break;

				case '~':
					state = State.AFTER_NOT;
					pos++;
					break;

				default:
					throw new LexicalException("Useful error message");

				}
				break;
			// Checking for tokens .. and ...
			case AFTER_DOT:
				// get the next character
				//ch = sb.charAt(pos);
				if (ch == '.') {
					state = State.AFTER_DOTDOT;
					pos++;
					break;
				} else {
					return new Token(Kind.DOT, ".", pos, line);
				}

			case AFTER_DOTDOT:
				// get the next character
				//ch = sb.charAt(pos);
				if (ch == '.') {
					pos++;
					return new Token(Kind.DOTDOTDOT, "...", pos, line);
				} else {
					return new Token(Kind.DOTDOT, "..", pos, line);
				}

			case AFTER_COLON:
				//ch = sb.charAt(pos);
				if (pos == ':') {
					pos++;
					return new Token(Kind.COLONCOLON, "::", pos, line);
				} else
					return new Token(Kind.COLON, ":", pos, line);

			case AFTER_EQ:
				//ch = sb.charAt(pos);
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_EQEQ, "==", pos, line);
				} else
					return new Token(Kind.ASSIGN, "=", pos, line);

			case AFTER_GT:
				//ch = sb.charAt(pos);
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_GE, ">=", pos, line);
				} else if (ch == '>') {
					pos++;
					return new Token(Kind.BIT_SHIFTR, ">>", pos, line);
				} else
					return new Token(Kind.REL_GT, ">", pos, line);

			case AFTER_LT:
				//ch = sb.charAt(pos);
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_LE, "<=", pos, line);
				} else if (ch == '<') {
					pos++;
					return new Token(Kind.BIT_SHIFTL, "<<", pos, line);
				} else
					return new Token(Kind.REL_LT, "<", pos, line);

			case AFTER_DIV:
				//ch = sb.charAt(pos);
				if (ch == '/') {
					pos++;
					return new Token(Kind.OP_DIVDIV, "//", pos, line);
				} else
					return new Token(Kind.OP_DIV, "/", pos, line);

			case AFTER_NOT:
				//ch = sb.charAt(pos);
				if (ch == '=') {
					pos++;
					return new Token(Kind.REL_NOTEQ, "~=", pos, line);
				} else
					return new Token(Kind.BIT_XOR, "~", pos, line);

			}

		}
		return new Token(EOF, "eof", pos, line);
	}

}
