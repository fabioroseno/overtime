package com.overtime.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import entities.annotations.EntityDescriptor;

@Entity
@DiscriminatorValue("Approved")
@EntityDescriptor(hidden = true)
public class StatusPaid extends Status {

    public StatusPaid() {
        this.setId(3L);
        this.setDescription("Approved");
    }

    StatusPaid(Overtime overtime) {
        this();
        this.overtime = overtime;
    }

    @Override
    public void approve() {
        throw new IllegalStateException("This request has already been paid");
    }

    @Override
    public void pay() {
        throw new IllegalStateException("This request has already been paid");
    }

    @Override
    public void reject() {
        Status cancelado = new StatusCanceled(overtime);
        overtime.setStatus(cancelado);
    }

    @Override
    public void revert(String remark) {
        throw new IllegalStateException("This request has already been approved");
    }
}