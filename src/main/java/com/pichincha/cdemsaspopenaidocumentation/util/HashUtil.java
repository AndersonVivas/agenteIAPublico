package com.pichincha.cdemsaspopenaidocumentation.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {

  private static final String ALGORITHM = "SHA-256";

  private HashUtil() {
  }

  public static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available in this JVM.", exception);
    }
  }

  public static String shortSha256Hex(String value, int length) {
    return sha256Hex(value).substring(0, length);
  }
}
