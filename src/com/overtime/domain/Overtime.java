package com.overtime.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.validator.constraints.NotEmpty;

import entities.RepositoryAtomic;
import entities.annotations.ActionDescriptor;
import entities.annotations.Editor;
import entities.annotations.ParameterDescriptor;
import entities.annotations.View;
import entities.annotations.Views;
import entities.descriptor.PropertyType;

@Data
@Entity
@Table(name = "OVERTIME")
@EqualsAndHashCode(of = "id")
@Views({
    @View(name = "RequestOvertime",
      title = "Request Overtime",
      members = "[#employee;#beginning;#ending;#description;request()]",
      namedQuery = "Select new com.overtime.domain.Overtime()",
      roles = "LOGGED"),
    @View(name = "AuthorizeOvertime",
      title = "Authorize Overtime",
      members = "[employee;beginning,ending;#description,remark;[approve(),reject()]]",
      namedQuery = "Select ot from Overtime ot,StatusWaitingForApproval st where ot.status = st",
      roles = "Admin,Supervisor"),
    @View(name = "AuthorizePaymentOfOvertime",
      title = "Authorize Payment of Overtime",
      members = "Request Overtime[[employee;beginning;ending],description],Action[pay();revert();reject()]",
      namedQuery = "Select ot from Overtime ot,StatusWaitingForPayment st where ot.status = st",
      roles = "Admin,RH"),
    @View(name = "Overtimes",
      title = "Overtimes",
      filters = "employee",
      members = "[employee;beginning;ending;remark;description;status;[approve(),reject(),pay(),revert()]]",
      template = "@FORM+@FILTER",
      roles = "Admin")
})
public class Overtime implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @ManyToOne
    private Employee employee;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date beginning;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date ending;

    @Lob
    @NotEmpty
    private String description;

    @Lob
    private String remark;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private Status status = new StatusWaitingForApproval(this);

    @ActionDescriptor(refreshView = true)
    public void approve() {
        this.status.setOvertime(this);
        status.approve();
        RepositoryAtomic.save(this);
    }

    public Date getBeginning() {
        return beginning;
    }

    public String getDescription() {
        return description;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Date getEnding() {
        return ending;
    }

    public Integer getId() {
        return id;
    }

    public String getRemark() {
        return remark;
    }

    public Status getStatus() {
        return status;
    }

    @ActionDescriptor(image = "image:round_ok", refreshView = true)
    public void pay() {
        this.status.setOvertime(this);
        status.pay();
        RepositoryAtomic.save(this);
    }

    @ActionDescriptor(confirm = true, image = "image:trash", refreshView = true)
    public void reject() {
        this.status.setOvertime(this);
        status.reject();
        RepositoryAtomic.save(this);
    }

    @ActionDescriptor(refreshView = true)
    public String request() {
        RepositoryAtomic.save(this);
        return "i18n['com.overtime.domain.Overtime.request.sucess']";
    }

    @ActionDescriptor(image = "image:trackback", refreshView = true)
    public void revert(
            @Editor(propertyType = PropertyType.MEMO)
            @ParameterDescriptor(displayName = "Remark") String remark) {
        this.status.setOvertime(this);
        status.revert(remark);
        RepositoryAtomic.save(this);
    }

    public void setBeginning(Date beginning) {
        this.beginning = beginning;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setEnding(Date ending) {
        this.ending = ending;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
        
}
