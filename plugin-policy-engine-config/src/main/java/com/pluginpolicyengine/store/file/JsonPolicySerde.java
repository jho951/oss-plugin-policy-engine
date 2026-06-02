package com.pluginpolicyengine.store.file;

import com.pluginpolicyengine.core.PolicyDefinition;
import com.pluginpolicyengine.core.Targeting;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.*;

/**
 * 정책 정의용 내부 JSON 직렬화/역직렬화 유틸리티입니다.
 */
final class JsonPolicySerde {

	private final ObjectMapper om;

	/**
	 * 이 프로젝트 기본 설정으로 Jackson serde를 생성합니다.
	 */
	JsonPolicySerde() {
		this.om = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	/**
	 * 지원 포맷 2가지:
	 * 1) Map 형태:
	 * {
	 *   "checkout.newFlow": { ...defWithoutKeyOrWithKey... },
	 *   "search.ranking":   { ... }
	 * }
	 * 2) List 형태:
	 * [
	 *   { "key": "checkout.newFlow", ... },
	 *   { "key": "search.ranking", ... }
	 * ]
	 * @param json 객체 맵 또는 리스트 형식의 JSON 문자열
	 * @return 파싱된 정책 맵(입력이 잘못되었거나 읽을 수 없으면 빈 맵)
	 */
	Map<String, PolicyDefinition> parseToMap(String json) {
		if (isBlank(json)) return new HashMap<>();

		try {
			JsonNode root = om.readTree(json);
			Map<String, PolicyDefinition> out = new HashMap<>();

			if (root.isArray()) {
				for (JsonNode n : root) {
					PolicyDefinition def = toCore(n);
					if (def != null) out.put(def.key(), def);
				}
				return out;
			}

			if (root.isObject()) {
				Iterator<Map.Entry<String, JsonNode>> it = root.fields();
				while (it.hasNext()) {
					Map.Entry<String, JsonNode> e = it.next();
					String key = e.getKey();
					JsonNode node = e.getValue();

					PolicyDefinition def = toCore(node, key);
					if (def != null) out.put(def.key(), def);
				}
				return out;
			}

			return out;
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	private PolicyDefinition toCore(JsonNode node) {
		return toCore(node, null);
	}

	private PolicyDefinition toCore(JsonNode node, String keyFromMap) {
		if (node == null || node.isNull()) return null;

		String key = text(node, "key");
		if (isBlank(key)) key = keyFromMap;
		if (isBlank(key)) return null;

		boolean enabled = bool(node, "enabled", true);
		int rolloutPercent = integer(node, "rolloutPercent", 100);
		String defaultVariant = text(node, "defaultVariant");
		if (isBlank(defaultVariant)) defaultVariant = "on";

		Targeting targeting = parseTargeting(node.get("targeting"));

		PolicyDefinition.Builder b = PolicyDefinition.builder(key)
			.enabled(enabled)
			.rolloutPercent(rolloutPercent)
			.defaultVariant(defaultVariant)
			.targeting(targeting);

		JsonNode variants = node.get("variants");
		if (variants != null && variants.isArray()) {
			for (JsonNode v : variants) {
				String name = text(v, "name");
				int weight = integer(v, "weight", 0);
				if (!isBlank(name) && weight > 0) {
					b.variant(name, weight);
				}
			}
		}
		return b.build();
	}

	private Targeting parseTargeting(JsonNode t) {
		if (t == null || t.isNull()) return Targeting.allowAll();

		Targeting.Builder b = Targeting.builder();

		addAllStrings(t.get("allowUserIds"), b::allowUser);
		addAllStrings(t.get("denyUserIds"), b::denyUser);
		addAllStrings(t.get("allowGroups"), b::allowGroup);
		addAllStrings(t.get("denyGroups"), b::denyGroup);

		JsonNode requireAttrsIn = t.get("requireAttrsIn");
		if (requireAttrsIn != null && requireAttrsIn.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> it = requireAttrsIn.fields();
			while (it.hasNext()) {
				Map.Entry<String, JsonNode> e = it.next();
				String attrKey = e.getKey();
				Set<String> values = new HashSet<>();
				JsonNode arr = e.getValue();
				if (arr != null && arr.isArray()) {
					for (JsonNode v : arr) {
						if (v.isTextual()) values.add(v.asText());
					}
				}
				if (!values.isEmpty()) {
					b.requireAttrIn(attrKey, values);
				}
			}
		}
		return b.build();
	}

	private static void addAllStrings(JsonNode node, java.util.function.Consumer<String> add) {
		if (node == null || node.isNull()) return;
		if (node.isArray()) {
			for (JsonNode n : node) if (n.isTextual()) add.accept(n.asText());
		}
	}

	private static String text(JsonNode node, String field) {
		JsonNode v = node.get(field);
		return (v != null && v.isTextual()) ? v.asText() : null;
	}

	private static boolean bool(JsonNode node, String field, boolean def) {
		JsonNode v = node.get(field);
		return (v != null && v.isBoolean()) ? v.asBoolean() : def;
	}

	private static int integer(JsonNode node, String field, int def) {
		JsonNode v = node.get(field);
		return (v != null && v.canConvertToInt()) ? v.asInt() : def;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
