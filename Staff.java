
//abstract class with getters and staff attributes
//No shared job methods, as each staff role has different responsibilities
//constructor used by subclasses through super()
public abstract class Staff {
    private final int staffId;
    private final String name;

    public Staff(int staffId, String name) {
        if (staffId < 0) {
            throw new IllegalArgumentException("Staff ID cannot be negative.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Staff name cannot be blank.");
        }

        this.staffId = staffId;
        this.name = name;
    }

    //getters
    public int getStaffId() {
        return staffId; 
    }

    public String getName() {
        return name;
    }
}
