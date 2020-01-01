package types;

import java.util.HashSet;

public class Type {
    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
}
