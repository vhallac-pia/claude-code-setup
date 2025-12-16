package com.pia.orbitant.troubleticket.mapper;

import com.pia.orbitant.common.core.mapper.BaseAppMapper;
import com.pia.orbitant.common.core.rest.annotation.MapField;
import com.pia.orbitant.common.exception.common.ExceptionFactory;
import com.pia.orbitant.common.exception.common.OrbitantException;
import com.pia.orbitant.troubleticket.entity.InternalBackOfficeUserTicket;
import com.pia.orbitant.troubleticket.entity.InternalBpmPlatformProcessReference;
import com.pia.orbitant.troubleticket.entity.InternalTroubleTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.tmforum.openapi.troubleticket.model.*;

import java.util.Objects;

@jakarta.annotation.Generated(value = "DNext API Generator")
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        ContactMediumMapper.class,
        AttachmentRefOrValueMapper.class,
        RelatedPartyPartyRefOrPartyRoleRefMapper.class,
        CharacteristicMapper.class})
public interface TroubleTicketMapper
        extends BaseAppMapper<TroubleTicket, TroubleTicketFVO, TroubleTicketMVO, InternalTroubleTicket> {

    //TODO : what is tmf merge-patch strategy:
    //1. new mvo patched completly
    //2. new mvo and original entity merged
    //    @BeanMapping(
    //            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,  if condition for null values (merge old and new)
    //            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    //    )

    TroubleTicketMapper INSTANCE = Mappers.getMapper(TroubleTicketMapper.class);
    String MAP_QUERY_NAME = "com.pia.orbitant.troubleticket.mapper.TroubleTicketMapper::getMapperMethodName";
    String MANAGED_ENTITY_TYPE = "TroubleTicket";

    @MapField(dto="@type", entity="atType")
    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }


    @Override
    default TroubleTicketMVO toMVO(InternalTroubleTicket entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity.getAtType()) {
            case "TroubleTicket" -> toTroubleTicketMVO(entity);
            case "BackOfficeUserTicket" -> toBackOfficeUserTicketMVO((InternalBackOfficeUserTicket) entity);
            default -> toTroubleTicketMVO(entity);
        };
    }

    @Override
    default TroubleTicket toDto(InternalTroubleTicket entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity.getAtType()) {
            case "TroubleTicket" -> toTroubleTicketDto(entity);
            case "BackOfficeUserTicket" -> toBackOfficeUserTicketDto((InternalBackOfficeUserTicket) entity);
            default -> toTroubleTicketDto(entity);
        };
    }

    @Override
    default InternalTroubleTicket toEntity(TroubleTicketFVO fvo) {
        if (fvo == null) {
            return null;
        }

        return switch (fvo.getAtType()) {
            case "TroubleTicket" -> toTroubleTicketEntity(fvo);
            case "BackOfficeUserTicket" -> toBackOfficeUserTicketEntity((BackOfficeUserTicketFVO) fvo);
            default -> toTroubleTicketEntity(fvo);
        };
    }

    @Override
    default InternalTroubleTicket mvoToEntity(TroubleTicketMVO mvo) {
        if (mvo == null) {
            return null;
        }

        return switch (mvo.getAtType()) {
            case "TroubleTicket" -> troubleTicketMvoToEntity(mvo);
            case "BackOfficeUserTicket" -> backOfficeUserTicketMvoToEntity((BackOfficeUserTicketMVO) mvo);
            default -> troubleTicketMvoToEntity(mvo);
        };
    }

    @Named("toTroubleTicketMVO")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValueMVO")
    TroubleTicketMVO toTroubleTicketMVO(InternalTroubleTicket entity);

    @Named("toBackOfficeUserTicketMVO")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValueMVO")
    BackOfficeUserTicketMVO toBackOfficeUserTicketMVO(InternalBackOfficeUserTicket entity);

    @Named("toTroubleTicketDto")
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(" + MANAGED_ENTITY_TYPE + ".class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(" + MANAGED_ENTITY_TYPE + ".class))")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
    TroubleTicket toTroubleTicketDto(InternalTroubleTicket entity);

    @Named("toBackOfficeUserTicketDto")
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(" + MANAGED_ENTITY_TYPE + ".class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(" + MANAGED_ENTITY_TYPE + ".class))")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
    @Mapping(target = "contactMedium", source = "contactMedium", qualifiedByName = "toContactMediumDto")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToDto")
    BackOfficeUserTicket toBackOfficeUserTicketDto(InternalBackOfficeUserTicket entity);

    @Named("toTroubleTicketEntity")
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toInternalAttachmentRefOrValue")
    InternalTroubleTicket toTroubleTicketEntity(TroubleTicketFVO fvo);

    @Named("toBackOfficeUserTicketEntity")
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toInternalAttachmentRefOrValue")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToEntity")
    InternalBackOfficeUserTicket toBackOfficeUserTicketEntity(BackOfficeUserTicketFVO fvo);

    @Named("troubleTicketMvoToEntity")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "mvoToEntityAttachmentRefOrValue")
    InternalTroubleTicket troubleTicketMvoToEntity(TroubleTicketMVO mvo);

    @Named("backOfficeUserTicketMvoToEntity")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "mvoToEntityAttachmentRefOrValue")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToEntity")
    InternalBackOfficeUserTicket backOfficeUserTicketMvoToEntity(BackOfficeUserTicketMVO mvo);

    @Named("statusStringToEnum")
    default TroubleTicketStatusType statusStringToEnum(String enumString) throws OrbitantException {
        if(Objects.isNull(enumString)){
            return null;
        }
        try {
            return TroubleTicketStatusType.fromValue(enumString);
        } catch (IllegalArgumentException e) {
            throw ExceptionFactory.BadRequest.throwInvalidEnumerationException(null, "status", enumString);
        }
    }

    @Named("statusEnumToString")
    default String statusEnumToString(TroubleTicketStatusType enumType) {
        return Objects.nonNull(enumType) ? enumType.getValue() : null;
    }

    @Named("backOfficeUserTicketSpecificationBpmPlatformDomainToEntity")
    @Mapping(target = "bpmPlatformDomain", source = "bpmPlatformDomain", qualifiedByName = "bpmPlatformDomainEnumToString")
    InternalBpmPlatformProcessReference backOfficeUserTicketSpecificationBpmPlatformDomainToEntity(BpmPlatformProcessReference bpmPlatformProcessReference);


    @Named("backOfficeUserTicketSpecificationBpmPlatformDomainToDto")
    @Mapping(target = "bpmPlatformDomain", source = "bpmPlatformDomain", qualifiedByName = "bpmPlatformDomainStringToEnum")
    BpmPlatformProcessReference backOfficeUserTicketSpecificationBpmPlatformDomainToDto(InternalBpmPlatformProcessReference internalFormReference);

    @Named("bpmPlatformDomainStringToEnum")
    default BpmPlatformProcessReference.BpmPlatformDomainEnum bpmPlatformDomainStringToEnum(String enumString) {
        return Objects.nonNull(enumString) ? BpmPlatformProcessReference.BpmPlatformDomainEnum.fromValue(enumString) : null;
    }

    @Named("bpmPlatformDomainEnumToString")
    default String bpmPlatformDomainEnumToString(BpmPlatformProcessReference.BpmPlatformDomainEnum enumType) {
        return Objects.nonNull(enumType) ? enumType.getValue() : null;
    }

}