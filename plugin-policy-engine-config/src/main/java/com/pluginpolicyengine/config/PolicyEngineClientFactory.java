package com.pluginpolicyengine.config;

import com.pluginpolicyengine.api.PolicyEngineClient;
import com.pluginpolicyengine.core.PolicyEngine;
import com.pluginpolicyengine.core.PolicyStore;
import com.pluginpolicyengine.core.store.InMemoryPolicyStore;
import com.pluginpolicyengine.store.file.JsonFilePolicyStore;

/**
 * 정책 엔진 구성 요소를 생성하는 순수 Java 팩토리입니다.
 */
public final class PolicyEngineClientFactory {
	private PolicyEngineClientFactory() {
	}

	/**
	 * 설정에 맞는 저장소와 클라이언트를 생성합니다.
	 *
	 * @param config 엔진 설정. {@code null}이면 메모리 저장소를 사용합니다.
	 * @return 정책 클라이언트
	 */
	public static PolicyEngineClient create(PolicyEngineConfig config) {
		return create(createStore(config));
	}

	/**
	 * 주어진 저장소를 사용하는 클라이언트를 생성합니다.
	 *
	 * @param store 정책 저장소
	 * @return 정책 클라이언트
	 */
	public static PolicyEngineClient create(PolicyStore store) {
		PolicyEngine service = new PolicyEngine(store);
		return service::evaluate;
	}

	/**
	 * 설정에 맞는 저장소를 생성합니다.
	 *
	 * @param config 엔진 설정. {@code null}이면 메모리 저장소를 사용합니다.
	 * @return 정책 저장소
	 */
	public static PolicyStore createStore(PolicyEngineConfig config) {
		PolicyEngineConfig resolved = config == null ? PolicyEngineConfig.memory() : config;

		if (resolved.store() == PolicyEngineConfig.Store.MEMORY) {
			return new InMemoryPolicyStore();
		}

		if (isBlank(resolved.filePath())) {
			throw new IllegalArgumentException("filePath is required when store is FILE");
		}
		return new JsonFilePolicyStore(resolved.filePath(), resolved.cacheTtl());
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
