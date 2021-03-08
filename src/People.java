public abstract class People
{
    private int id;

    private String name;
    private String address;
    private String email;
    private String phone;

    public People(int id,
                  String name,
                  String address,
                  String email,
                  String phone){
        this.id = id;
        this.name = name;
        this.address = address;
        this.email = email;
        this.phone = phone;
    }

    public int getId (){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }


}
