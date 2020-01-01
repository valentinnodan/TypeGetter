package nodes;


import java.util.HashMap;

public class NodeLambda extends NodeExpr {
    private NodeVar var;
    private NodeExpr expr;

    @Override
    public String toString() {
        return "(\\" + var.toString() + "." + expr.toString() + ")";
    }

    public String toStringReal(HashMap<String, String> realNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("(\\").append(var.toStringReal(realNames)).append(".").append(expr.toStringReal(realNames)).append(')');
        return sb.toString();
    }

    public NodeLambda(NodeVar a , NodeExpr b){
        var = a;
        expr = b;
    }
    public String getVarName() {
        return var.toString();
    }
    public void setVar(String varName) {
        var = new NodeVar(varName);
    }
    public NodeExpr getExpr(){
        return expr;
    }
}
