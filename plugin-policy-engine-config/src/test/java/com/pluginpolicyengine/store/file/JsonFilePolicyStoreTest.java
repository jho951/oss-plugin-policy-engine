package com.pluginpolicyengine.store.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.pluginpolicyengine.core.PolicyContext;
import com.pluginpolicyengine.core.PolicyDefinition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonFilePolicyStoreTest {
	@TempDir
	Path tempDir;

	@Test
	void rejectsBlankFilePath() {
		assertThatThrownBy(() -> new JsonFilePolicyStore("  ", Duration.ZERO))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void findAllReadsMapShapedJsonWithTargetingAndVariants() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writeJson(policies, "{\n"
			+ "  \"checkout.newFlow\": {\n"
			+ "    \"enabled\": true,\n"
			+ "    \"rolloutPercent\": 75,\n"
			+ "    \"defaultVariant\": \"control\",\n"
			+ "    \"variants\": [\n"
			+ "      {\"name\": \"control\", \"weight\": 30},\n"
			+ "      {\"name\": \"treatment\", \"weight\": 70}\n"
			+ "    ],\n"
			+ "    \"targeting\": {\n"
			+ "      \"allowUserIds\": [\"user-1\"],\n"
			+ "      \"denyGroups\": [\"blocked\"],\n"
			+ "      \"requireAttrsIn\": {\"region\": [\"KR\", \"US\"]}\n"
			+ "    }\n"
			+ "  }\n"
			+ "}\n");
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.findAll()).containsOnlyKeys("checkout.newFlow");
		PolicyDefinition definition = store.find("checkout.newFlow").get();
		assertThat(definition.enabled()).isTrue();
		assertThat(definition.rolloutPercent()).isEqualTo(75);
		assertThat(definition.defaultVariant()).isEqualTo("control");
		assertThat(definition.variants()).extracting(PolicyDefinition.VariantAllocation::name)
			.containsExactly("control", "treatment");
		assertThat(definition.targeting().isExplicitlyAllowed(PolicyContext.builder().userId("user-1").build()))
			.isTrue();
		assertThat(definition.targeting().isExplicitlyDenied(PolicyContext.builder().group("blocked").build()))
			.isTrue();
		assertThat(definition.targeting().matchesEligibility(PolicyContext.builder()
			.userId("user-1")
			.attr("region", "KR")
			.build()))
			.isTrue();
	}

	@Test
	void innerJsonKeyOverridesMapKey() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writeJson(policies, "{\n"
			+ "  \"outer.key\": {\"key\": \"inner.key\", \"enabled\": true}\n"
			+ "}\n");
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.findAll()).containsOnlyKeys("inner.key");
		assertThat(store.find("outer.key")).isEmpty();
		assertThat(store.find("inner.key")).get().extracting(PolicyDefinition::key).isEqualTo("inner.key");
	}

	@Test
	void skipsInvalidVariantsAndUsesDefaultsForWrongTypes() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writeJson(policies, "{\n"
			+ "  \"feature.one\": {\n"
			+ "    \"enabled\": \"true\",\n"
			+ "    \"rolloutPercent\": \"not-number\",\n"
			+ "    \"defaultVariant\": 123,\n"
			+ "    \"variants\": [\n"
			+ "      {\"name\": \"control\", \"weight\": 0},\n"
			+ "      {\"name\": \"treatment\", \"weight\": 10},\n"
			+ "      {\"name\": 123, \"weight\": 10}\n"
			+ "    ],\n"
			+ "    \"targeting\": {\n"
			+ "      \"allowUserIds\": \"user-1\",\n"
			+ "      \"requireAttrsIn\": {\"region\": \"KR\", \"plan\": [\"PRO\"]}\n"
			+ "    }\n"
			+ "  }\n"
			+ "}\n");
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		PolicyDefinition definition = store.find("feature.one").get();
		assertThat(definition.enabled()).isTrue();
		assertThat(definition.rolloutPercent()).isEqualTo(100);
		assertThat(definition.defaultVariant()).isEqualTo("on");
		assertThat(definition.variants()).hasSize(1);
		assertThat(definition.variants().get(0).name()).isEqualTo("treatment");
		assertThat(definition.targeting().matchesEligibility(PolicyContext.builder()
			.attr("plan", "PRO")
			.build()))
			.isTrue();
	}

	@Test
	void parsesListShapedJsonAndIgnoresEntriesWithoutKeys() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writeJson(policies, "[\n"
			+ "  {\"key\": \"search.ranking\", \"enabled\": false, \"rolloutPercent\": -10},\n"
			+ "  {\"enabled\": true}\n"
			+ "]\n");
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.findAll()).containsOnlyKeys("search.ranking");
		PolicyDefinition definition = store.find("search.ranking").get();
		assertThat(definition.enabled()).isFalse();
		assertThat(definition.rolloutPercent()).isZero();
		assertThat(definition.defaultVariant()).isEqualTo("on");
	}

	@Test
	void blankFindKeyReturnsEmptyWithoutReadingFile() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writePolicy(policies, "checkout.newFlow", true);
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.find(null)).isEmpty();
		assertThat(store.find("  ")).isEmpty();
	}

	@Test
	void returnsEmptyResultsWhenFileDoesNotExist() {
		JsonFilePolicyStore store = new JsonFilePolicyStore(tempDir.resolve("missing.json").toString(), Duration.ZERO);

		assertThat(store.find("missing")).isEmpty();
		assertThat(store.findAll()).isEmpty();
		assertThatThrownBy(() -> store.findAll().clear())
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void malformedJsonFileReturnsEmptyResults() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writeJson(policies, "{not-json");
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.findAll()).isEmpty();
	}

	@Test
	void unsupportedJsonDocumentReturnsEmptyResults() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writeJson(policies, "\"not-a-policy-document\"");
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.findAll()).isEmpty();
	}

	@Test
	void reloadsFileOnEveryReadWhenTtlIsZero() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writePolicy(policies, "checkout.newFlow", true);
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ZERO);

		assertThat(store.find("checkout.newFlow")).get().extracting("enabled").isEqualTo(true);

		writePolicy(policies, "checkout.newFlow", false);

		assertThat(store.find("checkout.newFlow")).get().extracting("enabled").isEqualTo(false);
	}

	@Test
	void usesCachedSnapshotWhileTtlHasNotExpired() throws Exception {
		Path policies = tempDir.resolve("policies.json");
		writePolicy(policies, "checkout.newFlow", true);
		JsonFilePolicyStore store = new JsonFilePolicyStore(policies.toString(), Duration.ofHours(1));

		assertThat(store.find("checkout.newFlow")).get().extracting("enabled").isEqualTo(true);

		writePolicy(policies, "checkout.newFlow", false);

		assertThat(store.find("checkout.newFlow")).get().extracting("enabled").isEqualTo(true);
	}

	private static void writePolicy(Path path, String key, boolean enabled) throws Exception {
		String json = "{\n"
			+ "  \"" + key + "\": {\n"
			+ "    \"enabled\": " + enabled + ",\n"
			+ "    \"rolloutPercent\": 100\n"
			+ "  }\n"
			+ "}\n";
		writeJson(path, json);
	}

	private static void writeJson(Path path, String json) throws Exception {
		Files.write(path, json.getBytes(StandardCharsets.UTF_8));
	}
}
