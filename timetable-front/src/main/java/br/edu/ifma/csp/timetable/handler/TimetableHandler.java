package br.edu.ifma.csp.timetable.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

import br.edu.ifma.csp.timetable.dao.DisciplinaDao;
import br.edu.ifma.csp.timetable.dao.HorarioDao;
import br.edu.ifma.csp.timetable.dao.LocalDao;
import br.edu.ifma.csp.timetable.dao.ProfessorDao;
import br.edu.ifma.csp.timetable.model.Aula;
import br.edu.ifma.csp.timetable.model.DetalheDisciplina;
import br.edu.ifma.csp.timetable.model.Disciplina;
import br.edu.ifma.csp.timetable.model.Entidade;
import br.edu.ifma.csp.timetable.model.Horario;
import br.edu.ifma.csp.timetable.model.Local;
import br.edu.ifma.csp.timetable.model.MatrizCurricular;
import br.edu.ifma.csp.timetable.model.Periodo;
import br.edu.ifma.csp.timetable.model.Professor;
import br.edu.ifma.csp.timetable.model.Timeslot;
import br.edu.ifma.csp.timetable.model.Timetable;
import br.edu.ifma.csp.timetable.repository.Disciplinas;
import br.edu.ifma.csp.timetable.repository.Horarios;
import br.edu.ifma.csp.timetable.repository.Locais;
import br.edu.ifma.csp.timetable.repository.Professores;
import br.edu.ifma.csp.timetable.util.Lookup;

/**
 * Modelo de Timetable utilizado na resolução do Problema de Alocação de Horários do IFMA. <br>
 * O modelo a seguir é uma abstração baseada na classe {@code org.chocosolver.samples.AbstractProblem}. <br>
 * <b>Framework: </b> Choco Solver (v. 4.0.0) <br>
 * <b>Flow (CSP): </b> <br>
 * 	<ol>
 * 		<li>Definir modelo</li>
 * 		<li>Construir modelo: </li>
 * 		<ol>
 * 			<li>Definir variáveis </li>
 * 			<li>Definir domínios </li>
 * 			<li>Definir restrições </li>
 * 		</ol>
 * 		<li>Definir estratégia de busca </li>
 * 		<li>Executar busca</li>
 * 		<li>Recuperar resultado(s)</li>
 *  </ol>  
 *  
 * @author inalberth
 * @since 2016
 */
public class TimetableHandler {
	
	private Timetable timetable;
	
	private Disciplinas disciplinas;
	private Professores professores;
	private Horarios horarios;
	private Locais locais;
	
	IntVar [] varDisciplinas;
	IntVar [] varProfessores;
	IntVar [][] varHorariosPeriodo;
	IntVar [][] varHorariosProfessor;
	IntVar [][] varHorariosDisciplina;
	IntVar [][] varHorariosLocal;
	
	List<Professor> listProfessores;
	List<Disciplina> listDisciplina;
	List<Horario> listHorarios;
	List<DetalheDisciplina> listDetalhes;
	List<Local> listLocais;
	
	List<Timeslot> timeslots;
	
	int [] disciplinasId;
	int [] professoresId;
	int [] horariosId;
	int [] locaisId;
	int [] aulas;
	
	int [][] periodos;
	
	private Model model;
	private Solver solver;
	
	private final void init() {
		
		disciplinas = Lookup.dao(DisciplinaDao.class);
		professores = Lookup.dao(ProfessorDao.class);
		horarios = Lookup.dao(HorarioDao.class);
		locais = Lookup.dao(LocalDao.class);
		
		listProfessores = professores.allByMatrizCurricular(timetable.getMatrizCurricular());
		listDisciplina = disciplinas.allByMatrizCurricular(timetable.getMatrizCurricular());
		listDetalhes = allByMatrizCurricular(timetable.getMatrizCurricular());
		listHorarios = horarios.all();
		listLocais = locais.all();
		
		disciplinasId = extrairIds(listDisciplina);
		professoresId  = extrairIds(listProfessores);
		horariosId = extrairIds(listHorarios);
		locaisId = extrairIds(listLocais);
		aulas = extrairCreditos(listDetalhes);
	}

	
	private final void createSolver() {
		
		model = new Model("Timetable");
		
		solver = model.getSolver();
	}
	
