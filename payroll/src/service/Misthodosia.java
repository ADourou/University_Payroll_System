package service;

import model.Ipallilos;
import java.time.LocalDate;
import java.time.Period;

public class Misthodosia {

    public static double BASIKOS_DIOIKITIKOS = 900.0;
    public static double BASIKOS_DIDAKTIKOS = 1200.0;
    public static double EPIDOMA_EREYNAS = 100.0;
    public static double EPIDOMA_VIVLIOTHIKIS = 50.0;

    public static void updateBasikosDioikitikos(double neoPoso) throws Exception {
        if (neoPoso < BASIKOS_DIOIKITIKOS) {
            throw new Exception("Απαγορεύεται η μείωση μισθού! (Τρέχων: " + BASIKOS_DIOIKITIKOS + ")");
        }
        BASIKOS_DIOIKITIKOS = neoPoso;
    }

    public static void updateBasikosDidaktikos(double neoPoso) throws Exception {
        if (neoPoso < BASIKOS_DIDAKTIKOS) {
            throw new Exception("Απαγορεύεται η μείωση μισθού! (Τρέχων: " + BASIKOS_DIDAKTIKOS + ")");
        }
        BASIKOS_DIDAKTIKOS = neoPoso;
    }

    public static void updateEpidomaEreunas(double neoPoso) throws Exception {
        if (neoPoso < EPIDOMA_EREYNAS) {
            throw new Exception("Απαγορεύεται η μείωση επιδόματος! (Τρέχον: " + EPIDOMA_EREYNAS + ")");
        }
        EPIDOMA_EREYNAS = neoPoso;
    }

    public static void updateEpidomaVivliothikis(double neoPoso) throws Exception {
        if (neoPoso < EPIDOMA_VIVLIOTHIKIS) {
            throw new Exception("Απαγορεύεται η μείωση επιδόματος! (Τρέχον: " + EPIDOMA_VIVLIOTHIKIS + ")");
        }
        EPIDOMA_VIVLIOTHIKIS = neoPoso;
    }


    public static double ypologismosMisthou(Ipallilos emp) {

        // elegxos liksis symbasis , gia symbasiouchous
        if (emp.getStatus() == Ipallilos.Status.SYMVASIOUCHOS) {
            if (emp.getLixi_simbasis() != null && LocalDate.now().isAfter(emp.getLixi_simbasis())) {
                System.out.println("Η σύμβαση έληξε για: " + emp.getOnoma() + " " + emp.getEpitheto());
                return 0.0;
            }
        }

        double telikosMisthos = 0.0;

        //Ypologismos vasikou misthou
        //ean einai monimos
        if (emp.getStatus() == Ipallilos.Status.MONIMOS) {
            if (emp.getKatigoria() == Ipallilos.Katigoria.DIOIKITIKOS) {
                telikosMisthos = BASIKOS_DIOIKITIKOS;
            } else {
                telikosMisthos = BASIKOS_DIDAKTIKOS;
            }

            // prosaukhsh 15% - xronia ergasias
            if (emp.getImerominia_proslipsis() != null) {
                int xronia = Period.between(emp.getImerominia_proslipsis(), LocalDate.now()).getYears();
                if (xronia > 1) {
                    double prosauxisi = telikosMisthos * 0.15 * (xronia - 1);
                    telikosMisthos += prosauxisi;
                }
            }

        } else {
            // allios einai symbasiouchos
            telikosMisthos = emp.getMisthos_simbasis();
        }

        // epidomata
        if (emp.getKatigoria() == Ipallilos.Katigoria.DIDAKTIKOS) {
            if (emp.getStatus() == Ipallilos.Status.MONIMOS) {
                telikosMisthos += EPIDOMA_EREYNAS;
            } else {
                telikosMisthos += EPIDOMA_VIVLIOTHIKIS;
            }
        }

        // epidoma paidion
        double vasiYpologismou;
        if (emp.getStatus() == Ipallilos.Status.MONIMOS) {
            vasiYpologismou = (emp.getKatigoria() == Ipallilos.Katigoria.DIOIKITIKOS) ? BASIKOS_DIOIKITIKOS : BASIKOS_DIDAKTIKOS;
        } else {
            vasiYpologismou = emp.getMisthos_simbasis();
        }

        if (emp.isEggamos()) {
            // 5% syzygos + 5% gia kathe paidi
            double pososto = 0.05 + (emp.getPlithos_paidiwn() * 0.05);
            telikosMisthos += (vasiYpologismou * pososto);
        }

        return telikosMisthos;
    }

    public double ypologismosMisthouPareltontos(Ipallilos emp, LocalDate targetDate, int pastChildrenCount) {

        LocalDate startWorkDate = (emp.getImerominia_proslipsis() != null) ? emp.getImerominia_proslipsis() : LocalDate.now();
        if (targetDate.isBefore(startWorkDate)) {
            return 0.0;
        }

        if (emp.getStatus() == Ipallilos.Status.SYMVASIOUCHOS) {
            if (emp.getLixi_simbasis() != null && targetDate.isAfter(emp.getLixi_simbasis())) {
                return 0.0;
            }
            return emp.getMisthos_simbasis();
        }

        double basikos = (emp.getKatigoria() == Ipallilos.Katigoria.DIDAKTIKOS)
                ? getBasikosDidaktikos() : getBasikosDioikitikos();

        int yearsServiceThen = java.time.Period.between(startWorkDate, targetDate).getYears();

        double prosauksisiXronou = 0;
        if (yearsServiceThen > 1) {
            prosauksisiXronou = basikos * (yearsServiceThen * 0.15);
        }

        double familyBonus = 0;
        if (emp.isEggamos()) familyBonus += basikos * 0.05;
        familyBonus += basikos * (pastChildrenCount * 0.05);

        double extraBonus = (emp.getKatigoria() == Ipallilos.Katigoria.DIDAKTIKOS)
                ? getEpidomaEreunas() : getEpidomaVivliothikis();

        return basikos + prosauksisiXronou + familyBonus + extraBonus;
    }


    public static double getBasikosDioikitikos() { return BASIKOS_DIOIKITIKOS; }
    public static double getBasikosDidaktikos() { return BASIKOS_DIDAKTIKOS; }
    public static double getEpidomaEreunas() { return EPIDOMA_EREYNAS; }
    public static double getEpidomaVivliothikis() { return EPIDOMA_VIVLIOTHIKIS; }
}