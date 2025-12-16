package com.pia.orbitant.troubleticket.entity;


import com.pia.orbitant.common.mongo.entity.base.TenantEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@jakarta.annotation.Generated(value = "DNext API Generator")
@Document(collection = "trouble_ticket")
@Validated
public class InternalTroubleTicket extends TenantEntity implements Serializable {

    private String atType;

    private String atBaseType;

    private String atSchemaLocation;

    @Field("href")
    private String href;

    @Id
    @Field("id")
    private String id;

    @Field("name")
    private String name;

    private String description;

    @NotBlank
    private String severity;

    @NotBlank
    private String ticketType;

    @Valid
    private List<InternalAttachmentRefOrValue> attachment;

    private InternalChannelRef channel;

    private OffsetDateTime creationDate;

    private OffsetDateTime requestedResolutionDate;

    private OffsetDateTime expectedResolutionDate;

    private OffsetDateTime resolutionDate;

    private List<InternalExternalIdentifier> externalIdentifier;

    private OffsetDateTime lastUpdate;

    private List<InternalNote> note;

    private String priority;

    private List<InternalRelatedEntity> relatedEntity;

    private List<InternalRelatedPartyRefOrPartyRoleRef> relatedParty;

    private String status;

    private OffsetDateTime statusChangeDate;

    private String statusChangeReason;

    private List<InternalStatusChange> statusChangeHistory;

    private List<InternalTroubleTicketRelationship> troubleTicketRelationship;

    private InternalTroubleTicketSpecificationRef troubleTicketSpecification;

    private List<InternalCharacteristic> troubleTicketCharacteristic;


    public String getAtType() {
        return atType;
    }

    public void setAtType(String atType) {
        this.atType = atType;
    }

    public String getAtBaseType() {
        return atBaseType;
    }

    public void setAtBaseType(String atBaseType) {
        this.atBaseType = atBaseType;
    }

    public String getAtSchemaLocation() {
        return atSchemaLocation;
    }

