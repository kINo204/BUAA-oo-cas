import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {
    private HashMap<String, Func> functions;

    public PreProcessor() {
        functions = new HashMap<>();
    }

    public void readFunc(Lexer lexer) {
        Func func = new Func();
        assert "fgh".contains(lexer.peek()); // Function name must be f | g | h.
        final String funcName = lexer.next();
        lexer.next(); // jump the "(" token
        func.addParam(lexer.next()); // add the 1st param and step
        while (lexer.next().equals(",")) { // token ")" automatically jumped through this
            func.addParam(lexer.next());
        }
        func.setDef(lexer.rest()); // curToken must be "=" to get the correct rest() here
        functions.put(funcName, func);
    }

    public String subsFunc(String src) {
        Lexer lexer = new Lexer(src);
        StringBuilder sb = new StringBuilder();

        while (lexer.hasNext()) {
            if (functions.containsKey(lexer.peek())) { // function detected
                sb.append(func1(lexer.peek(), lexer));
                int bracs = 0;
                do {
                    lexer.next();
                    if (lexer.peek().equals("(")) {
                        bracs++;
                    } else if (lexer.peek().equals(")")) {
                        bracs--;
                    }
                } while (bracs > 0);
                lexer.next();
            } else {
                sb.append(lexer.next());
            }
        }
        // Last token can't be a function
        assert !functions.containsKey(lexer.peek());
        sb.append(lexer.peek());

        return sb.toString();
    }

    private String func1(String funcName, Lexer lexer) {
        // Parse real parameters
        Pattern pat = Pattern.compile("\\((([xyzfgh+\\-*^\\s()0-9]+,?)+)\\)");
        Matcher mat = pat.matcher(lexer.rest());
        boolean match = mat.find();
        assert match;
        ArrayList<String> realParams = new ArrayList<>();

        int bracs = 0;
        Lexer paramLexer = new Lexer(mat.group(1));
        StringBuilder paramSb = new StringBuilder();
        while (paramLexer.hasNext()) {
            if (paramLexer.peek().equals("(")) {
                bracs++;
            } else if (paramLexer.peek().equals(")")) {
                bracs--;
            } else if (paramLexer.peek().equals(",")) {
                if (bracs == 0) {
                    realParams.add(paramSb.toString());
                    paramSb.setLength(0);
                    paramLexer.next();
                    continue;
                }
            }
            paramSb.append(paramLexer.next());
        }
        if (paramLexer.peek().equals("(")) {
            bracs++;
        } else if (paramLexer.peek().equals(")")) {
            bracs--;
        }
        paramSb.append(paramLexer.next());
        assert bracs == 0;
        realParams.add(paramSb.toString());

        StringBuilder sb = new StringBuilder();
        Func func = functions.get(funcName);
        Lexer funcLexer = new Lexer(func.getDef());

        sb.append("(");
        while (funcLexer.hasNext()) {
            // If the curToken in function def is a function parameter:
            if (func.getParams().contains(funcLexer.peek())) {
                int paramInd = func.getParams().indexOf(funcLexer.next());
                sb.append("(");
                sb.append(realParams.get(paramInd));
                sb.append(")");
            } else {
                sb.append(funcLexer.next());
            }
        }
        // Different with that in subsFunc(), the last one can be a param:
        if (func.getParams().contains(funcLexer.peek())) {
            int paramInd = func.getParams().indexOf(funcLexer.peek());
            sb.append("(");
            sb.append(realParams.get(paramInd));
            sb.append(")");
        } else {
            sb.append(funcLexer.next());
        }
        sb.append(")");
        return sb.toString();
    }

}
