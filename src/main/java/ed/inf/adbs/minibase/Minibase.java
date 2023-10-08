package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The Minibase class parses queries from the input file, evaluates query and writes output into output file
 */
public class Minibase {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

       evaluateCQ(databaseDir, inputFile, outputFile);

    }

    /**
     * Evaluate query by creating appropriate Operator
     * 
     * @param databaseDir   database
     * @param inputFile     input file
     * @param outputFile    output file
     */
    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
        
        try {

            // Set database directory
            Catalog catalog = Catalog.getInstance();
            catalog.setDatabaseDir(databaseDir);

            // Extract query from inputFile
            Query query = QueryParser.parse(Paths.get(inputFile));
            System.out.println();
            System.out.println("Entire query: " + query);

            query = CQMinimizer.minimizeQuery(query);

            Head head = query.getHead();
            List<Term> headVariables = new ArrayList<Term>(head.getVariables());
            List<RelationalAtom> relationalAtoms = Extract.extractRelationalAtoms(query.getBody());
            List<ComparisonAtom> comparisonAtoms = Extract.extractComparisonAtoms(query.getBody());
            System.out.println("Comparison Atoms: " + comparisonAtoms);
            System.out.println();

            if (relationalAtoms.size() == 0){
                System.out.println("No relational atoms found.");
            }
            else {
                // Sum Operator
                if (head.getSumAggregate() != null) {
                    SumOperator sumOperator = new SumOperator(head, relationalAtoms, comparisonAtoms);
                    FileManager.writeDump(outputFile, sumOperator.getDump());
                    System.out.println("Successfully updated file with " + sumOperator.getName());
                }

                // Project Operator
                else if (needsReordering(relationalAtoms,headVariables)) {
                    ProjectOperator projectOperator = new ProjectOperator(head, relationalAtoms, comparisonAtoms);
                    FileManager.writeDump(outputFile, projectOperator.getDump());
                    System.out.println("Successfully updated file with " + projectOperator.getName());
                }

                // Select Operator
                else if (needsSelecting(relationalAtoms, comparisonAtoms)) {
                    SelectOperator selectOperator = new SelectOperator(relationalAtoms.get(0), comparisonAtoms);
                    FileManager.writeDump(outputFile, selectOperator.getDump());
                    System.out.println("Successfully updated file with " + selectOperator.getName());
                }

                // Scan Operator
                else {
                    ScanOperator scanOperator = new ScanOperator(relationalAtoms.get(0));
                    FileManager.writeDump(outputFile, scanOperator.getDump());
                    System.out.println("Successfully updated file with " + scanOperator.getName());
                }
            }
            
            System.out.println();
        }
        catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }

    }

    /**
     * Check if query requires reordering
     * 
     * @param relationalAtoms   RelationalAtoms in query
     * @param headVariables     head variables in query
     * @return                  true if reordering needed
     */
    static Boolean needsReordering(List<RelationalAtom> relationalAtoms, List<Term> headVariables) {
        if (relationalAtoms.size() > 1) {
            return true;
        }
        else if (relationalAtoms.size() == 1 && !relationalAtoms.get(0).getTerms().equals(headVariables)){
            return true;
        }
        return false;
    }

    /**
     * Check if query has selection conditions
     * 
     * @param relationalAtoms       RelationalAtoms in query
     * @param comparisonAtoms       ComparisonAtoms in query
     * @return                      true if selection needed
     */
    static Boolean needsSelecting(List<RelationalAtom> relationalAtoms, List<ComparisonAtom> comparisonAtoms) {
        if (relationalAtoms.size() == 1 && (comparisonAtoms.size() > 0 || Comparison.explicitComparisonExist(relationalAtoms.get(0).getTerms()))) {
            return true;
        }
        return false;
    }
}
