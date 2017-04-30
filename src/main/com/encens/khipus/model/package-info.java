@FilterDefs({
        @FilterDef(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME,
                defaultCondition = "idcompania = :currentCompanyId",
                parameters = @ParamDef(name = "currentCompanyId", type = "long")
        ),
        @FilterDef(name = com.encens.khipus.util.Constants.BUSINESS_UNIT_FILTER_NAME,
                defaultCondition = "IDUNIDADNEGOCIO IN(:executorUnitId)",
                parameters = @ParamDef(name = "executorUnitId", type = "long")
        )
})


@TypeDefs({
        @TypeDef(
                name = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME,
                typeClass = com.encens.khipus.model.usertype.IntegerBooleanUserType.class
        ),
        @TypeDef(
                name = com.encens.khipus.model.usertype.StringBooleanUserType.NAME,
                typeClass = com.encens.khipus.model.usertype.StringBooleanUserType.class,
                parameters = {
                        @Parameter(
                                name = com.encens.khipus.model.usertype.StringBooleanUserType.TRUE_PARAMETER,
                                value = com.encens.khipus.model.usertype.StringBooleanUserType.ACRONYM_TRUE_VALUE
                        ),
                        @Parameter(
                                name = com.encens.khipus.model.usertype.StringBooleanUserType.FALSE_PARAMETER,
                                value = com.encens.khipus.model.usertype.StringBooleanUserType.ACRONYM_FALSE_VALUE
                        )
                }
        )
}) package com.encens.khipus.model;

import org.hibernate.annotations.*;
