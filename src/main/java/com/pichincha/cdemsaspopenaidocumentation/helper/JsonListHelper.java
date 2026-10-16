package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JsonListHelper {

  private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() { };

  private final ObjectMapper objectMapper = new ObjectMapper();

  public String write(List<String> values) {
    try {
      return objectMapper.writeValueAsString(values == null ? List.of() : values);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to serialize list value.", exception);
    }
  }

  public List<String> read(String json) {
    if (StringUtils.isBlank(json)) {
      return List.of();
    }
    try {
      return List.copyOf(objectMapper.readValue(json, STRING_LIST));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to deserialize list value.", exception);
    }
  }
}
