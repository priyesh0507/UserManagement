package com.gemalto.cota.repo;

import java.util.List;

import com.gemalto.cota.entities.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {

    List<UserRequest> findAllByRequestStatus(String requestStatus);
    
    List<UserRequest> findAllByOrganization(String organization);

}