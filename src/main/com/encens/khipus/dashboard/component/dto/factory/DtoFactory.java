package com.encens.khipus.dashboard.component.dto.factory;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;

import java.util.*;

/**
 * @author
 * @version 2.17
 */
public class DtoFactory {
    private Map<Object, Dto> cache = new LinkedHashMap<Object, Dto>();

    private DtoConfiguration configuration;

    private InstanceBuilder instanceBuilder;

    public DtoFactory(DtoConfiguration configuration, InstanceBuilder instanceBuilder) {
        this.configuration = configuration;
        this.instanceBuilder = instanceBuilder;
    }

    public List<Dto> getDtoList(List data) {
        List<Dto> result = new ArrayList<Dto>();

        if (null != data && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                Object dataElement = data.get(i);
                if (dataElement instanceof Object[]) {
                    Object[] row = (Object[]) dataElement;
                    buildInstance(row);
                } else {
                    Object[] row = Arrays.asList(dataElement).toArray();
                    buildInstance(row);
                }
            }

            result.addAll(cache.values());

            clearCache();
        }

        return result;
    }

    protected Dto buildInstance(Object[] row) {
        Dto cachedDto = getCachedObject(row);

        Dto instance = instanceBuilder.buildInstance(cachedDto, row, configuration);

        updateCache(instance);

        return instance;
    }

    private Dto getCachedObject(Object[] row) {
        Integer idx = configuration.getIdField().getPosition();
        Object keyValue = row[idx];

        if (null == keyValue) {
            throw new RuntimeException("The value in the position: " + idx + " cannot be a null.");
        }

        return cache.get(keyValue);
    }

    private void updateCache(Dto dto) {
        Object key = dto.getField(configuration.getIdField().getName());

        cache.put(key, dto);
    }

    private void clearCache() {
        cache.clear();
    }
}
