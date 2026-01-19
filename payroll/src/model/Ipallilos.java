package model;

import java.time.LocalDate;

public class Ipallilos {

    public enum Katigoria{
        DIOIKITIKOS,
        DIDAKTIKOS,
    }
    public enum Status{
        MONIMOS,
        SYMVASIOUCHOS,
        SYNTAKSIOUCHOS //Mono oi monimoi einai syntaksiouxoi symfona me thn ekfonhsh
    }
    //ean pisteuoume oti mas voithaei mporoume na to xrhsimopoihsoume (px views)
//    public enum DepartmentType{
//        BIOLOGY,
//        CSD,
//        MATHS,
//        PHYSICS,
//        MATERIALS_SCIENCE,
//        PHILOSOPHY,
//        HISTORY_ARCHAEOLOGY,
//        MEDICINE,
//        PRIMARY_EDUCATION,
//        PRESCHOOL_EDUCATION,
//        ECONOMICS,
//        SOCIOLOGY,
//        POLITICAL_SCIENCE,
//        PSYCHOLOGY,
//        CHEMISTRY,
//        PHILOLOGY
//    }

    public String Onoma;
    public String Epitheto;
    public Status status;
    public Katigoria katigoria;
    public String IBAN;
    public String Dieuthinsi;
    public String Tilefono;
    public int AM;
    public String Onoma_Trapezas;
    public boolean Eggamos;
    public String Tmima;
//    public DepartmentType departmentType;
    public LocalDate Imerominia_proslipsis;
    //ean thelame na kanoume me klhronomikothta tha mporousame alla einai mperdema
    private int plithos_paidiwn;
    private LocalDate lixi_simbasis;
    private double misthos_simbasis;

    public Ipallilos(int AM, String Onoma, String Epitheto, Status status,
                     Katigoria katigoria, boolean Eggamos, LocalDate Imerominia_proslipsis,
                     int plithos_paidiwn, LocalDate lixi_simbasis, double misthos_simbasis,String Onoma_Trapezas) {
        this.AM = AM;
        this.Onoma = Onoma;
        this.Epitheto = Epitheto;
        this.status = status;
        this.katigoria = katigoria;
        this.Eggamos = Eggamos;
        this.Imerominia_proslipsis = Imerominia_proslipsis;
        this.plithos_paidiwn = plithos_paidiwn;
        this.lixi_simbasis = lixi_simbasis;
        this.misthos_simbasis = misthos_simbasis;
        this.Onoma_Trapezas = Onoma_Trapezas;
    }

    //getters
    public String getOnoma() {
        return Onoma;
    }
    public String getEpitheto() {
        return Epitheto;
    }
    public Katigoria getKatigoria() {
        return katigoria;
    }
    public Status getStatus() {
        return status;
    }
    public boolean isEggamos() {
        return Eggamos;
    }
    public int getPlithos_paidiwn() {
        return plithos_paidiwn;
    }

    public int getAM() {
        return AM;
    }

    public String getIBAN() {
        return IBAN;
    }

    public String getTilefono() {
        return Tilefono;
    }
    public String getOnoma_Trapezas(){return Onoma_Trapezas;}
    public LocalDate getImerominia_proslipsis() {
        return Imerominia_proslipsis;
    }
    public LocalDate getLixi_simbasis() {
        return lixi_simbasis;
    }
    public double getMisthos_simbasis() {
        return misthos_simbasis;
    }
}