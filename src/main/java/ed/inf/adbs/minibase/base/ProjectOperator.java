package ed.inf.adbs.minibase.base;

import java.util.*;

/**
 * The ProjectOperator class projects tuples corresponding to variables in the same order as head variables
 */
public class ProjectOperator extends Operator {

    private List<Term> projectVariables;
    private List<String> returnList;
    private Boolean sum = false;

    private JoinOperator joinOperator;
    private SelectOperator selectOperator;
    private ScanOperator scanOperator;

    private String operator;

    public ProjectOperator(Head head, List<RelationalAtom> relationalAtoms, List<ComparisonAtom> comparisonAtoms) {
        
        super(relationalAtoms.get(relationalAtoms.size()-1));
        projectVariables = Extract.extractHeadVariables(head);
        returnList = new ArrayList<String>();
        if (head.getSumAggregate() != null) { sum = true; }

        // If >1 RelationalAtoms, create JoinOperator
        if (relationalAtoms.size()>1) {
            joinOperator = new JoinOperator(relationalAtoms, comparisonAtoms);
            setNewAtom("PROJECT(" + joinOperator.getName() + ")", joinOperator.getJoinVariables());
            operator = new String("JoinOperator");
        }

        else if (relationalAtoms.size() == 1) {
            RelationalAtom atom = relationalAtoms.get(0);
            
            // If selection conditions exist, create SelectOperator
            if (comparisonAtoms.size()>0 || Comparison.explicitComparisonExist(atom.getTerms())) {
                selectOperator = new SelectOperator(atom, comparisonAtoms);
                setNewAtom("PROJECT(" + selectOperator.getName() + ")", selectOperator.getRelationalVariables());
                operator = new String("SelectOperator");
            }

            // Else, create scanOperator
            else {
                scanOperator = new ScanOperator(atom);
                operator = new String("ScanOperator");
                setNewAtom("PROJECT(" + scanOperator.getName() + ")", scanOperator.getRelationalVariables());
            }
        }
    }

    /**
     * Get next tuple in order of project(head) variables
     */
    public Tuple getNextTuple() {
        Tuple nextTuple = new Tuple();
        Tuple reorderedTuple = new Tuple();

        // Return next tuple based on Operator created
        if (operator.equals("JoinOperator")) {
            nextTuple = joinOperator.getNextTuple();
        }
        else if (operator.equals("SelectOperator")) {
            nextTuple = selectOperator.getNextTuple();
        }
        else if (operator.equals("ScanOperator")) {
            nextTuple = scanOperator.getNextTuple();
        }

        if (!nextTuple.isEmpty()) {
            nextTuple = reorderTuple(nextTuple);
            if (sum) { return nextTuple; }
            else if (returnList.indexOf(nextTuple.toString()) == -1) {
                reorderedTuple = nextTuple;
                returnList.add(reorderedTuple.toString());
            }
        }

        return reorderedTuple;
    }

    /**
     * Reorder tuple to order of head variables
     * 
     * @param tpl   tuple
     * @return      ordered tuple
     */
    private Tuple reorderTuple(Tuple tpl) {
        Tuple newTuple = new Tuple();
        Integer index;
        
        // If no project (head) variables, SumAggregate should exist, return tuple without reordering
        if (projectVariables.size() == 0) {
            return tpl;
        }

        // Add tuple data in order of project (head) variables
        for (int i=0;i<projectVariables.size();i++) {
            index = getRelationalVariables().indexOf(projectVariables.get(i));
            newTuple.add(tpl.get(index));
        }

        return newTuple;
    }

    /**
     * Reset Operator created
     */
    public void reset() {
        if (operator.equals("JoinOperator")) {
            joinOperator.reset();
        }
        else if (operator.equals("SelectOperator")) {
            selectOperator.reset();
        }
        else if (operator.equals("ScanOperator")) {
            scanOperator.reset();
        }
        returnList.clear();
    }

    /**
     * Return unique project(head) variables
     * 
     * @return  project variables
     */
    public List<Term> getProjectVariables() {
        return projectVariables;
    }

}