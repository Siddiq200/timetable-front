package br.edu.ifma.csp.timetable.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2016-09-25T11:18:24.508-0300")
@StaticMetamodel(Curso.class)
public class Curso_ extends Entidade_ {
	public static volatile SingularAttribute<Curso, Integer> id;
	public static volatile SingularAttribute<Curso, String> codigo;
	public static volatile SingularAttribute<Curso, String> nome;
	public static volatile SingularAttribute<Curso, String> descricao;
	public static volatile SingularAttribute<Curso, Departamento> departamento;
	public static volatile SetAttribute<Curso, MatrizCurricular> matrizes;
}
