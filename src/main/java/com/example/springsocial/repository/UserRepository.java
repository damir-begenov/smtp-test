package com.example.springsocial.repository;

import com.example.springsocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByName(String user);


    @Query(value = "select * FROM users where name like ?1 limit 1", nativeQuery = true)
    User findByUsernameTwo(String username);

    @Query(value= "select * from users where name like '%' || ?1 || '%' or email like '%' || ?1 || '%'", nativeQuery = true)
    List<User> getUsersByLike(String value);
    @Modifying
    @Query(value = "update user_roles set role_id = ?1 where user_id = ?2", nativeQuery = true)
    void updateRole(Integer role, Integer user);

    @Query(value = "select r.name from roles r where r.id = (select role_id from user_roles where user_id = (select id from users where name = ?1))", nativeQuery = true)
    String getRoleById(String username);

    @Query(value = "select count(*) from users", nativeQuery = true)
    Integer getUserNum();
    Boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.verificationCode = ?1")
    User findByVerificationCode(String code);
}