	private final void buildModel() {
		
		init();
		
		timeslots = new ArrayList<Timeslot>();
		
		varProfessores = model.intVarArray("P", disciplinasId.length, professoresId);
		varDisciplinas = new IntVar[disciplinasId.length];
		
		varHorariosDisciplina = new IntVar[varDisciplinas.length][];
		varHorariosLocal = new IntVar[varDisciplinas.length][];
		
		periodos = new int[timetable.getMatrizCurricular().getPeriodos().size()][];
		
		for (int i = 0; i < timetable.getMatrizCurricular().getPeriodos().size(); i++) {
			
			Periodo periodo = timetable.getMatrizCurricular().getPeriodos().get(i);
			
			List<DetalheDisciplina> detalhes = allObrigatoriasByPeriodo(periodo);
			
			periodos[i] = new int[detalhes.size()];
			
			for (int j = 0; j < detalhes.size(); j++) {
				periodos[i][j] = detalhes.get(j).getDisciplina().getId();
			}
		}
		
		for (int i = 0; i < varProfessores.length; i++) {
			
			varDisciplinas[i] = model.intVar("D[" + i + "]", disciplinasId[i]);
			
			Timeslot timeslot = new Timeslot();
			timeslot.addProfessor(varProfessores[i]);
			timeslot.addDisciplina(varDisciplinas[i]);
			
			varHorariosDisciplina[i] = new IntVar[aulas[i]];
			varHorariosLocal[i] = new IntVar[aulas[i]];
			
			for (int j = 0; j < aulas[i]; j++) {
				
				IntVar horario = model.intVar(varDisciplinas[i].getName() + "_" + "H" + (j+1), 0, horariosId.length);
				timeslot.addHorario(horario);
				
				IntVar local = model.intVar(varDisciplinas[i].getName() + "_" + "L" + (j+1), 0, locaisId.length);
				timeslot.addLocal(local);
				
				varHorariosDisciplina[i][j] = horario;
				varHorariosLocal[i][j] = local;
			}
			
			timeslots.add(timeslot);
		}
		
		manterProfessorQuePossuiPreferenciaConstraint();
		
		manterProfessoresComHorarioUnicoConstraint();
		
		manterLocaisComHorarioUnicoConstraint();
		
		manterDisciplinasComHorarioUnicoConstraint();
		
		manterHorariosOrdenadosConstraint();
		
		manterHorariosAlternadosConstraint();
		
		manterHorariosConsecutivosConstraint();
	}

	
	private final void configureSearch() {
		
		solver.makeCompleteStrategy(true);
		solver.setSearch(Search.minDomLBSearch(varDisciplinas));
	}
	
	
	public void execute() {
		
		createSolver();
		buildModel();
		configureSearch();
		solve();
	}

	
	private final void solve() {
		
		solver.showStatistics();
		
		if (solver.solve()) {
			prettyOut();
		}
	}
	
	public Disciplina getDisciplina(int disciplina) {
		
		for (Disciplina d : listDisciplina) {
			
			if (d.getId() == disciplina)
				return d;
		}
		
		return null;
	}
	
	public Professor getProfessor(int professor) {
		
		for (Professor p : listProfessores) {
			
			if (p.getId() == professor)
				return p;
		}
		
		return null;
	}
	
	public Local getLocal(int local) {
		
		int index = locaisId[local];
		
		for (Local l : listLocais) {
			
			if (l.getId() == index)
				return l;
		}
		
		return null;
	}
	
	public Horario getHorario(int horario) {
		
		int index = horariosId[horario];
		
		for (Horario h : listHorarios) {
			
			if (h.getId() == index)
				return h;
		}
		
		return null;
	}
	
