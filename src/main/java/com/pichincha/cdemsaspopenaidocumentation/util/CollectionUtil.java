package com.pichincha.cdemsaspopenaidocumentation.util;

import java.util.List;
import java.util.stream.IntStream;

public final class CollectionUtil {

  private CollectionUtil() {
  }

  public static <T> List<List<T>> partition(List<T> values, int size) {
    int chunks = (values.size() + size - 1) / size;
    return IntStream.range(0, chunks)
        .mapToObj(index -> values.subList(index * size,
            Math.min(values.size(), (index + 1) * size)))
        .toList();
  }
}
