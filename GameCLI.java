            customer.decreaseSatisfaction();
            if (customer.isAbandoning()) {
                abandonCustomer(customer);
            }
        }
    }

    // Reads a valid waiter action (0 to 3).
    private int readWaiterChoice() {
        while (true) {
            cli.displayWaiterActions();
            int choice = cli.getWaiterChoice();
            if (choice >= WAITER_WAIT && choice <= WAITER_SERVE) {
                return choice;
            }
            cli.displayError(
                    "Choose 0 to wait, 1 to seat, 2 to take an order, or 3 to serve.");
        }
    }

    // Finds the selected customer and carries out the waiter action.
    private void executeWaiterChoice(int action, int customerId) {
        if (action == WAITER_WAIT) {
            cli.displayMessage("The waiter waits.");
            return;
        }

        Customer customer = restaurant.findCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("No customer has id " + customerId + ".");
        }

        if (action == WAITER_SEAT) {
            seatCustomer(customer);
        } else if (action == WAITER_ORDER) {
            takeCustomerOrder(customer);
        } else if (action == WAITER_SERVE) {
            serveCustomer(customer);
        }
    }

    private void seatCustomer(Customer customer) {
        Table table = restaurant.getAvailableTable();
        if (table == null) {
            throw new IllegalStateException("No table is available.");
        }
        restaurant.getWaiter().seatCustomer(customer, table, currentTurn);
        restaurant.removeFromWaitingQueue(customer);
        cli.displayMessage("The waiter seated " + customer.getName()
                + " at table " + table.getTableId() + ".");
    }

    private void takeCustomerOrder(Customer customer) {
        MenuItem item = restaurant.selectMenuItem(customer);
        Order order = restaurant.getWaiter().takeOrder(customer, item, currentTurn);
        cli.displayMessage(customer.getName() + " ordered " + item.getName()
                + " (" + GameCLI.formatMoney(item.getPriceCents())
                + ", " + item.getPreparationUnits() + " preparation unit"
                + (item.getPreparationUnits() == 1 ? "" : "s")
                + "). Order id " + order.getOrderId() + ".");
    }

    private void serveCustomer(Customer customer) {
        restaurant.getWaiter().serveOrder(customer, currentTurn);
        restaurant.recordService();
        cli.displayMessage("The waiter served " + customer.getName()
                + ". The customer is now eating.");
    }

    private void abandonCustomer(Customer customer) {
        Order order = customer.getOrder();
        boolean cancelledOrder = false;
        if (order != null
                && (order.getStatus() == OrderStatus.PLACED
                || order.getStatus() == OrderStatus.PREPARING
                || order.getStatus() == OrderStatus.READY)) {
            order.cancel();
            cancelledOrder = true;
        }

        String tableNote = "";
        if (customer.getStatus() == CustomerStatus.WAITING) {
            restaurant.removeFromWaitingQueue(customer);
        } else if (customer.getStatus() == CustomerStatus.SEATED) {
            Table table = restaurant.findTableByCustomer(customer);
            if (table != null) {
                int tableId = table.getTableId();
                table.release();
                tableNote = " Table " + tableId + " is free.";
            }
        }

        customer.leave();
        restaurant.recordAbandonment();
        String orderNote = "";
        if (cancelledOrder) {
            orderNote = " Order " + order.getOrderId() + " was cancelled.";
        }
        cli.displayMessage(customer.getName()
                + " left unhappy (satisfaction 0)." + orderNote + tableNote);
    }

    // Builds the list of chef actions for orders needing preparation.
    private ArrayList<String> chefActionLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("0. Wait");
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {
            Customer customer = restaurant.getCustomers().get(i);
            Order order = customer.getOrder();
            if (order == null) {
                continue;
            }
            if (order.getStatus() != OrderStatus.PLACED
                    && order.getStatus() != OrderStatus.PREPARING) {
                continue;
            }
            lines.add(order.getOrderId() + ". Prepare order " + order.getOrderId()
                    + " for " + customer.getName()
                    + ": " + order.getMenuItem().getName()
                    + " (" + order.getPreparedUnits() + "/"
                    + order.getMenuItem().getPreparationUnits() + ")");
        }
        return lines;
    }

    // Finds an order using its ID.
    private Order findOrder(int orderId) {
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {