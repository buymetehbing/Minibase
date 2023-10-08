package ed.inf.adbs.minibase.base;

public class StringConstant extends Constant {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void substitute(String newValue) {
        this.value = newValue;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StringConstant)) {
            return false;
        }
        else {
            StringConstant s = (StringConstant)o;
            if (this.getValue().equals(s.getValue())) {
                return true;
            }
            return false;
        }
    }
}