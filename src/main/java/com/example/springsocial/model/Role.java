package com.example.springsocial.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "roles")
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    public Integer getId() {
        return id;
    }

    public ERole getName() {
        return name;
    }
}