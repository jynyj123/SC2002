package rush.control;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import rush.domain.Customer;
import rush.domain.CustomerState;
import rush.domain.Order;
import rush.domain.OrderStatus;
import rush.domain.Table;


/**
 * Stores the current state of the game
 * This includes the turn number, tables and all customers who have arrived
 * Other information such as revenue and queue position is calculated from the customers
 */
public class GameState {

    // Current turn number
    private int turn;

    // Tables in the restaurant
    private final List<Table> tables = new ArrayList<>();

    // Every customer who has arrived so far
    private final List<Customer> allCustomers = new ArrayList<>();


    // Creates the required number of tables
    public GameState(int tableCount) {
        for (int i = 1; i <= tableCount; i++) tables.add(new Table(i));
    }


    // TURN

    public int getTurn() { return turn; }

    public void setTurn(int turn) { this.turn = turn; }


    // CUSTOMERS

    // Returns all customers, but does not allow the list to be changed from outside
    public List<Customer> getAllCustomers() { return Collections.unmodifiableList(allCustomers); }


    /**
     * Adds a new customer to the game
     * New customers start waiting in the queue
     */
    public void addArrival(Customer c) { allCustomers.add(c); }


    /**
     * Finds a customer using their ID
     */
    public Optional<Customer> findCustomer(int customerId) {
        return allCustomers.stream().filter(c -> c.getId() == customerId).findFirst();
    }


    /**
     * Returns customers who are still waiting for a table
     * They are returned in the order they arrived
     */
    public List<Customer> getWaitingQueue() {
        return allCustomers.stream()
                .filter(c -> c.getState() == CustomerState.WAITING_IN_QUEUE)
                .toList();
    }


    // TABLES

    // Returns all tables, but does not allow the list to be changed from outside
    public List<Table> getTables() { return Collections.unmodifiableList(tables); }


    /**
     * Finds the first table that is not being used
     */
    public Optional<Table> findFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst();
    }


    // ORDERS

    /**
     * Returns every order taken so far, including cancelled orders
     */
    public List<Order> getOrders() {
        return allCustomers.stream()
                .map(Customer::getOrder)
                .filter(order -> order != null)
                .toList();
    }


    /**
     * Calculates the total revenue from customers who have paid
     */
    public int getRevenueCents() {
        return allCustomers.stream()
                .filter(c -> c.getState() == CustomerState.PAID)
                .mapToInt(c -> c.getOrder().getPriceCents())
                .sum();
    }


    /**
     * Counts how many customers have paid
     */
    public int getPaidCount() {
        return (int) allCustomers.stream().filter(c -> c.getState() == CustomerState.PAID).count();
    }


    /**
     * Counts how many orders have been served
     * Paid orders are also counted because they were served before payment
     */
    public int getServedCount() {
        return (int) getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.SERVED || o.getStatus() == OrderStatus.PAID)
                .count();
    }


    /**
     * Counts how many customers left because their satisfaction reached zero
     */
    public int getUnhappyDepartures() {
        return (int) allCustomers.stream().filter(c -> c.getState() == CustomerState.LEFT).count();
    }


    /**
     * Calculates the average satisfaction of customers who paid
     * Returns empty if no one has paid yet
     */
    public OptionalDouble getAverageSatisfaction() {
        return allCustomers.stream()
                .filter(c -> c.getState() == CustomerState.PAID)
                .mapToInt(Customer::getSatisfactionAtPayment)
                .average();
    }
}