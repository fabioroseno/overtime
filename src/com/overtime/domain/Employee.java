package com.overtime.domain;

import java.io.Serializable;

import javax.persistence.Entity;

import entities.annotations.View;
import entities.annotations.Views;

@Entity
@Views({
    @View(name = "Employees",
          title = "Employees",
          members = "Employee[nomeCompleto;nomeDeUsuario;senha;gruposUsuarios]",
          template = "@CRUD_PAGE",
          roles = "Admin,RH")
})
public class Employee extends User implements Serializable {

	private static final long serialVersionUID = 1L;
	
}
