package com.encens.khipus.dashboard.component.dto.factory;

import com.encens.khipus.dashboard.component.dto.Dto;
import com.encens.khipus.dashboard.component.dto.configuration.DtoConfiguration;

/**
 * @author
 * @version 2.17
 */
public interface InstanceBuilder {
    Dto buildInstance(Dto cachedDto, Object[] row, DtoConfiguration configuration);
}
