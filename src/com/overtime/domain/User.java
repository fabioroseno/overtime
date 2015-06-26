package com.overtime.domain;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import util.convert.I18N;

import com.overtime.enums.UserGroup;

import entities.Context;
import entities.Repository;
import entities.annotations.*;
import entities.descriptor.PropertyType;

@Entity
@Table(name = "USERS")
@NamedQueries({
	@NamedQuery(name = "Autenticacao", query = " From User u Where u.nomeDeUsuario = :nomeDeUsuario And u.senha = :senha "),
    @NamedQuery(name = "Administratores", query = " From User u Where 'Admin' in elements(u.gruposUsuarios) "),
	@NamedQuery(name = "ValidaUserNameEmail", query = " From User u Where u.nomeDeUsuario = :nomeDeUsuario Or u.email = :email ")})
@Views({
    @View(name = "autenticacao",
            title = "com.overtime.domain.User.view.autenticacao.title",
            members = "[#nomeDeUsuario; #senha; entrar()]",
            namedQuery = "Select new com.overtime.domain.User()",
            roles = "NOLOGGED"),
    @View(name = "cadastro",
            title = "com.overtime.domain.User.view.cadastro.title",
            members = "[#nomeCompleto; #email; #nomeDeUsuario; #senha; #confirmeSenha; [cadastrar()]]", 
            namedQuery = "Select new com.overtime.domain.User()",
            roles = "NOLOGGED"),
    @View(name = "perfil",
            title = "com.overtime.domain.User.view.perfil.title",
            members = "['':*foto, [*nomeCompleto; *email; *nomeDeUsuario; *gruposUsuarios; [editarPerfil()]]]",
            namedQuery = " From User u Where u = :nomeDeUsuario",
            params = {@Param(name = "nomeDeUsuario", value = "#{context.currentUser}")},
            roles = "LOGGED"),
    @View(name = "editarPerfil",
            title = "com.overtime.domain.User.view.editarPerfil.title",
            hidden = true,
            members = "['':*foto, [#nomeCompleto, alterarFoto(); #email, alterarSenha(); [*nomeDeUsuario; *gruposUsuarios; [salvarPerfil()]]]]",
            namedQuery = " From User u Where u = :nomeDeUsuario",
            params = {@Param(name = "nomeDeUsuario", value = "#{context.currentUser}")},
            roles = "LOGGED"),
    @View(name = "usuarios",
          title = "com.overtime.domain.User.view.usuarios.title",
          members = "Users[nomeCompleto; nomeDeUsuario; senha; email; gruposUsuarios]:2, *foto",
          template = "@CRUD+@PAGER",
          roles = "Admin"),
    @View(name = "sair",
          title = "com.overtime.domain.User.view.sair.title",
          members = "['':*foto, [msgSair; [sair(), cancelar()]]]",
          namedQuery = "From User u Where u = :nomeDeUsuario",
          params = {@Param(name = "nomeDeUsuario", value = "#{context.currentUser}")},
          roles = "LOGGED")
})
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String MSG_CADASTRO_EFETUADO_I18N = "msg.cadastro.efetuado";
	private static final String MSG_EDICAO_EFETUADA_I18N = "msg.edicao.efetuada";
	private static final String NOVA_SENHA_I18N = "com.overtime.domain.User.alterarSenha.arg[0].displayName";
	private static final String CONFIRMA_SENHA_I18N = "com.overtime.domain.User.alterarSenha.arg[1].displayName";
	private static final String NOVA_FOTO_I18N = "com.overtime.domain.User.alterarFoto.arg[0].displayName";
	
	private static final String MSG_NOME_USUARIO_INDISPONIVEL = I18N.getMessage("msg.nome.usuario.indisponivel", null);
	private static final String MSG_EMAIL_CADASTRADO = I18N.getMessage("msg.email.cadastrado", null);
	private static final String MSG_NOME_USUARIO_REQUERIDO = I18N.getMessage("msg.nome.usuario.requerido", null);
	private static final String MSG_NOME_COMPLETO_REQUERIDO = I18N.getMessage("msg.nome.completo.requerido", null);
	private static final String MSG_EMAIL_REQUERIDO = I18N.getMessage("msg.email.requerido", null);
	private static final String MSG_SENHA_REQUERIDA = I18N.getMessage("msg.senha.requerida", null);
	private static final String MSG_CONFIRMACAO_SENHA_REQUERIDA = I18N.getMessage("msg.confirmacao.requerida", null);
	private static final String MSG_NOME_SENHA_INVALIDOS = I18N.getMessage("msg.nome.senha.invalidos", null);
	private static final String MSG_SENHA_ALTERADA = I18N.getMessage("msg.senha.alterada", null);
	private static final String MSG_CONFIRMACAO_DIFERENTE = I18N.getMessage("msg.confirmacao.diferente", null);
	
	private static final String GO_PAGINA_INICIAL = "go:home";
	private static final String GO_LOGIN = "go:com.overtime.domain.User@autenticacao";
	private static final String GO_PERFIL = "go:com.overtime.domain.User@perfil";
	private static final String GO_EDITAR_PERFIL = "go:com.overtime.domain.User@editarPerfil";
	private static final String GO_USUARIOS = "go:com.overtime.domain.User@usuarios";
	
	private static final String ID_SQL_ADMINISTRADORES = "Administratores";
	private static final String ID_SQL_AUTENTICACAO = "Autenticacao";
	private static final String ID_SQL_VALIDA_NOME_USUARIO_EMAIL = "ValidaUserNameEmail";

    @Id
    @GeneratedValue
    @Column(name="cod_usuario")
    private Integer codUsuario;

    @Column(name="nome_completo", length = 60)
    @PropertyDescriptor(displayWidth = 40)
    private String nomeCompleto;

    @Column(length = 30)
    private String email;
    
    @NotEmpty
    @Username
    @Column(name="nome_de_usuario", length = 30, unique = true)
    private String nomeDeUsuario;

    @NotEmpty
    @Column(length = 32)
    @Type(type = "entities.security.Password")
    @PropertyDescriptor(secret = true, displayWidth = 30)
    private String senha;

    @Transient
    @PropertyDescriptor(secret = true, displayWidth = 30)
    private String confirmeSenha;

    @UserRoles
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name="USERS_GROUPS",
            joinColumns=@JoinColumn(name="cod_usuario")
      )
    @Column(name="cod_user_group")
    private List<UserGroup> gruposUsuarios = new ArrayList<UserGroup>();

    @Lob
    @Editor(propertyType = PropertyType.IMAGE)
    private byte[] foto;

    @Transient
    @PropertyDescriptor(readOnly = true, hidden = true)
    private String msgSair;
    
    public User() {
    }

    public User(String nomeDeUsuario, String senha, UserGroup... gruposUsuarios) {
        this.nomeDeUsuario = nomeDeUsuario;
        this.senha = senha;
        this.gruposUsuarios.addAll(Arrays.asList(gruposUsuarios));
    }
    
    @ActionDescriptor(preValidate = false, confirmMessage = MSG_CADASTRO_EFETUADO_I18N)
    public String cadastrar() {
    	
    	if("".equals(nomeCompleto.trim())) {
    		throw new SecurityException(MSG_NOME_COMPLETO_REQUERIDO);
    	}
    	
    	if("".equals(email.trim())) {
    		throw new SecurityException(MSG_EMAIL_REQUERIDO);
    	}
    	
    	if("".equals(nomeDeUsuario.trim())) {
    		throw new SecurityException(MSG_NOME_USUARIO_REQUERIDO);
    	}
    	
    	if("".equals(senha.trim())) {
    		throw new SecurityException(MSG_SENHA_REQUERIDA);
    	}
    	
    	if("".equals(confirmeSenha.trim())) {
    		throw new SecurityException(MSG_CONFIRMACAO_SENHA_REQUERIDA);
    	}
    	
    	List<User> usuarios = Repository.query(ID_SQL_VALIDA_NOME_USUARIO_EMAIL, nomeDeUsuario, email);
        if (usuarios.size() > 0) {
        	
        	for (User usuario : usuarios) {
        		if(email.equals(usuario.getEmail())) {
            		throw new SecurityException(MSG_EMAIL_CADASTRADO);
            	} else if(nomeDeUsuario.equals(usuario.getNomeDeUsuario())) {
            		throw new SecurityException(MSG_NOME_USUARIO_INDISPONIVEL);
            	}
			}
        	
        }
        
        if(!senha.equals(confirmeSenha)) {
    		throw new SecurityException(MSG_CONFIRMACAO_DIFERENTE);
    	}
        
        Repository.save(this);
    	return GO_LOGIN;
    }

    @ActionDescriptor(preValidate = false)
    public String editarPerfil() {
    	return GO_EDITAR_PERFIL;
	}

    @ActionDescriptor(preValidate = false)
    public String entrar() {
    	if("".equals(nomeDeUsuario.trim())) {
    		throw new SecurityException(MSG_NOME_USUARIO_REQUERIDO);
    	}
    	
    	if("".equals(senha.trim())) {
    		throw new SecurityException(MSG_SENHA_REQUERIDA);
    	}

        if (Repository.queryCount(ID_SQL_ADMINISTRADORES) == 0) {
            User admin = new User(nomeDeUsuario, senha, UserGroup.Admin);
            Repository.save(admin);
            Context.setCurrentUser(admin);
            return GO_USUARIOS;
        } else {
            List<User> users = Repository.query(ID_SQL_AUTENTICACAO, nomeDeUsuario, senha);
            if (users.size() == 1) {
                Context.setCurrentUser((User) users.get(0));
            } else {
                throw new SecurityException(MSG_NOME_SENHA_INVALIDOS);
            }
        }
        return GO_PAGINA_INICIAL;
    }

    @ActionDescriptor(preValidate = false)
    public String sair() {
        Context.clear();
        return GO_LOGIN;
    }

    @ActionDescriptor(preValidate = false)
    public String cancelar() {
        return GO_PAGINA_INICIAL;
    }

    @ActionDescriptor(confirmMessage = MSG_EDICAO_EFETUADA_I18N, preValidate = false)
    public String salvarPerfil() {
    	if("".equals(nomeCompleto.trim())) {
    		throw new SecurityException(MSG_NOME_COMPLETO_REQUERIDO);
    	}
    	
    	if("".equals(email.trim())) {
    		throw new SecurityException(MSG_EMAIL_REQUERIDO);
    	}
    	
    	if (codUsuario != null) {
        	List<User> usuarios = Repository.query(ID_SQL_VALIDA_NOME_USUARIO_EMAIL, nomeDeUsuario, email);
            if (usuarios.size() == 1) {
            	Repository.save(this);
            } else {
            	throw new SecurityException(MSG_EMAIL_CADASTRADO);
            }
        }
        return GO_PERFIL;
    }
    
	public String alterarSenha(
			@ParameterDescriptor(displayName = NOVA_SENHA_I18N, secret = true, required = true) String novaSenha,
			@ParameterDescriptor(displayName = CONFIRMA_SENHA_I18N, secret = true, required = true) String confSenha) {
		if (novaSenha.equals(confSenha)) {
			this.setSenha(novaSenha);
			Repository.save(this);
			return MSG_SENHA_ALTERADA;
		} else {
			throw new SecurityException(MSG_CONFIRMACAO_DIFERENTE);
		}
	}

    public void alterarFoto(@ParameterDescriptor(displayName = NOVA_FOTO_I18N) byte[] novaFoto) {
        this.foto = novaFoto;
        if (codUsuario != null) {
            Repository.save(this);
        }
    }

	public Integer getCodUsuario() {
		return codUsuario;
	}

	public void setCodUsuario(Integer codUsuario) {
		this.codUsuario = codUsuario;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNomeDeUsuario() {
		return nomeDeUsuario;
	}

	public void setNomeDeUsuario(String nomeDeUsuario) {
		this.nomeDeUsuario = nomeDeUsuario;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmeSenha() {
		return confirmeSenha;
	}

	public void setConfirmeSenha(String confirmeSenha) {
		this.confirmeSenha = confirmeSenha;
	}

	public List<UserGroup> getGruposUsuarios() {
		return gruposUsuarios;
	}

	public void setGruposUsuarios(List<UserGroup> gruposUsuarios) {
		this.gruposUsuarios = gruposUsuarios;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}
	
	public String getMsgSair() {
		return msgSair;
	}

	public void setMsgSair(String msgSair) {
		this.msgSair = msgSair;
	}

	@Override
    public String toString() {
        return nomeDeUsuario;
    }
	
}
