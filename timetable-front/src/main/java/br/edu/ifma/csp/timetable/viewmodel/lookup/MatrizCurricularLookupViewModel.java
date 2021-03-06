package br.edu.ifma.csp.timetable.viewmodel.lookup;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import br.edu.ifma.csp.timetable.model.Curso;
import br.edu.ifma.csp.timetable.model.MatrizCurricular;

public class MatrizCurricularLookupViewModel extends LookupViewModel<MatrizCurricular> {
	
	private Integer ano;
	private Curso curso;
	
	@AfterCompose(superclass=true)
	public void init() {
		
	}
	
	public Integer getAno() {
		return ano;
	}
	
	public void setAno(Integer ano) {
		this.ano = ano;
	}
	
	public Curso getCurso() {
		return curso;
	}
	
	public void setCurso(Curso curso) {
		this.curso = curso;
	}

	@Command
	public void pesquisar() {
		
	}

	@Command
	public void limpar() {
		
	}
}
