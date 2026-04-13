package com.pluginpolicyengine.config;

import java.time.Duration;
import java.util.Objects;

/**
 * 기능 플래그 엔진을 구성하기 위한 순수 Java 설정 객체입니다.
 */
public final class FeatureFlagConfig {
	private final Store store;
	private final String filePath;
	private final Duration cacheTtl;

	private FeatureFlagConfig(Builder builder) {
		this.store = Objects.requireNonNull(builder.store, "store");
		this.filePath = builder.filePath;
		this.cacheTtl = builder.cacheTtl == null ? Duration.ofSeconds(3) : builder.cacheTtl;
	}

	/**
	 * 지원하는 저장소 백엔드 종류입니다.
	 */
	public enum Store {
		/** JVM 메모리에 저장된 플래그를 사용하는 저장소입니다. */
		MEMORY,
		/** 외부 JSON 파일에서 플래그를 읽는 저장소입니다. */
		FILE
	}

	/**
	 * 메모리 저장소 설정을 생성합니다.
	 *
	 * @return 메모리 저장소용 설정
	 */
	public static FeatureFlagConfig memory() {
		return builder().store(Store.MEMORY).build();
	}

	/**
	 * 파일 저장소 설정을 생성합니다.
	 *
	 * @param filePath JSON 파일 경로
	 * @return 파일 저장소용 설정
	 */
	public static FeatureFlagConfig file(String filePath) {
		return builder().store(Store.FILE).filePath(filePath).build();
	}

	/**
	 * 설정 빌더를 생성합니다.
	 *
	 * @return 설정 빌더
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * @return 선택된 플래그 저장소 백엔드
	 */
	public Store store() {
		return store;
	}

	/**
	 * @return 저장소가 FILE일 때 사용할 JSON 파일 경로
	 */
	public String filePath() {
		return filePath;
	}

	/**
	 * @return 파일 기반 저장소의 캐시 TTL
	 */
	public Duration cacheTtl() {
		return cacheTtl;
	}

	/**
	 * {@link FeatureFlagConfig} 빌더입니다.
	 */
	public static final class Builder {
		private Store store = Store.MEMORY;
		private String filePath;
		private Duration cacheTtl = Duration.ofSeconds(3);

		/**
		 * 저장소 백엔드를 설정합니다.
		 *
		 * @param store 저장소 백엔드
		 * @return 현재 빌더
		 */
		public Builder store(Store store) {
			this.store = store == null ? Store.MEMORY : store;
			return this;
		}

		/**
		 * JSON 파일 경로를 설정합니다.
		 *
		 * @param filePath JSON 파일 경로
		 * @return 현재 빌더
		 */
		public Builder filePath(String filePath) {
			this.filePath = filePath;
			return this;
		}

		/**
		 * 파일 저장소 캐시 TTL을 설정합니다.
		 *
		 * @param cacheTtl 캐시 TTL
		 * @return 현재 빌더
		 */
		public Builder cacheTtl(Duration cacheTtl) {
			this.cacheTtl = cacheTtl;
			return this;
		}

		/**
		 * 설정 객체를 생성합니다.
		 *
		 * @return 설정 객체
		 */
		public FeatureFlagConfig build() {
			return new FeatureFlagConfig(this);
		}
	}
}
