package com.pluginpolicyengine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.pluginpolicyengine.api.PolicyEngineClient;
import com.pluginpolicyengine.core.PolicyContext;

class PolicyEngineClientFactoryTest {
	@TempDir
	Path tempDir;

	@Test
	void createsMemoryClientByDefault() {
		PolicyEngineClient client = PolicyEngineClientFactory.create((PolicyEngineConfig) null);

		assertThat(client.isAllowed("missing", PolicyContext.builder().userId("user-1").build())).isFalse();
	}

	@Test
	void createsFileBackedClient() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		String json = "{\n"
			+ "  \"checkout.newFlow\": {\n"
			+ "    \"enabled\": true,\n"
			+ "    \"rolloutPercent\": 100\n"
			+ "  }\n"
			+ "}\n";
		Files.write(policies, json.getBytes(StandardCharsets.UTF_8));

		PolicyEngineClient client = PolicyEngineClientFactory.create(
			PolicyEngineConfig.builder()
				.store(PolicyEngineConfig.Store.FILE)
				.filePath(policies.toString())
				.cacheTtl(Duration.ZERO)
				.build()
		);

		assertThat(client.isAllowed("checkout.newFlow", PolicyContext.builder().userId("user-1").build())).isTrue();
	}

	@Test
	void rejectsFileStoreWithoutPath() {
		PolicyEngineConfig config = PolicyEngineConfig.builder()
			.store(PolicyEngineConfig.Store.FILE)
			.build();

		assertThatThrownBy(() -> PolicyEngineClientFactory.create(config))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
