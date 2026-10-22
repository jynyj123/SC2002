package rush.domain;

/** Table: Occupied from seating until the customer pays or leaves. */
public class Table {
    private final int id;
    private Customer occupant;

    public Table(int id) { this.id = id; }

    public int getId() { return id; }
    public boolean isFree() { return occupant == null; }
    public Customer getOccupant() { return occupant; }

    /** Seats a customer. Caller must check isFree() first. */
    public void seat(Customer customer) {
        if (!isFree()) throw new IllegalStateException("Table " + id + " is occupied");
        this.occupant = customer;
    }

    /** Frees the table. */
    public void release() { this.occupant = null; }
}
