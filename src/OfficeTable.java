import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class OfficeTable {
    public static void populateOfficeTableFromCSV(Connection conn,
                                                  String fileName) throws SQLException {

        ArrayList<Office> offices = new ArrayList<Office>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null){
                String[] split = line.split(",");
                offices.add(new Office(split));
            }
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        String sql = createOfficeInsertSQL(offices);

        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    public static void getSalesAtOffices(Connection conn, int id){
        try {
            String agentsToSales = "(SELECT agent.ID as aid, sales.price as sid, sales.officeid as oid, agent.name " +
                    "as name FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                    "INNER JOIN sales ON agentToSales.SALESID = sales.ID)";
            String offices = "SELECT office.ID as ofid, aid, sid, oid, name, office.managerID as mid FROM " + agentsToSales + " INNER JOIN office ON office.ID = oid";
            String query = "SELECT ofid, aid, sid, name, mid FROM (" + offices + ")  ORDER BY ofid;";
            String statsQuery = "SELECT ofid, AVG(sid) as avgPrice, MAX(sid) as maxSale, COUNT(*) as freq FROM (" + offices + ") GROUP BY ofid ORDER BY ofid;";

            if(id != 0){
                agentsToSales = "(SELECT agent.ID as aid, sales.price as sid, sales.officeid as oid, agent.name " +
                        "as name FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                        "INNER JOIN sales ON agentToSales.SALESID = sales.ID WHERE sales.officeid = " + id + ")";
                offices = "SELECT office.ID as ofid, aid, sid, oid, name, office.managerID as mid FROM " + agentsToSales + " INNER JOIN office ON office.ID = oid";
                query = "SELECT ofid, aid, sid, name, mid FROM (" + offices + ")  ORDER BY ofid;";
                statsQuery = "SELECT ofid, AVG(sid) as avgPrice, MAX(sid) as maxSale, COUNT(*) as freq FROM (" + offices + ")  GROUP BY ofid ORDER BY ofid;";
            }
;
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            Statement stmtStats = conn.createStatement();
            ResultSet resultStats = stmtStats.executeQuery(statsQuery);

            String prev = "";
            while(result.next()){
                String oid = result.getString(1);
                if(!oid.equals(prev)) {
                    if(resultStats.next()) {
                        System.out.println("\nStatistics for Office " + resultStats.getString(1) + ": ");
                        System.out.println("Average sales price for this office: " + resultStats.getString(2));
                        System.out.println("Max sale price for this office: " + resultStats.getString(3));
                        System.out.println("Number of sales for this office: " + resultStats.getString(4));
                        System.out.println("Manager Id: " + result.getString(5));
                    }
                    System.out.println("\nSales for Office "+ oid + ": ");
                }
                System.out.println("\tAgent " +result.getString(2) + ", " + result.getString(4)+ ", made a sale for $" +result.getString(3));
                prev = oid;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void getBestOrWorstOffice(Connection conn, boolean best){
        String orderBy = "ASC";
        if(best){
            orderBy = "DESC";
        }
        // JFC - you can definitely simplify. I am sorry for the crimes upon the eyes that this inflicts.
        String agentsToSales = "(SELECT agent.ID as aid, sales.price as sid, sales.officeid as oid, agent.name " +
                "as name FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                "INNER JOIN sales ON agentToSales.SALESID = sales.ID)";
        String offices = "SELECT office.ID as ofid, aid, sid, oid, name FROM " + agentsToSales + " INNER JOIN office ON office.ID = oid";
        String query = "SELECT ofid, aid, sid, name FROM (" + offices + ")  ORDER BY ofid";

        String statsQuery = "SELECT ofid, SUM(sid) as sumSales from (" + query + ") GROUP BY ofid";
        String s = "SELECT TOP 1 MAX(sumSales) as m, ofid from (" + statsQuery + ") GROUP BY ofid ORDER BY m "+orderBy +";";
        try {
            /**
             * create and execute the query
             */
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(s);
            while(result.next()){
                if(best) {
                    System.out.println("Office " + result.getString(2) + " is the highest performing " +
                            "office, with $" + result.getString(1) + " total sales.");
                }
                else {
                    System.out.println("Office " + result.getString(2) + " is the lowest performing " +
                            "office, with $" + result.getString(1) + " total sales.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void getSalesPerMonth(Connection conn, int month){
        String agentsToSales = "(SELECT agent.ID as aid, sales.price as sid, sales.officeid as oid, agent.name " +
                "as name, sales.date as salesDate FROM agentToSales INNER JOIN agent ON agentToSales.AGENTID = agent.ID " +
                "INNER JOIN sales ON agentToSales.SALESID = sales.ID where sales.date like '" + month + "/%')";
        String offices = "SELECT office.ID as ofid, aid, sid, oid, name, office.managerID as mid, salesDate FROM " + agentsToSales + " INNER JOIN office ON office.ID = oid";
        String query = "SELECT ofid, aid, sid, name, mid, salesDate FROM (" + offices + ")  ORDER BY ofid;";

        try {
            /**
             * create and execute the query
             */
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            String prev="";
            while(result.next()){
                String oid = result.getString(1);
                if(!oid.equals(prev)) {
                    System.out.println("\nSales for Office "+ oid + ": ");
                }
                System.out.println("\tAgent " +result.getString(2) + ", " + result.getString(4)+ ", made a sale for $" +result.getString(3) + " on " + result.getString(6));
                prev = oid;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllAgentsAtOffice(Connection conn, int id){
        try {
            String query = "SELECT * FROM office AS o RIGHT JOIN agent AS a ON o.ID = a.OFFICEID where o.ID = " + id +";" ;
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                AgentTable.printAgent(
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
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    public static void createOfficeTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS office("
                    + "ID INT PRIMARY KEY,"
                    + "LOCATION VARCHAR(255),"
                    + "ADDRESS VARCHAR(255),"
                    + "MANAGERID INT,"
                    + "foreign key (MANAGERID) references agent"
                    + ");" ;
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void addOffice(Connection conn,
                                 int id,
                                 String location,
                                 String address,
                                 int managerID) {
        String query = String.format("INSERT INTO office "
                        + "VALUES(%d,\'%s\',\'%s\',\'%d\');",
                id, location, address, managerID);
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String createOfficeInsertSQL(ArrayList<Office> offices) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO office (id, LOCATION, ADDRESS, MANAGERID) VALUES");
        for (int i = 0; i < offices.size(); i++) {
            Office o = offices.get(i);
            sb.append(String.format("(%d,\'%s\',\'%s\',\'%d\')",
                    o.getId(), o.getLocation(), o.getAddress(), o.getManagerID()));
            if (i != offices.size() - 1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }
    public static ResultSet queryOfficeTable(Connection conn,
                                             ArrayList<String> columns,
                                             ArrayList<String> whereClauses,
                                             ArrayList<String> orderBy){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if(columns.isEmpty()){
            sb.append("* ");
        }
        else {
            for (int i = 0; i <columns.size() ; i++) {
                if(i != columns.size() - 1){
                    sb.append(columns.get(i) + ", ");
                }
                else{
                    sb.append(columns.get(i) + " ");
                }

            }
        }
        sb.append("FROM office ");
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

    public static void printOfficeTable(Connection conn) {
        String query = "SELECT * FROM office;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                printOffice(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getInt(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printOffice(int id, String location, String address, int managerID){
        System.out.printf("Office %d:\n\tLocation: %s\n\tAddress: %s\n\tManagerID: %d\n", id, location, address, managerID);
    }
}
