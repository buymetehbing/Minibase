package ed.inf.adbs.minibase.base;

import java.util.*;

/**
 * The JoinOperator class performs join operations on tuples from left and right child Operators
 */
public class JoinOperator extends Operator {

    private JoinOperator leftJoinOperator;
    private SelectOperator leftSelectOperator;
    private ScanOperator leftScanOperator;
    private String leftOperator;

    private SelectOperator rightSelectOperator;
    private ScanOperator rightScanOperator;
    private String rightOperator;

    private List<Term> leftVariables;
    private List<Term> rightVariables;
    private List<Term> commonVariables;
    private List<Term> joinVariables;

    private List<ComparisonAtom> leftComparisonAtoms;
    private List<ComparisonAtom> rightComparisonAtoms;
    private List<ComparisonAtom> joinComparisonAtoms;

    private Tuple left = new Tuple();
    private Tuple right = new Tuple();

    public JoinOperator(List<RelationalAtom> relationalAtoms,List<ComparisonAtom> comparisonAtoms) {
        super(relationalAtoms.get(relationalAtoms.size()-1));

        // Create strings to store child Operator names
        String rightName = new String();
        String leftName = new String();

        // Create right child Operator with rightmost RelationalAtom
        RelationalAtom rightAtom = relationalAtoms.get(relationalAtoms.size()-1);
        rightComparisonAtoms = Extract.extractRelationComparisonAtoms(rightAtom.getTerms(), comparisonAtoms);

        // If selection conditions exist, create right SelectOperator, else create right ScanOperator
        if (rightComparisonAtoms.size()>0 || Comparison.explicitComparisonExist(rightAtom.getTerms())) {
            rightOperator = new String("SelectOperator");
            rightSelectOperator = new SelectOperator(rightAtom, rightComparisonAtoms);
            rightVariables = rightSelectOperator.getRelationalVariables();
            rightName = rightSelectOperator.getName();
        }
        else {
            rightOperator = new String("ScanOperator");
            rightScanOperator = new ScanOperator(rightAtom);
            rightVariables = rightScanOperator.getRelationalVariables();
            rightName = rightScanOperator.getName();
        }

        // Create left child Operator with other RelationalAtom(s)
        List<RelationalAtom> otherRelationalAtoms = new ArrayList<RelationalAtom>();
        otherRelationalAtoms.addAll(relationalAtoms);
        otherRelationalAtoms.remove(relationalAtoms.size()-1);

        // If more than one RelationalAtom remaining, create left JoinOperator using otherRelationalAtoms
        if (otherRelationalAtoms.size() > 1) {
            leftOperator = new String("JoinOperator");
            leftJoinOperator = new JoinOperator(otherRelationalAtoms, comparisonAtoms);
            leftName = leftJoinOperator.getName();
            leftVariables = leftJoinOperator.getJoinVariables();
            leftComparisonAtoms = Extract.extractRelationComparisonAtoms(leftVariables, comparisonAtoms);
        }

        else if (otherRelationalAtoms.size() == 1) {
            RelationalAtom leftAtom = relationalAtoms.get(0);
            leftComparisonAtoms = Extract.extractRelationComparisonAtoms(leftAtom.getTerms(), rightComparisonAtoms);
        
            // If selection conditions exist, create left SelectOperator, else create left ScanOperator
            if (leftComparisonAtoms.size()>0 || Comparison.explicitComparisonExist(leftAtom.getTerms())) {
                leftOperator = new String("SelectOperator");
                leftSelectOperator = new SelectOperator(leftAtom, leftComparisonAtoms);
                leftName = leftSelectOperator.getName();
                leftVariables = leftSelectOperator.getRelationalVariables();
            }
            else {
                leftOperator = new String("ScanOperator");
                leftScanOperator = new ScanOperator(leftAtom);
                leftName = leftScanOperator.getName();
                leftVariables = leftScanOperator.getRelationalVariables();
            }
        }

        // Extract common variables from left and right Operator variable lists
        commonVariables = Extract.extractCommonVariables(leftVariables, rightVariables);

        // Extract join variables from left and right Operator variable lists
        joinVariables = Extract.extractJoinVariables(leftVariables, rightVariables,commonVariables);

        // Extract ComparisonAtoms for join conditions
        joinComparisonAtoms = Extract.extractRelationComparisonAtoms(joinVariables,comparisonAtoms);
        joinComparisonAtoms.removeAll(leftComparisonAtoms);
        joinComparisonAtoms.removeAll(rightComparisonAtoms);

        // Set new atom name and variable list
        setNewAtom(leftName + " JOIN " + rightName, joinVariables);
    }


