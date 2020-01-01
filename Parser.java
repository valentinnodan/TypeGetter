import nodes.*;

import java.util.ArrayList;

class Parser {
    private String expression;
    private int placeString = -1;
    private char currChar;
    private StringBuilder name;
    private token currToken = token.DEFAULT;

    enum token {EXPRESSION, APPLICATION, VARIABLE, ATOM, APPLICATION_A, EXPRESSION_A, DEFAULT, END, LAMBDA, LAMBDA_L, LBRACKET, RBRACKET, DOT}

    private boolean nextChar() {
        if (placeString < expression.length() - 1) {
            placeString++;
            currChar = expression.charAt(placeString);
            return true;
        } else {
            placeString = expression.length();
            return false;
        }
    }

    private void next() {
        if (placeString == -1) {
            nextChar();
        }
        while (currChar == ' ' || currChar == '\n' || currChar == '\t' || currChar == '\r') {
            if (!nextChar()) {
                break;
            }
        }

        if (placeString >= expression.length()) {
            currToken = token.END;
            return;
        }

        switch (currChar) {
            case '\\':
                nextChar();
                currToken = token.LAMBDA_L;
                return;
            case '(':
                nextChar();
                currToken = token.LBRACKET;
                return;
            case ')':
                nextChar();
                currToken = token.RBRACKET;
                return;
            case '.':
                nextChar();
                currToken = token.DOT;
                return;
            default:
                StringBuilder sb = new StringBuilder();
                if (currChar >= 'a' && currChar <= 'z') {
                    while ((currChar >= 'a' && currChar <= 'z') || Character.isDigit(currChar) || currChar == '\'') {
                        sb.append(currChar);
                        if (!nextChar()) {
                            break;
                        }
                    }
                }
                name = sb;
                currToken = token.VARIABLE;
        }
    }

    private NodeExpr Expression(boolean flag) throws Exception {
        switch (currToken) {
            case LBRACKET:
            case VARIABLE:
                // E -> AE'
                NodeExpr application = Application(flag);
                NodeExpr ea = EA();
                if (ea != null) {
                    return new NodeAppl(application, ea, flag);
                } else {
                    return application;
                }
            case LAMBDA_L:
                return Lambda();
            default:
                throw new Exception("hi1");
        }
    }

    private NodeExpr Application(boolean flag) throws Exception {
        switch (currToken) {
            case LBRACKET:
            case VARIABLE:
                NodeExpr atom = Atom();
                NodeExpr applicationA = ApplicationA();
                if (applicationA == null) {
                    return atom;
                }
                if (applicationA instanceof NodeAppl) {
                    ArrayList<NodeExpr> arrayList = new ArrayList<>();
                    arrayList.add(atom);
                    while (applicationA instanceof NodeAppl && !((NodeAppl) applicationA).getFlag()) {
                        arrayList.add(((NodeAppl) applicationA).getLeft());

                        applicationA = ((NodeAppl) applicationA).getRight();
                    }
                    arrayList.add(applicationA);
                    NodeExpr curNode = new NodeAppl(arrayList.get(0), arrayList.get(1), flag);
                    for (int i = 2; i < arrayList.size(); ++i) {
                        curNode = new NodeAppl(curNode, arrayList.get(i), flag);
                    }
                    return curNode;
                } else {
                    return new NodeAppl(atom, applicationA, flag);
                }
            default:
                throw new Exception("hi1");
        }
    }

    private NodeExpr ApplicationA() throws Exception {
        switch (currToken) {
            case LBRACKET:
            case VARIABLE:
                NodeExpr atom = Atom();
                NodeExpr applicationA = ApplicationA();
                if (applicationA == null) {
                    return atom;
                }
                return new NodeAppl(atom, applicationA);
            case LAMBDA_L:
            case RBRACKET:
            case END:
                return null;
            default:
                throw new Exception("hi1");
        }
    }

    private NodeExpr Atom() throws Exception {
        switch (currToken) {
            case LBRACKET:
                next();
                NodeExpr expression = Expression(true);
                next();
                return expression;
            case VARIABLE:
                NodeVar var = new NodeVar(name.toString());
                next();
                return var;
            default:
                throw new Exception("hi1");
        }
    }

    private NodeExpr EA() throws Exception {
        switch (currToken) {
            case LAMBDA_L:
                return Lambda();
            case END:
            case RBRACKET:
                return null;
            default:
                throw new Exception("hi1");
        }
    }

    private NodeLambda Lambda() throws Exception {
        switch (currToken) {
            case LAMBDA_L:
                next();
                if (currToken != token.VARIABLE) {
                    throw new Exception("hi1");
                }
                NodeVar variable = new NodeVar(name.toString());
                next();
                if (currToken != token.DOT) {
                    throw new Exception("hi1");
                }
                next();
                NodeExpr expression = Expression(false);
                return new NodeLambda(variable, expression);
            default:
                throw new Exception("hi");
        }
    }

    Parser(String expression) {
        this.expression = expression;
    }

    NodeExpr parse() throws Exception {
        next();
        return Expression(false);
    }
}