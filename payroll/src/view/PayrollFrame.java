package view;

import model.Ipallilos;
import service.IpallilosDAO;
import service.Misthodosia;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PayrollFrame extends JFrame {

    private JTextField amField;
    private JTextArea resultArea;
    private IpallilosDAO dao;
    private Misthodosia service;

    public PayrollFrame() {
        dao = new IpallilosDAO();
        service = new Misthodosia();

        setTitle("Διαχείριση Προσωπικού Παν. Κρήτης");
        // setSize(850, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("ΑΜ Υπαλλήλου:"));
        amField = new JTextField(10);
        JButton btnSearch = new JButton("Αναζήτηση & Υπολογισμός");
        searchPanel.add(amField);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton btnHire = new JButton("Πρόσληψη Νέου");
        JButton btnRenew = new JButton("Ανανέωση Σύμβασης");
        JButton btnEdit = new JButton("Επεξεργασία");
        JButton btnFire = new JButton("Απόλυση");
        JButton btnShowAll = new JButton("Λίστα Όλων");
        JButton btnTotalCost = new JButton("Συνολικό Κόστος");
        JButton btnSettings = new JButton("Ρυθμίσεις");
        JButton btnPayAll = new JButton("Καταβολή Μισθοδοσίας");

        btnHire.setBackground(new Color(220, 255, 220));
        btnEdit.setBackground(new Color(255, 255, 200));
        btnFire.setBackground(new Color(255, 220, 220));
        btnSettings.setBackground(Color.LIGHT_GRAY);
        btnPayAll.setBackground(Color.GREEN);
        actionPanel.add(btnHire);
        actionPanel.add(btnEdit);
        actionPanel.add(btnRenew);
        actionPanel.add(btnFire);
        actionPanel.add(btnShowAll);
        actionPanel.add(btnTotalCost);
        actionPanel.add(btnSettings);
        actionPanel.add(btnPayAll);

        topPanel.add(searchPanel);
        topPanel.add(actionPanel);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> calculateOneEmployee());
        btnShowAll.addActionListener(e -> showAllEmployees());
        btnTotalCost.addActionListener(e -> calculateTotalCost());
        btnHire.addActionListener(e -> showHireDialog());
        btnRenew.addActionListener(e -> showRenewDialog());
        btnFire.addActionListener(e -> showFireDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnSettings.addActionListener(e -> showSettingsDialog());
        btnPayAll.addActionListener(e -> pliromiDialog());
    }

    private void pliromiDialog(){
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Θέλετε να προχωρήσετε σε πληρωμή ΟΛΩΝ των υπαλλήλων;",
                "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                String reportText = dao.executePayroll();
                
                JTextArea textArea = new JTextArea(reportText);
                textArea.setEditable(false);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(450, 400));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Αποδεικτικό Πληρωμής", JOptionPane.INFORMATION_MESSAGE);
            }
    }

    private void showSettingsDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField txtBasikosDid = new JTextField(String.valueOf(Misthodosia.BASIKOS_DIDAKTIKOS));
        JTextField txtBonusRes = new JTextField(String.valueOf(Misthodosia.EPIDOMA_EREYNAS));

        JTextField txtBasikosDio = new JTextField(String.valueOf(Misthodosia.BASIKOS_DIOIKITIKOS));
        JTextField txtBonusLib = new JTextField(String.valueOf(Misthodosia.EPIDOMA_VIVLIOTHIKIS));

        panel.add(new JLabel("--- ΔΙΔΑΚΤΙΚΟΙ ---"));
        panel.add(new JLabel(""));
        panel.add(new JLabel("Βασικός Μισθός (€):"));
        panel.add(txtBasikosDid);
        panel.add(new JLabel("Επίδομα Έρευνας (€):"));
        panel.add(txtBonusRes);

        panel.add(new JLabel("--- ΔΙΟΙΚΗΤΙΚΟΙ ---"));
        panel.add(new JLabel(""));
        panel.add(new JLabel("Βασικός Μισθός (€):"));
        panel.add(txtBasikosDio);
        panel.add(new JLabel("Επίδομα Βιβλιοθήκης (€):"));
        panel.add(txtBonusLib);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ρυθμίσεις Μισθοδοσίας", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double nBasikosDid = Double.parseDouble(txtBasikosDid.getText());
                double nBasikosDio = Double.parseDouble(txtBasikosDio.getText());
                double nRes = Double.parseDouble(txtBonusRes.getText());
                double nLib = Double.parseDouble(txtBonusLib.getText());

                Misthodosia.updateBasikosDidaktikos(nBasikosDid);
                Misthodosia.updateBasikosDioikitikos(nBasikosDio);
                Misthodosia.updateEpidomaEreunas(nRes);
                Misthodosia.updateEpidomaVivliothikis(nLib);

                dao.saveSalariesToDB(nBasikosDio, nBasikosDid, nRes, nLib);

                JOptionPane.showMessageDialog(this, "Οι αλλαγές αποθηκεύτηκαν!");

                if (!amField.getText().isEmpty()) {
                    calculateOneEmployee();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Σφάλμα: " + ex.getMessage(), "Απέτυχε", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        String inputAM = JOptionPane.showInputDialog(this, "Δώσε ΑΜ για αλλαγή στοιχείων:");
        if (inputAM == null || inputAM.isEmpty())
            return;

        int am = Integer.parseInt(inputAM);
        Ipallilos currentEmp = dao.getIpallilosByAM(am);

        if (currentEmp == null) {
            JOptionPane.showMessageDialog(this, "Δεν βρέθηκε υπάλληλος με αυτό το ΑΜ.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2));

        JTextField txtName = new JTextField(currentEmp.getOnoma());
        JTextField txtSurname = new JTextField(currentEmp.getEpitheto());

        String oldAddr = currentEmp.Dieuthinsi != null ? currentEmp.Dieuthinsi : "";
        JTextField txtAddress = new JTextField(oldAddr);

        String oldPhone = currentEmp.Tilefono != null ? currentEmp.Tilefono : "";
        JTextField txtPhone = new JTextField(oldPhone);

        String oldIBAN = currentEmp.IBAN != null ? currentEmp.IBAN : "";
        JTextField txtIBAN = new JTextField(oldIBAN);

        String oldBank = currentEmp.Onoma_Trapezas != null ? currentEmp.Onoma_Trapezas : "";
        JTextField txtBank = new JTextField(oldBank); // Εδώ το αφήνω Text Field για ευκολία, ή μπορείς να βάλεις
                                                      // ComboBox

        JCheckBox chkMarried = new JCheckBox("Έγγαμος");
        chkMarried.setSelected(currentEmp.isEggamos()); // Προ-επιλογή ανάλογα με το τι ήταν

        JTextField txtKids = new JTextField(String.valueOf(currentEmp.getPlithos_paidiwn()));

        panel.add(new JLabel("Όνομα:"));
        panel.add(txtName);
        panel.add(new JLabel("Επίθετο:"));
        panel.add(txtSurname);
        panel.add(new JLabel("Διεύθυνση:"));
        panel.add(txtAddress);
        panel.add(new JLabel("Τηλέφωνο:"));
        panel.add(txtPhone);
        panel.add(new JLabel("IBAN:"));
        panel.add(txtIBAN);
        panel.add(new JLabel("Τράπεζα:"));
        panel.add(txtBank);
        panel.add(new JLabel("Οικογ. Κατάσταση:"));
        panel.add(chkMarried);
        panel.add(new JLabel("Αριθμός Παιδιών:"));
        panel.add(txtKids);

        int result = JOptionPane.showConfirmDialog(this, panel, "Επεξεργασία (ΑΜ: " + am + ")",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            boolean success = dao.updateEmployeeData(
                    am,
                    txtName.getText(),
                    txtSurname.getText(),
                    txtAddress.getText(),
                    chkMarried.isSelected(),
                    Integer.parseInt(txtKids.getText()),
                    txtPhone.getText(),
                    txtIBAN.getText(),
                    txtBank.getText());

            if (success) {
                JOptionPane.showMessageDialog(this, "Τα στοιχεία ενημερώθηκαν!");
                amField.setText(String.valueOf(am));
                calculateOneEmployee();
            } else {
                JOptionPane.showMessageDialog(this, "Σφάλμα κατά την ενημέρωση.");
            }
        }
    }

    // parathiro proslipsis
    private void showHireDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        LocalDate today = LocalDate.now();
        LocalDate autoDate;
        if (today.getDayOfMonth() == 1) {
            autoDate = today;
        } else {
            autoDate = today.plusMonths(1).withDayOfMonth(1);
        }

        JTextField txtAM = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtSurname = new JTextField();
        JTextField txtAddress = new JTextField("Heraklion");
        JTextField txtPhone = new JTextField("69...");
        JTextField txtIBAN = new JTextField("GR...");

        String[] bankNames = { "Piraeus Bank", "Alpha Bank", "Eurobank", "National Bank (NBG)", "Optima Bank",
                "Credia Bank" };
        JComboBox<String> txtOnomaTrapezas = new JComboBox<>(bankNames);

        JComboBox<Ipallilos.Katigoria> comboKat = new JComboBox<>(Ipallilos.Katigoria.values());
        JTextField txtProslipsi = new JTextField(autoDate.toString());
        txtProslipsi.setEditable(false);
        txtProslipsi.setBackground(Color.LIGHT_GRAY);

        JCheckBox chkMarried = new JCheckBox("Έγγαμος");
        JTextField txtKids = new JTextField("0");

        JComboBox<Ipallilos.Status> comboStatus = new JComboBox<>(Ipallilos.Status.values());

        JTextField txtMisthos = new JTextField("0.0");
        JTextField txtLixi = new JTextField("2026-12-31");
        panel.add(new JLabel("Αριθμός Μητρώου (ΑΜ):"));
        panel.add(txtAM);
        panel.add(new JLabel("Όνομα:"));
        panel.add(txtName);
        panel.add(new JLabel("Επίθετο:"));
        panel.add(txtSurname);

        panel.add(new JLabel("Διεύθυνση:"));
        panel.add(txtAddress);
        panel.add(new JLabel("Τηλέφωνο:"));
        panel.add(txtPhone);
        panel.add(new JLabel("IBAN:"));
        panel.add(txtIBAN);
        panel.add(new JLabel("Τράπεζα:"));
        panel.add(txtOnomaTrapezas);

        panel.add(new JLabel("Κατηγορία:"));
        panel.add(comboKat);
        panel.add(new JLabel("Ημ. Πρόσληψης:"));
        panel.add(txtProslipsi);

        panel.add(new JLabel("Οικογ. Κατάσταση:"));
        panel.add(chkMarried);
        panel.add(new JLabel("Αριθμός Παιδιών:"));
        panel.add(txtKids);

        panel.add(new JLabel("Τύπος Σχέσης:"));
        panel.add(comboStatus);

        panel.add(new JLabel("--- ΓΙΑ ΣΥΜΒΑΣΙΟΥΧΟΥΣ ---"));
        panel.add(new JLabel(""));
        panel.add(new JLabel("Μισθός Σύμβασης (€):"));
        panel.add(txtMisthos);
        panel.add(new JLabel("Λήξη (YYYY-MM-DD):"));
        panel.add(txtLixi);

        int result = JOptionPane.showConfirmDialog(this, panel, "Φόρμα Πρόσληψης", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int am = Integer.parseInt(txtAM.getText());
                String name = txtName.getText();
                String surname = txtSurname.getText();
                Ipallilos.Katigoria kat = (Ipallilos.Katigoria) comboKat.getSelectedItem();
                Ipallilos.Status status = (Ipallilos.Status) comboStatus.getSelectedItem();
                boolean married = chkMarried.isSelected();
                int kids = Integer.parseInt(txtKids.getText());
                String bank = (String) txtOnomaTrapezas.getSelectedItem();
                List<LocalDate> childDates = new java.util.ArrayList<>();
                if (kids > 0) {
                    for (int i = 1; i <= kids; i++) {
                        String dateStr = JOptionPane.showInputDialog(this,
                                "Ημερομηνία γέννησης παιδιού " + i + "\n(Μορφή: YYYY-MM-DD)", "2020-01-01");

                        if (dateStr != null && !dateStr.isEmpty()) {
                            childDates.add(LocalDate.parse(dateStr));
                        }
                    }
                }

                LocalDate lixi = null;
                double misthos = 0.0;
                if (status == Ipallilos.Status.SYMVASIOUCHOS) {
                    lixi = LocalDate.parse(txtLixi.getText());
                    misthos = Double.parseDouble(txtMisthos.getText());
                }

                Ipallilos newEmp = new Ipallilos(am, name, surname, status, kat, married, autoDate, kids, lixi, misthos,
                        bank);
                newEmp.Dieuthinsi = txtAddress.getText();
                newEmp.Tilefono = txtPhone.getText();
                newEmp.IBAN = txtIBAN.getText();

                boolean success = dao.saveEmployee(newEmp, childDates);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Ο υπάλληλος προσλήφθηκε επιτυχώς!");
                    resultArea.setText("Νέα εγγραφή:\n" + newEmp.getOnoma() + " " + newEmp.getEpitheto());
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα! Ίσως το ΑΜ υπάρχει ήδη.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος δεδομένα: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showRenewDialog() {
        String inputAM = JOptionPane.showInputDialog(this, "Δώσε ΑΜ για ανανέωση:");
        int am = Integer.parseInt(inputAM);

        Ipallilos emp = dao.getIpallilosByAM(am);

        LocalDate today = LocalDate.now();
        LocalDate nextMonth;
        if (today.getDayOfMonth() == 1)
            nextMonth = today;
        else
            nextMonth = today.plusMonths(1).withDayOfMonth(1);

        JTextField txtStart = new JTextField(nextMonth.toString());
        JTextField txtEnd = new JTextField("2027-12-31");
        JTextField txtSalary = new JTextField("0.0");

        if (emp != null)
            txtSalary.setText(String.valueOf(emp.getMisthos_simbasis()));

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Έναρξη:"));
        panel.add(txtStart);
        panel.add(new JLabel("Λήξη:"));
        panel.add(txtEnd);
        panel.add(new JLabel("Μισθός:"));
        panel.add(txtSalary);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ανανέωση Σύμβασης", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            dao.renewContract(am, txtStart.getText(), txtEnd.getText(), Double.parseDouble(txtSalary.getText()));

            JOptionPane.showMessageDialog(this, "Η ανανέωση έγινε επιτυχώς.");

            amField.setText(inputAM);
            calculateOneEmployee();
        }
    }

    // parathiro apolyshs
    private void showFireDialog() {
        String input = JOptionPane.showInputDialog(this, "Δώσε ΑΜ για Απόλυση/Συνταξιοδότηση:");
        if (input != null && !input.isEmpty()) {
            try {
                int am = Integer.parseInt(input);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Ο υπάλληλος θα πληρωθεί κανονικά αυτόν τον μήνα.\n" +
                                "Η διακοπή θα ισχύει από την 1η του επόμενου.\n\n" +
                                "Είστε σίγουρος;",
                        "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = dao.fireEmployee(am);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Ορίστηκε ημερομηνία λήξης στο τέλος του μήνα.");
                        amField.setText(String.valueOf(am));
                        calculateOneEmployee();
                    } else {
                        JOptionPane.showMessageDialog(this, "Σφάλμα: Δεν βρέθηκε ο υπάλληλος.");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Μη έγκυρο ΑΜ.");
            }
        }
    }

    private void calculateOneEmployee() {
        String amText = amField.getText().trim();
        if (amText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Δώσε ΑΜ!", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int am = Integer.parseInt(amText);
            Ipallilos emp = dao.getIpallilosByAM(am);
            if (emp != null) {
                double salary = service.ypologismosMisthou(emp);
                printEmployeeDetails(emp, salary);
            } else {
                resultArea.append("\nΟ υπάλληλος με ΑΜ " + am + " δεν βρέθηκε.\n");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Το ΑΜ πρέπει να είναι αριθμός.", "Λάθος", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAllEmployees() {
        resultArea.setText("ΛΙΣΤΑ ΟΛΩΝ ΤΩΝ ΥΠΑΛΛΗΛΩΝ\n");
        List<Integer> allAMs = dao.getAllAMs();

        if (allAMs.isEmpty()) {
            resultArea.append("Η βάση δεδομένων είναι άδεια.");
            return;
        }

        for (int am : allAMs) {
            Ipallilos emp = dao.getIpallilosByAM(am);

            // an den vrethei o ypalhlos tote proxorame ston epomeno
            if (emp == null)
                continue;

            double salary = service.ypologismosMisthou(emp);

            String info = "AM: " + emp.getAM() +
                    " | " + emp.getOnoma() +
                    " " + emp.getEpitheto() +
                    " | " + emp.getStatus() +
                    " | Μισθός: " + String.format("%.2f", salary) + " €\n";

            resultArea.append(info);
        }
        resultArea.append("\n");
    }

    private void calculateTotalCost() {
        String inputDate = JOptionPane.showInputDialog(this,
                "Δώστε ημερομηνία παρελθόντος για σύγκριση (YYYY-MM-DD):",
                "2024-01-01");

        if (inputDate == null || inputDate.isEmpty())
            return;

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(inputDate);

            // den theloume melontikh hmeromhnia
            if (targetDate.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Η ημερομηνία πρέπει να είναι στο παρελθόν!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Λάθος μορφή! Χρησιμοποιήστε YYYY-MM-DD (π.χ. 2023-05-20)");
            return;
        }

        resultArea.setText("ΣΤΑΤΙΣΤΙΚΑ & ΑΝΑΛΥΣΗ ΜΙΣΘΟΔΟΣΙΑΣ\n");
        resultArea.append("Περίοδος Σύγκρισης: ΣΗΜΕΡΑ vs " + targetDate + "\n\n");

        List<Integer> allAMs = dao.getAllAMs();

        double currentTotalSum = 0;
        double pastTotalSum = 0;

        double sumDid = 0, maxDid = -1, minDid = Double.MAX_VALUE;
        int countDid = 0;
        double sumDio = 0, maxDio = -1, minDio = Double.MAX_VALUE;
        int countDio = 0;

        for (int am : allAMs) {
            Ipallilos emp = dao.getIpallilosByAM(am);
            if (emp == null)
                continue;

            double salaryNow = service.ypologismosMisthou(emp);

            int kidsThen = dao.getChildrenCountAtDate(am, targetDate);

            double salaryThen = service.ypologismosMisthouPareltontos(emp, targetDate, kidsThen);

            if (salaryNow > 0) {
                currentTotalSum += salaryNow;
                pastTotalSum += salaryThen;
                if (emp.getKatigoria() == Ipallilos.Katigoria.DIDAKTIKOS) {
                    sumDid += salaryNow;
                    countDid++;
                    if (salaryNow > maxDid)
                        maxDid = salaryNow;
                    if (salaryNow < minDid)
                        minDid = salaryNow;
                } else {
                    sumDio += salaryNow;
                    countDio++;
                    if (salaryNow > maxDio)
                        maxDio = salaryNow;
                    if (salaryNow < minDio)
                        minDio = salaryNow;
                }
            }
        }

        resultArea.append("ΓΕΝΙΚΑ ΣΤΑΤΙΣΤΙΚΑ (ΤΡΕΧΟΝΤΑ)\n");
        resultArea.append(String.format("Σύνολο Διδακτικών:   %10.2f €\n", sumDid));
        resultArea.append(String.format("Σύνολο Διοικητικών:  %10.2f €\n", sumDio));

        resultArea.append(" ΑΝΑΛΥΣΗ ΤΑΣΗΣ\n");
        resultArea.append(String.format("Μισθοδοσία Σήμερα:   %10.2f €\n", currentTotalSum));
        resultArea.append(String.format("Μισθοδοσία στις %s: %10.2f €\n", targetDate, pastTotalSum));

        double diff = currentTotalSum - pastTotalSum;
        double percent = (pastTotalSum > 0) ? (diff / pastTotalSum) * 100 : 0.0;
        if (pastTotalSum == 0 && currentTotalSum > 0)
            percent = 100.0;

        resultArea.append(String.format("Διαφορά Κόστους:     %+10.2f €\n", diff));
        resultArea.append(String.format("ΜΕΤΑΒΟΛΗ:            %+9.2f %%\n", percent));
        resultArea.append("==========================================\n");
    }

    private void printEmployeeDetails(Ipallilos emp, double salary) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ΣΤΟΙΧΕΙΑ ΥΠΑΛΛΗΛΟΥ ---\n");
        sb.append("ΑΜ:            ").append(emp.getAM()).append("\n");
        sb.append("Ονοματεπώνυμο: ").append(emp.getOnoma()).append(" ").append(emp.getEpitheto()).append("\n");
        sb.append("Κατηγορία:     ").append(emp.getKatigoria()).append("\n");
        sb.append("Κατάσταση:     ").append(emp.getStatus()).append("\n");

        if (emp.getLixi_simbasis() != null) {
            sb.append("Λήξη Σύμβασης: ").append(emp.getLixi_simbasis()).append("\n");
        }

        sb.append("--------------------------\n");
        sb.append(String.format("ΤΕΛΙΚΟΣ ΜΙΣΘΟΣ: %.2f €\n", salary));

        if (salary == 0)
            sb.append("(Δεν πληρώνεται λόγω λήξης σύμβασης)\n");

        sb.append("--------------------------\n");
        resultArea.append(sb.toString());
    }
}