package com.ecommerce.project.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street Name must be atleat 5 character")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building Name must be atleat 5 character")
    private String buildingName;

    @NotBlank
    @Size(min = 4, message = "City name must be atleat 4 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State name must be atleat 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must be atleat 2 characters")
    private String country;

    @NotBlank
    @Size(min = 2, message = "Pincode name must be atleat 2 characters")
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();

    public Address(String street, String buildingName, List<User> users, String pincode, String country, String state, String city) {
        this.street = street;
        this.buildingName = buildingName;
        this.users = users;
        this.pincode = pincode;
        this.country = country;
        this.state = state;
        this.city = city;
    }
}
