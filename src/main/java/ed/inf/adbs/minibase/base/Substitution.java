package ed.inf.adbs.minibase.base;

import java.util.*;
import org.antlr.v4.runtime.misc.Pair;

/**
 * The Substitution class creates substitution list for query minimization. 
 */
public class Substitution {

    /**
     * Create and return substitution list to minimize a query
     * 
     * @param headVariables         head variables from query
     * @param relationalAtoms       RelationalAtom from query
     * @return                      term substitution list
     */
    
     public static List<Pair<Term,Term>> getSubList(List<Term> headVariables, List<RelationalAtom> relationalAtoms) {

        // Extract RelationalAtom names and terms from query
        List<String> relationalNames = Extract.extractRelationalNames(relationalAtoms);
        List<List<Term>> relationalTerms = Extract.extractRelationalTerms(relationalAtoms);

        // Create term substitution list
        List<Pair<Term,Term>> subList = new ArrayList<Pair<Term,Term>>();
        
        // Iterate through pairs of RelationalAtom names
        for (int i=0;i<relationalNames.size()-1;i++) {
            for (int j=i+1;j<relationalNames.size();j++) {

                // Get possible term substitutions for repeated relations and add to substitution list
                if (relationalNames.get(i).equals(relationalNames.get(j))) {
                    List<Pair<Term,Term>> subs = possibleSubsitutions(headVariables, relationalTerms.get(i), relationalTerms.get(j));
                    subList.addAll(subs);
                }
            }
        }

        // Remove conflicting term substitutions (terms being substituted to multiple different values)
        subList = solveConflicts(subList);

        // Return final substitution list
        return subList;
    }


    /**
     * Create and return list of possible term substitutions
     * 
     * Rules for substitution:
     * 1. Terms being compared must be in the same position in their respective list.
     * 2. Terms that are head variables or constants cannot be substituted.
     * 3. Terms that are not head variables can be substituted with constants or head variables.
     * 4. Terms can only be substituted if there is at least 1 matching terms and no mismatches.
     * 5. If no match is found or a mismatch is found, return empty list.
     * 6. If constant term is found in same position as a head variable term, return empty list.
     * 
     * @param headVariables     list of head variables
     * @param t1                terms in first RelationalAtom
     * @param t2                terms in second RelationalAtom
     * @return                  list of possible term substitutions
     */

    public static List<Pair<Term,Term>> possibleSubsitutions(List<Term> headVariables,List<Term>t1,List<Term>t2) {

        // Create list to store substitutions
        List<Pair<Term,Term>> subList = new ArrayList<Pair<Term,Term>>();
        int match = 0;

        // Iterate through terms in same position for both RelationalAtoms
        for (int i=0;i<t1.size();i++) {

            // Add substitution to list if constant and non-head variable found in same position
            if (t1.get(i) instanceof Constant && t2.get(i) instanceof Variable) {
                if (!isHeadVariable(headVariables, t2.get(i))) {
                    Pair<Term,Term> sub = new Pair<Term,Term>(t2.get(i), t1.get(i));
                    subList.add(sub);
                }
                // If head variable, return empty list
                else { return Collections.emptyList(); }
            }

            // Check for mismatches if both constants
            else if (t1.get(i) instanceof Constant && t2.get(i) instanceof Constant) {
                if (t1.get(i).equals(t2.get(i))) {
                    match += 1;
                }

                // If mismatch found, return empty list
                else { return Collections.emptyList(); }
            }

            // Add substitution if head variable and non-head variable found in same 
            else if (t1.get(i) instanceof Variable && t2.get(i) instanceof Variable) {
                if (!isHeadVariable(headVariables, t1.get(i)) && isHeadVariable(headVariables, t2.get(i))) {
                    Pair<Term,Term> sub = new Pair<Term,Term>(t1.get(i), t2.get(i));
                    subList.add(sub);
                }

                // Check if variables match
                else if (t1.get(i).equals(t2.get(i))) {
                    match += 1;
                }

                // If mismatch found, return empty list
                else { return Collections.emptyList(); }
            }

            // Add substitution to list if constant and non-head variable found in same position
            else if (t1.get(i) instanceof Variable && t2.get(i) instanceof Constant) {
                if (!isHeadVariable(headVariables, t1.get(i))) {
                    Pair<Term,Term> sub = new Pair<Term,Term>(t1.get(i), t2.get(i));
                    subList.add(sub);
                }

                // If head variable, return empty list
                else { return Collections.emptyList(); }
            }
        }
        
        // Return substitution list if at least one match is found
        if (match > 0) {
            return subList;
        }

        // Otherwise, return empty list
        else { return Collections.emptyList(); }
    }

    /**
     * Return true if term is in the query head
     * 
     * @param headVariables         list of query head variables
     * @param t                     term
     * @return                      true if head variable, else false
     */
    public static Boolean isHeadVariable(List<Term> headVariables, Term t) {

        // Iterate through head variables and return true if term found
        for (Term headVariable:headVariables) {
            if (t.equals(headVariable)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Remove conflicting substitutions in the substitution list. These refer to variable terms substituted to multiple different values.
     * 
     * @param subList   initial substitution list
     * @return          substitution list without conflicts
     */

     public static List<Pair<Term,Term>> solveConflicts(List<Pair<Term,Term>> subList) {

        // Get list of terms to be substituted
        List<Term> termsToSubstitute= new ArrayList<Term>();
        for (Pair<Term,Term> p : subList) { termsToSubstitute.add(p.a); }

        // Create new substitution list with variable terms substituted only once
        List<Pair<Term,Term>> finalSubList = new ArrayList<Pair<Term,Term>>();
        for (int i=0;i<termsToSubstitute.size();i++) {
            if (Collections.frequency(termsToSubstitute, termsToSubstitute.get(i)) == 1) {
                finalSubList.add(subList.get(i));
            }
        }

        // Return new substitution list
        return finalSubList;
    }

    /**
     * Remove ComparisonAtom variables from the substitution list
     * 
     * @param subList               substitution list
     * @param comparisonVariables   variables in ComparisonAtoms
     * @return                      substitution list without ComparisonAtoms variables
     */

    public static List<Pair<Term,Term>> removeComparisonVariables (List<Pair<Term,Term>> subList, List<Term> comparisonVariables) {

        // Get list of terms to be substituted
        List<Term> termsToSubstitute= new ArrayList<Term>();
        for (Pair<Term,Term> p : subList) { termsToSubstitute.add(p.a); }

        // Create new substitution list without ComparisonAtoms variables
        List<Pair<Term,Term>> finalSubList = new ArrayList<Pair<Term,Term>>();
        for (int i=0;i<termsToSubstitute.size();i++) {
            if (!comparisonVariables.contains(termsToSubstitute.get(i))) {
                finalSubList.add(subList.get(i));
            }
        }

        // Return new substitution list
        return finalSubList;
    }
}
