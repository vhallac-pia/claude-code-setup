package com.pia.orbitant.troubleticket.entity;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;


@jakarta.annotation.Generated(value = "DNext API Generator")
@Validated
public class InternalBackOfficeUserTicket extends InternalTroubleTicket implements Serializable {


    private List<InternalAclRelatedParty> aclRelatedParty;


    private List<String> candidateRoles;

    private List<String> candidateUsers;

    private List<String> candidateOrganizations;

    private List<String> candidateGroups;

    private List<String> assignedQueues;

    @NotBlank
    private String category;

    private String subcategory;

    @Valid
    private List<InternalContactMedium> contactMedium;

    private List<InternalExternalReference> externalReference;

    private InternalSLARef serviceLevelAgreement;

    @Valid
    private InternalTimePeriod validFor;

    @Valid
    private InternalBpmPlatformProcessReference bpmPlatformProcessReference;

    public List<InternalAclRelatedParty> getAclRelatedParty() {
        return aclRelatedParty;
    }

    public void setAclRelatedParty(List<InternalAclRelatedParty> aclRelatedParty) {
        this.aclRelatedParty = aclRelatedParty;
    }

    @NotEmpty
    public List< @NotBlank String> getCandidateRoles() {
        return candidateRoles;
    }

    public void setCandidateRoles(List<String> candidateRoles) {
        this.candidateRoles = candidateRoles;
    }

    public List<String> getCandidateUsers() {
        return candidateUsers;
    }

    public void setCandidateUsers(List<String> candidateUsers) {
        this.candidateUsers = candidateUsers;
    }

    public List<String> getCandidateOrganizations() {
        return candidateOrganizations;
    }

    public void setCandidateOrganizations(List<String> candidateOrganizations) {
        this.candidateOrganizations = candidateOrganizations;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public List<InternalContactMedium> getContactMedium() {
        return contactMedium;
    }

    public void setContactMedium(List<InternalContactMedium> contactMedium) {
        this.contactMedium = contactMedium;
    }

    public List<InternalExternalReference> getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(List<InternalExternalReference> externalReference) {
        this.externalReference = externalReference;
    }

    public InternalSLARef getServiceLevelAgreement() {
        return serviceLevelAgreement;
    }

    public void setServiceLevelAgreement(InternalSLARef serviceLevelAgreement) {
        this.serviceLevelAgreement = serviceLevelAgreement;
    }

    public InternalTimePeriod getValidFor() {
        return validFor;
    }

    public void setValidFor(InternalTimePeriod validFor) {
        this.validFor = validFor;
    }

    public InternalBpmPlatformProcessReference getBpmPlatformProcessReference() {
        return bpmPlatformProcessReference;
    }

    public void setBpmPlatformProcessReference(InternalBpmPlatformProcessReference bpmPlatformProcessReference) {
        this.bpmPlatformProcessReference = bpmPlatformProcessReference;
    }

    public void setCandidateGroups(List<String> candidateGroups) {
        this.candidateGroups = candidateGroups;
    }

    public List<String> getCandidateGroups() {
        return candidateGroups;
    }

    public void setAssignedQueues(List<String> assignedQueues) {
        this.assignedQueues = assignedQueues;
    }

    public List<String> getAssignedQueues() {
        return assignedQueues;
    }

    @Override
    public String toString() {
        return "InternalBackOfficeUserTicket{" +

                "aclRelatedParty='" + aclRelatedParty + "'," +

                "candidateRoles='" + candidateRoles + "'," +

                "candidateUsers='" + candidateUsers + "'," +

                "candidateOrganizations='" + candidateOrganizations + "'," +

                "candidateGroups='" + candidateGroups + "'," +

                "assignedQueues='" + assignedQueues + "'," +

                "category='" + category + "'," +

                "subcategory='" + subcategory + "'," +

                "contactMedium='" + contactMedium + "'," +

                "externalReference='" + externalReference + "'," +

                "serviceLevelAgreement='" + serviceLevelAgreement + "'," +

                "validFor='" + validFor + "'," +

                "bpmPlatformProcessReference='" + bpmPlatformProcessReference + "'," +

                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InternalBackOfficeUserTicket that = (InternalBackOfficeUserTicket) o;

        if (!aclRelatedParty.equals(that.aclRelatedParty)) return false;

        if (!candidateRoles.equals(that.candidateRoles)) return false;

        if (!candidateUsers.equals(that.candidateUsers)) return false;

        if (!candidateOrganizations.equals(that.candidateOrganizations)) return false;

        if (!candidateGroups.equals(that.candidateGroups)) return false;

        if (!assignedQueues.equals(that.assignedQueues)) return false;

        if (!category.equals(that.category)) return false;

        if (!subcategory.equals(that.subcategory)) return false;

        if (!contactMedium.equals(that.contactMedium)) return false;

        if (!externalReference.equals(that.externalReference)) return false;

        if (!serviceLevelAgreement.equals(that.serviceLevelAgreement)) return false;

        if (!validFor.equals(that.validFor)) return false;

        if (!bpmPlatformProcessReference.equals(that.bpmPlatformProcessReference)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;

        result = 31 * result + (aclRelatedParty != null ? aclRelatedParty.hashCode() : 0);

        result = 31 * result + (candidateRoles != null ? candidateRoles.hashCode() : 0);

        result = 31 * result + (candidateUsers != null ? candidateUsers.hashCode() : 0);

        result = 31 * result + (candidateOrganizations != null ? candidateOrganizations.hashCode() : 0);

        result = 31 * result + (candidateGroups != null ? candidateGroups.hashCode() : 0);

        result = 31 * result + (assignedQueues != null ? assignedQueues.hashCode() : 0);

        result = 31 * result + (category != null ? category.hashCode() : 0);

        result = 31 * result + (subcategory != null ? subcategory.hashCode() : 0);

        result = 31 * result + (contactMedium != null ? contactMedium.hashCode() : 0);

        result = 31 * result + (externalReference != null ? externalReference.hashCode() : 0);

        result = 31 * result + (serviceLevelAgreement != null ? serviceLevelAgreement.hashCode() : 0);

        result = 31 * result + (validFor != null ? validFor.hashCode() : 0);

        result = 31 * result + (bpmPlatformProcessReference != null ? bpmPlatformProcessReference.hashCode() : 0);

        return result;
    }
}
