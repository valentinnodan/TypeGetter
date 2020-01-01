import nodes.NodeAppl;
import nodes.NodeExpr;
import nodes.NodeLambda;
import nodes.NodeVar;
import types.Type;
import types.TypeImpl;
import types.TypeVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Test {
    static class Pair {
        Type first, second;

        Pair(Type first, Type second) {
            this.first = first;
            this.second = second;
        }

        public void swap() {
            Type tmp = first;
            first = second;
            second = tmp;
        }
    }

    static HashMap<String, String> realNames = new HashMap<>();
    static HashMap<String, String> currContext = new HashMap<>();
    static HashMap<String, Type> types = new HashMap<>();
    static ArrayList<Pair> conditions = new ArrayList<>();
    static ArrayList<Pair> conditions1 = new ArrayList<>();
    static HashSet<String> freeVariables = new HashSet<>();
    static int countVars = 0;
    static int countTypes = 0;

    public static void rename(NodeExpr node) {
        StringBuilder sb = new StringBuilder();
        sb.append('A');
        if (node instanceof NodeLambda) {
            realNames.put(sb.append(countVars++).toString(), ((NodeLambda) node).getVarName());
            currContext.put(((NodeLambda) node).getVarName(), sb.toString());
            ((NodeLambda) node).setVar(sb.toString());
            rename(((NodeLambda) node).getExpr());
            currContext.remove(realNames.get(sb.toString()));
        } else if (node instanceof NodeAppl) {
            rename(((NodeAppl) node).getLeft());
            rename(((NodeAppl) node).getRight());
        } else if (node instanceof NodeVar) {
            if (currContext.containsKey(node.toString())) {
                ((NodeVar) node).setName(currContext.get(node.toString()));
            }
        }
    }

    public static void understand(NodeExpr node) {
        if (node instanceof NodeLambda) {
            StringBuilder sb = new StringBuilder();
            sb.append('t');
            if (!types.containsKey(((NodeLambda) node).getVarName())) {
                types.put(((NodeLambda) node).getVarName(), new TypeVar(sb.append(countTypes++).toString()));
            }
            understand(((NodeLambda) node).getExpr());
            types.put(node.toString(), new TypeImpl(types.get(((NodeLambda) node).getVarName()), types.get(((NodeLambda) node).getExpr().toString())));
        } else if (node instanceof NodeAppl) {
            understand(((NodeAppl) node).getLeft());
            understand(((NodeAppl) node).getRight());
            Type leftType = types.get(((NodeAppl) node).getLeft().toString());
            Type rightType = types.get(((NodeAppl) node).getRight().toString());
            if (leftType instanceof TypeImpl) {
                types.put(node.toString(), ((TypeImpl) leftType).getRight());
                Pair pair = new Pair(((TypeImpl) leftType).getLeft(), rightType);
                conditions.add(pair);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append('t');
                Type typeVar = new TypeVar(sb.append(countTypes++).toString());
                types.put(node.toString(), typeVar);
                Pair pair = new Pair(leftType, new TypeImpl(rightType, typeVar));
                conditions.add(pair);
            }
        } else if (node instanceof NodeVar) {
            if (!types.containsKey(node.toString())) {
                StringBuilder sb = new StringBuilder();
                sb.append('t');
                types.put(node.toString(), new TypeVar(sb.append(countTypes++).toString()));
            }
            if (!freeVariables.contains(node.toString()) && !node.toString().startsWith("A")) {
                freeVariables.add(node.toString());
            }
        }
    }

    public static boolean checkVar(Type type, TypeVar var) {
        if (type instanceof TypeVar) {
            return type.equals(var);
        } else {
            return (checkVar(((TypeImpl) type).getLeft(), var) || checkVar(((TypeImpl) type).getRight(), var));
        }
    }

    public static Type change(Type type, Pair pair) {
        if (type instanceof TypeVar) {
            if (type.equals(pair.first)) {
                return pair.second;
            }
            return type;
        } else {
            Type left = change(((TypeImpl) type).getLeft(), pair);
            Type right = change(((TypeImpl) type).getRight(), pair);
            return new TypeImpl(left, right);
        }
    }

    public static boolean unificate() {
        int counter = 50;
        while (0 < counter--) {
            for (Pair p : conditions) {
                if (p.first.equals(p.second)) {
                    conditions.remove(p);
                    break;
                }
                if (p.second instanceof TypeVar && p.first instanceof TypeImpl) {
                    p.swap();
                }
                if (p.first instanceof TypeImpl && p.second instanceof TypeImpl) {
                    Pair fst = new Pair(((TypeImpl) p.first).getLeft(), ((TypeImpl) p.second).getLeft());
                    Pair snd = new Pair(((TypeImpl) p.first).getRight(), ((TypeImpl) p.second).getRight());
                    conditions.add(fst);
                    conditions.add(snd);
                    conditions.remove(p);
                    break;
                }
                if (p.first instanceof TypeVar) {
                    if (checkVar(p.second, (TypeVar) p.first)) {
                        return false;
                    }
                    for (Pair t : conditions) {
                        if (!t.equals(p)) {
                            t.first = change(t.first, p);
                            t.second = change(t.second, p);
                        }
                    }
                }
            }
        }
        return true;
    }

    public static Type substitute(Type type) {
        if (type instanceof TypeVar) {
            for (Pair pair : conditions) {
                if (type.equals(pair.first)) {
                    return pair.second;
                }
            }
            return type;
        }
        return new TypeImpl(substitute(((TypeImpl) type).getLeft()), substitute(((TypeImpl) type).getRight()));

    }

    public static void getTypes(NodeExpr node) {
        if (!unificate()) {
            System.out.println("Expression has no type");
            return;
        }
        types.replaceAll((k, v) -> substitute(types.get(k)));
        StringBuilder hypothesisString = new StringBuilder();
        if (freeVariables.size() > 0) {
            for (String fV : freeVariables) {
                hypothesisString.append(fV).append(" : ").append(types.get(fV).toString()).append(", ");
            }
            hypothesisString.deleteCharAt(hypothesisString.length() - 1).deleteCharAt(hypothesisString.length() - 1);
        }
        visualize(node, 0, hypothesisString.toString());
    }

    public static void visualize(NodeExpr node, int stars, String hypothesis) {
        if (node instanceof NodeLambda) {
            for (int i = 0; i < stars; i++) {
                System.out.print("*   ");
            }
            System.out.print(hypothesis);
            if (hypothesis.length() > 0) {
                System.out.print(" ");
            }
            System.out.print("|- ");
            System.out.print(node.toStringReal(realNames));
            System.out.print(" : ");
            System.out.print(types.get(node.toString()));
            System.out.println(" [rule #3]");
            StringBuilder sb = new StringBuilder();
            if (hypothesis.length() > 0) {
                sb.append(hypothesis).append(", ")
                        .append(realNames.get(((NodeLambda) node).getVarName())).append(" : ")
                        .append(types.get(((NodeLambda) node).getVarName()));
            } else {
                sb.append(realNames.get(((NodeLambda) node).getVarName())).append(" : ")
                        .append(types.get(((NodeLambda) node).getVarName()));
            }

            visualize(((NodeLambda) node).getExpr(), stars + 1, sb.toString());
        } else if (node instanceof NodeAppl) {
            for (int i = 0; i < stars; i++) {
                System.out.print("*   ");
            }
            System.out.print(hypothesis);
            if (hypothesis.length() > 0) {
                System.out.print(" ");
            }
            System.out.print("|- ");
            System.out.print(node.toStringReal(realNames));
            System.out.print(" : ");
            System.out.print(types.get(node.toString()));
            System.out.println(" [rule #2]");
            visualize(((NodeAppl) node).getLeft(), stars + 1, hypothesis);
            visualize(((NodeAppl) node).getRight(), stars + 1, hypothesis);
        } else if (node instanceof NodeVar) {
            for (int i = 0; i < stars; i++) {
                System.out.print("*   ");
            }
            System.out.print(hypothesis);
            if (hypothesis.length() > 0) {
                System.out.print(" ");
            }
            System.out.print("|- ");
            System.out.print(node.toStringReal(realNames));
            System.out.print(" : ");
            System.out.print(types.get(node.toString()));
            System.out.println(" [rule #1]");
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (in.hasNextLine()) {
            sb.append(in.nextLine()).append('\n');
        }


        Parser parser = new Parser(sb.toString());
        NodeExpr node = parser.parse();
        rename(node);
        understand(node);
        getTypes(node);
    }
}