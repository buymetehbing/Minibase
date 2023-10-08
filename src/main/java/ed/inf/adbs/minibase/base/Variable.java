package ed.inf.adbs.minibase.base;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void substitute(String newValue) {
        this.name = newValue;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Variable)) {
            return false;
        }
        else {
            Variable v = (Variable)o;
            if (this.getName().equals(v.getName())) {
                return true;
            }
            return false;
        }
    }
}
