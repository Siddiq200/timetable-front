package br.edu.ifma.csp.timetable.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValuesException;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Vbox;

import br.edu.ifma.csp.timetable.dao.HorarioDao;
import br.edu.ifma.csp.timetable.dao.LocalDao;
import br.edu.ifma.csp.timetable.dao.ProfessorDao;
import br.edu.ifma.csp.timetable.handler.TimetableHandler;
import br.edu.ifma.csp.timetable.model.Aula;
import br.edu.ifma.csp.timetable.model.DetalheTimetable;
import br.edu.ifma.csp.timetable.model.Local;
import br.edu.ifma.csp.timetable.model.Periodo;
import br.edu.ifma.csp.timetable.model.Professor;
import br.edu.ifma.csp.timetable.model.Timetable;
import br.edu.ifma.csp.timetable.repository.Horarios;
import br.edu.ifma.csp.timetable.repository.Locais;
import br.edu.ifma.csp.timetable.repository.Professores;
import br.edu.ifma.csp.timetable.util.Lookup;
import br.edu.ifma.csp.timetable.util.Validations;

public class TimetableViewModel extends ViewModel<Timetable> {
	
	private Horarios horarios;
	private Locais locais;
	private Professores professores;
	
	private List<Professor> colProfessores;
	private List<Local> colLocais;
	private List<String> colHorariosInicio;
	
	private Local local;
	private Professor professor;
	private Periodo periodo;
	
	@Wire("#form #grid")
	private Grid grid;
	
	private List<DetalheTimetable> detalhesSelecionados;
	
	@Init
	@AfterCompose(superclass=true)
	public void init(@ContextParam(ContextType.VIEW) Component view) {
		
		horarios = Lookup.dao(HorarioDao.class);
		professores = Lookup.dao(ProfessorDao.class);
		locais = Lookup.dao(LocalDao.class);
		
		setColProfessores(professores.all());
		setColLocais(locais.all());
		
		initHorarios();
		
		Selectors.wireComponents(view, this, false);
	}
	
	@NotifyChange("colHorariosInicio")
	private void initHorarios() {
		
		colHorariosInicio = new ArrayList<String>();
		
		for (Date horario : horarios.allHorariosInicio()) {
			colHorariosInicio.add(horario.toString());
		}
	}
	
	@Command
	@NotifyChange("*")
	public void salvar() {
	
		Validations.validate(null, entidadeSelecionada, repository);
		
		try {
			
			setProfessor(null);
			setPeriodo(null);
			setLocal(null);
			
			TimetableHandler handler = new TimetableHandler();
			handler.setTimetable(entidadeSelecionada);
			handler.execute();
			
			buildRows();
			
		} catch (WrongValuesException ex) {
			
			Clients.showNotification("AA");
			
			ex.printStackTrace();
		}
	}
	
	private void buildRows() {
		
		List<Date> listaHorarios = horarios.allHorariosInicio();
		
		grid.getRows().getChildren().clear();
		
		for (Date horario : listaHorarios) {
			
			Row row = new Row();
			
			Label label = new Label(horario.toString());
			//label.setSclass("z-column");
			
			Vbox layoutSegunda = new Vbox();
			layoutSegunda.setSpacing("20px");
			//layoutSegunda.setAlign("center");
			//layoutSegunda.setPack("center");
			
			Vbox layoutTerca = new Vbox();
			layoutTerca.setSpacing("20px");
//			layoutTerca.setAlign("center");
//			layoutTerca.setPack("center");
			
			Vbox layoutQuarta = new Vbox();
			layoutQuarta.setSpacing("20px");
//			layoutQuarta.setAlign("center");
//			layoutQuarta.setPack("center");
			
			Vbox layoutQuinta = new Vbox();
			layoutQuinta.setSpacing("20px");
//			layoutQuinta.setAlign("center");
//			layoutQuinta.setPack("center");
			
			Vbox layoutSexta = new Vbox();
			layoutSexta.setSpacing("20px");
//			layoutSexta.setAlign("center");
//			layoutSexta.setPack("center");
			
			row.getChildren().addAll(Arrays.asList(new Component[]{label, layoutSegunda, layoutTerca, layoutQuarta, layoutQuinta, layoutSexta}));
			grid.getRows().getChildren().add(row);
		}
		
		for (Aula aula : entidadeSelecionada.getAulas()) {
				
			Vbox vlayout = new Vbox();
			
			//vlayout.setPack("center");
			//vlayout.setAlign("center");
			
			Label labelPeriodo = new Label(String.valueOf(entidadeSelecionada.getMatrizCurricular().getCurso().getCodigo() + "." + aula.getPeriodo()));
			labelPeriodo.setSclass("grid-label");
			
			Label labelDisciplina = new Label(aula.getDisciplina().getSigla());
			labelDisciplina.setTooltiptext(aula.getDisciplina().getDescricao());
			
			Label labelProfessor = new Label(aula.getProfessor().getNome());
			labelProfessor.setSclass("grid-label");
			
			Label labelLocal = new Label(aula.getLocal().getNome());
			labelLocal.setSclass("grid-label");
			
			vlayout.appendChild(labelDisciplina);
			vlayout.appendChild(labelProfessor);
			vlayout.appendChild(labelLocal);
			vlayout.appendChild(labelPeriodo);
			
			int rowIndex = getRowIndex(aula.getHorario().getHoraInicio().toString());
			int colIndex = getColumnIndex(aula.getHorario().getDia().getDescricao());
			
			if (!(rowIndex == 0 && colIndex == 0)) {
			
				Component cell = grid.getCell(rowIndex, colIndex);
				
				cell.appendChild(vlayout);
			}
		}
	}
	