    public void setAtSchemaLocation(String atSchemaLocation) {
        this.atSchemaLocation = atSchemaLocation;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public List<InternalAttachmentRefOrValue> getAttachment() {
        return attachment;
    }

    public void setAttachment(List<InternalAttachmentRefOrValue> attachment) {
        this.attachment = attachment;
    }

    public InternalChannelRef getChannel() {
        return channel;
    }

    public void setChannel(InternalChannelRef channel) {
        this.channel = channel;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public OffsetDateTime getRequestedResolutionDate() {
        return requestedResolutionDate;
    }

    public void setRequestedResolutionDate(OffsetDateTime requestedResolutionDate) {
        this.requestedResolutionDate = requestedResolutionDate;
    }

    public OffsetDateTime getExpectedResolutionDate() {
        return expectedResolutionDate;
    }

    public void setExpectedResolutionDate(OffsetDateTime expectedResolutionDate) {
        this.expectedResolutionDate = expectedResolutionDate;
    }

    public OffsetDateTime getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(OffsetDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public List<InternalExternalIdentifier> getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(List<InternalExternalIdentifier> externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(OffsetDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<InternalNote> getNote() {
        return note;
    }

    public void setNote(List<InternalNote> note) {
        this.note = note;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<InternalRelatedEntity> getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(List<InternalRelatedEntity> relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public List<InternalRelatedPartyRefOrPartyRoleRef> getRelatedParty() {
        return relatedParty;
    }

    public void setRelatedParty(List<InternalRelatedPartyRefOrPartyRoleRef> relatedParty) {
        this.relatedParty = relatedParty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(OffsetDateTime statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public String getStatusChangeReason() {
        return statusChangeReason;
    }

    public void setStatusChangeReason(String statusChangeReason) {
        this.statusChangeReason = statusChangeReason;
    }

    public List<InternalStatusChange> getStatusChangeHistory() {
        return statusChangeHistory;
    }

    public void setStatusChangeHistory(List<InternalStatusChange> statusChangeHistory) {
        this.statusChangeHistory = statusChangeHistory;
    }

    public List<InternalTroubleTicketRelationship> getTroubleTicketRelationship() {
        return troubleTicketRelationship;
    }

    public void setTroubleTicketRelationship(List<InternalTroubleTicketRelationship> troubleTicketRelationship) {
        this.troubleTicketRelationship = troubleTicketRelationship;
    }

    public InternalTroubleTicketSpecificationRef getTroubleTicketSpecification() {
        return troubleTicketSpecification;
    }

    public void setTroubleTicketSpecification(InternalTroubleTicketSpecificationRef troubleTicketSpecification) {
        this.troubleTicketSpecification = troubleTicketSpecification;
    }

    public List<InternalCharacteristic> getTroubleTicketCharacteristic() {
        return troubleTicketCharacteristic;
    }

    public void setTroubleTicketCharacteristic(List<InternalCharacteristic> troubleTicketCharacteristic) {
        this.troubleTicketCharacteristic = troubleTicketCharacteristic;
    }


    @Override
    public String toString() {
        return "InternalTroubleTicket{" +

                "atType='" + atType + "'," +

                "atBaseType='" + atBaseType + "'," +

                "atSchemaLocation='" + atSchemaLocation + "'," +

                "href='" + href + "'," +

                "id='" + id + "'," +

                "name='" + name + "'," +

                "description='" + description + "'," +

                "severity='" + severity + "'," +

                "ticketType='" + ticketType + "'," +

                "attachment='" + attachment + "'," +

                "channel='" + channel + "'," +

                "creationDate='" + creationDate + "'," +

                "requestedResolutionDate='" + requestedResolutionDate + "'," +

                "expectedResolutionDate='" + expectedResolutionDate + "'," +

                "resolutionDate='" + resolutionDate + "'," +

                "externalIdentifier='" + externalIdentifier + "'," +

                "lastUpdate='" + lastUpdate + "'," +

                "note='" + note + "'," +

                "priority='" + priority + "'," +

                "relatedEntity='" + relatedEntity + "'," +

                "relatedParty='" + relatedParty + "'," +

                "status='" + status + "'," +

                "statusChangeDate='" + statusChangeDate + "'," +

                "statusChangeReason='" + statusChangeReason + "'," +

                "statusChangeHistory='" + statusChangeHistory + "'," +

                "troubleTicketRelationship='" + troubleTicketRelationship + "'," +

                "troubleTicketSpecification='" + troubleTicketSpecification + "'," +

                "troubleTicketCharacteristic='" + troubleTicketCharacteristic + "'," +

                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InternalTroubleTicket that = (InternalTroubleTicket) o;

        if (!atType.equals(that.atType)) return false;

        if (!atBaseType.equals(that.atBaseType)) return false;

        if (!atSchemaLocation.equals(that.atSchemaLocation)) return false;

        if (!href.equals(that.href)) return false;

        if (!id.equals(that.id)) return false;

        if (!name.equals(that.name)) return false;

        if (!description.equals(that.description)) return false;

        if (!severity.equals(that.severity)) return false;

        if (!ticketType.equals(that.ticketType)) return false;

        if (!attachment.equals(that.attachment)) return false;

        if (!channel.equals(that.channel)) return false;

        if (!creationDate.equals(that.creationDate)) return false;

        if (!requestedResolutionDate.equals(that.requestedResolutionDate)) return false;

        if (!expectedResolutionDate.equals(that.expectedResolutionDate)) return false;

        if (!resolutionDate.equals(that.resolutionDate)) return false;

        if (!externalIdentifier.equals(that.externalIdentifier)) return false;

        if (!lastUpdate.equals(that.lastUpdate)) return false;

        if (!note.equals(that.note)) return false;

        if (!priority.equals(that.priority)) return false;

        if (!relatedEntity.equals(that.relatedEntity)) return false;

        if (!relatedParty.equals(that.relatedParty)) return false;

        if (!status.equals(that.status)) return false;

        if (!statusChangeDate.equals(that.statusChangeDate)) return false;

        if (!statusChangeReason.equals(that.statusChangeReason)) return false;

        if (!statusChangeHistory.equals(that.statusChangeHistory)) return false;

        if (!troubleTicketRelationship.equals(that.troubleTicketRelationship)) return false;

        if (!troubleTicketSpecification.equals(that.troubleTicketSpecification)) return false;

        if (!troubleTicketCharacteristic.equals(that.troubleTicketCharacteristic)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;

        result = 31 * result + (atType != null ? atType.hashCode() : 0);

        result = 31 * result + (atBaseType != null ? atBaseType.hashCode() : 0);

        result = 31 * result + (atSchemaLocation != null ? atSchemaLocation.hashCode() : 0);

        result = 31 * result + (href != null ? href.hashCode() : 0);

        result = 31 * result + (id != null ? id.hashCode() : 0);

        result = 31 * result + (name != null ? name.hashCode() : 0);

        result = 31 * result + (description != null ? description.hashCode() : 0);

        result = 31 * result + (severity != null ? severity.hashCode() : 0);

        result = 31 * result + (ticketType != null ? ticketType.hashCode() : 0);

        result = 31 * result + (attachment != null ? attachment.hashCode() : 0);

        result = 31 * result + (channel != null ? channel.hashCode() : 0);

        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);

        result = 31 * result + (requestedResolutionDate != null ? requestedResolutionDate.hashCode() : 0);

        result = 31 * result + (expectedResolutionDate != null ? expectedResolutionDate.hashCode() : 0);

        result = 31 * result + (resolutionDate != null ? resolutionDate.hashCode() : 0);

        result = 31 * result + (externalIdentifier != null ? externalIdentifier.hashCode() : 0);

        result = 31 * result + (lastUpdate != null ? lastUpdate.hashCode() : 0);

        result = 31 * result + (note != null ? note.hashCode() : 0);

        result = 31 * result + (priority != null ? priority.hashCode() : 0);

        result = 31 * result + (relatedEntity != null ? relatedEntity.hashCode() : 0);

        result = 31 * result + (relatedParty != null ? relatedParty.hashCode() : 0);

        result = 31 * result + (status != null ? status.hashCode() : 0);

        result = 31 * result + (statusChangeDate != null ? statusChangeDate.hashCode() : 0);

        result = 31 * result + (statusChangeReason != null ? statusChangeReason.hashCode() : 0);

        result = 31 * result + (statusChangeHistory != null ? statusChangeHistory.hashCode() : 0);

        result = 31 * result + (troubleTicketRelationship != null ? troubleTicketRelationship.hashCode() : 0);

        result = 31 * result + (troubleTicketSpecification != null ? troubleTicketSpecification.hashCode() : 0);

        result = 31 * result + (troubleTicketCharacteristic != null ? troubleTicketCharacteristic.hashCode() : 0);

        return result;
    }
}
