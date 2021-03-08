import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LandWithoutHouseTable {
    /**
     * Reads a cvs file for data and adds them to the landWithoutHouse table
     *
     * Does not create the table. It must already be created
     *
     * @param conn: database connection to work with
     * @param f1
     * @throws SQLException
     */
    public static void populateLandWithoutHouseTableFromCSV(Connection conn,
                                                         String f1, String f2)
            throws SQLException{
        /**
         * Structure to store the data as you read it in
         * Will be used later to populate the table
         */
        ArrayList<LandWithoutHouse> landWithoutHouses = new ArrayList<LandWithoutHouse>();
        try {
            BufferedReader br1=new BufferedReader(new InputStreamReader(new FileInputStream(f1)));
            BufferedReader br2=new BufferedReader(new InputStreamReader(new FileInputStream(f2)));
            String line1,line2;

            while ((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                if(line1.charAt(0) == ','){
                    break;
                }
                String split1[] = line1.split(",");
                String split2[] = line2.split(",");
                String realLine = "";
                realLine += split1[0];
                for (int i = 1; i < split2.length; i++) {
                    realLine += "," + split2[i];
                }
                for (int i = 1; i < split1.length; i++) {
                    realLine += ","  + split1[i];
                }
                String[] split = realLine.split(",");
                landWithoutHouses.add(new LandWithoutHouse(split));
                //write this line to the output file
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        /**
         * Creates the SQL query to do a bulk add of all people
         * that were read in. This is more efficent then adding one
         * at a time
         */
        String sql = createLandWithoutHouseInsertSQL(landWithoutHouses);

        /**
         * Create and execute an SQL statement
         *
         * execute only returns if it was successful
         */
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }
    /**
     * Create the landWithoutHouse table with the given attributes
     *
     * @param conn: the database connection to work with
     */
    public static void createLandWithoutHouseTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS landWithoutHouse("
                    + "ID INT PRIMARY KEY,"
                    + "ISFORSALE BOOLEAN,"
                    + "PRICE INT,"
                    + "SALEDATE VARCHAR(255),"
                    + "LOCATION VARCHAR(255),"
                    + "LSIZE INT,"
                    + "LANDCLASS VARCHAR(255),"
                    + "OWNERID INT,"
                    + "FOREIGN KEY (OWNERID) REFERENCES client,"
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
     * Adds a single LandWithoutHouse to the database
     *
     * @param conn
     * @param id
     */
    public static void addLandWithoutHouse(Connection conn,
                                        int id,
                                        boolean isForSale,
                                        int price,
                                        String saleDate,
                                        String location,
                                        int l_size,
                                        String landClass,
                                        int ownerID){

        /**
         * SQL insert statement
         */
        String query = String.format("INSERT INTO landWithoutHouse " +
                        "VALUES(%d,\'%b\',\'%d\',\'%s\',\'%s\',\'%d\',\'%s\',\'%d\');",
                id, isForSale, price, saleDate, location, l_size,landClass, ownerID);
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
     * This creates an sql statement to do a bulk add of landWithoutHouses
     *
     * @param landWithoutHouses: list of landWithoutHouse objects to add
     *
     * @return
     */
    public static String createLandWithoutHouseInsertSQL(ArrayList<LandWithoutHouse> landWithoutHouses){
        StringBuilder sb = new StringBuilder();

        /**
         * The start of the statement,
         * tells it the table to add it to
         * the order of the data in reference
         * to the columns to add it to
         */
        sb.append("INSERT INTO landWithoutHouse (id, ISFORSALE, PRICE, SALEDATE," +
                " LOCATION, LSIZE, LANDCLASS, OWNERID) VALUES");


        for(int i = 0; i < landWithoutHouses.size(); i++){
            LandWithoutHouse lwh = landWithoutHouses.get(i);
            sb.append(String.format("(%d,\'%b\',\'%d\',\'%s\',\'%s\',\'%d\',\'%s\',\'%d\')",
                    lwh.getId(), lwh.isForSale(), lwh.getPrice(), lwh.getSaleDate(), lwh.getLocation(), lwh.getL_size(),
                    lwh.getLandClass(), lwh.getOwnerID()));
            if( i != landWithoutHouses.size()-1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Makes a query to the landWithoutHouse table
     * with given columns and conditions
     *
     * @param conn
     * @param columns: columns to return
     * @param whereClauses: conditions to limit query by
     * @return
     */
    public static ResultSet queryLandWithoutHouseTable(Connection conn,
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
        sb.append("FROM landWithoutHouse ");

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
    public static void printLandWithoutHouseTable(Connection conn){
        String query = "SELECT * FROM landWithoutHouse;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while(result.next()){
                System.out.printf("landWithoutHouse %d: %b %d %s %s %d %s %d\n",
                        result.getInt(1),
                        result.getBoolean(2),
                        result.getInt(3),
                        result.getString(4),
                        result.getString(5),
                        result.getInt(6),
                        result.getString(7),
                        result.getInt(8));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
