package com.gemalto.cota.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class UserRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String requestStatus;
    private boolean status;
    private String comment;
    private String organization;
    private String tempData;

    // avoid this "No default constructor for entity"


}