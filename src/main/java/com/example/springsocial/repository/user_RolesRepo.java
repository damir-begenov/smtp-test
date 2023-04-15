package com.example.springsocial.repository;

import com.example.springsocial.model.user_roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface user_RolesRepo extends JpaRepository<user_roles, Long> {

}
