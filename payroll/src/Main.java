import view.PayrollFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        service.IpallilosDAO dao = new service.IpallilosDAO();
        dao.initMisthosIfEmpty();
        dao.loadSalariesFromDB();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.PayrollFrame frame = new view.PayrollFrame();
                frame.setVisible(true);
            }
        });
    }
}
