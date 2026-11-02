package com.cinema.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "hall_type", nullable = false)
    private HallType hallType = HallType.TWO_D;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public enum HallType {
        TWO_D("2D"), THREE_D("3D"), IMAX("IMAX"), FOUR_DX("4DX");

        private final String value;
        HallType(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Cinema getCinema() { return cinema; }
    public void setCinema(Cinema cinema) { this.cinema = cinema; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public HallType getHallType() { return hallType; }
    public void setHallType(HallType hallType) { this.hallType = hallType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}