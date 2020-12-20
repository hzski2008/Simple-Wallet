package com.example.wallet.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "accounts")
public class Account implements Serializable {
  @Schema(description = "Unique identifier of the user account.", 
    example = "1", required = true)  
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;
  
  @Schema(description = "Name of the user.", 
    example = "John Smith", required = true)
  @NotBlank(message = "Name is mandatory")
  private String name;
  
  @Schema(description = "Account balance. Must be 0 or a positive number", 
    example = "200.55", required = true)
  @DecimalMin(value = "0.0", inclusive = true, message = "amount can not be negative")
  private Double balance;

  @Schema(description = "Optimistic lock for concurrency")
  @Version
  private Long version;

  protected Account() {}

  public Account(String name, Double balance) {
    this.name = name;
    this.balance = balance;
  }

  // copy constructor
  public Account(Account that) {
    this(that.getName(), that.getBalance());
    this.setId(that.getId());
  }

  @Override
  public String toString() {
    return String.format("Account[id=%d, name='%s', balance='%.2f']", id, name, balance);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getBalance() {
    return balance;
  }

  public void setBalance(Double balance) {
    this.balance = balance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Account))
      return false;
    Account user = (Account) o;
    return Objects.equals(this.id, user.id) && Objects.equals(this.name, user.name)
      && Objects.equals(this.balance, user.balance);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Objects.hashCode(this.id);
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + Objects.hashCode(this.balance);
    return hash;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}