	public void prettyOut() {
		
		for (Timeslot timeslot : timeslots) {
			
			System.out.print(timeslot.getDisciplina().getName() + " = " + getDisciplina(timeslot.getDisciplina().getValue()) + ", ");
			System.out.println(timeslot.getProfessor().getName() + " = " + getProfessor(timeslot.getProfessor().getValue()));
			
			for (int i = 0; i < timeslot.getLocais().size(); i++) {
				
				IntVar local = timeslot.getLocais().get(i);
				IntVar horario = timeslot.getHorarios().get(i);
				
				System.out.println(local.getName() + " = " + getLocal(local.getValue()) + ", " + horario.getName() + " = " + getHorario(horario.getValue()));
			}
		}
	}
	
	/**
	 * <b>Restrição Forte:</b> <br>
	 *  
	 * Um professor só poderá ser selecionado para ministrar apenas as disciplinas para as quais tenha preferência. 
	 * Dado um um professor <i>P<sub>i<sub></i> e um conjunto <i>D<sub>j<sub></i> disciplinas disponíveis, 
	 * é utilizada a restrição {@link Model #table(IntVar, IntVar, Tuples)}, a qual é responsável por selecionar
	 * para uma variável um valor dentre os disponíveis na coleção.
	 */
	
	private void manterProfessorQuePossuiPreferenciaConstraint() {
		
		Tuples tuples = new Tuples(true);
		
		for (int i = 0; i < varProfessores.length; i++) {
			
			int [] professoresId = extrairIds(professores.allByPreferenciaDisciplina(disciplinas.byId(disciplinasId[i])));
			
			for (int j = 0; j < professoresId.length; j++) {
				tuples.add(disciplinasId[i], professoresId[j]);
			}
		}
		
		for (int i = 0; i < varProfessores.length; i++) {			
			model.table(varDisciplinas[i], varProfessores[i], tuples).post();
		}
	}
	
	/**
	 * <b>Restrição Forte:</b> <br>
	 *  
	 * Um professor não pode ser selecionado para ministrar duas aulas de disciplinas diferentes
	 * no mesmo horário. Dado um conjunto de <b>{@code n}</b> horários de um professor, é aplicada a restrição 
	 * {@link Model #allDifferent(IntVar[], String)}, a qual mantém um professor com apenas uma ocorrência de horário.
	 */

	private void manterProfessoresComHorarioUnicoConstraint() {
		
		varHorariosProfessor = new IntVar[professoresId.length][];
		
		for (int i = 0; i < professoresId.length; i++) {
			
			List<IntVar> horarios = new ArrayList<IntVar>();
			
			int [] disciplinasProfessorId = extrairIds(disciplinas.allByPreferenciaProfessor(professores.byId(professoresId[i])));
			
			for (int j = 0; j < disciplinasProfessorId.length; j++) {
				
				if (getTimeslotDisciplina(disciplinasProfessorId[j]) != null) {
				
					horarios.addAll(getTimeslotDisciplina(disciplinasProfessorId[j]).getHorarios());
				}
			}
			
			varHorariosProfessor[i] = horarios.toArray(new IntVar[horarios.size()]);
			model.allDifferent(varHorariosProfessor[i], "NEQS").post();
		}
	}
	
	/**
	 * <b>Restrição Forte:</b> <br>
	 *  
	 * As ofertas de aula para disciplinas de mesmo período (turma) não devem estar sobrepostas.
	 * A partir do conjunto <i>D</i> de <b>{@code m}</b> de disciplinas selecionadas e outro conjunto <i>H<sub>D</sub></i> de 
	 * horários correspondentes, é realizada a associação e agrupamento de todos os horários por turma e, em seguida, aplica-se a 
	 * restrição {@link Model #allDifferent(IntVar[], String)}, a qual mantém apenas uma oferta de aula por horário.
	 */

	private void manterDisciplinasComHorarioUnicoConstraint() {
		
		varHorariosPeriodo = new IntVar[periodos.length][];
		
		for (int i = 0, index = 0; i < periodos.length; i++) {
			
			List<IntVar> horarios = new ArrayList<IntVar>();
			
			for (int j = index; j < index + periodos[i].length; j++) {
				horarios.addAll(timeslots.get(j).getHorarios());
			}
			
			varHorariosPeriodo[i] = horarios.toArray(new IntVar[horarios.size()]);
			model.allDifferent(varHorariosPeriodo[i], "NEQS").post();
			index += periodos[i].length;
		}		
	}
	
