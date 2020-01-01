package nodes;

import types.Type;

import java.util.HashMap;

public class NodeVar extends NodeExpr {
    private String name;

    @Override
    public String toString() {
        return name;
    }

    public String toStringReal(HashMap<String, String> realNames){
        if (name.startsWith("A")){
            return realNames.get(name);
        }
        return name;
    }

    public NodeVar(String a){
        name = a;
    }

    public void setName(String name) {
        this.name = name;
    }
}
