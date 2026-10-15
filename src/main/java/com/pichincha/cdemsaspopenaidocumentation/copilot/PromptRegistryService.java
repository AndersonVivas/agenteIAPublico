package com.pichincha.cdemsaspopenaidocumentation.copilot;

/**
 * Resolves prompt content from an externally stored location. No prompt text is ever
 * hardcoded in source code; prompts are authored and versioned outside the deployable
 * artifact so they can change without a recompile or redeploy.
 */
public interface PromptRegistryService {

  String loadPrompt(String promptId);
}
