public class Lexer {

    private final String input;
    private String curToken;
    private TokenType curType;
    private int pos = 0;

    public void step(int length) {
        final int tpos = pos;
        do {
            next();
        } while (hasNext() && pos - tpos < length);
    }

    public enum TokenType {
        NUM, VAR, ADD, SUB, MUL, IND, OTH, BRAC
    }

    public Lexer(String input) {
        this.input = input;
        next();
    }

    public String rest() {
        return input.substring(pos);
    }

    public String peek() {
        return curToken;
    }

    public boolean hasNext() {
        boolean nonBlank = false;
        for (int i = pos; i < input.length(); i++) {
            if (input.charAt(i) != ' ' && input.charAt(i) != '\t') {
                nonBlank = true;
                break;
            }
        }
        return nonBlank;
    }

    public String next() {
        String prevToken = curToken;

        if (pos == input.length()) {
            return prevToken;
        }

        // skip blank chars
        while (input.charAt(pos) == ' ' || input.charAt(pos) == '\t') {
            pos++;
            if (pos == input.length()) {
                return prevToken;
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
                    curType = TokenType.IND;
                    break;
                default:
                    curType = TokenType.OTH;
            }
            pos++;
        }
        return prevToken;
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
