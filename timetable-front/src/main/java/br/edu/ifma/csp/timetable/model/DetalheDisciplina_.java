package br.edu.ifma.csp.timetable.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2017-04-21T10:40:22.819-0300")
@StaticMetamodel(DetalheDisciplina.class)
public class DetalheDisciplina_ extends Entidade_ {
	public static volatile SingularAttribute<DetalheDisciplina, Integer> id;
	public static volatile SingularAttribute<DetalheDisciplina, Integer> creditos;
	public static volatile SingularAttribute<DetalheDisciplina, Integer> cargaHoraria;
	public static volatile SingularAttribute<DetalheDisciplina, Boolean> obrigatoria;
	public static volatile SingularAttribute<DetalheDisciplina, String> grupoEletiva;
	public static volatile SingularAttribute<DetalheDisciplina, Boolean> disciplinaLaboratorio;
	public static volatile SingularAttribute<DetalheDisciplina, Periodo> periodo;
	public static volatile SingularAttribute<DetalheDisciplina, Disciplina> disciplina;
	public static volatile ListAttribute<DetalheDisciplina, Disciplina> preRequisitos;
}
