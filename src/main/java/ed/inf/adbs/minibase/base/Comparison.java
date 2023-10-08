package ed.inf.adbs.minibase.base;

import java.util.List;

/**
 * The Comparison class performs selection and join checks based on ComparisonAtoms and explicit conditions
 */
public class Comparison {
    
    /**
     * Return true if explicit selection conditions exist (Constant is in term list)
     * 
     * @param relationalTerms       term list of RelationalAtom
     * @return                      true if Constant is found, else false
     */
    public static Boolean explicitComparisonExist(List<Term> relationalTerms) {

        // Return true if Constant is found
        for (int i=0;i<relationalTerms.size();i++) {
            if (relationalTerms.get(i) instanceof Constant) {
                return true;
            }
        }
        return false;
    }

    /**
     * Substitute variables in ComparisonAtom to corresponding value in tuple and return new ComparisonAtom
     * 
     * @param relationalTerms       terms corresponding to that of tuple
     * @param tpl                   tuple
     * @param comparisonAtom        ComparisonAtom to be substituted
     * @return                      substituted ComparisonAtom
     */ 
    public static ComparisonAtom comparisonSubstitute(List<Term> relationalTerms, Tuple tpl, ComparisonAtom comparisonAtom) {
        
        Term firstTerm, secondTerm;

        // Substitute first term in ComparisonAtom if variable, else retain original constant term
        if (comparisonAtom.getTerm1() instanceof Variable) {
            firstTerm = tupleSubstitute(relationalTerms,tpl,comparisonAtom.getTerm1());
        }
        else { firstTerm = comparisonAtom.getTerm1(); }

        // Substitute second term in ComparisonAtom if variable, else retain original constant term
        if (comparisonAtom.getTerm2() instanceof Variable) {
            secondTerm = tupleSubstitute(relationalTerms,tpl,comparisonAtom.getTerm2());
        }
        else { secondTerm = comparisonAtom.getTerm2(); }

        // Create and return substituted ComparisonAtom
        ComparisonAtom subComparisonAtom = new ComparisonAtom(firstTerm,secondTerm,comparisonAtom.getOp());
        return subComparisonAtom;
    }

    /**
     * Substitute variable term for value in tuple using term list and return it
     * 
     * @param relationalTerms       terms corresponding to that of tuple
     * @param tpl                   tuple
     * @param term                  term to be substituted
     * @return                      (substituted) term
     */

    public static Term tupleSubstitute(List<Term> relationalTerms, Tuple tpl, Term term) {

        // Substitute term if found in tuple's corresponding term list
        for (int i=0;i<relationalTerms.size();i++) {
            if (relationalTerms.get(i).equals(term)) {
                return tpl.get(i);
            }
        }
        // If no substitute, return original term
        return term;
    }

    /**
     * Return true if values if ComparisonAtom satisfy comparison condition
     * 
     * @param comparisonAtom    ComparisonAtom
     * @return                  true if comparison condition satisfied
     */
    public static Boolean checkSatisfy(ComparisonAtom comparisonAtom) {
        Term term1 = comparisonAtom.getTerm1();
        Term term2 = comparisonAtom.getTerm2();
        ComparisonOperator op = comparisonAtom.getOp();

        // If both StringConstant, check if comparison condition satisfied
        if (term1 instanceof StringConstant && term2 instanceof StringConstant) {
            if (Comparison.checkStringSatisfy(op, (StringConstant)term1, (StringConstant)term2)) {
                return true;
            }
        }
        // If both IntegerConstant, check if comparison condition satisfied
        else if (term1 instanceof IntegerConstant && term2 instanceof IntegerConstant) {
            if (Comparison.checkIntSatisfy(op, (IntegerConstant)term1, (IntegerConstant)term2)) {
                return true;
            }
        }
        // Otherwise, return false
        return false;
    }
    
    /**
     * Check if StringConstant satisfy comparison condition
     * 
     * @param op    ComparisonOperator
     * @param t1    first StringConstant
     * @param t2    second StringConstant
     * @return      true if satisfy
     */
    public static Boolean checkStringSatisfy(ComparisonOperator op, StringConstant t1, StringConstant t2) {
        String term1 = t1.getValue();
        String term2 = t2.getValue();

        // Perform checks based on ComparisonOperator
        switch(op) {
            case EQ:
                if (term1.equals(term2)) { return true; }
                else { return false;}

            case NEQ:
                if (!term1.equals(term2)) { return true; }
                else { return false;}

            case GT: return false;
            case GEQ: return false;
            case LT: return false;
            case LEQ: return false;
            default: return false;
        }
    }

    /**
     * Check if IntegerConstant satisfy comparison condition
     * 
     * @param op    ComparisonOperator
     * @param t1    first IntegerConstant
     * @param t2    second IntegerConstant
     * @return      true if satisfy
     */
    public static Boolean checkIntSatisfy(ComparisonOperator op, IntegerConstant t1, IntegerConstant t2) {
        Integer term1 = t1.getValue();
        Integer term2 = t2.getValue();

        // Perform checks based on ComparisonOperator
        switch(op) {
            case EQ:
                if (term1 == term2) { return true; }
                else { return false;}

            case NEQ:
                if (term1 != term2) { return true; }
                else { return false;}

            case GT:
                if (term1 > term2) { return true; }
                else { return false;}

            case GEQ:
                if (term1 >= term2) { return true; }
                else { return false;}

            case LT:
                if (term1 < term2) { return true; }
                else { return false;}

            case LEQ:
                if (term1 <= term2) { return true; }
                else { return false;}
                
            default: return false;
        }
    }
}
