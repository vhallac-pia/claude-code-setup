package com.pia.orbitant.dcmms.entity;

import java.net.URI;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.tmforum.openapi.dcmms.model.TimePeriod;
import org.tmforum.openapi.dcmms.model.PartyRef;
import org.tmforum.openapi.dcmms.model.Characteristic;
import org.tmforum.openapi.dcmms.model.PaymentMethodRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.tmforum.openapi.dcmms.model.PartyRole;
import org.tmforum.openapi.dcmms.model.AccountRef;
import org.tmforum.openapi.dcmms.model.AgreementRef;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.tmforum.openapi.dcmms.model.CreditProfile;
import jakarta.validation.Valid;
import jakarta.annotation.Generated;
import org.tmforum.openapi.dcmms.model.PartyRoleSpecificationRef;
import org.tmforum.openapi.dcmms.model.RelatedPartyOrPartyRole;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.tmforum.openapi.dcmms.model.ContactMedium;
import java.time.OffsetDateTime;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;


@Generated(value = "DNext API Generator")
@Document(collection = "customer")
public class InternalCustomer extends InternalPartyRole implements Serializable  {


    @Override
    public String toString() {
        return "InternalCustomer{" +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InternalCustomer that = (InternalCustomer) o;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        return result;
    }
}
