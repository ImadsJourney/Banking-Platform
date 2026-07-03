package com.banking.banking_platform.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.banking.banking_platform.auth.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "accounts")
@Data
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "iban", nullable = false)
  private String iban;

  @Column(name = "balance", nullable = false)
  private BigDecimal balance;

  @Column(name = "daily_limit")
  private BigDecimal dailyLimit;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private AccountType type;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private StatusType status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @PrePersist
  void localDateTime() {
    createdAt = LocalDateTime.now();
  }

}
