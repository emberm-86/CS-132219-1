package com.test.assessment;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class LogService {

  EventRepository eventRepository;
  FileReader fileReader;

  public void saveFileContentToDatabase() {
    saveLogEventsToDatabase(fileReader.readFile());
  }

  public void saveLogEventsToDatabase(List<LogEvent> logEvents) {
    convertLogEventsToDbEvents(logEvents)
        .forEach(
            event -> {
              Event savedEvent = eventRepository.save(event);
              log.info("Event saved: " + savedEvent);
            });
    log.info("Number of log events recorded: {}", eventRepository.count());
  }

  public List<Event> convertLogEventsToDbEvents(List<LogEvent> logEvents) {
    Map<String, Map<String, LogEvent>> logEventMap =
        logEvents.stream()
            .collect(groupingBy(LogEvent::getState, toMap(LogEvent::getId, identity())));

    Map<String, LogEvent> startedMap = logEventMap.getOrDefault("STARTED", emptyMap());
    Map<String, LogEvent> finishedMap = logEventMap.getOrDefault("FINISHED", emptyMap());

    return startedMap.entrySet().stream()
        .map(startedLogEventEntry -> createEvent(startedLogEventEntry, finishedMap))
        .filter(Objects::nonNull)
        .collect(toList());
  }

  private Event createEvent(
      Entry<String, LogEvent> startedLogEventEntry, Map<String, LogEvent> finishedMap) {

    String startedLogEventId = startedLogEventEntry.getKey();
    LogEvent startedLogEvent = startedLogEventEntry.getValue();

    if (!finishedMap.containsKey(startedLogEventId)) {
      log.debug("No FINISHED record found with id: {}", startedLogEventId);
      return null;
    }

    LogEvent finishedLogEvent = finishedMap.get(startedLogEventId);

    if (!logEventTypeCheck().test(startedLogEvent, finishedLogEvent)) {
      log.debug("Type mismatch for log events: {},{}", startedLogEvent, finishedLogEvent);
      return null;
    }

    if (!timeStampCheck().test(startedLogEvent, finishedLogEvent)) {
      log.debug("Timestamp inconsistency for log events: {},{}", startedLogEvent, finishedLogEvent);
      return null;
    }

    long duration = finishedLogEvent.getTimestamp() - startedLogEvent.getTimestamp();
    boolean alert = duration > 4;

    return Event.builder()
        .logEventId(startedLogEventId)
        .duration(duration)
        .type(startedLogEvent.getType())
        .host(startedLogEvent.getHost())
        .alert(alert)
        .build();
  }

  private BiPredicate<LogEvent, LogEvent> logEventTypeCheck() {
    return (started, finished) ->
        (started.getType() == null && finished.getType() == null)
            || (started.getType() != null && started.getType().equals(finished.getType()));
  }

  private BiPredicate<LogEvent, LogEvent> timeStampCheck() {
    return (started, finished) -> finished.getTimestamp() >= started.getTimestamp();
  }
}
