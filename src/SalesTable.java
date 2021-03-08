import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SalesTable {
    /**
     * Reads a cvs file for data and adds them to the sales table
     *
     * Does not create the table. It must already be created
     *
     * @param conn: database connection to work with
     * @param fileName
     * @throws SQLException
     */
    public static void populateSalesTableFromCSV(Connection conn,
                                                         String fileName)
            throws SQLException{
        /**
         * Structure to store the data as you read it in
         * Will be used later to populate the table
         */
        ArrayList<Sale> sales = new ArrayList<Sale>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while((line = br.readLine()) != null){
                String[] split = line.split(",");
                sales.add(new Sale(split));
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
        String sql = createSalesInsertSQL(sales);

        /**
         * Create and execute an SQL statement
         *
         * execute only returns if it was successful
         */
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }
    /**
     * Create the sales table with the given attributes
     *
     * @param conn: the database connection to work with
     */
    public static void createSaleTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS sales("
                    + "ID INT PRIMARY KEY,"
                    + "DATE VARCHAR(255),"
                    + "PRICE INT,"
                    + "BUYERID INT,"
                    + "SELLERID INT,"
                    + "OFFICEID INT,"
                    + "LOCATION VARCHAR(255),"
                    + "foreign key (BUYERID) references client,"
                    + "foreign key (SELLERID) references client,"
                    + "foreign key (OFFICEID) references office"
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
     * Adds a single sale to the database
     *
     * @param conn
     * @param id
     */
    public static void addSale(Connection conn,
                                        int id,
                                        String date,
                               int price,
                               int buyerID,
                               int sellerID,
                               int officeID,
                               String location){

        /**
         * SQL insert statement
         */
        String query = String.format("INSERT INTO sales " +
                        "VALUES(%d,\'%s\',\'%d\',\'%d\',\'%d\',\'%d\',\'%s\');",
                id, date, price, buyerID, sellerID, officeID, location);
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
     * This creates an sql statement to do a bulk add of sales
     *
     * @param sales: list of Sales objects to add
     *
     * @return
     */
    public static String createSalesInsertSQL(ArrayList<Sale> sales){
        StringBuilder sb = new StringBuilder();

        /**
         * The start of the statement,
         * tells it the table to add it to
         * the order of the data in reference
         * to the columns to add it to
         */
        sb.append("INSERT INTO sales (id, DATE, PRICE, BUYERID, SELLERID, OFFICEID, LOCATION) VALUES");


        for(int i = 0; i < sales.size(); i++){
            Sale s = sales.get(i);
            sb.append(String.format("(%d,\'%s\',\'%d\',\'%d\',\'%d\',\'%d\',\'%s\')",
                   s.getId(), s.getDate(), s.getPrice(), s.getBuyerID(), s.getSellerID(), s.getOfficeID(), s.getS_location()));
            if( i != sales.size()-1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Makes a query to the sales table
     * with given columns and conditions
     *
     * @param conn
     * @param columns: columns to return
     * @param whereClauses: conditions to limit query by
     * @return
     */
    public static ResultSet querySalesTable(Connection conn,
                                                    ArrayList<String> columns,
                                                    ArrayList<String> whereClauses){
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
        sb.append("FROM sales ");

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
    public static void printSalesTable(Connection conn){
        String query = "SELECT * FROM sales;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while(result.next()){
                System.out.printf("Sale %d: %s %d %d %d %d %s\n",
                        result.getInt(1),
                        result.getString(2),
                        result.getInt(3),
                        result.getInt(4),
                        result.getInt(5),
                        result.getInt(6),
                        result.getString(7));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
