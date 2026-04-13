package com.pluginpolicyengine.core;

import java.util.*;

/**
 * 타게팅 및 롤아웃 규칙 평가에 사용하는 요청 컨텍스트입니다.
 */
public final class FlagContext {
	private final String userId;              // deterministic rollout/AB의 기준
	private final Set<String> groups;         // "beta", "staff" 등
	private final Map<String, String> attrs;  // "region"="KR", "plan"="PRO" 등

	private FlagContext(Builder b) {
		this.userId = b.userId;
		this.groups = Collections.unmodifiableSet(new HashSet<>(b.groups));
		this.attrs = Collections.unmodifiableMap(new HashMap<>(b.attrs));
	}

	/**
	 * @return 결정적 버킷팅에 사용하는 안정적인 사용자 식별자
	 */
	public String userId() { return userId; }

	/**
	 * @return 불변 그룹 집합 (예: beta, staff)
	 */
	public Set<String> groups() { return groups; }

	/**
	 * @return 불변 속성 맵 (예: region, plan)
	 */
	public Map<String, String> attrs() { return attrs; }

	/**
	 * {@link FlagContext} 빌더를 생성합니다.
	 *
	 * @return 컨텍스트 빌더
	 */
	public static Builder builder() { return new Builder(); }

	/**
	 * {@link FlagContext} 빌더입니다.
	 */
	public static final class Builder {
		private String userId;
		private final Set<String> groups = new HashSet<>();
		private final Map<String, String> attrs = new HashMap<>();

		/**
		 * 사용자 식별자를 설정합니다.
		 *
		 * @param userId 안정적인 사용자 식별자
		 * @return 현재 빌더
		 */
		public Builder userId(String userId) { this.userId = userId; return this; }

		/**
		 * 그룹 하나를 추가합니다.
		 *
		 * @param g 그룹 이름
		 * @return 현재 빌더
		 */
		public Builder group(String g) { if (g != null) this.groups.add(g); return this; }

		/**
		 * 여러 그룹을 추가합니다.
		 *
		 * @param gs 그룹 이름 목록
		 * @return 현재 빌더
		 */
		public Builder groups(Collection<String> gs) { if (gs != null) this.groups.addAll(gs); return this; }

		/**
		 * 속성 하나를 추가합니다.
		 *
		 * @param k 속성 키
		 * @param v 속성 값
		 * @return 현재 빌더
		 */
		public Builder attr(String k, String v) { if (k != null && v != null) this.attrs.put(k, v); return this; }

		/**
		 * 여러 속성을 추가합니다.
		 *
		 * @param m 속성 맵
		 * @return 현재 빌더
		 */
		public Builder attrs(Map<String,String> m) { if (m != null) this.attrs.putAll(m); return this; }

		/**
		 * 불변 {@link FlagContext}를 생성합니다.
		 *
		 * @return 새 컨텍스트 인스턴스
		 */
		public FlagContext build() { return new FlagContext(this); }
	}
}
