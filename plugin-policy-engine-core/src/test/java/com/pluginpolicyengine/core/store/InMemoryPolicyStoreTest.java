package com.pluginpolicyengine.core.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pluginpolicyengine.core.PolicyDefinition;

import org.junit.jupiter.api.Test;

class InMemoryPolicyStoreTest {
	@Test
	void storesFindsAndRemovesPolicyDefinitions() {
		InMemoryPolicyStore store = new InMemoryPolicyStore();
		PolicyDefinition definition = PolicyDefinition.builder("feature.one").build();

		store.put(definition);

		assertThat(store.find("feature.one")).contains(definition);
		assertThat(store.findAll()).containsEntry("feature.one", definition);

		store.remove("feature.one");

		assertThat(store.find("feature.one")).isEmpty();
	}

	@Test
	void findAllReturnsUnmodifiableMapView() {
		InMemoryPolicyStore store = new InMemoryPolicyStore();
		store.put(PolicyDefinition.builder("feature.one").build());

		assertThatThrownBy(() -> store.findAll().clear())
			.isInstanceOf(UnsupportedOperationException.class);
	}
}
