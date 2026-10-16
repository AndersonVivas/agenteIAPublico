package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class DatabaseSignalDetector extends LinePatternSignalDetector {

  private static final Set<String> EXTENSIONS = Set.of("java", "kt", "groovy", "scala",
      "properties", "yaml", "yml", "sql", "xml", "json");
  private static final Pattern DB_PATTERN = Pattern.compile(
      "\\bspring\\.datasource\\b|\\bjdbc:[^\\s]+|"
          + "\\bEntityManager\\b|\\bJpaRepository\\b|\\bCrudRepository\\b|"
          + "\\bMongoRepository\\b|\\bRedisTemplate\\b|\\bCassandraTemplate\\b|"
          + "\\bR2dbc\\b|\\bFlyway\\b|\\bLiquibase\\b|"
          + "@Entity\\b|"
          + "\\bSELECT\\b\\s+.*\\bFROM\\b|"
          + "\\bINSERT\\b\\s+\\bINTO\\b|"
          + "\\bUPDATE\\b\\s+\\w+\\s+\\bSET\\b|"
          + "\\bDELETE\\b\\s+\\bFROM\\b",
      Pattern.CASE_INSENSITIVE);

  public DatabaseSignalDetector() {
    super("DATABASE", EXTENSIONS, 200);
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    return DB_PATTERN.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    String normalized = normalize(line);
    if (normalized.contains("jdbc:") || normalized.contains("datasource")) {
      return "DB_CONNECTION";
    }
    if (normalized.contains("@entity") || normalized.contains("jparepository")
        || normalized.contains("crudrepository") || normalized.contains("mongorepository")) {
      return "DB_PERSISTENCE_MODEL";
    }
    if (normalized.contains("select") || normalized.contains("insert")
        || normalized.contains("update") || normalized.contains("delete")) {
      return "DB_SQL_STATEMENT";
    }
    return "DB_GENERIC";
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    return switch (matchedRule) {
      case "DB_CONNECTION" -> SignalProfile.of("DATASTORE_CONNECTION_CONFIG", "high",
          "startup", 0.99).withGuidance("Database connectivity or credential configuration issue.",
          "Check datasource secrets, network route, and database health.");
      case "DB_SQL_STATEMENT" -> SignalProfile.of("DATASTORE_QUERY_USAGE", "low", "runtime",
          0.88);
      default -> SignalProfile.of("DATASTORE_ACCESS_PATTERN", "low", "runtime", 0.88);
    };
  }
}
