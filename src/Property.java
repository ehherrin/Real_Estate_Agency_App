import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Property {
    private int id;
    private int price;
    private int l_size;
    private int ownerID;

    private boolean isForSale;
    private String saleDate;
    private String location;

    public Property(int id,
                    int price,
                    int l_size,
                    boolean isForSale,
                    String saleDate,
                    String location,
                    int ownerID){
        this.id = id;
        this.price = price;
        this.l_size = l_size;
        this.isForSale = isForSale;
        this.location = location;
        this.ownerID = ownerID;
        if (saleDate.equals("N/A")){
            this.saleDate = null;
        }
        else {
            this.saleDate = saleDate;
        }
        /*else {
            DateFormat df = new SimpleDateFormat("MM/DD/YYYY");
            try {
                this.saleDate = df.parse(saleDate);
                System.out.println(saleDate);
                System.out.println(this.saleDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }*/
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public int getL_size() {
        return l_size;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public boolean isForSale() {
        return isForSale;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public String getLocation() {
        return location;
    }
}
