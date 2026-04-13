package com.pluginpolicyengine.core.store;

import com.pluginpolicyengine.core.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주로 로컬/개발 환경에서 사용하는 메모리 기반 {@link FlagStore} 구현체입니다.
 */
public final class InMemoryFlagStore implements FlagStore {
	private final Map<String, FlagDefinition> map = new ConcurrentHashMap<>();

	/**
	 * 키로 플래그 정의를 조회합니다.
	 *
	 * @param key 기능 플래그 키
	 * @return 플래그가 존재하면 해당 정의
	 */
	@Override
	public Optional<FlagDefinition> find(String key) {
		return Optional.ofNullable(map.get(key));
	}

	/**
	 * 현재 저장된 모든 플래그 정의를 반환합니다.
	 *
	 * @return 전체 플래그의 불변 뷰
	 */
	@Override
	public Map<String, FlagDefinition> findAll() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * 플래그 정의를 추가하거나 교체합니다.
	 *
	 * @param def 저장할 정의
	 */
	public void put(FlagDefinition def) { map.put(def.key(), def); }

	/**
	 * 키로 플래그를 삭제합니다.
	 *
	 * @param key 기능 플래그 키
	 */
	public void remove(String key) { map.remove(key); }
}
