package br.edu.ifma.csp.timetable.viewmodel;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValuesException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

import br.edu.ifma.csp.timetable.model.Entidade;
import br.edu.ifma.csp.timetable.repository.Repository;
import br.edu.ifma.csp.timetable.util.Validations;

public abstract class ViewModel<T extends Entidade> {
	
	protected Repository<T> repository;
	protected T entidadeSelecionada;
	
	private Boolean editando;
	private Boolean consultando;
	private Boolean removivel;
	
	private List<T> col;
	
	@AfterCompose
	@NotifyChange("*")
	public void initViewModel() throws NamingException, InstantiationException, IllegalAccessException {
		
		repository = InitialContext.doLookup("java:module/" + retornaTipo().getSimpleName() + "Dao");
		setCol(repository.all());
		novo();
	}
	
	public abstract void init(Component view);
	
	@Command
	@NotifyChange({"entidadeSelecionada", "consultando", "removivel", "editando"})
	public void novo() throws InstantiationException, IllegalAccessException {
		
		entidadeSelecionada = retornaTipo().newInstance();
		entidadeSelecionada.setUsuarioUltAlteracao("user");
		entidadeSelecionada.setDataUltAlteracao(new Date());
		
		setEditando(true);
		setConsultando(false);
		setRemovivel(true);
	}
	
	@Command
	@NotifyChange({"entidadeSelecionada", "consultando", "removivel", "editando", "col"})
	public void salvar() throws WrongValuesException {
		
		try {
			
			Validations.validate(entidadeSelecionada, repository);
			
			repository.save(entidadeSelecionada);
			
			pesquisar();
			
			Clients.showNotification("Informações salvas com sucesso!", Clients.NOTIFICATION_TYPE_INFO, null, "middle_center", 1500);
			
		} catch(WrongValuesException ex) {
			Validations.showValidationErrors();
		}
	}
	
	@Command
	@GlobalCommand
	@NotifyChange("*")
	public void pesquisar() {
	
		setEntidadeSelecionada(null);
		setCol(repository.all());
		
		setConsultando(true);
		setEditando(false);
		setRemovivel(true);
	}
	
	@Command
	@NotifyChange({"entidadeSelecionada", "consultando", "removivel", "editando"})
	public void editar() {
		
		setEditando(true);
		setConsultando(false);
		setRemovivel(false);
	}
	
	@Command
	@NotifyChange({"entidadeSelecionada", "consultando", "removivel", "editando", "col"})
	public void excluir() {
		
		Messagebox.show("Deseja realmente excluir este registro?", "Excluir Registro?", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
			
			@NotifyChange({"entidadeSelecionada", "consultando", "removivel", "editando", "col"})
			public void onEvent(Event ev) throws Exception {
				
				if (ev.getName().equals(Messagebox.ON_YES)) {
					
					repository.delete(entidadeSelecionada);

					Clients.showNotification("Registro excluído com sucesso!", Clients.NOTIFICATION_TYPE_INFO, null, "middle_center", 1500);
					
					BindUtils.postGlobalCommand(null, null, "pesquisar", null);
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private Class<T> retornaTipo() {
	    
		Class<?> clazz = this.getClass();
	     
	    while (!clazz.getSuperclass().equals(ViewModel.class)) {
	        clazz = clazz.getSuperclass();
	    }
	     
	    ParameterizedType tipoGenerico = (ParameterizedType) clazz.getGenericSuperclass();
	    
	    return (Class<T>) tipoGenerico.getActualTypeArguments()[0];
    }
	
	public Boolean getRemovivel() {
		return removivel;
	}
	
	public void setRemovivel(Boolean removivel) {
		this.removivel = removivel;
	}
	
	public Boolean getEditando() {
		return editando;
	}
	
	public void setEditando(Boolean editando) {
		this.editando = editando;
	}
	
	public Boolean getConsultando() {
		return consultando;
	}
	
	public void setConsultando(Boolean consultando) {
		this.consultando = consultando;
	}
	
	/*public boolean isEditando() {
		return entidadeSelecionada != null;
	}
	
	public boolean isConsultando() {
		return !isEditando();
	}
	
	public boolean isRemovivel() {
		return !(isEditando() && entidadeSelecionada.getId() != 0);
	}*/
	
	public List<T> getCol() {
		return col;
	}
	
	public void setCol(List<T> col) {
		this.col = col;
	}
	
	public T getEntidadeSelecionada() {
		return entidadeSelecionada;
	}
	
	public void setEntidadeSelecionada(T entidadeSelecionada) {
		this.entidadeSelecionada = entidadeSelecionada;
	}
}
