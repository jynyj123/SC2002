package rush.control;


import rush.domain.Customer;


//Used to create a customer for a scheduled arrival

@FunctionalInterface
public interface CustomerFactory {
    Customer create(int customerId);
}
