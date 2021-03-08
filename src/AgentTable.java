import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class AgentTable {

    /**
     * Reads a cvs file for data and adds them to the agent table
     *
     * Does not create the table. It must already be created
     *
     * @param conn: database connection to work with
     * @param fileName
     * @throws SQLException
     */
    public static void populateAgentTableFromCSV(Connection conn,
                                                  String fileName)
            throws SQLException{
        /**
         * Structure to store the data as you read it in
         * Will be used later to populate the table
         */
        ArrayList<Agent> agents = new ArrayList<Agent>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = br.readLine()) != null){
                String[] split = line.split(",");
                agents.add(new Agent(split));
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
        String sql = createAgentInsertSQL(agents);

        /**
         * Create and execute an SQL statement
         *
         * execute only returns if it was successful
         */
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }
    /**
     * Create the agent table with the given attributes
     *
     * @param conn: the database connection to work with
     */
    public static void createAgentTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS agent("
                    + "ID INT PRIMARY KEY AUTO_INCREMENT,"
                    + "NAME VARCHAR(255),"
                    + "PHONE VARCHAR(255),"
                    + "EMAIL VARCHAR(255),"
                    + "ADDRESS VARCHAR(255),"
                    + "SALARY INT,"
                    + "COMMISSIONS INT,"
                    + "MANAGERID INT,"
                    + "OFFICEID INT,"
                    + "CONSTRAINT FKMANAGER FOREIGN KEY (MANAGERID) REFERENCES agent (ID),"
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
    public static void getSalesPerMonth(Connection conn, int id, int month){
        String query = "SELECT sales.price as sales, agent.Name, agent.id as aid, agent.officeID as oid, agent.managerid as mid, " +
                "sales.date as date FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                "INNER JOIN sales ON agentToSales.SALESID = sales.ID where sales.date like '" + month + "/%'";
        try {
            /**
             * create and execute the query
             */
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            String prev = "";
            while(result.next()){
                if (!result.getString(3).equals(prev)){
                    System.out.println("Agent " +result.getString(3) + ", " + result.getString(2) );
                }
                System.out.println("\tSale: $" +result.getString(1) + " on " + result.getString(6));
                prev = result.getString(3);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void getAllAgentsToSales(Connection conn, int id){
        String query = "SELECT sales.price as sales, agent.Name, agent.id as aid, agent.officeID as oid, agent.managerid as mid FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                "INNER JOIN sales ON agentToSales.SALESID = sales.ID";
        String statsQuery = "SELECT aid, AVG(sales) as avgPrice, MAX(sales) as maxSale, COUNT(*) as freq FROM (" + query + ") GROUP BY aid ORDER BY aid;";

        if(id != 0){
            query = "SELECT sales.price as sales, agent.Name, agent.id as aid, agent.officeID as oid, agent.managerid as mid FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                    "INNER JOIN sales ON agentToSales.SALESID = sales.ID WHERE agent.id = " + id;
            statsQuery = "SELECT aid, AVG(sales) as avgPrice, MAX(sales) as maxSale, COUNT(*) as freq FROM (" + query + ") WHERE aid = " + id + " GROUP BY aid ORDER BY aid;";
        }
        try {
            /**
             * create and execute the query
             */
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            Statement stmtStats = conn.createStatement();
            ResultSet resultStats = stmtStats.executeQuery(statsQuery);
            String prev = "";
            while(result.next()){
                if(!result.getString(2).equals(prev)) {
                    if(resultStats.next()) {
                        System.out.println("\nStatistics for Agent " + resultStats.getString(1) + ", " +result.getString(2)+ ": ");
                        System.out.println("Primary Office: "+ result.getString(4));
                        System.out.println("Manager ID: " + result.getString(5));
                        System.out.println("Average sales price for this agent: " + resultStats.getString(2));
                        System.out.println("Max sale price for this agent: " + resultStats.getString(3));
                        System.out.println("Number of sales for this agent: " + resultStats.getString(4));
                    }
                    System.out.println("Sales: ");
                }
                System.out.println("\t" + result.getInt(1));
                prev = result.getString(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getBestOrWorstAgent(Connection conn, boolean best){
        String orderBy = "ASC";
        if(best){
            orderBy = "DESC";
        }
        String query = "SELECT sales.price as salesPrice, agent.id as aid FROM (agentToSales INNER JOIN agent ON " +
                "agentToSales.AGENTID = agent.ID " +
                "INNER JOIN sales ON agentToSales.SALESID = sales.ID)";
        String statsQuery = "SELECT aid, SUM(salesPrice) as sumSales from (" + query + ") GROUP BY aid";
        String s = "SELECT TOP 1 MAX(sumSales) as m, aid from (" + statsQuery + ") GROUP BY aid ORDER BY m "+orderBy +";";
        try {
            /**
             * create and execute the query
             */
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(s);
            while(result.next()){
                if(best) {
                    System.out.println("Agent " + result.getString(2) + " is the highest performing " +
                            "agent, with $" + result.getString(1) + " total sales.");
                }
                else {
                    System.out.println("Agent " + result.getString(2) + " is the lowest performing " +
                            "agent, with $" + result.getString(1) + " total sales.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single agent to the database
     *
     * @param conn
     * @param name
     * @param phone
     * @param email
     * @param address
     * @param salary
     */
    public static void addAgent(Connection conn,
                                String name,
                                String phone,
                                String email,
                                String address,
                                int salary,
                                int commissions,
                                int managerID,
                                int officeID){

        /**
         * SQL insert statement
         */
        String query = String.format("INSERT INTO agent " +
                        "VALUES(default,\'%s\',\'%s\',\'%s\',\'%s\',\'%d\',\'%d\',\'%d\',\'%d\');",
                name, phone, email, address, salary, commissions, managerID, officeID);
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
     * This creates an sql statement to do a bulk add of agents
     *
     * @param agents: list of Agent objects to add
     *
     * @return
     */
    public static String createAgentInsertSQL(ArrayList<Agent> agents){
        StringBuilder sb = new StringBuilder();

        /**
         * The start of the statement,
         * tells it the table to add it to
         * the order of the data in reference
         * to the columns to add it to
         */
        sb.append("INSERT INTO agent (id, NAME, PHONE, EMAIL, ADDRESS, SALARY, COMMISSIONS, MANAGERID, OFFICEID) VALUES");


        for(int i = 0; i < agents.size(); i++){
            Agent a = agents.get(i);
            sb.append(String.format("(%d,\'%s\',\'%s\',\'%s\',\'%s\',\'%d\',\'%d\',\'%d\',\'%d\')",
                   a.getId(), a.getName(), a.getPhone(), a.getEmail(), a.getAddress(),
                    a.getSalary(), a.getCommissions(), a.getManagerID(), a.getOfficeID()));
            if( i != agents.size()-1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Makes a query to the agent table
     * with given columns and conditions
     *
     * @param conn
     * @param columns: columns to return
     * @param whereClauses: conditions to limit query by
     * @return
     */
    public static ResultSet queryAgentTable(Connection conn,
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
        sb.append("FROM agent ");

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
    public static void printAgentTable(Connection conn){
        String query = "SELECT * FROM agent;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while(result.next()){
                printAgent(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        result.getInt(6),
                        result.getInt(7),
                        result.getInt(8),
                        result.getInt(9));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    /* NAME, PHONE, EMAIL, ADDRESS, SALARY, COMMISSIONS, MANAGERID, OFFICEID*/
    public static void printAgent(int id, String name, String phone, String email, String address, int salary,
                                  int commissions, int managerID, int officeID){
        System.out.printf("agent %d:\n\tName: %s\n\tPhone: %s\n\tEmail: %s\n\tAddress: %s\n\tSalary: %d\n\tCommissions: " +
                        "%d\n\tManagerID: %d\n\tOfficeID: %d\n", id, name, phone, email, address, salary, commissions,
                managerID, officeID);
    }
}
