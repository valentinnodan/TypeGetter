package types;

import java.util.HashSet;

public class TypeVar extends Type {
    String name;
    public TypeVar(String name){
        this.name = name;
    }
    public String toString(){
        return name;
    }
}
