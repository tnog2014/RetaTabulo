package tabulo.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.web.util.NestedServletException;

import tabulo.ApplicationException;

public class TestUtils {

	public static void assertApplicationExceptionMessage(
			Exception e,
			String message) {
		assertEquals(NestedServletException.class, e.getClass());
		Throwable ee = e.getCause();
		assertEquals(ApplicationException.class, ee.getClass());
		assertEquals(message, ee.getMessage());
	}

	public static <T> List<ConstraintViolation<T>> convertToListSortedByMessage(
			Set<ConstraintViolation<T>> violations) {
		List<ConstraintViolation<T>> list = new ArrayList<ConstraintViolation<T>>(
				violations);
		Comparator<ConstraintViolation<T>> comp = new Comparator<ConstraintViolation<T>>() {

			@Override
			public int compare(
					ConstraintViolation<T> o1,
					ConstraintViolation<T> o2) {
				return o1.getMessage().compareTo(o2.getMessage());
			}

		};
		Collections.sort(list, comp);
		return list;
	}
}
