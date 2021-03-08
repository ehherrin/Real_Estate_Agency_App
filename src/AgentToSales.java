public class AgentToSales {
    private int agentID;
    private int salesID;

    public AgentToSales(int agentID, int salesID){
        this.agentID = agentID;
        this.salesID = salesID;
    }

    public AgentToSales(String[] data){
        this(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
    }

    public int getAgentID(){
        return agentID;
    }

    public int getSalesID() {
        return salesID;
    }
}
