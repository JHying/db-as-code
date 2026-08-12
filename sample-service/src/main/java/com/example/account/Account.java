package com.example.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps the ACCOUNT table.
 * <p>
 * The {@code @Column} set here must match the published DDL exactly: with
 * {@code spring.jpa.hibernate.ddl-auto=validate}, any divergence (a field with
 * no backing column, or a type mismatch) fails application start-up. That is
 * Layer 2 of the local protection described in the project README.
 * <p>
 * Entity conventions: boxed types only (never primitives); NUMBER(1) maps
 * to Boolean; NUMBER(18,4) carries explicit precision/scale.
 */
@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "SEQACCOUNT", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", length = 64, nullable = false)
    private String username;

    @Column(name = "NICKNAME", length = 128)
    private String nickname;

    @Column(name = "BALANCE", precision = 18, scale = 4, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    /** Added by pending change PROJ-101 (NUMBER(1) -> Boolean). */
    @Column(name = "STATUS", nullable = false)
    private Boolean status = Boolean.FALSE;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    protected Account() {
        // for JPA
    }

    public Account(String username, String nickname, BigDecimal balance, Boolean status) {
        this.username = username;
        this.nickname = nickname;
        this.balance = balance;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getStatus() {
        return status;
    }
}
