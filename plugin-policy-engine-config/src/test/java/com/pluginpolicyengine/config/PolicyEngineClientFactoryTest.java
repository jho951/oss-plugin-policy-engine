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
import com.pluginpolicyengine.core.PolicyDecision;
import com.pluginpolicyengine.core.PolicyContext;
import com.pluginpolicyengine.core.PolicyStore;
import com.pluginpolicyengine.core.store.InMemoryPolicyStore;
import com.pluginpolicyengine.store.file.JsonFilePolicyStore;

class PolicyEngineClientFactoryTest {
	@TempDir
	Path tempDir;

	@Test
	void createStoreUsesMemoryStoreByDefault() {
		assertThat(PolicyEngineClientFactory.createStore(null)).isInstanceOf(InMemoryPolicyStore.class);
		assertThat(PolicyEngineClientFactory.createStore(PolicyEngineConfig.memory())).isInstanceOf(InMemoryPolicyStore.class);
	}

	@Test
	void createStoreUsesJsonFilePolicyStoreForFileConfig() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		Files.write(policies, "{}".getBytes(StandardCharsets.UTF_8));

		PolicyStore store = PolicyEngineClientFactory.createStore(PolicyEngineConfig.builder()
			.store(PolicyEngineConfig.Store.FILE)
			.filePath(policies.toString())
			.cacheTtl(Duration.ZERO)
			.build());

		assertThat(store).isInstanceOf(JsonFilePolicyStore.class);
	}

	@Test
	void createsClientFromCustomStore() {
		PolicyStore store = key -> java.util.Optional.of(com.pluginpolicyengine.core.PolicyDefinition.builder(key).build());

		PolicyEngineClient client = PolicyEngineClientFactory.create(store);
		PolicyDecision decision = client.evaluate("feature.one", PolicyContext.builder().userId("user-1").build());

		assertThat(decision.allowed()).isTrue();
		assertThat(decision.reason()).isEqualTo("ROLLOUT_IN");
	}

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
