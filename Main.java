
import java.io.*;
import java.util.*;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Account> accounts = new ArrayList<>();
    static Queue<String> customerQueue = new LinkedList<>();

    static class Account {
        String accountNo, name, pin;
        int balance;
        boolean blocked;
        Stack<String> history = new Stack<>();

        Account(String no, String name, String pin,
                int balance, boolean blocked) {
            this.accountNo = no;
            this.name = name;
            this.pin = pin;
            this.balance = balance;
            this.blocked = blocked;
        }
    }

    public static void main(String[] args) {
        loadAccounts();

        while (true) {
            System.out.println("\n===== ATM SIMULATOR =====");
            System.out.println("1. Login / Serve Customer");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();

            if (choice == 1) {
                serveCustomer();
            } else if (choice == 2) {
                saveAccounts();
                System.out.println("Thank you for using ATM.");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void serveCustomer() {
        System.out.print("Enter Account Number: ");
        String no = sc.nextLine().trim();

        customerQueue.add(no);
        String accountNo = customerQueue.poll();

        Account a = findAccount(accountNo);

        if (a == null) {
            System.out.println("Account not found.");
            return;
        }

        if (a.blocked) {
            System.out.println("Your card is blocked.");
            return;
        }

        System.out.println("Serving: " + a.name +
                           " (" + a.accountNo + ")");

        int attempts = 3;
        boolean login = false;

        while (attempts > 0) {
            System.out.print("Enter PIN: ");
            String enteredPin = sc.nextLine().trim();

            if (a.pin.equals(enteredPin)) {
                login = true;
                break;
            }

            attempts--;
            System.out.println("Incorrect PIN. Attempts left: "
                               + attempts);
        }

        if (!login) {
            a.blocked = true;
            saveAccounts();
            System.out.println("Card blocked after 3 wrong attempts.");
            return;
        }

        System.out.println("Login successful. Welcome, " + a.name);
        accountMenu(a);
    }

    static void accountMenu(Account a) {
        while (true) {
            System.out.println("\n===== ATM MENU =====");
            System.out.println("1. Check Balance");
            System.out.println("2. Add Customer");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Mini Statement");
            System.out.println("6. Logout");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    System.out.println("Available Balance: Rs."
                                       + a.balance);
                    break;

                case 2:
                    addCustomer();
                    break;

                case 3:
                    deposit(a);
                    break;

                case 4:
                    withdraw(a);
                    break;

                case 5:
                    showStatement(a);
                    break;

                case 6:
                    saveAccounts();
                    System.out.println("Logged out successfully.");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void deposit(Account a) {
        System.out.print("Enter amount: ");
        int amount = readInt();

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        a.balance += amount;
        a.history.push("Deposit " + amount +
                       " | Balance " + a.balance);

        saveAccounts();

        System.out.println("Deposit successful. Balance: "
                           + a.balance);
    }

    static void withdraw(Account a) {
        System.out.print("Enter amount: ");
        int amount = readInt();

        if (amount <= 0 || amount % 100 != 0
                || amount > a.balance) {
            System.out.println("Invalid amount or insufficient balance.");
            return;
        }

        a.balance -= amount;
        a.history.push("Withdraw " + amount +
                       " | Balance " + a.balance);

        System.out.println("Cash dispensed:");

        int remaining = amount;
        int[] notes = {500, 200, 100};

        for (int note : notes) {
            int count = remaining / note;

            if (count > 0) {
                System.out.println(note + " x " + count);
                remaining %= note;
            }
        }

        saveAccounts();
        System.out.println("Balance: " + a.balance);
    }

    static void showStatement(Account a) {
        System.out.println("Mini Statement:");

        if (a.history.empty()) {
            System.out.println("No transactions.");
            return;
        }

        int count = 1;

        for (int i = a.history.size() - 1; i >= 0; i--) {
            System.out.println(count + ". " + a.history.get(i));
            count++;
        }
    }

    static void addCustomer() {
        System.out.print("Enter Account Number: ");
        String no = sc.nextLine().trim();

        Account a = findAccount(no);

        if (a != null) {
            customerQueue.add(no);
            System.out.println("Customer added to queue.");
        } else {
            System.out.println("Account not found.");
        }
    }

    static Account findAccount(String no) {
        for (Account a : accounts) {
            if (a.accountNo.equalsIgnoreCase(no)) {
                return a;
            }
        }
        return null;
    }

    static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void loadAccounts() {
        File file = new File("accounts.txt");

        if (!file.exists()) {
            accounts.add(new Account("ACC101", "Riya",
                                     "1234", 5000, false));
            accounts.add(new Account("ACC102", "Aman",
                                     "2345", 3200, false));
            accounts.add(new Account("ACC103", "Neha",
                                     "3456", 500, false));
            saveAccounts();
            return;
        }

        try (BufferedReader br =
                 new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\|");

                if (data.length == 5) {
                    accounts.add(new Account(
                        data[0], data[1], data[2],
                        Integer.parseInt(data[3]),
                        Boolean.parseBoolean(data[4])
                    ));
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading account data.");
        }
    }

    static void saveAccounts() {
        try (FileWriter fw = new FileWriter("accounts.txt")) {
            for (Account a : accounts) {
                fw.write(a.accountNo + "|" + a.name + "|" +
                         a.pin + "|" + a.balance + "|" +
                         a.blocked + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving account data.");
        }
    }
}
