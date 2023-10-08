package ed.inf.adbs.minibase.base;

import java.io.File;
import java.io.FileNotFoundException;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * The ScanOperator class scans and returns data from files
 */
public class ScanOperator extends Operator {

    private File file;
    private Scanner sc;
    private List<String> schema;

    public ScanOperator(RelationalAtom relationalAtom) {
        super(relationalAtom);
        schema = Catalog.getInstance().getSchema(this.getName());

        String fileName = Catalog.getInstance().getCSVFile(relationalAtom.getName());
        try {
            file = new File(fileName);
            sc = new Scanner(file);
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    } 

    /**
     * Get next tuple in file
     */
    public Tuple getNextTuple() {
        Tuple tpl;
        if (sc.hasNextLine()) {
            String data = sc.nextLine();
            tpl = new Tuple(data, schema);
            return tpl;
        }
        else { 
            tpl = new Tuple();
            return tpl; 
        }
    }

    /**
     * Reset Scanner
     */
    public void reset() {
        sc.close();
        try {
            sc = new Scanner(file);
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
