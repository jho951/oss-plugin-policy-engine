package com.pluginpolicyengine.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pluginpolicyengine.api.FeatureFlagClient;
import com.pluginpolicyengine.core.FlagContext;

class FeatureFlagClientFactoryTest {
	@TempDir
	Path tempDir;

	@Test
	void createsMemoryClientByDefault() {
		FeatureFlagClient client = FeatureFlagClientFactory.create((FeatureFlagConfig) null);

		assertFalse(client.isEnabled("missing", FlagContext.builder().userId("user-1").build()));
	}

	@Test
	void createsFileBackedClient() throws Exception {
		Path flags = tempDir.resolve("flags.json");
		Files.writeString(flags, """
			{
			  "checkout.newFlow": {
			    "enabled": true,
			    "rolloutPercent": 100
			  }
			}
			""");

		FeatureFlagClient client = FeatureFlagClientFactory.create(
			FeatureFlagConfig.builder()
				.store(FeatureFlagConfig.Store.FILE)
				.filePath(flags.toString())
				.cacheTtl(Duration.ZERO)
				.build()
		);

		assertTrue(client.isEnabled("checkout.newFlow", FlagContext.builder().userId("user-1").build()));
	}

	@Test
	void rejectsFileStoreWithoutPath() {
		FeatureFlagConfig config = FeatureFlagConfig.builder()
			.store(FeatureFlagConfig.Store.FILE)
			.build();

		assertThrows(IllegalArgumentException.class, () -> FeatureFlagClientFactory.create(config));
	}
}
