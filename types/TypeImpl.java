package types;

import java.util.HashSet;

public class TypeImpl extends Type {
    Type left;
    Type right;

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(left.toString()).append("->").append(right.toString()).append(')');
        return sb.toString();
    }
    public Type getLeft(){
        return left;
    }
    public Type getRight(){
        return right;
    }
    public TypeImpl(Type left, Type right){
        this.left = left;
        this.right = right;
    }
}
