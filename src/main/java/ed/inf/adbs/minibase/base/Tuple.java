package ed.inf.adbs.minibase.base;

import java.util.*;

import ed.inf.adbs.minibase.Utils;

/**
 * The Tuple class manages tuples created from relation data using ScanOperator
 */
public class Tuple {
    
    private List<Term> tuple;

    public Tuple() {
        tuple = new ArrayList<Term>();
    }

    public Tuple(String data, List<String> schema) {
        tuple = new ArrayList<Term>();
        List<String> dataList = Arrays.asList(data.split(", "));
       
        for (int i=0;i<schema.size();i++) {
            if (schema.get(i).equals("int")) {
                IntegerConstant term = new IntegerConstant(Integer.parseInt(dataList.get(i)));
                tuple.add(term);
            }
            else if (schema.get(i).equals("string")) {
                StringConstant term = new StringConstant(dataList.get(i).replace("'", ""));
                tuple.add(term);
            }
        }
    }

    /**
     * Add new term to tuple
     * @param term  term to add
     */
    public void add(Term term) {
        tuple.add(term);
    }

    /**
     * Check if tuple is empty
     * @return  true if empty
     */
    public Boolean isEmpty() {
        return tuple.isEmpty();
    }

    /**
     * Get term in index position i
     * @param i     index
     * @return      term
     */
    public Term get(int i) {
        return tuple.get(i);
    }

    /**
     * Get tuple string value
     */
    @Override public String toString() {
        return Utils.join(tuple, ", ");
    }

}
