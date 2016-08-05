package br.edu.ifma.csp.timetable.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.WrongValuesException;

import br.edu.ifma.csp.timetable.model.Entidade;

public class Validations {
	
	public static void validate(Binder binder, Entidade entidade) {
		
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		
		Set<ConstraintViolation<Entidade>> violations = validator.validate(entidade);
		List<WrongValueException> listaExceções = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		
		for (ConstraintViolation<Entidade> cv : violations) {
			sb.append(cv.getMessage().split("#")[1] + "\n");
		}
		
		if (sb.length() > 0)
			listaExceções.add(new WrongValueException(binder.getView(), sb.toString()));
		
		if (listaExceções.size() != 0) {
			throw new WrongValuesException(listaExceções.toArray(new WrongValueException[0]));
		}
	}
}