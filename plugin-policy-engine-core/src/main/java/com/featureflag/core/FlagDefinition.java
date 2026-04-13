package com.pluginpolicyengine.core;

import java.time.Instant;
import java.util.*;

/**
 * 기능 플래그 설정을 표현하는 불변 모델입니다.
 */
public final class FlagDefinition {
	private final String key;
	private final boolean enabled;
	private final int rolloutPercent; // 0~100
	private final Targeting targeting;
	private final List<VariantAllocation> variants; // empty면 boolean flag처럼 사용
	private final String defaultVariant;
	private final Instant updatedAt;

	private FlagDefinition(Builder b) {
		this.key = Objects.requireNonNull(b.key, "key");
		this.enabled = b.enabled;
		this.rolloutPercent = clamp(b.rolloutPercent, 0, 100);
		this.targeting = b.targeting != null ? b.targeting : Targeting.allowAll();
		this.variants = Collections.unmodifiableList(new ArrayList<>(b.variants));
		this.defaultVariant = b.defaultVariant != null ? b.defaultVariant : "on";
		this.updatedAt = b.updatedAt != null ? b.updatedAt : Instant.now();
	}

	private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

	/**
	 * @return 고유 기능 플래그 키
	 */
	public String key() { return key; }

	/**
	 * @return 플래그의 전역 활성 여부
	 */
	public boolean enabled() { return enabled; }

	/**
	 * @return 0~100 범위의 롤아웃 비율
	 */
	public int rolloutPercent() { return rolloutPercent; }

	/**
	 * @return 플래그에 적용되는 타게팅 규칙
	 */
	public Targeting targeting() { return targeting; }

	/**
	 * @return 가중치 기반 variant 정의 목록
	 */
	public List<VariantAllocation> variants() { return variants; }

	/**
	 * @return 가중치 선택이 불가능할 때 사용할 기본 variant
	 */
	public String defaultVariant() { return defaultVariant; }

	/**
	 * @return 정의의 갱신 시각
	 */
	public Instant updatedAt() { return updatedAt; }

	/**
	 * 지정한 플래그 키에 대한 빌더를 생성합니다.
	 *
	 * @param key 고유 기능 플래그 키
	 * @return 정의 빌더
	 */
	public static Builder builder(String key) { return new Builder(key); }

	/**
	 * {@link FlagDefinition} 빌더입니다.
	 */
	public static final class Builder {
		private final String key;
		private boolean enabled = true;
		private int rolloutPercent = 100;
		private Targeting targeting;
		private final List<VariantAllocation> variants = new ArrayList<>();
		private String defaultVariant;
		private Instant updatedAt;

		private Builder(String key) { this.key = key; }

		/**
		 * 플래그 활성 여부를 설정합니다.
		 *
		 * @param v 활성 값
		 * @return 현재 빌더
		 */
		public Builder enabled(boolean v) { this.enabled = v; return this; }

		/**
		 * 롤아웃 비율을 설정합니다.
		 *
		 * @param v 롤아웃 비율
		 * @return 현재 빌더
		 */
		public Builder rolloutPercent(int v) { this.rolloutPercent = v; return this; }

		/**
		 * 타게팅 규칙을 설정합니다.
		 *
		 * @param t 타게팅 설정
		 * @return 현재 빌더
		 */
		public Builder targeting(Targeting t) { this.targeting = t; return this; }

		/**
		 * 가중치 variant를 추가합니다.
		 *
		 * @param name variant 이름
		 * @param weight variant 가중치
		 * @return 현재 빌더
		 */
		public Builder variant(String name, int weight) { this.variants.add(new VariantAllocation(name, weight)); return this; }

		/**
		 * 기본 variant를 설정합니다.
		 *
		 * @param v 기본 variant
		 * @return 현재 빌더
		 */
		public Builder defaultVariant(String v) { this.defaultVariant = v; return this; }

		/**
		 * 갱신 시각을 설정합니다.
		 *
		 * @param t 갱신 시각
		 * @return 현재 빌더
		 */
		public Builder updatedAt(Instant t) { this.updatedAt = t; return this; }

		/**
		 * 불변 {@link FlagDefinition}을 생성합니다.
		 *
		 * @return 정의 인스턴스
		 */
		public FlagDefinition build() { return new FlagDefinition(this); }
	}

	/**
	 * variant 가중치 정보입니다.
	 */
	public static final class VariantAllocation {
		private final String name;
		private final int weight; // 합계가 100일 필요는 없음(내부에서 합산)

		/**
		 * variant 가중치 항목을 생성합니다.
		 *
		 * @param name variant 이름
		 * @param weight 음수가 아닌 variant 가중치
		 */
		public VariantAllocation(String name, int weight) {
			this.name = Objects.requireNonNull(name, "name");
			this.weight = Math.max(0, weight);
		}

		/**
		 * @return variant 이름
		 */
		public String name() { return name; }

		/**
		 * @return 음수가 아닌 variant 가중치
		 */
		public int weight() { return weight; }
	}
}
