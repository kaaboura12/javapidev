package Models;

import java.time.LocalDate;

public class Reservation {
    private int id_reservation;
    private Integer userid;
    private Integer idevent;
    private LocalDate reservation_date;
    private int seats_reserved;
    private double total_amount;
    private User user;
    private Event event;

    public Reservation() {
    }

    public Reservation(Integer userid, Integer idevent, LocalDate reservation_date, 
                     int seats_reserved, double total_amount) {
        this.userid = userid;
        this.idevent = idevent;
        this.reservation_date = reservation_date;
        this.seats_reserved = seats_reserved;
        this.total_amount = total_amount;
    }

    public Reservation(int id_reservation, Integer userid, Integer idevent, 
                     LocalDate reservation_date, int seats_reserved, double total_amount) {
        this.id_reservation = id_reservation;
        this.userid = userid;
        this.idevent = idevent;
        this.reservation_date = reservation_date;
        this.seats_reserved = seats_reserved;
        this.total_amount = total_amount;
    }

    public Reservation(User user, Event event, LocalDate reservation_date, 
                     int seats_reserved, double total_amount) {
        this.user = user;
        this.userid = user.getUser_id();
        this.event = event;
        this.idevent = event.getIdevent();
        this.reservation_date = reservation_date;
        this.seats_reserved = seats_reserved;
        this.total_amount = total_amount;
    }

    public int getId_reservation() {
        return id_reservation;
    }

    public void setId_reservation(int id_reservation) {
        this.id_reservation = id_reservation;
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

    public Integer getIdevent() {
        return idevent;
    }

    public void setIdevent(Integer idevent) {
        this.idevent = idevent;
        if (this.event != null && this.event.getIdevent() != idevent) {
            this.event = null;
        }
    }

    public LocalDate getReservation_date() {
        return reservation_date;
    }

    public void setReservation_date(LocalDate reservation_date) {
        this.reservation_date = reservation_date;
    }

    public int getSeats_reserved() {
        return seats_reserved;
    }

    public void setSeats_reserved(int seats_reserved) {
        this.seats_reserved = seats_reserved;
        // Update total_amount if event is available
        if (this.event != null) {
            this.total_amount = seats_reserved * this.event.getSeatprice();
        }
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userid = user.getUser_id();
        }
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
        if (event != null) {
            this.idevent = event.getIdevent();
            // Update total_amount based on the event's seat price
            this.total_amount = this.seats_reserved * event.getSeatprice();
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id_reservation=" + id_reservation +
                ", userid=" + userid +
                ", idevent=" + idevent +
                ", reservation_date=" + reservation_date +
                ", seats_reserved=" + seats_reserved +
                ", total_amount=" + total_amount +
                '}';
    }
} 