package ed.inf.adbs.minibase.base;

import java.util.*;

/**
 * The SelectOperator gets tuples from the ScanOperator and returns tuples satisfying selection conditions
 */
public class SelectOperator extends Operator {

    private ScanOperator scanOperator;
    private List<ComparisonAtom> comparisonAtoms;

    public SelectOperator (RelationalAtom relationalAtom, List<ComparisonAtom> comparisonAtoms) {
        
        super(relationalAtom);

        scanOperator = new ScanOperator(relationalAtom);
        this.comparisonAtoms = Extract.extractRelationComparisonAtoms(scanOperator.getRelationalTerms(), comparisonAtoms);

        setNewAtom("SELECT(" + scanOperator.getName() + ")", scanOperator.getRelationalTerms());
    }

    /**
     * Get next tuple satisfying selection conditions
     */
    public Tuple getNextTuple() {
        Tuple tpl = scanOperator.getNextTuple();
        while (!tpl.isEmpty()) {

            // Return tuple if selection conditions satisfied
            if (satisfyComparisons(tpl)) {
                // Remove data in tuples corresponding to Constant terms
                tpl = removeConstants(tpl);
                return tpl;
            }
            tpl = scanOperator.getNextTuple();
        }
        return tpl;
    }

    /**
     * Remove data in tuples corresponding to Constant terms
     * 
     * @param tpl   tuple
     * @return      tuple without data corresponding to Constant terms
     */
    private Tuple removeConstants(Tuple tpl) {
        List<Term> relationalTerms = scanOperator.getRelationalTerms();
        if (getRelationalVariables().size() == relationalTerms.size()) {
            return tpl;
        }
        else {
            Tuple newTuple = new Tuple();
            for (int i=0;i<relationalTerms.size();i++) {
                if (relationalTerms.get(i) instanceof Variable) {
                    newTuple.add(tpl.get(i));
                }
            }
            return newTuple;
        }
    }

    /**
     * Reset scanOperator
     */
    public void reset() {
        scanOperator.reset();
    }

    /**
     * Return true is selection conditions satisfied
     * 
     * @param tpl   tuple
     * @return      true if conditions satisfied
     */
    private Boolean satisfyComparisons(Tuple tpl) {
        if (!satisfyExplicit(tpl) || !satisfyImplicit(tpl)) {
            return false; 
        }
        return true;
    }

    /**
     * Return true is explicit selection conditions satisfied
     * 
     * @param tpl   tuple
     * @return      true if conditions satisfied
     */
    private Boolean satisfyExplicit(Tuple tpl) {
        List<Term> relationalTerms = scanOperator.getRelationalTerms();
        for (int i=0;i<relationalTerms.size();i++) {

            if (relationalTerms.get(i) instanceof Constant) {
                if (!relationalTerms.get(i).equals(tpl.get(i))) {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * Return true is ComparisonAtom selection conditions satisfied
     * 
     * @param tpl   tuple
     * @return      true if conditions satisfied
     */
    private Boolean satisfyImplicit(Tuple tpl) {
        List<Term> relationalTerms = scanOperator.getRelationalTerms();

        for (int i=0;i<comparisonAtoms.size();i++) {
            ComparisonAtom atom = comparisonAtoms.get(i);
            ComparisonAtom subComparisonAtom = Comparison.comparisonSubstitute(relationalTerms,tpl,atom);

            if (!Comparison.checkSatisfy(subComparisonAtom)) {
                return false;
            }
        }
        return true;
    }

}