    /**
     * Get next joined tuple satisfying join conditions
     */
    public Tuple getNextTuple() {
        Tuple nextTuple;

        // Get next left tuple if right is empty
        if (right.isEmpty()) {
            left = getNextLeft();
        }

        // Get next right tuple if left not empty
        while (!left.isEmpty()) {
            right = getNextRight();

            while (!right.isEmpty()) {

                // Check if tuples satisfy join conditions
                if (satisfyJoin(left,right)) {
                    nextTuple = getMergedTuple(left,right);
                    return nextTuple;
                }

                // Get next right tuple if conditions are not satisfied
                else {
                    right = getNextRight();
                }
            }

            // Get next left tuple if right is empty and reset right
            left = getNextLeft();
            resetRight();
        }

        // If both empty, return empty Tuple
        nextTuple = new Tuple();
        return nextTuple;

    }

    /**
     * Get next tuple from left Operator
     * @return  left tuple
     */
    private Tuple getNextLeft() {
        Tuple nextLeft;

        if (leftOperator.equals("JoinOperator")) {
            nextLeft = leftJoinOperator.getNextTuple();
        }
        else if (leftOperator.equals("SelectOperator")) {
            nextLeft = leftSelectOperator.getNextTuple();
        }
        else if (leftOperator.equals("ScanOperator")) {
            nextLeft = leftScanOperator.getNextTuple();
        }
        else {
            nextLeft = new Tuple();
        }

        return nextLeft;
    }

    /**
     * Get next tuple from right Operator
     * @return  right tuple
     */     
    private Tuple getNextRight() {
        Tuple nextRight;

        if (rightOperator.equals("SelectOperator")) {
            nextRight = rightSelectOperator.getNextTuple();
        }
        else if (rightOperator.equals("ScanOperator")) {
            nextRight = rightScanOperator.getNextTuple();
        }
        else {
            nextRight = new Tuple();
        }

        return nextRight;
    }

    /**
     * Check if join conditions are satisfied
     * 
     * @param left      left tuple
     * @param right     right tuple
     * @return          true if conditions satisfied
     */
    private Boolean satisfyJoin(Tuple left, Tuple right) {

        // Check if common variables join conditions satisfied
        if (commonVariables.size()>0) {
            if (!satisfyCommonVariables(left, right)) {
                return false;
            }
        }

        // Check if ComparisonAtoms join conditions satisfied
        if (joinComparisonAtoms.size()>0) {
            if (!satisfyJoinComparisons(left, right)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return true if common variables in tuples have the same value
     * 
     * @param left      left tuple
     * @param right     right tuple
     * @return          true if values are the same for common variables
     */
    private Boolean satisfyCommonVariables(Tuple left, Tuple right) {
        Integer leftIndex, rightIndex;

        for (int i=0;i<commonVariables.size();i++) {
            leftIndex = leftVariables.indexOf(commonVariables.get(i));
            rightIndex = rightVariables.indexOf(commonVariables.get(i));

            // Check if common variables have same value in both tuples
            if (!left.get(leftIndex).equals(right.get(rightIndex))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if join conditions from ComparisonAtoms are satisfied
     * 
     * @param left      left tuple
     * @param right     right tuple
     * @return          true if conditions satisfied
     */
    private Boolean satisfyJoinComparisons(Tuple left, Tuple right) {

        for (int i=0;i<joinComparisonAtoms.size();i++) {
            ComparisonAtom comparisonAtom = joinComparisonAtoms.get(i);

            // Substitute variables with tuple data
            comparisonAtom = Comparison.comparisonSubstitute(leftVariables, left, comparisonAtom);
            comparisonAtom = Comparison.comparisonSubstitute(rightVariables, right, comparisonAtom);

            // Check if ComparisonAtom has comparison conditions satisfied
            if (!Comparison.checkSatisfy(comparisonAtom)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return merged tuple 
     * 
     * @param left      left tuple
     * @param right     right tuple
     * @return          merged tuple
     */
    private Tuple getMergedTuple(Tuple left, Tuple right) {

        // Create merged tuple from unique joined variables
        Tuple mergedTuple = new Tuple();

        for (int i=0;i<leftVariables.size();i++) {
            if (leftVariables.get(i) instanceof Variable) {
                mergedTuple.add(left.get(i));
            }
        }
        for (int i=0;i<rightVariables.size();i++) {
            if (rightVariables.get(i) instanceof Variable && !commonVariables.contains((Variable)rightVariables.get(i))) {
                mergedTuple.add(right.get(i));
            }
        }

        return mergedTuple;
    }

    /**
     * Reset right Operator created
     */
    private void resetRight() {

        if (rightOperator.equals("SelectOperator")) {
            rightSelectOperator.reset();
        }
        else if (rightOperator.equals("ScanOperator")){
            rightScanOperator.reset();
        }
    }

    /**
     * Reset both left and right Operator created
     */
    public void reset() {

        // Reset left Operator created
        if (leftOperator.equals("JoinOperator")) {
            leftJoinOperator.reset();
        }
        else if (leftOperator.equals("SelectOperator")) {
            leftSelectOperator.reset();
        }
        else if (leftOperator.equals("ScanOperator"))  {
            leftScanOperator.reset();
        }
        
        // Reset right Operator
        resetRight();
    }

    /**
     * Get join variables
     * @return  join variables
     */
    public List<Term> getJoinVariables() {
        return joinVariables;
    }
}