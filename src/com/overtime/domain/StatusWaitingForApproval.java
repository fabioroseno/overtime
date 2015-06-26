package com.overtime.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import entities.annotations.EntityDescriptor;

@Entity
@DiscriminatorValue("WaitingForApproval")
@EntityDescriptor(hidden = true)
public class StatusWaitingForApproval extends Status {

    public StatusWaitingForApproval() {
        this.setId(1L);
        this.setDescription("Waiting For Approval");
    }

    public StatusWaitingForApproval(Overtime overtime) {
        this();
        this.overtime = overtime;
    }

    @Override
    public void approve() {
        Status aguardandoValidacao = new StatusWaitingForPayment(overtime);
        overtime.setStatus(aguardandoValidacao);
    }

    @Override
    public void pay() {
        throw new IllegalStateException("This request was not approved.");
    }

    @Override
    public void reject() {
        Status cancelado = new StatusCanceled(overtime);
        overtime.setStatus(cancelado);
    }

    @Override
    public void revert(String remark) {
        throw new IllegalStateException("This request was not approved.");
    }
}
