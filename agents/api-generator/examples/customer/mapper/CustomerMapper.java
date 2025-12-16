package com.pia.orbitant.dcmms.mapper;

import com.pia.orbitant.common.core.mapper.BaseAppMapper;
import com.pia.orbitant.dcmms.entity.InternalCustomer;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.tmforum.openapi.dcmms.model.*;

@jakarta.annotation.Generated(value = "DNext API Generator")
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper extends BaseAppMapper<Customer, CustomerFVO, CustomerMVO, InternalCustomer> {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    String MAP_QUERY_NAME = "com.pia.orbitant.dcmms.mapper.CustomerMapper::getMapperMethodName";
    String MANAGED_ENTITY_TYPE = "Customer";

    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }

    @Override
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    CustomerMVO toMVO(InternalCustomer entity);

    @Override
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(" + MANAGED_ENTITY_TYPE + ".class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(" + MANAGED_ENTITY_TYPE + ".class))")
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    Customer toDto(InternalCustomer entity);

    @Override
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    InternalCustomer toEntity(CustomerFVO fvo);

    @Override
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    InternalCustomer mvoToEntity(CustomerMVO mvo);

}