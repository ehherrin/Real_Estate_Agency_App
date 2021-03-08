import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ClientTable {
    public static void populateClientTableFromCSV(Connection conn,
                                                  String fileName) throws SQLException {

        ArrayList<Client> clients = new ArrayList<Client>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null){
                String[] split = line.split(",");
                clients.add(new Client(split));
            }
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        String sql = createClientInsertSQL(clients);

        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    public static void createClientTable(Connection conn){
        try {
            String query = "CREATE TABLE IF NOT EXISTS client("
                    + "ID INT PRIMARY KEY,"
                    + "NAME VARCHAR(255),"
                    + "PHONE VARCHAR(255),"
                    + "EMAIL VARCHAR(255),"
                    + "ADDRESS VARCHAR(255),"
                    + "REFERRALNO INT,"
                    + ");" ;
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void addClient(Connection conn,
                                 int id,
                                 String name,
                                 String phone,
                                 String email,
                                 String address,
                                 int referralNo) {
        String query = String.format("INSERT INTO client " +
                "VALUES(%d,\'%s\',\'%s\',\'%s\',\'%s\',\'%d\');",
                id, name, phone, email, address, referralNo);
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String createClientInsertSQL(ArrayList<Client> clients) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO client (id, NAME, PHONE, EMAIL, ADDRESS, REFERRALNO) VALUES");
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            sb.append(String.format("(%d,\'%s\',\'%s\',\'%s\',\'%s\',\'%d\')",
                    c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(), c.getReferralNo()));
            if (i != clients.size() - 1){
                sb.append(",");
            }
            else{
                sb.append(";");
            }
        }
        return sb.toString();
    }
    public static ResultSet queryClientTable(Connection conn,
                                             ArrayList<String> columns,
                                             ArrayList<String> whereClauses){
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
        sb.append("FROM client ");
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

    public static void printClientTable(Connection conn) {
        String query = "SELECT * FROM client;";
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while(result.next()){
                System.out.printf("Client %d: %s\t%s\t%s\t%s\t%d\n",
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        result.getInt(6));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
