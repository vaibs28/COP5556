
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

	private enum State {
		START, HAVE_EQ, HAVE_KW
	};

	static int pos = -1;
	static int line = -1;
	List<Token> tokenList = new ArrayList<Token>();

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
		while ((ch = r.read()) != -1) {
			pos = pos + 1;

			switch (state) {
			case START:
				switch (ch) {

				case ',':
					token = new Token(Kind.COMMA, ",", pos, line);
					return token;
				case ':':
					int nextChar = r.read();

					if (nextChar == ':')
						token = new Token(Kind.COLONCOLON, "::", pos, line);
					else
						token = new Token(Kind.COLON, ":", pos, line);
					return token;
				case '=':
					nextChar = r.read();
					if (nextChar == '=')
						token = new Token(Kind.REL_EQEQ, "==", pos, line);
					else
						token = new Token(Kind.ASSIGN, "=", pos, line);
					return token;
				case ';':
					token = new Token(Kind.SEMI, ";", pos, line);
					return token;
				case '.':
					nextChar = r.read();
					if (nextChar == '.') {
						nextChar = r.read();
						if (nextChar == '.') {
							token = new Token(Kind.DOTDOTDOT, "...", pos, line);
							return token;
						} else {
							token = new Token(Kind.DOTDOT, "..", pos, line);
							return token;
						}
					}
					token = new Token(Kind.DOT, ".", pos, line);
					return token;

				case '(':
					token = new Token(Kind.LPAREN, "(", pos, line);
					return token;

				case ')':
					token = new Token(Kind.RPAREN, ")", pos, line);
					return token;
				case '[':
					token = new Token(LSQUARE, "[", pos, line);
					return token;
				default:
					throw new LexicalException("Useful error message");
					
				}
			}
		}
		return token;

	}

}
