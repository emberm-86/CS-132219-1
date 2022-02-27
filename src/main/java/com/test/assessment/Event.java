package com.test.assessment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;
  String logEventId;
  Long duration;
  String type;
  String host;
  Boolean alert;

  public static Event.Builder builder() {
    return new Event.Builder();
  }

  static class Builder {

    String logEventId;
    Long duration;
    String type;
    String host;
    Boolean alert;

    public Event.Builder logEventId(String logEventId) {
      this.logEventId = logEventId;
      return this;
    }

    public Event.Builder duration(Long duration) {
      this.duration = duration;
      return this;
    }

    public Event.Builder type(String type) {
      this.type = type;
      return this;
    }

    public Event.Builder host(String host) {
      this.host = host;
      return this;
    }

    public Event.Builder alert(Boolean alert) {
      this.alert = alert;
      return this;
    }

    public Event build() {
      Event dbEvent = new Event();
      dbEvent.setLogEventId(logEventId);
      dbEvent.setDuration(duration);
      dbEvent.setType(type);
      dbEvent.setHost(host);
      dbEvent.setAlert(alert);
      return dbEvent;
    }
  }
}