package com.test.assessment;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ApplicationTests {

  @Autowired
  LogService logService;

  @Autowired
  EventRepository eventRepository;

  @ParameterizedTest
  @MethodSource("createLogEventData")
  public void testConversion(List<LogEvent> logEvents) {
    Event expectedA = Event.builder()
        .logEventId("scsmbstgra")
        .duration(5L)
        .type("APPLICATION_LOG")
        .host("12345")
        .alert(true).build();

    Event expectedB = Event.builder()
        .logEventId("scsmbstgrb")
        .duration(3L)
        .alert(false).build();

    Event expectedC = Event.builder()
        .logEventId("scsmbstgrc")
        .duration(8L)
        .alert(true).build();

    List<Event> events = logService.convertLogEventsToDbEvents(logEvents);

    assertEquals(3, events.size());
    assertEquals(events, asList(expectedA, expectedB, expectedC));
  }

  @ParameterizedTest
  @MethodSource("createLogEventData")
  public void testSavingToDatabase(List<LogEvent> logEvents) {
    logService.saveLogEventsToDatabase(logEvents);

    assertEquals(3, eventRepository.count());
  }

  private static Stream<Arguments> createLogEventData() {
    return Stream.of(
        Arguments.of(
            asList(
                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("STARTED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495212L).build(),

                LogEvent.builder()
                    .id("scsmbstgrb")
                    .state("STARTED")
                    .timestamp(1491377495213L).build(),

                LogEvent.builder()
                    .id("scsmbstgrc")
                    .state("FINISHED")
                    .timestamp(1491377495218L).build(),

                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("FINISHED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495217L).build(),

                LogEvent.builder()
                    .id("scsmbstgrc")
                    .state("STARTED")
                    .timestamp(1491377495210L).build(),

                LogEvent.builder()
                    .id("scsmbstgrb")
                    .state("FINISHED")
                    .timestamp(1491377495216L).build())));
  }

  @ParameterizedTest
  @MethodSource("createLogEventDataForFailure")
  public void testNegativeCases(List<LogEvent> logEvents) {
    List<Event> events = logService.convertLogEventsToDbEvents(logEvents);

    assertEquals(events, emptyList());
  }

  private static Stream<Arguments> createLogEventDataForFailure() {
    return Stream.of(
        Arguments.of( // check for different types
            asList(
                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("STARTED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495212L).build(),

                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("FINISHED")
                    .host("12345")
                    .timestamp(1491377495214L).build())),

        Arguments.of( // check for earlier finished record
            asList(
                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("STARTED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495212L).build(),

                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("FINISHED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495211L).build())),

        Arguments.of( // check for no corresponding finished record
            singletonList(
                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("STARTED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495212L).build())),

        Arguments.of( // check for no corresponding started record
            singletonList(
                LogEvent.builder()
                    .id("scsmbstgra")
                    .state("FINISHED")
                    .type("APPLICATION_LOG")
                    .host("12345")
                    .timestamp(1491377495212L).build())));
  }
}
