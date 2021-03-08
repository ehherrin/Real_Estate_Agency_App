
public class LandWithoutHouse extends Property {

    private String landClass;

    public LandWithoutHouse(int id,
                         int price,
                         int l_size,
                         boolean isForSale,
                         String saleDate,
                         String location,
                         String landClass,
                         int ownerID) {
        super(id, price, l_size, isForSale, saleDate, location, ownerID);
        this.landClass = landClass;
    }

    public LandWithoutHouse(String[] data){
        this(Integer.parseInt(data[0]), Integer.parseInt(data[4]),Integer.parseInt(data[6]),
                Boolean.parseBoolean(data[1]), data[5], data[2]+data[3], data[8],
                Integer.parseInt(data[7]));
    }

    public String getLandClass() {
        return landClass;
    }
}
