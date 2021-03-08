import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AgentToSalesTable {

    /**
     * Reads a cvs file for data and adds them to the agentToSales table
     *
     * Does not create the table. It must already be created
     *
     * @param conn: database connection to work with
     * @param fileName
     * @throws SQLException
     */
    public static void populateAgentToSalesTableFromCSV(Connection conn,
                                                 String fileName)
            throws SQLException{
        /**
         * Structure to store the data as you read it in
         * Will be used later to populate the table
         */
        ArrayList<AgentToSales> agentToSales = new ArrayList<AgentToSales>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = br.readLine()) != null){
                String[] split = line.split(",");
                agentToSales.add(new AgentToSales(split));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Creates the SQL query to do a bulk add of all people
         * that were read in. This is more efficent then adding one
         * at a time
         */
        String sql = createAgentToSalesInsertSQL(agentToSales);

        /**
         * Create and execute an SQL statement
         *
         * execute only returns if it was successful
         */
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }
    /**
     * Create the agentToSales table with the given attributes
     *
     * @param conn: the database connection to work with
     */
    public static void createAgentToSalesTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS agentToSales("
                    + "AGENTID INT,"
                    + "SALESID INT,"
                    + "PRIMARY KEY(AGENTID, SALESID),"
                    + "FOREIGN KEY (SALESID) REFERENCES sales,"
                    + ");" ;
            /**
             * Create a query and execute
             */
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single agentToSales to the database
     *
     * @param conn
     */
    public static void addAgentToSales(Connection conn,
                                int agentID,
                                int salesID){

        /**
         * SQL insert statement
         */
        String query = String.format("INSERT INTO agentToSales " +
                        "VALUES(%d,\'%d\');",
                agentID, salesID);
        try {
            /**
             * create and execute the query
             */
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This creates an sql statement to do a bulk add of agentToSales
     *
     * @param agentToSales: list of AgentToSales objects to add
     *
     * @return
     */
    public static String createAgentToSalesInsertSQL(ArrayList<AgentToSales> agentToSales){
        StringBuilder sb = new StringBuilder();

        /**
         * The start of the statement,
         * tells it the table to add it to
         * the order of the data in reference
         * to the columns to add it to
         */
        sb.append("INSERT INTO agentToSales (AGENTID, SALESID) VALUES");


        for(int i = 0; i < agentToSales.size(); i++){
            AgentToSales a = agentToSales.get(i);
            sb.append(String.format("(%d,\'%d\')",
                    a.getAgentID(), a.getSalesID()));
            if( i != agentToSales.size()-1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Makes a query to the agentToSales table
     * with given columns and conditions
     *
     * @param conn
     * @param columns: columns to return
     * @param whereClauses: conditions to limit query by
     * @return
     */
    public static ResultSet queryAgentToSalesTable(Connection conn,
                                            ArrayList<String> columns,
                                            ArrayList<String> whereClauses,
                                            ArrayList<String> orderBy){
        StringBuilder sb = new StringBuilder();

        /**
         * Start the select query
         */
        sb.append("SELECT ");

        /**
         * If we gave no columns just give them all to us
         *
         * other wise add the columns to the query
         * adding a comma top seperate
         */
        if(columns.isEmpty()){
            sb.append("* ");
        }
        else{
            for(int i = 0; i < columns.size(); i++){
                if(i != columns.size() - 1){
                    sb.append(columns.get(i) + ", ");
                }
                else{
                    sb.append(columns.get(i) + " ");
                }
            }
        }

        /**
         * Tells it which table to get the data from
         */
        sb.append("FROM agentToSales ");

        /**
         * If we gave it conditions append them
         * place an AND between them
         */
        if(!whereClauses.isEmpty()){
            sb.append("WHERE ");
            for(int i = 0; i < whereClauses.size(); i++){
                if(i != whereClauses.size() -1){
                    sb.append(whereClauses.get(i) + " AND ");
                }
                else{
                    sb.append(whereClauses.get(i));
                }
            }
        }
        if(!orderBy.isEmpty()){
            sb.append("order by ");
            for(int i = 0; i < orderBy.size(); i++){
                if(i != orderBy.size() -1){
                    sb.append(orderBy.get(i) + " AND ");
                }
                else{
                    sb.append(orderBy.get(i));
                }
            }
        }

        /**
         * close with semi-colon
         */
        sb.append(";");

        //Print it out to verify it made it right
        System.out.println("Query: " + sb.toString());
        try {
            /**
             * Execute the query and return the result set
             */
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Queries and print the table
     * @param conn
     */
    public static void printAgentToSalesTable(Connection conn){
        String query = "SELECT * FROM agentToSales;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while(result.next()){
                printAgentToSales(
                        result.getInt(1),
                        result.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    /* NAME, PHONE, EMAIL, ADDRESS, SALARY, COMMISSIONS, MANAGERID, OFFICEID*/
    public static void printAgentToSales(int agentID, int salesID){
        System.out.printf("agentToSales %d:\n\tSales ID: %d\n", agentID, salesID);
    }
}
