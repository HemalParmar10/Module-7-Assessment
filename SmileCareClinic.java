import java.sql.*;
import java.util.Scanner;

public class SmileCareClinic {
	private static final String URL = "jdbc:mysql://localhost:3306/smilecare";
	private static final String USER = "root";
	private static final String PASS = "1234";
	private static Connection conn;

	public static void main(String[] args) {
		try {
			conn = DriverManager.getConnection(URL, USER, PASS);
			Scanner sc = new Scanner(System.in);
			int choice;
			do {
				System.out.println("\n==== SmileCare Dental Clinic ====");
				System.out.println("1. Register Patient");
				System.out.println("2. Book Appointment");
				System.out.println("3. Generate Invoice");
				System.out.println("4. View Patient History");
				System.out.println("5. Exit");
				System.out.print("Choose option: ");
				choice = sc.nextInt();
				sc.nextLine();

				switch (choice) {
				case 1 -> registerPatient(sc);
				case 2 -> bookAppointment(sc);
				case 3 -> generateInvoice(sc);
				case 4 -> viewHistory(sc);
				case 5 -> System.out.println("Goodbye!");
				}
			} while (choice != 5);
		} catch (SQLException e) {
			System.out.println("Database Error: " + e.getMessage());
		}
	}

	private static void registerPatient(Scanner sc) throws SQLException {
		System.out.print("Enter Name: ");
		String name = sc.nextLine();
		System.out.print("Enter Phone: ");
		String phone = sc.nextLine();
		System.out.print("Enter Insurance Provider: ");
		String insurance = sc.nextLine();

		PreparedStatement ps = conn.prepareStatement("INSERT INTO patients (name, phone, insurance) VALUES (?,?,?)");
		ps.setString(1, name);
		ps.setString(2, phone);
		ps.setString(3, insurance);
		ps.executeUpdate();
		System.out.println("Patient registered successfully!");
	}

	private static void bookAppointment(Scanner sc) throws SQLException {
		System.out.print("Enter Patient ID: ");
		int pid = sc.nextInt();
		sc.nextLine();
		System.out.print("Enter Service (cleaning/filling/root canal): ");
		String service = sc.nextLine();
		System.out.print("Enter Appointment Time (YYYY-MM-DD HH:MM:SS): ");
		String time = sc.nextLine();
		System.out.print("Enter Estimated Cost: ");
		double cost = sc.nextDouble();

		PreparedStatement ps = conn.prepareStatement(
				"INSERT INTO appointments (patient_id, service, appointment_time, estimated_cost) VALUES (?,?,?,?)");
		ps.setInt(1, pid);
		ps.setString(2, service);
		ps.setString(3, time);
		ps.setDouble(4, cost);
		ps.executeUpdate();
		System.out.println("Appointment booked successfully!");
	}

	private static void generateInvoice(Scanner sc) throws SQLException {
		System.out.print("Enter Appointment ID: ");
		int aid = sc.nextInt();

		PreparedStatement ps = conn.prepareStatement("SELECT service, estimated_cost FROM appointments WHERE id=?");
		ps.setInt(1, aid);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			String service = rs.getString("service");
			double cost = rs.getDouble("estimated_cost");
			double tax = cost * 0.1;
			double discount = cost * 0.05;
			double total = cost + tax - discount;

			String invoice = "Service: " + service + "\nBase: " + cost + "\nTax: " + tax + "\nDiscount: " + discount
					+ "\nTotal: " + total;

			PreparedStatement ins = conn
					.prepareStatement("INSERT INTO invoices (appointment_id, total_cost, invoice_text) VALUES (?,?,?)");
			ins.setInt(1, aid);
			ins.setDouble(2, total);
			ins.setString(3, invoice);
			ins.executeUpdate();

			System.out.println("Invoice Generated:\n" + invoice);
		} else {
			System.out.println("Appointment not found!");
		}
	}

	private static void viewHistory(Scanner sc) throws SQLException {
		System.out.print("Enter Patient ID: ");
		int pid = sc.nextInt();

		PreparedStatement ps = conn.prepareStatement("SELECT a.service, a.appointment_time, i.invoice_text "
				+ "FROM appointments a LEFT JOIN invoices i ON a.id=i.appointment_id " + "WHERE a.patient_id=?");
		ps.setInt(1, pid);
		ResultSet rs = ps.executeQuery();

		System.out.println("\n--- Patient History ---");
		while (rs.next()) {
			System.out.println("Service: " + rs.getString("service"));
			System.out.println("Time: " + rs.getString("appointment_time"));
			System.out.println("Invoice:\n" + rs.getString("invoice_text"));
			System.out.println("---------------------------");
		}
	}
}
