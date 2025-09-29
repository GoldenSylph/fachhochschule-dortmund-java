package problemset;
import java.util.ArrayList;
import java.util.List;

public class Task2UML {
	 public static void main(String[] args) {
	       
	        AGV agv1 = new AGV("AGV1", 80.0, 5.0, 120, "Station A", 10.0f, 8.5f);
	        AGV agv2 = new AGV("AGV2", 90.0, 4.0, 100, "Station B", 12.0f, 9.0f);
	        AGV agv3 = new AGV("AGV3", 70.0, 6.0, 90,  "Station C", 11.0f, 7.5f);

	        
	        IOperation op1 = new IOperation("OP1", "Transport materials", 300);
	        op1.addResource(agv1);
	        IOperation op2 = new IOperation("OP2", "Load machine", 200);
	        op2.addResource(agv2);
	        IOperation op3 = new IOperation("OP3", "Unload materials", 250);
	        op3.addResource(agv1);
	        op3.addResource(agv3);
	        IOperation op4 = new IOperation("OP4", "Move parts", 150);
	        op4.addResource(agv2);

	     
	        IndustrialProcess p1 = new IndustrialProcess("P1");
	        p1.addOperation(op1);
	        p1.addOperation(op2);
	        IndustrialProcess p2 = new IndustrialProcess("P2");
	        p2.addOperation(op3);
	        p2.addOperation(op4);
	        
	        simulateProcess(p1);
	        simulateProcess(p2);
	    }

	    static void simulateProcess(IndustrialProcess p) {
	        System.out.println("Process: " + p.getID());
	        System.out.println("Total Duration: " + p.processDuration());
	        System.out.println("AGV count: " + p.processResources().size());
	        System.out.println("AGVs (full data):");
	        for (AGV a : p.processResources()) {
	            System.out.println("  " + a.getData()); 
	        }
	        System.out.println("Total Energy Consumption : " 
	                           + p.totalEnergyConsumption());
	        System.out.println("");
	    }
	}

	class IndustrialProcess {
	    private String ID;
	    private List<IOperation> operations;

	    public IndustrialProcess(String ID) {
	        this.ID = ID;
	        this.operations = new ArrayList<>();
	    }

	    public void addOperation(IOperation op) {
	        operations.add(op);
	    }
	    
	    public long processDuration() {
	        long total = 0;
	        for (IOperation op : operations) {
	            total += op.getDuration();
	        }
	        return total;
	    }

	    public List<AGV> processResources() {
	        List<AGV> resources = new ArrayList<>();
	        for (IOperation op : operations) {
	            for (AGV agv : op.getResources()) {
	                if (!resources.contains(agv)) {
	                    resources.add(agv);
	                }
	            }
	        }
	        return resources;
	    }

	    public double totalEnergyConsumption() {
	        double total = 0.0;
	        for (IOperation op : operations) {
	            double hours = op.getDuration() / 60.0;
	            for (AGV agv : op.getResources()) {
	                total += agv.getConsumption() * hours;
	            }
	        }
	        return total;
	    }

	    public String getID() {
	        return ID;
	    }

	}

	class IOperation {
	    private String ID;
	    private String description;
	    private long nominalTime; 
	    private List<AGV> resources;

	    public IOperation(String ID, String description, long nominalTime) {
	        this.ID = ID;
	        this.description = description;
	        this.nominalTime = nominalTime;
	        this.resources = new ArrayList<>();
	    }

	  
	    public void addResource(AGV agv) {
	        resources.add(agv);
	    }

	    public void setData(String description, long nominalTime) {
	        this.description = description;
	        this.nominalTime = nominalTime;
	    }

	    public String getData() {
	        return "Operation[ID=" + ID + ", Desc=" + description + ", Time=" + nominalTime + "]";
	    }

	    public long getDuration() {
	        return nominalTime;
	    }

	    public List<AGV> getResources() {
	        return resources;
	    }
	}



	class AGV {
	    private String ID;
	    private double batteryLoad;
	    private double consumption; 
	    private long chargingTime;
	    private String position;
	    private float maxSpeed;
	    private float actSpeed;

	    public AGV(String ID, double batteryLoad, double consumption,
	               long chargingTime, String position, float maxSpeed, float actSpeed) {
	        this.ID = ID;
	        this.batteryLoad = batteryLoad;
	        this.consumption = consumption;
	        this.chargingTime = chargingTime;
	        this.position = position;
	        this.maxSpeed = maxSpeed;
	        this.actSpeed = actSpeed;
	    }

	    public void setData(double batteryLoad, double consumption,
	                        long chargingTime, String position, float maxSpeed, float actSpeed) {
	        this.batteryLoad = batteryLoad;
	        this.consumption = consumption;
	        this.chargingTime = chargingTime;
	        this.position = position;
	        this.maxSpeed = maxSpeed;
	        this.actSpeed = actSpeed;
	    }

	    public String getData() {
	        return "AGV[ID=" + ID + ", Battery=" + batteryLoad + ", Cons=" + consumption
	                + ", ChargeTime=" + chargingTime + ", Pos=" + position
	                + ", MaxSpeed=" + maxSpeed + ", ActSpeed=" + actSpeed + "]";
	    }

	  
	    public double getConsumption() {
	        return consumption;
	    }

	    public String getID() {
	        return ID;
	    }

}
