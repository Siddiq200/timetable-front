package br.edu.ifma.csp.timetable.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2016-09-25T13:13:11.297-0300")
@StaticMetamodel(Professor.class)
public class Professor_ extends Entidade_ {
	public static volatile SingularAttribute<Professor, Integer> id;
	public static volatile SingularAttribute<Professor, String> nome;
	public static volatile SingularAttribute<Professor, String> endereco;
	public static volatile SingularAttribute<Professor, Departamento> departamento;
	public static volatile SetAttribute<Professor, Aula> aulas;
	public static volatile SetAttribute<Professor, PreferenciaDisciplinaProfessor> preferencias;
}
