package service;

import model.Ipallilos;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IpallilosDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/universitypayroll";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Ipallilos getIpallilosByAM(int am) {
        Ipallilos emp = null;

        String sql = "SELECT i.*, " +
                "s.lixi_simbasis, s.misthos_simbasis, " +
                "(SELECT COUNT(*) FROM Paidia p WHERE p.AM_ipallilou = i.AM) as paidia_count " +
                "FROM Ipallilos i " +
                "LEFT JOIN Simbasi s ON i.AM = s.AM_ipallilou " +
                "WHERE i.AM = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, am);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String onoma = rs.getString("Onoma");
                String epitheto = rs.getString("Epitheto");
                String onoma_trapezas = rs.getString("Onoma_Trapezas");
                boolean eggamos = rs.getBoolean("Eggamos");
                int paidia = rs.getInt("paidia_count");
                String dieuthinsi = rs.getString("Dieuthinsi");
                String tilefono = rs.getString("Tilefono");
                String iban = rs.getString("IBAN");

                Ipallilos.Status status = Ipallilos.Status.valueOf(rs.getString("Status"));
                Ipallilos.Katigoria katigoria = Ipallilos.Katigoria.valueOf(rs.getString("Katigoria"));
                
                Date sqlHireDate = rs.getDate("Imerominia_proslipsis");
                LocalDate hireDate = (sqlHireDate != null) ? sqlHireDate.toLocalDate() : null;

                Date sqlLixi = rs.getDate("lixi_simbasis");
                LocalDate lixiSymvasis = (sqlLixi != null) ? sqlLixi.toLocalDate() : null;

                double misthosSymvasis = rs.getDouble("misthos_simbasis");

                emp = new Ipallilos(
                        am, onoma, epitheto, status, katigoria,
                        eggamos, hireDate, paidia, lixiSymvasis, misthosSymvasis, onoma_trapezas
                );

                emp.Dieuthinsi = dieuthinsi;
                emp.Tilefono = tilefono;
                emp.IBAN = iban;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emp;
    }

    // Methodos pou epistrefei lista me ola ta AM pou yparxoun sthn vash
    public List<Integer> getAllAMs() {
        List<Integer> amList = new ArrayList<>();
        String sql = "SELECT AM FROM Ipallilos";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                amList.add(rs.getInt("AM"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amList;
    }

    // methodos gia thn proslipsi ypallhlou
    public boolean saveEmployee(Ipallilos emp, List<LocalDate> childDates) {
        String sqlIpallilos = "INSERT INTO Ipallilos (AM, Onoma, Epitheto, Status, Katigoria, Eggamos, Imerominia_proslipsis, Dieuthinsi, Tmima, IBAN, Tilefono, Onoma_Trapezas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CSD', ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlIpallilos)) {
                stmt.setInt(1, emp.getAM());
                stmt.setString(2, emp.getOnoma());
                stmt.setString(3, emp.getEpitheto());
                stmt.setString(4, emp.getStatus().toString());
                stmt.setString(5, emp.getKatigoria().toString());
                stmt.setBoolean(6, emp.isEggamos());

                LocalDate date = emp.getImerominia_proslipsis() != null ? emp.getImerominia_proslipsis() : LocalDate.now();
                stmt.setDate(7, java.sql.Date.valueOf(date));
                stmt.setString(8, emp.Dieuthinsi);
                stmt.setString(9, emp.getIBAN());
                stmt.setString(10, emp.getTilefono());
                stmt.setString(11, emp.getOnoma_Trapezas());
                
                stmt.executeUpdate();
            }

            if (childDates != null && !childDates.isEmpty()) {
                String sqlPaidi = "INSERT INTO Paidia (AM_ipallilou, imerominia_gennisis) VALUES (?, ?)";
                try (PreparedStatement stmtP = conn.prepareStatement(sqlPaidi)) {
                    for (LocalDate birthDate : childDates) {
                        stmtP.setInt(1, emp.getAM());
                        stmtP.setDate(2, java.sql.Date.valueOf(birthDate));
                        stmtP.executeUpdate();
                    }
                }
            }

            if (emp.getStatus() == Ipallilos.Status.SYMVASIOUCHOS) {
                String sqlSimbasi = "INSERT INTO Simbasi (AM_ipallilou, enarxi_simbasis, lixi_simbasis, misthos_simbasis) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmtS = conn.prepareStatement(sqlSimbasi)) {
                    stmtS.setInt(1, emp.getAM());
                    stmtS.setDate(2, java.sql.Date.valueOf(emp.getImerominia_proslipsis())); 
                    stmtS.setDate(3, java.sql.Date.valueOf(emp.getLixi_simbasis()));
                    stmtS.setDouble(4, emp.getMisthos_simbasis());
                    stmtS.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String executePayroll() {
        StringBuilder report = new StringBuilder();
        LocalDate today = LocalDate.now();

        double sumDidaktikoi = 0;
        double sumDioikitikoi = 0;
        int countDid = 0;
        int countDio = 0;
        String sqlInsert = "INSERT INTO Pliromi (AM_ipallilou, imerominia, poso_basiko, poso_epidomata, poso_sinolo) VALUES (?, ?, ?, ?, ?)";
        
        List<Integer> allAMs = getAllAMs();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {

            conn.setAutoCommit(false);

            for (int am : allAMs) {
                Ipallilos emp = getIpallilosByAM(am);
                if (emp == null) continue;

                double basic = 0;
                if (emp.getStatus() == Ipallilos.Status.SYMVASIOUCHOS) {
                    basic = emp.getMisthos_simbasis();
                } else {
                    if (emp.getKatigoria() == Ipallilos.Katigoria.DIDAKTIKOS) {
                        basic = service.Misthodosia.BASIKOS_DIDAKTIKOS;
                    } else {
                        basic = service.Misthodosia.BASIKOS_DIOIKITIKOS;
                    }
                }
                double total = service.Misthodosia.ypologismosMisthou(emp);
                double allowances = total - basic;

                stmt.setInt(1, am);
                stmt.setDate(2, java.sql.Date.valueOf(today));
                stmt.setDouble(3, basic);      // poso_basiko
                stmt.setDouble(4, allowances); // poso_epidomata
                stmt.setDouble(5, total);      // poso_sinolo
                stmt.executeUpdate();

                if (emp.getKatigoria() == Ipallilos.Katigoria.DIDAKTIKOS) {
                    sumDidaktikoi += total;
                    countDid++;
                } else {
                    sumDioikitikoi += total;
                    countDio++;
                }
            }

            conn.commit();
            
            report.append(" ΑΠΟΔΕΙΚΤΙΚΟ ΜΙΣΘΟΔΟΣΙΑΣ (" + today + ") \n\n");
            
            report.append(">> ΚΑΤΗΓΟΡΙΑ: ΔΙΔΑΚΤΙΚΟΙ\n");
            report.append("   Πλήθος: " + countDid + "\n");
            report.append("   Σύνολο Πληρωμών: " + String.format("%.2f", sumDidaktikoi) + " €\n\n");
            
            report.append(">> ΚΑΤΗΓΟΡΙΑ: ΔΙΟΙΚΗΤΙΚΟΙ\n");
            report.append("   Πλήθος: " + countDio + "\n");
            report.append("   Σύνολο Πληρωμών: " + String.format("%.2f", sumDioikitikoi) + " €\n\n");
            
            report.append("-----------------------------------------\n");
            report.append("ΓΕΝΙΚΟ ΣΥΝΟΛΟ ΕΞΟΔΩΝ: " + String.format("%.2f", (sumDidaktikoi + sumDioikitikoi)) + " €");

        } catch (SQLException e) {
            e.printStackTrace();
            return "Σφάλμα: " + e.getMessage();
        }

        return report.toString();
    }

    public void renewContract(int am, String start, String end, double salary) {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "INSERT INTO Simbasi (AM_ipallilou, enarxi_simbasis, lixi_simbasis, misthos_simbasis) VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, am);
            stmt.setDate(2, java.sql.Date.valueOf(start));
            stmt.setDate(3, java.sql.Date.valueOf(end));
            stmt.setDouble(4, salary);

            stmt.executeUpdate();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fireEmployee(int am) {
        // Briskoumai thn teleutaia hmera tou trexontos mhna
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        String sqlUpdate = "UPDATE Simbasi SET lixi_simbasis = ? WHERE AM_ipallilou = ?";

        String sqlInsert = "INSERT INTO Simbasi (AM_ipallilou, enarxi_simbasis, lixi_simbasis, misthos_simbasis) VALUES (?, ?, ?, 0.0)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setDate(1, java.sql.Date.valueOf(lastDayOfMonth));
                stmt.setInt(2, am);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Ενημερώθηκε η λήξη σύμβασης για τον ΑΜ " + am);
                    return true;
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setInt(1, am);
                stmt.setDate(2, java.sql.Date.valueOf(today));
                stmt.setDate(3, java.sql.Date.valueOf(lastDayOfMonth));

                stmt.executeUpdate();
                System.out.println("Ορίστηκε συνταξιοδότηση για τον μόνιμο ΑΜ " + am);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEmployeeData(int am, String newName, String newSurname, String newAddress,
            boolean newEggamos, int newPaidiaCount,
            String newPhone, String newIBAN, String newBank) {

        String sqlUpdateInfo = "UPDATE Ipallilos SET Onoma = ?, Epitheto = ?, Dieuthinsi = ?, Eggamos = ?, Tilefono = ?, IBAN = ?, Onoma_Trapezas = ? WHERE AM = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateInfo)) {
                stmt.setString(1, newName);
                stmt.setString(2, newSurname);
                stmt.setString(3, newAddress);
                stmt.setBoolean(4, newEggamos);
                stmt.setString(5, newPhone);
                stmt.setString(6, newIBAN);
                stmt.setString(7, newBank);

                stmt.setInt(8, am);
                stmt.executeUpdate();
            }

            String deleteKids = "DELETE FROM Paidia WHERE AM_ipallilou = ?";
            try (PreparedStatement stmtDel = conn.prepareStatement(deleteKids)) {
                stmtDel.setInt(1, am);
                stmtDel.executeUpdate();
            }

            if (newPaidiaCount > 0) {
                String insertKids = "INSERT INTO Paidia (AM_ipallilou, imerominia_gennisis) VALUES (?, '2020-01-01')";
                try (PreparedStatement stmtIns = conn.prepareStatement(insertKids)) {
                    for (int i = 0; i < newPaidiaCount; i++) {
                        stmtIns.setInt(1, am);
                        stmtIns.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // allagh dieuthinsis
    public void updateAddress(int am, String newAddress) {
        String sql = "UPDATE Ipallilos SET Dieuthinsi = ? WHERE AM = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newAddress);
            stmt.setInt(2, am);
            stmt.executeUpdate();
            System.out.println("Η διεύθυνση άλλαξε.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getChildrenCountAtDate(int am, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM Paidia WHERE AM_ipallilou = ? AND imerominia_gennisis <= ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, am);
            stmt.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    

    // Initialize pinaka Misthos
    public void initMisthosIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM Misthos";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                // System.out.println("NOOO DATAAAA");              
                String insertSql = "INSERT INTO Misthos (katigoria_ypallilou, basikos_misthos, poso_eidiko) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    
                    pstmt.setString(1, "DIOIKITIKOS");
                    pstmt.setDouble(2, 900.0);
                    pstmt.setDouble(3, 50.0);
                    pstmt.executeUpdate();
                    pstmt.setString(1, "DIDAKTIKOS");
                    pstmt.setDouble(2, 1200.0);
                    pstmt.setDouble(3, 100.0);
                    pstmt.executeUpdate();
                }

            }

            // else {
            //         System.out.println("SOS!!! data found");
            //     }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get salary from db
    public void loadSalariesFromDB() {
        String sql = "SELECT * FROM Misthos";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String kat = rs.getString("katigoria_ypallilou");
                double basikos = rs.getDouble("basikos_misthos");
                double eidiko = rs.getDouble("poso_eidiko");

                if ("DIOIKITIKOS".equals(kat)) {
                    Misthodosia.BASIKOS_DIOIKITIKOS = basikos;
                    Misthodosia.EPIDOMA_VIVLIOTHIKIS = eidiko;
                } else if ("DIDAKTIKOS".equals(kat)) {
                    Misthodosia.BASIKOS_DIDAKTIKOS = basikos;
                    Misthodosia.EPIDOMA_EREYNAS = eidiko;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // save to db
    public void saveSalariesToDB(double bDio, double bDid, double eEreunas, double eVivl) {
        String sql = "UPDATE Misthos SET basikos_misthos = ?, poso_eidiko = ? WHERE katigoria_ypallilou = ?";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, bDio);
                stmt.setDouble(2, eVivl);
                stmt.setString(3, "DIOIKITIKOS");
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, bDid);
                stmt.setDouble(2, eEreunas);
                stmt.setString(3, "DIDAKTIKOS");
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}