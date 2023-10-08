package ed.inf.adbs.minibase.base;

import java.util.*;

/**
 * The Extract class extracts query relevant information such as head variables, relational atoms, etc.
 */
public class Extract {

    /**
     * Extract and return all variables in head, including those in SumAggregate. Ordered by head variables before SumAggregate variables.
     * 
     * @param head      query head
     * @return          all head variables
     */
    public static List<Term> extractHeadVariables(Head head) {

        // Create head variable list
        List<Term> headVariables = new ArrayList<Term>();

        // Add SumAggregate variables to list
        SumAggregate agg = head.getSumAggregate();
        if (agg != null) {
            List<Term> productTerms = agg.getProductTerms();
            for (int i=0;i<productTerms.size();i++) {
                if (productTerms.get(i) instanceof Variable) {
                    headVariables.add(productTerms.get(i));
                }
            }
            // Remove SumAggregate variables also found in head to prevent duplicates
            headVariables.removeAll(head.getVariables());
        }
        // Add head variables to list
        headVariables.addAll(0,head.getVariables());

        return headVariables;
    }

    /**
     * Extract and return unique RelationalAtoms in query body
     *  
     * @param body  query body
     * @return      unique RelationalAtoms in query
     */
    public static List<RelationalAtom> extractRelationalAtoms(List<Atom> body) {

        // Create list of RelationalAtoms
        List<RelationalAtom> relationalAtoms = new ArrayList<RelationalAtom>();
        for (int i=0;i<body.size();i++) {

            // Check if atom is RelationalAtom and not already in list
            if (body.get(i) instanceof RelationalAtom && !relationalAtoms.contains(body.get(i))) {
                relationalAtoms.add((RelationalAtom) body.get(i));
            }
        }

        return relationalAtoms;
    }

    /**
     * Extract and return RelationalAtom names from query body
     * @param body  query body
     * @return      list of RelationalAtom names
     */
    public static List<String> extractRelationalNames(List<RelationalAtom> body) {

        // Create list of RelationalAtom names
        List<String> relationalNames = new ArrayList<String>();
        for (int i=0;i<body.size();i++) {

            // Check if atom is RelationalAtom
            if (body.get(i) instanceof RelationalAtom) {
                RelationalAtom atom = (RelationalAtom)body.get(i);
                relationalNames.add(atom.getName());
            }
        }

        return relationalNames;
    }

    /**
     * Extract and return RelationalAtom terms in query body
     * 
     * @param body  query body
     * @return      RelationalAtom terms 
     */

    public static List<List<Term>> extractRelationalTerms(List<RelationalAtom> body) {

        // Create list of RelationalAtom terms
        List<List<Term>> relationalTerms = new ArrayList<List<Term>>(body.size());
        for (int i=0;i<body.size();i++) {

            // Check if atom is RelationalAtom
            if (body.get(i) instanceof RelationalAtom) {
                RelationalAtom atom = (RelationalAtom)body.get(i);
                List<Term> terms = atom.getTerms();
                relationalTerms.add(terms);
            }
        }

        return relationalTerms;
    }

    /**
     * Extract and return ComparisonAtoms in query body
     * @param body  query body
     * @return      list of ComparisonAtoms
     */
    public static List<ComparisonAtom> extractComparisonAtoms(List<Atom> body) {

        // Create list of ComparisonAtoms
        List<ComparisonAtom> comparisonAtoms = new ArrayList<ComparisonAtom>();
        for (int i=0;i<body.size();i++) {

            // Check if atom is ComparisonAtom
            if (body.get(i) instanceof ComparisonAtom) {
                comparisonAtoms.add((ComparisonAtom) body.get(i));
            }
        }

        return comparisonAtoms;
    }

    /**
     * Extract and return ComparisonAtoms containing only terms in list
     * 
     * @param relationalTerms   list of terms
     * @param comparisonAtoms   ComparisonAtoms
     * @return                  ComparisonAtoms containing only terms in list
     */
    public static List<ComparisonAtom> extractRelationComparisonAtoms(List<Term> relationalTerms,List<ComparisonAtom> comparisonAtoms) {

        // Create list of ComparisonAtoms containing only terms in list
        List<ComparisonAtom> relationComparisonAtoms = new ArrayList<ComparisonAtom>();
        for (int i=0;i<comparisonAtoms.size();i++) {
            ComparisonAtom atom = (ComparisonAtom) comparisonAtoms.get(i);

            // Check if other terms are found in ComparisonAtom, if so do not add
            if (atom.getTerm1() instanceof Variable && !relationalTerms.contains(atom.getTerm1())) {
                continue;
            }
            if (atom.getTerm2() instanceof Variable && !relationalTerms.contains(atom.getTerm2())) {
                continue;
            }
            else {
                relationComparisonAtoms.add(atom);
            }
        }

        return relationComparisonAtoms;
    }

    /**
     * Extract and return common variable terms found in both variable lists
     * 
     * @param leftVariables     left variables
     * @param rightVariables    right variables
     * @return                  list of common variables
     */
    public static List<Term> extractCommonVariables(List<Term> leftVariables, List<Term> rightVariables) {

        // Create list of common variables
        List<Term> commonVariables = new ArrayList<Term>();
        commonVariables.addAll(leftVariables);
        commonVariables.retainAll(rightVariables);
        return commonVariables;
    }

    /**
     * Extract and return unique variable terms found in both variable lists
     * 
     * @param leftVariables     left variables
     * @param rightVariables    right variables
     * @param commonVariables   common variables
     * @return                  combined list of unique variables
     */
    public static List<Term> extractJoinVariables(List<Term> leftVariables, List<Term> rightVariables, List<Term> commonVariables) {

        // Create combined list of unique variables
        List<Term> joinVariables = new ArrayList<Term>();
        joinVariables.addAll(rightVariables);
        joinVariables.removeAll(commonVariables);
        joinVariables.addAll(0,leftVariables);
        return joinVariables;
    }

}
