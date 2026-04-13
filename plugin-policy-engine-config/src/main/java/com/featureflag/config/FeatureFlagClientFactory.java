package com.pluginpolicyengine.config;

import com.pluginpolicyengine.api.FeatureFlagClient;
import com.pluginpolicyengine.core.FeatureFlagService;
import com.pluginpolicyengine.core.FlagStore;
import com.pluginpolicyengine.core.store.InMemoryFlagStore;
import com.pluginpolicyengine.store.file.JsonFileFlagStore;

/**
 * 기능 플래그 엔진 구성 요소를 생성하는 순수 Java 팩토리입니다.
 */
public final class FeatureFlagClientFactory {
	private FeatureFlagClientFactory() {
	}

	/**
	 * 설정에 맞는 저장소와 클라이언트를 생성합니다.
	 *
	 * @param config 엔진 설정. {@code null}이면 메모리 저장소를 사용합니다.
	 * @return 기능 플래그 클라이언트
	 */
	public static FeatureFlagClient create(FeatureFlagConfig config) {
		return create(createStore(config));
	}

	/**
	 * 주어진 저장소를 사용하는 클라이언트를 생성합니다.
	 *
	 * @param store 플래그 저장소
	 * @return 기능 플래그 클라이언트
	 */
	public static FeatureFlagClient create(FlagStore store) {
		FeatureFlagService service = new FeatureFlagService(store);
		return service::evaluate;
	}

	/**
	 * 설정에 맞는 저장소를 생성합니다.
	 *
	 * @param config 엔진 설정. {@code null}이면 메모리 저장소를 사용합니다.
	 * @return 플래그 저장소
	 */
	public static FlagStore createStore(FeatureFlagConfig config) {
		FeatureFlagConfig resolved = config == null ? FeatureFlagConfig.memory() : config;

		if (resolved.store() == FeatureFlagConfig.Store.MEMORY) {
			return new InMemoryFlagStore();
		}

		if (resolved.filePath() == null || resolved.filePath().isBlank()) {
			throw new IllegalArgumentException("filePath is required when store is FILE");
		}
		return new JsonFileFlagStore(resolved.filePath(), resolved.cacheTtl());
	}
}
