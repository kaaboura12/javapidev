package Models;

import java.time.LocalDate;

public class Donation {
    private int iddon;
    private int idevent;
    private Integer userid;
    private String donorname;
    private String email;
    private double montant;
    private LocalDate date;
    private String payment_method;
    private String num_tlf;
    private Event event;
    private User user;

    public Donation() {
    }

    public Donation(int idevent, Integer userid, String donorname, String email, 
                  double montant, LocalDate date, String payment_method, String num_tlf) {
        this.idevent = idevent;
        this.userid = userid;
        this.donorname = donorname;
        this.email = email;
        this.montant = montant;
        this.date = date;
        this.payment_method = payment_method;
        this.num_tlf = num_tlf;
    }

    public Donation(int iddon, int idevent, Integer userid, String donorname, String email, 
                  double montant, LocalDate date, String payment_method, String num_tlf) {
        this.iddon = iddon;
        this.idevent = idevent;
        this.userid = userid;
        this.donorname = donorname;
        this.email = email;
        this.montant = montant;
        this.date = date;
        this.payment_method = payment_method;
        this.num_tlf = num_tlf;
    }

    public Donation(Event event, Integer userid, String donorname, String email, 
                  double montant, LocalDate date, String payment_method, String num_tlf) {
        this.event = event;
        this.idevent = event.getIdevent();
        this.userid = userid;
        this.donorname = donorname;
        this.email = email;
        this.montant = montant;
        this.date = date;
        this.payment_method = payment_method;
        this.num_tlf = num_tlf;
    }

    public Donation(Event event, User user, String donorname, double montant, 
                  LocalDate date, String payment_method, String num_tlf) {
        this.event = event;
        this.idevent = event.getIdevent();
        this.user = user;
        this.userid = user.getUser_id();
        this.donorname = donorname;
        this.email = user.getEmail();
        this.montant = montant;
        this.date = date;
        this.payment_method = payment_method;
        this.num_tlf = num_tlf;
    }

    public int getIddon() {
        return iddon;
    }

    public void setIddon(int iddon) {
        this.iddon = iddon;
    }

    public int getIdevent() {
        return idevent;
    }

    public void setIdevent(int idevent) {
        this.idevent = idevent;
        if (this.event != null && this.event.getIdevent() != idevent) {
            this.event = null;
        }
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
        if (this.user != null && this.user.getUser_id() != userid) {
            this.user = null;
        }
    }

    public String getDonorname() {
        return donorname;
    }

    public void setDonorname(String donorname) {
        this.donorname = donorname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getNum_tlf() {
        return num_tlf;
    }

    public void setNum_tlf(String num_tlf) {
        this.num_tlf = num_tlf;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
        if (event != null) {
            this.idevent = event.getIdevent();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userid = user.getUser_id();
            this.email = user.getEmail();
        }
    }

    @Override
    public String toString() {
        return "Donation{" +
                "iddon=" + iddon +
                ", idevent=" + idevent +
                ", userid=" + userid +
                ", donorname='" + donorname + '\'' +
                ", email='" + email + '\'' +
                ", montant=" + montant +
                ", date=" + date +
                ", payment_method='" + payment_method + '\'' +
                ", num_tlf='" + num_tlf + '\'' +
                '}';
    }
} 