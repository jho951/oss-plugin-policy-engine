package com.pluginpolicyengine.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class PolicyEngineConfigTest {
	@Test
	void memoryFactoryUsesMemoryStoreDefaults() {
		PolicyEngineConfig config = PolicyEngineConfig.memory();

		assertThat(config.store()).isEqualTo(PolicyEngineConfig.Store.MEMORY);
		assertThat(config.filePath()).isNull();
		assertThat(config.cacheTtl()).isEqualTo(Duration.ofSeconds(3));
	}

	@Test
	void fileFactoryUsesFileStoreAndDefaultTtl() {
		PolicyEngineConfig config = PolicyEngineConfig.file("/tmp/policies.json");

		assertThat(config.store()).isEqualTo(PolicyEngineConfig.Store.FILE);
		assertThat(config.filePath()).isEqualTo("/tmp/policies.json");
		assertThat(config.cacheTtl()).isEqualTo(Duration.ofSeconds(3));
	}

	@Test
	void builderTreatsNullStoreAsMemoryAndNullTtlAsDefault() {
		PolicyEngineConfig config = PolicyEngineConfig.builder()
			.store(null)
			.cacheTtl(null)
			.build();

		assertThat(config.store()).isEqualTo(PolicyEngineConfig.Store.MEMORY);
		assertThat(config.cacheTtl()).isEqualTo(Duration.ofSeconds(3));
	}

	@Test
	void builderAcceptsCustomCacheTtl() {
		PolicyEngineConfig config = PolicyEngineConfig.builder()
			.store(PolicyEngineConfig.Store.FILE)
			.filePath("policies.json")
			.cacheTtl(Duration.ZERO)
			.build();

		assertThat(config.store()).isEqualTo(PolicyEngineConfig.Store.FILE);
		assertThat(config.filePath()).isEqualTo("policies.json");
		assertThat(config.cacheTtl()).isEqualTo(Duration.ZERO);
	}
}