	/**
	 * <b>Restrição Forte:</b> <br>
	 *  
	 * Um professor não pode ser selecionado para ministrar duas aulas de disciplinas diferentes
	 * no mesmo horário. Dado um conjunto de <b>{@code n}</b> horários de um professor, é aplicada a restrição 
	 * {@link Model #allDifferent(IntVar[], String)}, a qual mantém um professor com apenas uma ocorrência de horário.
	 */

	private void manterHorariosOrdenadosConstraint() {
		
		for (int i = 0; i < varHorariosDisciplina.length; i++) {
		
			for (int j = 0; j < varHorariosDisciplina[i].length; j++) {
				
				IntVar d1 = varHorariosDisciplina[i][j];
				IntVar d2 = null;
				
				if ((j+1) < varHorariosDisciplina[i].length) {
					
					d2 = varHorariosDisciplina[i][j+1];
					model.arithm(d1, "<", d2).post();
				}
			}
		}
	}
	
	/**
	 * <b>Restrição Forte (Pedagógica):</b> <br>
	 * 
	 * Restrição complementar à {@link TimetableHandler #manterHorariosConsecutivosConstraint()}.
	 * Deve haver um intervalo mínimo de dias entre as ofertas consecutivas de aula de uma disciplina. Para o domínio apresentado,
	 * o intevalo mínimo é de 1 dia. Para satisfazê-la, dado um conjunto de <b>{@code n}</b> horários de uma disciplina <i>D<sub>i</sub></i>, 
	 * aplica-se a restrição {@link Model #arithm(IntVar, String, IntVar, String, int)}, a qual mantém entre os
	 * horários <i>H<sub>i1</sub></i> e <i>H<sub>i2</sub></i>, a diferença de valor resultante em, no mínimo, um dia .
	 */
	
	private void manterHorariosAlternadosConstraint() {
		
		for (int i = 0; i < timeslots.size(); i++) {
			
			Timeslot timeslot = timeslots.get(i);
			
			int aula = aulas[i];
			
			if (aula == 6) {
				
				IntVar horario1 = timeslot.getHorarios().get(0);
				IntVar horario2 = timeslot.getHorarios().get(1);
				IntVar horario3 = timeslot.getHorarios().get(2);
				IntVar horario4 = timeslot.getHorarios().get(3);
				IntVar horario5 = timeslot.getHorarios().get(4);
				IntVar horario6 = timeslot.getHorarios().get(5);
				
				IntVar local1 = timeslot.getLocais().get(0);
				IntVar local2 = timeslot.getLocais().get(1);
				IntVar local3 = timeslot.getLocais().get(2);
				IntVar local4 = timeslot.getLocais().get(3);
				IntVar local5 = timeslot.getLocais().get(4);
				IntVar local6 = timeslot.getLocais().get(5);
				
				model.arithm(local1, "=", local2).post();
				model.arithm(local2, "=", local3).post();
				model.arithm(local3, "=", local4).post();
				model.arithm(local4, "=", local5).post();
				model.arithm(local5, "=", local6).post();
				
				model.or(
						model.and(
								model.arithm(horario3, "-", horario1, "=", 2),
								model.arithm(horario4, "-", horario3, "=", 16),
								model.arithm(horario6, "-", horario4, "=", 2)),
						
						model.and(
								model.arithm(horario3, "-", horario2, "=", 17),
								model.arithm(horario5, "-", horario4, "=", 17)
								)
						).post();
				
			
			} else if (aula == 5) {
				
				IntVar horario1 = timeslot.getHorarios().get(0);
				IntVar horario3 = timeslot.getHorarios().get(2);
				IntVar horario4 = timeslot.getHorarios().get(3);
				
				IntVar local1 = timeslot.getLocais().get(0);
				IntVar local2 = timeslot.getLocais().get(1);
				IntVar local3 = timeslot.getLocais().get(2);
				IntVar local4 = timeslot.getLocais().get(3);
				IntVar local5 = timeslot.getLocais().get(4);
				
				model.arithm(horario3, "-", horario1, "=", 2).post();
				model.arithm(horario4, "-", horario3, "=", 16).post();
				
				model.arithm(local1, "=", local2).post();
				model.arithm(local2, "=", local3).post();
				model.arithm(local3, "=", local4).post();
				model.arithm(local4, "=", local5).post();
								
			} else if (aula == 4) {
				
				IntVar horario1 = timeslot.getHorarios().get(0);
				IntVar horario2 = timeslot.getHorarios().get(1);
				IntVar horario3 = timeslot.getHorarios().get(2);
				IntVar horario4 = timeslot.getHorarios().get(3);
				
				IntVar local1 = timeslot.getLocais().get(0);
				IntVar local2 = timeslot.getLocais().get(1);
				IntVar local3 = timeslot.getLocais().get(2);
				IntVar local4 = timeslot.getLocais().get(3);
				
				model.arithm(horario2, "-", horario1, "=", 1).post();
				model.arithm(horario3, "-", horario2, "=", 17).post();
				model.arithm(horario4, "-", horario3, "=", 1).post();
				
				model.arithm(local1, "=", local2).post();
				model.arithm(local2, "=", local3).post();
				model.arithm(local3, "=", local4).post();
				
			} else if (aula == 3) {
				
				IntVar horario1 = timeslot.getHorarios().get(0);
				IntVar horario2 = timeslot.getHorarios().get(1);
				IntVar horario3 = timeslot.getHorarios().get(2);
				
				IntVar local1 = timeslot.getLocais().get(0);
				IntVar local2 = timeslot.getLocais().get(1);
				IntVar local3 = timeslot.getLocais().get(2);
				
				model.arithm(horario2, "-", horario1, "=", 1).post();
				model.arithm(horario3, "-", horario2, "=", 1).post();
				
				model.arithm(local1, "=", local2).post();
				model.arithm(local2, "=", local3).post();
				
				model.notMember(horario3, new int [] {0, 9, 18, 27, 36}).post();
				model.notMember(horario1, new int [] {8, 17, 24, 32, 40}).post();
			}
		}
	}
	
