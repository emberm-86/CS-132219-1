package com.test.assessment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileReader {

  final ObjectMapper objectMapper;
  final ResourceLoader resourceLoader;

  @Value("${log.file.name}")
  String fileName;

  public List<LogEvent> readFile() {
    Resource resource = resourceLoader.getResource(fileName);
    List<LogEvent> result = new LinkedList<>();

    try (Scanner scanner = new Scanner(resource.getInputStream())) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        LogEvent logEvent = objectMapper.readValue(line, LogEvent.class);
        result.add(logEvent);
      }
    } catch (IOException e) {
      log.error(e.getLocalizedMessage(), e);
    }
    return result;
  }
}