package com.pluginpolicyengine.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class PolicyStoreTest {
	@Test
	void defaultFindAllReturnsEmptyImmutableMap() {
		PolicyStore store = key -> Optional.empty();

		assertThat(store.findAll()).isEmpty();
		assertThatThrownBy(() -> store.findAll().put("feature.one", PolicyDefinition.builder("feature.one").build()))
			.isInstanceOf(UnsupportedOperationException.class);
	}
}
