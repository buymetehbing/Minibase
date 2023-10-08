package ed.inf.adbs.minibase.base;

import java.util.*;

/**
 * The Catalog singleton class returns relational schema and file names
 */
public class Catalog {

    private String CSVDirectory;
    private String schemaFile;
    private static Catalog instance;

    private Catalog() {
    }

    /**
     * Return instance of Catalog class
     * @return  Catalog instance
     */
    public static Catalog getInstance() {
        if (instance == null) {
            instance = new Catalog();
        }
        return instance;
    }

    /**
     * Set database directory for catalog
     * 
     * @param databaseDir   database directory
     */
    public void setDatabaseDir(String databaseDir) {
        this.CSVDirectory = databaseDir + "/files";
        this.schemaFile = databaseDir + "/schema.txt";
    }


    /**
     * Get CSV file name for a relation
     * 
     * @param relationName      RelationalAtom name
     * @return                  CSV file name
     */
    public String getCSVFile(String relationName) {
        String relationFile = String.format("/%s.csv",relationName);
        return CSVDirectory.concat(relationFile);
    }

    /**
     * Get schema for a relation
     * 
     * @param relationName      RelationalAtom name
     * @return                  relation schema
     */
    public List<String> getSchema(String relationName) {
        List<String> schema = Arrays.asList(FileManager.readSchemaFile(schemaFile, relationName).split(" "));
        return schema;
    }

}