	@Command
	@NotifyChange({"professor", "periodo", "local"})
	public void limpar(@BindingParam("opc") int opc) {
		
		switch (opc) {
		
			case 1:
				setPeriodo(null);
				break;
			
			case 2:
				setProfessor(null);
				break;
				
			case 3:
				setLocal(null);
				break;
		}
		
		lookup();
	}
	
	private int getRowIndex(String label) {
		
		for (int i = 0; i < grid.getRows().getChildren().size(); i++) {
			
			Component c = grid.getCell(i, 0);
			
			if (c instanceof Label) {
				
				if (((Label)c).getValue().equalsIgnoreCase(label)) {
					return i;
				}
			}
		}
		
		return 0;
	}
	
	private int getColumnIndex(String label) {
		
		for (Component column : grid.getColumns().getChildren()) {
			
			if (column instanceof Column) {
				
				if (((Column)column).getLabel().equalsIgnoreCase(label)) {
					
					return grid.getColumns().getChildren().indexOf(column);
				}
			}
		}
		
		return 0;
	}
	
	public void lookup() {
		
		buildRows();
		
		for (Component c : grid.getRows().getChildren()) {
			
			if (c instanceof Row && c.getChildren().size() > 0) {
			
				for (Component x : c.getChildren()) {
					
					if (x instanceof Vbox && c.getChildren().size() > 0) {
						
						for (Component v : x.getChildren()) {
							
							if (v instanceof Vbox && v.getChildren().size() > 0) {
								
								Component local = v.getChildren().get(2);
								Component comp = v.getChildren().get(1);
								Component periodo = v.getChildren().get(3);
								
								if (getPeriodo() != null) {
									
									if (!((Label)periodo).getValue().contains(String.valueOf(getPeriodo().getCodigo()))) {
										v.setVisible(false);
									}
								}
								
								if (getLocal() != null) {
									
									if (!((Label)local).getValue().equalsIgnoreCase(getLocal().getNome())) {
										v.setVisible(false);
									}
								}
								
								if (getProfessor() != null) {
								
									if (comp instanceof Label) {
										
										if (!((Label)comp).getValue().equalsIgnoreCase(getProfessor().getNome())) {
											v.setVisible(false);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Command
	@NotifyChange("entidadeSelecionada")
	public void onChange() {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("matrizCurricular", entidadeSelecionada.getMatrizCurricular());
		
		BindUtils.postGlobalCommand(null, null, "setMatrizCurricular", params);
	}
	
	@Command
	@NotifyChange("entidadeSelecionada")
	public void adicionarDetalhe() {
		
		DetalheTimetable detalheTimetable = new DetalheTimetable();
		
		detalheTimetable.setTimetable(entidadeSelecionada);
		entidadeSelecionada.getDetalhes().add(detalheTimetable);
	}
	
	@Command
	@NotifyChange("entidadeSelecionada")
	public void removerDetalhe() {
		entidadeSelecionada.getDetalhes().removeAll(detalhesSelecionados);
	}
	
	public boolean isSolucaoEncontrada() {
		return entidadeSelecionada != null && entidadeSelecionada.getAulas().size() > 0;
	}
	
	public List<DetalheTimetable> getDetalhesSelecionados() {
		return detalhesSelecionados;
	}
	
	public void setDetalhesSelecionados(List<DetalheTimetable> detalhesSelecionados) {
		this.detalhesSelecionados = detalhesSelecionados;
	}

	public Local getLocal() {
		return local;
	}

	public void setLocal(Local local) {
		this.local = local;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}
	
	public Periodo getPeriodo() {
		return periodo;
	}
	
	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}

	public List<Professor> getColProfessores() {
		return colProfessores;
	}

	public void setColProfessores(List<Professor> colProfessores) {
		this.colProfessores = colProfessores;
	}

	public List<Local> getColLocais() {
		return colLocais;
	}

	public void setColLocais(List<Local> colLocais) {
		this.colLocais = colLocais;
	}
	
	public List<String> getColHorariosInicio() {
		return colHorariosInicio;
	}
	
	public void setColHorariosInicio(List<String> colHorariosInicio) {
		this.colHorariosInicio = colHorariosInicio;
	}
}