	/**
	 * <b>Restrição Forte:</b> <br>
	 * 
	 * Deve haver uma quantidade mínima de ofertas de aula consecutivas para uma disciplina. Para o domínio apresentado, 
	 * o mínimo de aulas é {@code 2}. A partir do conjunto <i>H<sub>i</sub></i> de horários de uma disciplina <i>D</i>, é aplicada
	 * a restrição {@link Model #arithm(IntVar, String, IntVar, String, int)}, a qual mantém entre os
	 * horários <i>H<sub>i1</sub></i> e <i>H<sub>i2</sub></i>, a diferença de valor em 1, ou seja, consecutivos.
	 */

	private void manterHorariosConsecutivosConstraint() {
		
		for (int i = 0; i < timeslots.size(); i++) {
			
			Timeslot timeslot = timeslots.get(i);
			
			for (int j = 0; j < timeslot.getHorarios().size(); j++) {
				
				IntVar horario2 = null;
				
				if ((j+1) < timeslot.getHorarios().size()) {
					
					horario2 = timeslot.getHorarios().get(++j);
					
					if (j % 2 != 0) {
						model.notMember(horario2, new int[]{0, 9, 18, 27, 36}).post();
					}
				}
			}
		}
	}
	
	/**
	 * <b>Restrição Forte:</b> <br>
	 * 
	 * Deve haver um local e um horário para cada oferta de aula gerada, uma vez que deve ser atendida a restrição de que o mesmo 
	 * local não pode ser alocado no mesmo horário para mais de uma oferta.
	 * Para assegurar que esta restrição seja atendida define-se para cada <i>timeslot</i> gerado na modelagem inicial:
	 * 
	 * <ul>
	 * 	<li>Uma variável {@code z}<sub>i</sub>, a qual varia entre {@code 0} e {@code 100000};</li>
	 * 	<li>Uma <i>view</i>, na forma {@code z}<sub>i</sub>{@code = x}<sub>i</sub> &times {@code 1000 + y}<sub>i</sub>;</li>
	 * </ul>
	 * 
	 * Ao final, para cada variável {@code z}<sub>i</sub> criada, é aplicada a restrição {@link Model #allDifferent(IntVar[], String)}, 
	 * a qual é responsável por assegurar que os valores de uma coleção de variáveis sejam diferentes entre si.
	 */
	
	
	private void manterLocaisComHorarioUnicoConstraint() {
		
		List<IntVar> list = new ArrayList<IntVar>();
		
		for (int i = 0; i < timeslots.size(); i++) {
			
			for (int j = 0; j < timeslots.get(i).getHorarios().size(); j++) {
				
				IntVar horario = timeslots.get(i).getHorarios().get(j);
				IntVar local = timeslots.get(i).getLocais().get(j);
				
				IntVar z = model.intVar("z", 0, 100000);
				
				model.sum(new IntVar[]{model.intScaleView(horario, 1000), local}, "=", z).post();
				
				list.add(z);
			}
		}
		
		model.allDifferent(list.toArray(new IntVar[list.size()]), "NEQS").post();
	}
	
