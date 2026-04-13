package com.pluginpolicyengine.core;

import java.util.*;

/**
 * 허용/차단 및 속성 기반 자격 조건을 정의하는 타게팅 규칙입니다.
 */
public final class Targeting {
	private final Set<String> allowUserIds;
	private final Set<String> denyUserIds;
	private final Set<String> allowGroups;
	private final Set<String> denyGroups;
	private final Map<String, Set<String>> requireAttrsIn; // attrKey -> allowedValues

	private Targeting(Builder b) {
		this.allowUserIds = unmodSet(b.allowUserIds);
		this.denyUserIds = unmodSet(b.denyUserIds);
		this.allowGroups = unmodSet(b.allowGroups);
		this.denyGroups = unmodSet(b.denyGroups);
		this.requireAttrsIn = unmodMapSet(b.requireAttrsIn);
	}

	private static Set<String> unmodSet(Set<String> s) { return Collections.unmodifiableSet(new HashSet<>(s)); }
	private static Map<String, Set<String>> unmodMapSet(Map<String, Set<String>> m) {
		Map<String, Set<String>> out = new HashMap<>();
		for (var e : m.entrySet()) out.put(e.getKey(), Collections.unmodifiableSet(new HashSet<>(e.getValue())));
		return Collections.unmodifiableMap(out);
	}

	/**
	 * 규칙이 없는 타게팅을 생성합니다.
	 *
	 * @return 별도 차단 규칙이 없다면 모두 허용하는 타게팅
	 */
	public static Targeting allowAll() { return builder().build(); }

	/**
	 * 타게팅 빌더를 생성합니다.
	 *
	 * @return 타게팅 빌더
	 */
	public static Builder builder() { return new Builder(); }

	/**
	 * 사용자/그룹 규칙에 의해 명시적으로 차단되는지 확인합니다.
	 *
	 * @param ctx 요청 컨텍스트
	 * @return 차단 규칙과 일치하면 {@code true}
	 */
	public boolean isExplicitlyDenied(FlagContext ctx) {
		String uid = ctx.userId();
		if (uid != null && denyUserIds.contains(uid)) return true;
		for (String g : ctx.groups()) if (denyGroups.contains(g)) return true;
		return false;
	}

	/**
	 * 사용자/그룹 규칙에 의해 명시적으로 허용되는지 확인합니다.
	 *
	 * @param ctx 요청 컨텍스트
	 * @return 허용 규칙과 일치하면 {@code true}
	 */
	public boolean isExplicitlyAllowed(FlagContext ctx) {
		String uid = ctx.userId();
		if (uid != null && allowUserIds.contains(uid)) return true;
		for (String g : ctx.groups()) if (allowGroups.contains(g)) return true;
		return false;
	}

	/**
	 * eligibility 평가가 필요한 타게팅 조건이 하나라도 있는지 확인합니다.
	 *
	 * @return allow 사용자/그룹 또는 속성 조건이 있으면 {@code true}
	 */
	public boolean hasEligibilityRules() {
		return !allowUserIds.isEmpty() || !allowGroups.isEmpty() || !requireAttrsIn.isEmpty();
	}

	/**
	 * 컨텍스트가 자격(eligibility) 규칙을 만족하는지 확인합니다.
	 *
	 * @param ctx 요청 컨텍스트
	 * @return 자격 조건을 만족하면 {@code true}
	 */
	public boolean matchesEligibility(FlagContext ctx) {
		// allow-list/그룹/속성 조건 중 “하나라도” 충족하면 eligible로 두고 싶으면 여기서 OR로 바꾸면 됨.
		// v1은 운영에서 흔한 방식: "requireAttrsIn"은 AND, allowUserIds/allowGroups는 OR
		if (!requireAttrsIn.isEmpty()) {
			for (var e : requireAttrsIn.entrySet()) {
				String actual = ctx.attrs().get(e.getKey());
				if (actual == null || !e.getValue().contains(actual)) return false;
			}
		}
		if (!allowUserIds.isEmpty() || !allowGroups.isEmpty()) {
			return isExplicitlyAllowed(ctx);
		}
		return true;
	}

	/**
	 * {@link Targeting} 빌더입니다.
	 */
	public static final class Builder {
		private final Set<String> allowUserIds = new HashSet<>();
		private final Set<String> denyUserIds = new HashSet<>();
		private final Set<String> allowGroups = new HashSet<>();
		private final Set<String> denyGroups = new HashSet<>();
		private final Map<String, Set<String>> requireAttrsIn = new HashMap<>();

		/**
		 * 허용 사용자 ID를 추가합니다.
		 *
		 * @param id 사용자 ID
		 * @return 현재 빌더
		 */
		public Builder allowUser(String id) { if (id != null) allowUserIds.add(id); return this; }

		/**
		 * 차단 사용자 ID를 추가합니다.
		 *
		 * @param id 사용자 ID
		 * @return 현재 빌더
		 */
		public Builder denyUser(String id) { if (id != null) denyUserIds.add(id); return this; }

		/**
		 * 허용 그룹을 추가합니다.
		 *
		 * @param g 그룹 이름
		 * @return 현재 빌더
		 */
		public Builder allowGroup(String g) { if (g != null) allowGroups.add(g); return this; }

		/**
		 * 차단 그룹을 추가합니다.
		 *
		 * @param g 그룹 이름
		 * @return 현재 빌더
		 */
		public Builder denyGroup(String g) { if (g != null) denyGroups.add(g); return this; }

		/**
		 * 특정 속성 키에 대해 허용 값 집합을 설정합니다.
		 *
		 * @param key 속성 키
		 * @param values 허용 값 집합
		 * @return 현재 빌더
		 */
		public Builder requireAttrIn(String key, Set<String> values) {
			if (key != null && values != null) requireAttrsIn.put(key, new HashSet<>(values));
			return this;
		}

		/**
		 * 불변 타게팅 규칙을 생성합니다.
		 *
		 * @return 타게팅 인스턴스
		 */
		public Targeting build() { return new Targeting(this); }
	}
}
