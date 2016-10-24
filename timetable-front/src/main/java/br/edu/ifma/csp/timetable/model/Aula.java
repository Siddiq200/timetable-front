package br.edu.ifma.csp.timetable.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="AULA")
public class Aula extends Entidade {

	private static final long serialVersionUID = 7872936954532471767L;
	
	@Id
	@Column(name="ID_AULA")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ID_PROFESSOR")
	private Professor professor;
	
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ID_DISCIPLINA")
	private Disciplina disciplina;
	
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ID_LOCAL")
	private Local local;
	
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ID_HORARIO")
	private Horario horario;
	
	@NotNull(message="Timetable obrigatória.")
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ID_TIMETABLE")
	private Timetable timetable;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Professor getProfessor() {
		return professor;
	}
	
	public void setProfessor(Professor professor) {
		this.professor = professor;
	}
	
	public Disciplina getDisciplina() {
		return disciplina;
	}
	
	public void setDisciplina(Disciplina disciplina) {
		this.disciplina = disciplina;
	}
	
	public Local getLocal() {
		return local;
	}
	
	public void setLocal(Local local) {
		this.local = local;
	}
	
	public Horario getHorario() {
		return horario;
	}
	
	public void setHorario(Horario horario) {
		this.horario = horario;
	}
	
	public Timetable getTimetable() {
		return timetable;
	}
	
	public void setTimetable(Timetable timetable) {
		this.timetable = timetable;
	}
}