package tabulo.form;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExtendedTeamFormTest {

	@Test
	public void test_getter_setter() {
		ExtendedTeamForm form = new ExtendedTeamForm();
		form.setId(123);
		form.setName("name");
		form.setDescription("description");
		form.setAuth(true);
		assertEquals(Integer.valueOf(123), form.getId());
		assertEquals("name", form.getName());
		assertEquals("description", form.getDescription());
		assertTrue(form.isAuth());
	}

}