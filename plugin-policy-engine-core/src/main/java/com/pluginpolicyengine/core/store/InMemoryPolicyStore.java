package com.pluginpolicyengine.core.store;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.pluginpolicyengine.core.PolicyDefinition;
import com.pluginpolicyengine.core.PolicyStore;

/** 로컬/개발 환경에서 사용하는 메모리 기반 {@link PolicyStore} 구현체입니다. */
public final class InMemoryPolicyStore implements PolicyStore {
	private final Map<String, PolicyDefinition> map = new ConcurrentHashMap<>();

	/**
	 * 키로 정책 정의를 조회합니다.
	 *
	 * @param key 정책 키
	 * @return 정책이 존재하면 해당 정의
	 */
	@Override
	public Optional<PolicyDefinition> find(String key) {
		return Optional.ofNullable(map.get(key));
	}

	/**
	 * 현재 저장된 모든 정책 정의를 반환합니다.
	 *
	 * @return 전체 정책의 불변 뷰
	 */
	@Override
	public Map<String, PolicyDefinition> findAll() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * 정책 정의를 추가하거나 교체합니다.
	 *
	 * @param def 저장할 정의
	 */
	public void put(PolicyDefinition def) { map.put(def.key(), def); }

	/**
	 * 키로 정책을 삭제합니다.
	 *
	 * @param key 정책 키
	 */
	public void remove(String key) { map.remove(key); }
}
