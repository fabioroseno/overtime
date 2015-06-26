package com.overtime.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import entities.annotations.EntityDescriptor;

@Entity
@DiscriminatorValue("WaitingForPayment")
@EntityDescriptor(hidden = true)
public class StatusWaitingForPayment extends Status {

    public StatusWaitingForPayment() {
        this.setId(2L);
        this.setDescription("Waiting For Payment");
    }

    public StatusWaitingForPayment(Overtime overtime) {
        this();
        this.overtime = overtime;
    }

    @Override
    public void approve() {
        throw new IllegalStateException("This request has already been approved");
    }

    @Override
    public void pay() {
        Status validado = new StatusPaid();
        overtime.setStatus(validado);
    }

    @Override
    public void reject() {
        Status cancelado = new StatusCanceled(overtime);
        overtime.setStatus(cancelado);
    }

    @Override
    public void revert(String remark) {
        Status aguardandoAutorizacao = new StatusWaitingForApproval();
        overtime.setRemark(remark);
        overtime.setStatus(aguardandoAutorizacao);
    }
}
