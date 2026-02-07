package Users;

public abstract class ElectionStaff extends User {
    private String staffID;
    private static int sIDcounter = 100;

    public ElectionStaff(String name) {
        super(name);
        this.staffID = "STAFF" + sIDcounter++;
    }

    public String getStaffID() {
        return staffID;
    }
} 