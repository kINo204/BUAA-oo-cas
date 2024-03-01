public class Lexer {

    private final String input;
    private String curToken;
    private TokenType curType;
    private int pos = 0;

    public enum TokenType {
        NUM, VAR, ADD, SUB, MUL, EXP, BRAC
    }

    public Lexer(String input) {
        this.input = input;
        next();
    }

    public String peek() {
        return curToken;
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }

        // skip blank chars
        while (input.charAt(pos) == ' ' || input.charAt(pos) == '\t') {
            pos++;
            if (pos == input.length()) {
                return;
            }
        }

        // get next token
        if (Character.isDigit(input.charAt(pos))) {
            curType = TokenType.NUM;
            curToken = getNumber();
        } else if (Character.isAlphabetic(input.charAt(pos))) {
            curType = TokenType.VAR;
            curToken = getVar();
        } else {
            curToken = String.valueOf(input.charAt(pos));
            switch (input.charAt(pos)) {
                case '+' :
                    curType = TokenType.ADD;
                    break;
                case '-' :
                    curType = TokenType.SUB;
                    break;
                case '*':
                    curType = TokenType.MUL;
                    break;
                case '(':
                case ')':
                    curType = TokenType.BRAC;
                    break;
                case '^':
                    curType = TokenType.EXP;
                    break;
                default:
            }
            pos++;
        }
    }

    public TokenType type() {
        return curType;
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '+') {
            pos++;
        } else if (input.charAt(pos) == '-') {
            sb.append('-');
            pos++;
        }

        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }

    private String getVar() {
        StringBuilder sb = new StringBuilder();
        // NOTE: only alphabetic allowed for variables for now.
        while (pos < input.length() && Character.isAlphabetic(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }

        return sb.toString();
    }
}
