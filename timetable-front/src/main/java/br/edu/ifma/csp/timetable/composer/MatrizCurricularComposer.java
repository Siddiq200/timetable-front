package br.edu.ifma.csp.timetable.composer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.impl.ReferenceBindingImpl;

import br.edu.ifma.csp.timetable.dao.TurnoDao;
import br.edu.ifma.csp.timetable.model.DetalheDisciplina;
import br.edu.ifma.csp.timetable.model.MatrizCurricular;
import br.edu.ifma.csp.timetable.model.Periodo;
import br.edu.ifma.csp.timetable.model.Turno;
import br.edu.ifma.csp.timetable.repository.Turnos;
import br.edu.ifma.csp.timetable.util.Lookup;

public class MatrizCurricularComposer extends Composer<MatrizCurricular> {

	private static final long serialVersionUID = 20928881137239658L;
	
	private List<Periodo> periodosSelecionados;
	private List<DetalheDisciplina> disciplinasSelecionadas;
	private List<Turno> colTurnos;
	private List<String> colGrupoEletivas;
	private List<Integer> colPeriodos;
	
	private Turnos turnos;

	@Init
	public void init() {
		
		turnos = Lookup.dao(TurnoDao.class);
		setColTurnos(turnos.all());
		
		colGrupoEletivas = new ArrayList<String>();
		colGrupoEletivas.add("I-A");
		colGrupoEletivas.add("I-B");
		colGrupoEletivas.add("II");
		
		getBinder().notifyChange(this, "*");
	}
	
	@Override
	public void edit() {
		
		super.edit();
		
		setColTurnos(turnos.all());
		
		if (entidade != null && entidade.getSemestres() != null) {
			adicionarColPeriodos(entidade.getSemestres());
		}
	}
	
	
	public void adicionarPeriodo() {
		
		Periodo periodo = new Periodo();
		periodo.setMatrizCurricular(entidade);
		entidade.getPeriodos().add(periodo);
		
		getBinder().notifyChange(entidade, "periodos");
	}
	
	public void removerPeriodo() {
		
		entidade.getPeriodos().removeAll(periodosSelecionados);
		
		getBinder().notifyChange(entidade, "periodos");
	}
	
	public void adicionarDisciplina(ReferenceBindingImpl periodo) {
		
		Periodo periodoSelecionado = (Periodo) periodo.getValue(null);
		
		DetalheDisciplina detalheDisciplina = new DetalheDisciplina();
		detalheDisciplina.setPeriodo(periodoSelecionado);
		
		periodoSelecionado.getDetalhes().add(0, detalheDisciplina);
		
		getBinder().notifyChange(periodoSelecionado, "detalhes");
	}
	
	public void removerDisciplina(ReferenceBindingImpl periodo) {
		
		Periodo periodoSelecionado = (Periodo) periodo.getValue(null);
		
		periodoSelecionado.getDetalhes().removeAll(disciplinasSelecionadas);
		
		getBinder().notifyChange(periodoSelecionado, "detalhes");
	}
	
	public void adicionarColPeriodos(int semestres) {
		setColPeriodos(IntStream.of(IntStream.rangeClosed(1, semestres).toArray()).boxed().collect(Collectors.toList()));
		getBinder().notifyChange(this, "colPeriodos");
	}
	
	public List<Periodo> getPeriodosSelecionados() {
		return periodosSelecionados;
	}
	
	public void setPeriodosSelecionados(List<Periodo> periodosSelecionados) {
		this.periodosSelecionados = periodosSelecionados;
	}

	public List<DetalheDisciplina> getDisciplinasSelecionadas() {
		return disciplinasSelecionadas;
	}

	public void setDisciplinasSelecionadas(List<DetalheDisciplina> disciplinasSelecionadas) {
		this.disciplinasSelecionadas = disciplinasSelecionadas;
	}
	
	public List<String> getColGrupoEletivas() {
		return colGrupoEletivas;
	}
	
	public void setColGrupoEletivas(List<String> colGrupoEletivas) {
		this.colGrupoEletivas = colGrupoEletivas;
	}
	
	public List<Turno> getColTurnos() {
		return colTurnos;
	}
	
	public void setColTurnos(List<Turno> colTurnos) {
		this.colTurnos = colTurnos;
	}

	public List<Integer> getColPeriodos() {
		return colPeriodos;
	}

	public void setColPeriodos(List<Integer> colPeriodos) {
		this.colPeriodos = colPeriodos;
	}
}