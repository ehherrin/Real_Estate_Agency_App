import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class LandWithHouseTable {
    /**
     * Reads a cvs file for data and adds them to the landWithoutHouse table
     *
     * Does not create the table. It must already be created
     *
     * @param conn: database connection to work with
     * @throws SQLException
     */
    public static void populateLandWithHouseTableFromCSV(Connection conn,
                                                 String f1, String f2)
            throws SQLException{
        /**
         * Structure to store the data as you read it in
         * Will be used later to populate the table
         */
        ArrayList<LandWithHouse> landWithHouses = new ArrayList<LandWithHouse>();

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
                landWithHouses.add(new LandWithHouse(split));
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
        String sql = createLandWithHouseInsertSQL(landWithHouses);

        /**
         * Create and execute an SQL statement
         *
         * execute only returns if it was successful
         */
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }
    /**
     * Create the landWithHouse table with the given attributes
     *
     * @param conn: the database connection to work with
     */
    public static void createLandWithHouseTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS landWithHouse("
                    + "ID INT PRIMARY KEY,"
                    + "ISFORSALE BOOLEAN,"
                    + "PRICE INT,"
                    + "SALEDATE VARCHAR(255),"
                    + "LOCATION VARCHAR(255),"
                    + "LSIZE INT,"
                    + "BEDCOUNT INT,"
                    + "BATHCOUNT INT,"
                    + "HSIZE INT,"
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
     * Adds a single LandWithHouse to the database
     *
     * @param conn
     * @param id
     */
    public static void addLandWithHouse(Connection conn,
                                int id,
                                boolean isForSale,
                                        int price,
                                        String saleDate,
                                        String location,
                                        int l_size,
                                        int bedCount,
                                        int bathCount,
                                        int h_size,
                                        int ownerID){

        /**
         * SQL insert statement
         */
        String query = String.format("INSERT INTO landWithHouse " +
                        "VALUES(%d,\'%b\',\'%d\',\'%s\',\'%s\',\'%d\',\'%d\',\'%d\',\'%d\',\'%d\');",
                id, isForSale, price, saleDate, location, l_size, bedCount, bathCount, h_size, ownerID);
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
     * This creates an sql statement to do a bulk add of landWithHouses
     *
     * @param landWithHouses: list of landWithHouse objects to add
     *
     * @return
     */
    public static String createLandWithHouseInsertSQL(ArrayList<LandWithHouse> landWithHouses){
        StringBuilder sb = new StringBuilder();

        /**
         * The start of the statement,
         * tells it the table to add it to
         * the order of the data in reference
         * to the columns to add it to
         */
        sb.append("INSERT INTO landWithHouse (id, ISFORSALE, PRICE, SALEDATE," +
                " LOCATION, LSIZE, BEDCOUNT, BATHCOUNT, HSIZE, OWNERID) VALUES");


        for(int i = 0; i < landWithHouses.size(); i++){
            LandWithHouse lwh = landWithHouses.get(i);
            sb.append(String.format("(%d,\'%b\',\'%d\',\'%s\',\'%s\',\'%d\',\'%d\',\'%d\',\'%d\',\'%d\')",
                    lwh.getId(), lwh.isForSale(), lwh.getPrice(), lwh.getSaleDate(), lwh.getLocation(), lwh.getL_size(),
                    lwh.getBedCount(), lwh.getBathCount(), lwh.getH_size(), lwh.getOwnerID()));
            if( i != landWithHouses.size()-1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Makes a query to the landWithHouse table
     * with given columns and conditions
     *
     * @param conn
     * @param columns: columns to return
     * @param whereClauses: conditions to limit query by
     * @return
     */
    public static ResultSet queryLandWithHouseTable(Connection conn,
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
        sb.append("FROM landWithHouse ");

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
    public static void printLandWithHouseTable(Connection conn){
        String query = "SELECT * FROM landWithHouse;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);

            while(result.next()){
                printLandWithHouse(
                        result.getInt(1),
                        result.getBoolean(2),
                        result.getInt(3),
                        result.getString(4),
                        result.getString(5),
                        result.getInt(6),
                        result.getInt(7),
                        result.getInt(8),
                        result.getInt(9),
                        result.getInt(10));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void printLandWithHouse(int id, boolean isforsale, int price, String saledate, String location,
                                          int lsize, int bedcount, int bathcount, int hsize, int ownerID){
        System.out.printf("landWithHouse %d:\n\tIsForSale: %b\n\tPrice: %d\n\tSale Date: %s\n\tLocation: %s\n\t" +
                        "Land Footage: %d\n\tNumber of Beds: %d\n\tNumber of Bathrooms: %d\n\tHouse Footage: %d\n\t" +
                        "OwnerID: %d\n",
                id, isforsale, price, saledate, location, lsize, bedcount, bathcount, hsize, ownerID);

    }

}
