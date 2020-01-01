package nodes;


import java.util.HashMap;

public class NodeAppl extends NodeExpr {
    private NodeExpr left;
    private NodeExpr right;
    private boolean flagApo = false;

    @Override
    public String toString() {
        return "(" + left.toString() + " " + right.toString() + ")";
    }

    public String toStringReal(HashMap<String, String> realNames) {
        return "(" + left.toStringReal(realNames) + " " + right.toStringReal(realNames) + ")";
    }
    public NodeAppl(NodeExpr a, NodeExpr b) {
        left = a;
        right = b;
    }
    public NodeAppl(NodeExpr a, NodeExpr b, boolean flagApo) {
        left = a;
        right = b;
        this.flagApo = flagApo;
    }

    public NodeExpr getLeft() {
        return left;
    }

    public NodeExpr getRight() {
        return right;
    }
    public boolean getFlag() {
        return flagApo;
    }
}
