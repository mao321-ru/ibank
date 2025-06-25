package com.example.ibank.shared.notification;

import java.time.OffsetDateTime;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class EventCreate {

  private String source;

  private String eventType;

  private OffsetDateTime eventTime;

  private String userLogin;

  private String message;

}

