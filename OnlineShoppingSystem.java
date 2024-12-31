import java.sql.*;
import java.util.Scanner;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
class CustomerAccess {
    Connection con;
    Statement stmt;
    Scanner scanner;
    public CustomerAccess(Connection con, Statement stmt, Scanner scanner) {
        this.con = con;
        this.stmt = stmt;
        this.scanner = scanner;
    }
    public void register() {
        System.out.print("Enter customer username: ");
        String username = scanner.nextLine();
        System.out.print("Enter customer password: ");
        String password = scanner.nextLine();
        System.out.print("Enter customer email: ");
        String email = scanner.nextLine();
        try {
            int result = stmt.executeUpdate("INSERT INTO users (username, password,email) VALUES ('" + username + "', '" + password + "', '" + email + "')");
            if (result ==1) {
                System.out.println("Customer registration successful!");
            } else {
                System.out.println("Customer registration failed. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void login() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        try(Statement stmt = con.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM users WHERE username = " + username + " AND password = " + password );
            if (resultSet.next()) {
                System.out.println("Login successful!");
                int choice;
                do {
                    System.out.println("Customer Menu:");
                    System.out.println("1. View Products");
                    System.out.println("2. Add to Cart");
                    System.out.println("3. View Cart");
                    System.out.println("4. Checkout");
                    System.out.println("5. View Transactions");
                    System.out.println("6. Exit");
                    System.out.print("Enter your choice: ");
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            viewProducts();
                            break;
                        case 2:
                            addToCart(resultSet.getInt("user_id"));
                            break;
                        case 3:
                            viewCart(resultSet.getInt("user_id"));
                            break;
                        case 4:
                            checkout(resultSet.getInt("user_id"));
                            break;
                        case 5:
                            viewTransactions(resultSet.getInt("user_id"));
                            break;
                        case 6:
                            System.out.println("Exiting Customer Menu...");
                            break;
                        default:
                            System.out.println("Invalid choice. Try again.");
                    }
                } while (choice != 6);
            } else {
                System.out.println("Invalid credentials. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void viewProducts() {
        try (Statement stmt = con.createStatement()){
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM products");
            System.out.println("Product List:");
            System.out.println("ID                 Name               Price");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt(1) + "                 " + resultSet.getString(2) + "                 " + resultSet.getDouble(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addToCart(int customerId) {
        viewProducts();
        System.out.print("Enter the ID of the product to add to cart: ");
        int productId = scanner.nextInt();
        System.out.print("Enter the quantity: ");
        int quantity = scanner.nextInt();
        try (Statement stmt = con.createStatement()){
            int result = stmt.executeUpdate("INSERT INTO cart (user_id, product_id, quantity) VALUES (" + customerId + ", " + productId + ", " + quantity + ")");
            if (result ==1) {
                System.out.println("Product added to cart successfully!");
            } else {
                System.out.println("Failed to add product to cart. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void viewCart(int customerId) {
        try(Statement stmt = con.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM cart INNER JOIN products ON cart.product_id = products.product_id WHERE user_id = " + customerId);
            System.out.println("Shopping Cart:");
            System.out.println("ID                 Name                 Price                 Quantity");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("product_id") + "                 " + resultSet.getString("product_name") + "                 " +
                        resultSet.getDouble("price") + "                 " + resultSet.getInt("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void checkout(int customerId) {
        viewCart(customerId);
        System.out.print("Enter the ID of the product to purchase: ");
        int productId = scanner.nextInt();
        System.out.print("Enter the quantity: ");
        int quantity = scanner.nextInt();
        try(Statement stmt = con.createStatement()) {
            ResultSet productResult = stmt.executeQuery("SELECT * FROM products WHERE product_id = " + productId);
            if (productResult.next()) {
                double totalPrice = productResult.getDouble("price") * quantity;
                int orderResult = stmt.executeUpdate("INSERT INTO orders (user_id, product_id, quantity, total_price) VALUES (" +
                        customerId + ", " + productId + ", " + quantity + ", " + totalPrice + ")");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date());
                int transactionResult = stmt.executeUpdate("INSERT INTO transactions (user_id, order_id, transaction_date) VALUES (" +
                        customerId + ", LAST_INSERT_ID(), '" + date + "')");
                int updateCartResult = stmt.executeUpdate("UPDATE cart SET quantity = quantity - " + quantity +
                        " WHERE user_id = " + customerId + " AND product_id = " + productId);
                if (orderResult ==1 && transactionResult ==1 && updateCartResult ==1) {
                    System.out.println("Checkout successful! ");
                } else {
                    System.out.println("Checkout failed. Please try again.");
                }
            } else {
                System.out.println("Product not found. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void viewTransactions(int customerId) {
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM transactions WHERE user_id = " + customerId);
            System.out.println("Transaction History:");
            System.out.println("Transaction ID           Order ID           Date");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("transaction_id") + "                 " + resultSet.getInt("order_id") + "                 " + resultSet.getString("transaction_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
class AdminAccess{
    Connection con;
    Statement stmt;
    Scanner scanner;
    public AdminAccess(Connection con, Statement stmt, Scanner scanner) {
        this.con = con;
        this.stmt = stmt;
        this.scanner = scanner;
    }
    public void login() {
        System.out.print("Enter admin username: ");
        String username = scanner.nextLine();
        System.out.print("Enter admin password: ");
        String password = scanner.nextLine();
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM admins WHERE username = '" + username + "' AND password = '" + password + "'");
            if (resultSet.next()) {
                System.out.println("Admin login successful!");
                manageProducts();
            } else {
                System.out.println("Invalid admin credentials. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void manageProducts() throws SQLException {
        int choice;
        do {
            System.out.println("Product Management:");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    viewProducts();
                    break;
                case 3:
                    updateProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 5:
                    System.out.println("Exiting Product Management...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 5);
    }

    public void addProduct() {
        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();
        System.out.print("Enter product price: ");
        double productPrice = scanner.nextDouble();

        try {
            int result = stmt.executeUpdate("INSERT INTO products (product_name, price) VALUES ('" + productName + "', " + productPrice + ")");
            if (result ==1) {
                System.out.println("Product added successfully!");
            } else {
                System.out.println("Failed to add product. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void viewProducts() {
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM products");
            System.out.println("Product List:");
            System.out.println("ID                  Name                  Price");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt(1) + "                 "  + resultSet.getString(2) + "                 "  + resultSet.getDouble(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateProduct() {
        viewProducts();
        System.out.print("Enter the ID of the product to update: ");
        int productId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter new product name: ");
        String productName = scanner.nextLine();
        System.out.print("Enter new product price: ");
        double productPrice = scanner.nextDouble();
        try {
            int result = stmt.executeUpdate("UPDATE products SET product_name = '" + productName + "', price = " + productPrice + " WHERE product_id = " + productId);
            if (result ==1) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("Failed to update product. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deleteProduct() throws SQLException {
        int check=1;
        viewProducts();
        System.out.print("Enter the ID of the product to delete: ");
        int productId = scanner.nextInt();
        try {
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM orders");
            while (resultSet.next()) {
                if(resultSet.getInt(3)==productId){
                    int orderId= resultSet.getInt(1);
                    try {
                        Statement deleteStmt = con.createStatement();
                        int result = deleteStmt.executeUpdate("DELETE FROM transactions WHERE order_id = " + orderId);
                        if (result ==1) {
                            result = deleteStmt.executeUpdate("DELETE FROM orders WHERE product_id = " + productId);
                            if (result ==1) {
                                result = deleteStmt.executeUpdate("DELETE FROM cart WHERE product_id = " + productId);
                               check=0;
                            }
                        }
                    }catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try{
            Statement delStmt = con.createStatement();
            int result = delStmt.executeUpdate("DELETE FROM products WHERE product_id = " + productId);
            if (result ==1) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Failed to delete product. Please try again.");
            }
        }catch (SQLException e) {
            e.printStackTrace();}


        }
}
public class OnlineShoppingSystem {
    Connection con;
    Statement stmt;
    Scanner scanner;
    AdminAccess adminAccess;
    CustomerAccess customerAccess;
    public OnlineShoppingSystem() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost/onlineshop", "root", "raboonisherin");
            stmt = con.createStatement();
            scanner = new Scanner(System.in);
            adminAccess = new AdminAccess(con, stmt, scanner);
            customerAccess = new CustomerAccess(con, stmt, scanner);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        OnlineShoppingSystem shoppingSystem = new OnlineShoppingSystem();
        System.out.println("Welcome to Abdiel store");
        shoppingSystem.run();
    }
    public void run() {
        int choice;
        do {
            System.out.println("1. Login as Admin");
            System.out.println("2. Login as Customer");
            System.out.println("3. Register");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    adminAccess.login();
                    break;
                case 2:
                    customerAccess.login();
                    break;
                case 3:
                    customerAccess.register();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 4);
    }
}