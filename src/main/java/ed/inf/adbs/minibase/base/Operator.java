package ed.inf.adbs.minibase.base;

import java.util.ArrayList;
import java.util.List;

/**
 * The Operator class is an abstract class for all Operators
 */
public abstract class Operator {

    private String relationalName;
    private List<Term> relationalTerms;
    private List<Term> relationalVariables;

    public Operator(RelationalAtom relationalAtom) {
        
        this.relationalName = relationalAtom.getName();
        this.relationalTerms = relationalAtom.getTerms();
        setRelationalVariables();

    } 

    public abstract Tuple getNextTuple();
    public abstract void reset();

    public String getName() {
        return relationalName;
    }

    public List<Term> getRelationalTerms() {
        return relationalTerms;
    }

    public List<Term> getRelationalVariables() {
        return relationalVariables;
    }

    public void setNewAtom(String relationalName,List<Term> relationalTerms) {
        this.relationalName = relationalName;
        this.relationalTerms = relationalTerms;
        setRelationalVariables();
    }
    
    private void setRelationalVariables() {
        relationalVariables = new ArrayList<Term>();
        for (int i=0;i<relationalTerms.size();i++) {
            if (relationalTerms.get(i) instanceof Variable) {
                relationalVariables.add(relationalTerms.get(i));
            }
        }
    }

    public void dump() {
        Tuple tpl = getNextTuple();
        while (!tpl.isEmpty()) {
            System.out.println(tpl);
            tpl = getNextTuple();
        }
        reset();
    }

    public List<Tuple> getDump() {
        List<Tuple> tupleDump = new ArrayList<Tuple>();
        Tuple tpl = getNextTuple();
        while (!tpl.isEmpty()) {
            tupleDump.add(tpl);
            tpl = getNextTuple();
        } 
        reset();
        return tupleDump;
    }
    
}
