package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.ServiceStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class NurseService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    @ManyToOne
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurse;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "availability_schedule", nullable = false)
    private String availabilitySchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceStatus status = ServiceStatus.ACTIVE;

    // Getters, setters

    public Integer getServiceId() {
        return serviceId;
    }

    public User getNurse() {
        return nurse;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getAvailabilitySchedule() {
        return availabilitySchedule;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setAvailabilitySchedule(String availabilitySchedule) {
        this.availabilitySchedule = availabilitySchedule;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public NurseService() {}

    public NurseService(User nurse, String serviceName, String description, Double price, String availabilitySchedule, ServiceStatus status) {
        this.nurse = nurse;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.availabilitySchedule = availabilitySchedule;
        this.status = status;
    }
}

