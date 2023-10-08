package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.*;
import org.antlr.v4.runtime.misc.Pair;

/**
 * The CQMinimizer class parses queries from the input file and write the minimized query into the output file. 
 */
public class CQMinimizer {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);

    }

    /**
     * Extract query from input file and write minimized query into output file
     * 
     * @param inputFile     input file containing query
     * @param outputFile    output file to write minimized query
     */

    public static void minimizeCQ(String inputFile, String outputFile) {

        try {

            // Extract query
            Query query = QueryParser.parse(Paths.get(inputFile));
            System.out.println();
            System.out.println("Entire query: " + query);

            // Minimize query
            Query finalQuery = minimizeQuery(query);
            System.out.println("Minimized query: " + finalQuery);
            System.out.println();

            // Write minimized query
            FileManager.writeFile(outputFile, finalQuery.toString());
            
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }

    }

    /**
     * Minimize the query and return the minimized query
     * 
     * @param query     initial query
     * @return          minimized query
     * 
     * This method performs CQ minimization on a query object.
     */


    public static Query minimizeQuery(Query query) {

        // Extraction of head variables and body atoms
        Head head = query.getHead();
        List<Atom> body = query.getBody();
        List<Term> headVariables = Extract.extractHeadVariables(head);
        List<RelationalAtom> relationalAtoms = Extract.extractRelationalAtoms(body);
        List<ComparisonAtom> comparisonAtoms = Extract.extractComparisonAtoms(body);

        // Form substitution list
        List<Pair<Term,Term>> subList = Substitution.getSubList(headVariables, relationalAtoms);

        if (subList.size() > 0) {
            System.out.println("Substitution list: " + subList);

            // Minimize query body by performing substitutions
            List<Atom> newBody = minimizeQueryBody(relationalAtoms, subList);
            newBody.addAll(comparisonAtoms);
    
            // Create and return minimized query
            Query finalQuery = new Query(head, newBody);
            return finalQuery;
        }

        // Return original query if no substitutions
        else { return query; }

    }

    /**
     * Minimize query body by removing RelationalAtoms with terms in the substitution list
     * 
     * @param relationalNames   RelationalAtom names from query
     * @param relationalTerms   RelationalAtom terms from query
     * @param subList           term substitution list
     * @return                  minimized query body
     */

    public static List<Atom> minimizeQueryBody(List<RelationalAtom> relationalAtoms,List<Pair<Term,Term>> subList) {

        // Get terms in substitution list
        List<Term> termsToSubstitute= new ArrayList<Term>();
        for (Pair<Term,Term> p : subList) { termsToSubstitute.add(p.a); }

        List<String> relationalNames = Extract.extractRelationalNames(relationalAtoms);
        List<List<Term>> relationalTerms = Extract.extractRelationalTerms(relationalAtoms);

        // Create new query body
        List<Atom> newBody = new ArrayList<Atom>();

        // Iterate through terms of each RelationalAtoms in query
        for (List<Term> terms : relationalTerms) {
            boolean add = true;
            for (Term t: terms) {

                // If RelationalAtom contains term in substitution list, do not add it to the new body
                if (termsToSubstitute.contains(t)) {
                    add = false;
                    break;
                }
            }

            // Add RelationalAtoms that do not contain substituted terms
            if (add) {
                int i = relationalTerms.indexOf(terms);
                String atomName = relationalNames.get(i);
                List<Term> atomTerms = terms;
                RelationalAtom atom = new RelationalAtom(atomName, atomTerms);
                newBody.add(atom);
            }
        }

        return newBody;
    }

}
