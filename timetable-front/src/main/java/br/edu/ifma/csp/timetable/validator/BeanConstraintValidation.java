package br.edu.ifma.csp.timetable.validator;

import br.edu.ifma.csp.timetable.model.Entidade;
import br.edu.ifma.csp.timetable.repository.Repository;

public interface BeanConstraintValidation {
	
	public void setRepository(Repository<Entidade> repository);
	
	public void setEntidade(Entidade entidade);
}
