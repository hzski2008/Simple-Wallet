package com.example.wallet.model;

import com.example.wallet.commontypes.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
//import org.springframework.data.annotation.Version;

@Entity
@Table(name = "events")
public class Event implements Serializable {

  @Schema(description = "Unique identifier of the transaction.", 
    example = "1", required = true)  
  @Id
  //@GeneratedValue(strategy=GenerationType.IDENTITY)
  @NotNull(message = "eventId is mandatory")
  private Long eventId; //event id
  
  @Schema(description = "Unique identifier of the user account.", 
    example = "1")
  //@NotNull(message = "userId is mandatory")
  private Long userId; //user Id
  
  @Schema(description = "Transaction type, either 'profit' or 'purchase'", 
    example = "purchase", required = true)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "eventType is mandatory")
  private EventType eventType;
  
  @Schema(description = "Amount to be deduced or added to user balance", 
    example = "100.0", required = true)
  @NotNull(message = "amount can not be empty")
  @DecimalMin(value = "0.0", inclusive = false, message = "amount must be positive")
  private Double amount;
  
  @Schema(description = "Transaction timestamp. It should be sent from client.", 
    example = "2020-12-20T16:41:07.438Z", required = true)
  @NotNull(message = "Timestamp must be provided")
  @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
  private Date timestamp;

  protected Event() {}

  public Event(Long eventId, Long userId, EventType eventType, Double amount, Date timeStamp) {
    this.eventId = eventId;
    this.userId = userId;
    this.eventType = eventType;
    this.amount = amount;
    this.timestamp = timeStamp;
  }

  // copy contructor
  public Event(Event that) {
    this(that.getEventId(), that.getUserId(), that.getEventType(), that.getAmount(), that.getTimestamp());
  }
  private String convertDateToString(Date ts) {
    String pattern = "MM/dd/yyyy HH:mm:ss";
    DateFormat df = new SimpleDateFormat(pattern);
    return df.format(ts);
  }

  @Override
  public String toString() {
    String ts = this.convertDateToString(this.timestamp);

    return String.format(
      "Event[eventId=%d, userId='%d', amaount='%.2f', transaction type='%s', timestamp='%s']",
      eventId, userId, amount, eventType, ts);
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public EventType getEventType() {
    return this.eventType;
  }

  public void setEventType(EventType type) {
    this.eventType = type;
  }

  public Date getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(Date ts) {
    this.timestamp = ts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Event))
      return false;
    Event event = (Event) o;
    return Objects.equals(this.eventId, event.eventId) && Objects.equals(this.userId, event.userId)
      && Objects.equals(this.amount, event.amount) && Objects.equals(this.eventType, event.eventType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.userId, this.eventId, this.amount, this.eventType);
  }
}