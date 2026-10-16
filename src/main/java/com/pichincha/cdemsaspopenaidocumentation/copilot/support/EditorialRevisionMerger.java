package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Applies the editorial revisions over the drafted sections. A revision is accepted only when
 * it cites exactly the same evidence ids as its draft, so the editor can improve the wording
 * but never add, drop or replace the evidence behind a statement.
 */
public final class EditorialRevisionMerger {

  private static final Pattern EVIDENCE_ID = Pattern.compile("EV-[0-9a-fA-F]+");

  private EditorialRevisionMerger() {
  }

  public static RenderedSections merge(RenderedSections drafts, Map<String, String> revisions) {
    Map<String, String> bodies = new LinkedHashMap<>(drafts.bodiesBySectionId());
    List<String> warnings = new ArrayList<>(drafts.warnings());
    revisions.forEach((sectionId, revision) -> {
      if (evidenceIds(revision).equals(evidenceIds(bodies.get(sectionId)))) {
        bodies.put(sectionId, revision);
      } else {
        warnings.add("Editorial revision of section '" + sectionId
            + "' was discarded because it changed the cited evidence ids.");
      }
    });
    return new RenderedSections(bodies, warnings);
  }

  private static Set<String> evidenceIds(String markdown) {
    return markdown == null ? Set.of() : EVIDENCE_ID.matcher(markdown).results()
        .map(MatchResult::group).collect(Collectors.toSet());
  }
}