	private List<DetalheDisciplina> allObrigatoriasByPeriodo(Periodo periodo) {
		
		List<DetalheDisciplina> list = new ArrayList<DetalheDisciplina>();
		
		for (DetalheDisciplina detalhe : periodo.getDetalhes()) {
			
			if (detalhe.isObrigatoria()) {
				list.add(detalhe);
			}
		}
		
		return list;
	}
	
	public List<Aula> recuperaAulas() {
		
		List<Aula> aulas = new ArrayList<Aula>();
		
		for (Timeslot timeslot : timeslots) {
			
			for (int i = 0; i < timeslot.getHorarios().size(); i++) {
				
				Aula aula = new Aula();
				aula.setDisciplina(getDisciplina(timeslot.getDisciplina().getValue()));
				aula.setProfessor(getProfessor(timeslot.getProfessor().getValue()));
				aula.setLocal(getLocal(timeslot.getLocais().get(i).getValue()));
				aula.setHorario(getHorario(timeslot.getHorarios().get(i).getValue()));
				aula.setTimetable(getTimetable());
				
				aulas.add(aula);
			}
		}
		
		return aulas;
	}
	
	private List<DetalheDisciplina> allByMatrizCurricular(MatrizCurricular matriz) {
		
		List<DetalheDisciplina> listaDetalhes = new ArrayList<DetalheDisciplina>();
		
		for (Disciplina disciplina : listDisciplina) {
			
			for (DetalheDisciplina detalhe : disciplina.getDetalhes()) {
				
				if (detalhe.getPeriodo().getMatrizCurricular().getId() == matriz.getId()) {
					listaDetalhes.add(detalhe);
				}
			}
		}
		
		return listaDetalhes;
	}
	
	private Timeslot getTimeslotDisciplina(int disciplina) {
		
		for (Timeslot timeslot : timeslots) {
			
			if (timeslot.getDisciplina().getValue() == disciplina)
				return timeslot;
		}
		
		return null;
	}
	
	private int [] extrairCreditos(List<DetalheDisciplina> detalhes) {
		
		List<Integer> lista = new ArrayList<Integer>();
		
		for (DetalheDisciplina detalhe : detalhes) {
			lista.add(detalhe.getCreditos());
		}
		
		return Arrays.stream(lista.toArray(new Integer[lista.size()])).mapToInt(Integer::intValue).toArray();
	}
	
	private int [] extrairIds(List<? extends Entidade> list) {
		
		List<Integer> listaIds = new ArrayList<Integer>();
		
		for (Entidade obj : list) {
			listaIds.add(obj.getId());
		}
		
		return Arrays.stream(listaIds.toArray(new Integer[listaIds.size()])).mapToInt(Integer::intValue).toArray();
	}
	
	public Model getModel() {
		return model;
	}
	
	public Solver getSolver() {
		return solver;
	}

	public void setTimetable(Timetable timetable) {
		this.timetable = timetable;
	}
	
	public Timetable getTimetable() {
		return timetable;
	}
}