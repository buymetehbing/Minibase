package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelationalAtom)) {
            return false;
        }
        else {
            RelationalAtom r = (RelationalAtom)o;
            if (this.getName().equals(r.getName()) && this.getTerms().equals(r.getTerms())) {
                return true;
            }
            return false;
        }
    }
}
