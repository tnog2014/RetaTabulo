package tabulo.form;

import static org.junit.Assert.*;

import org.junit.Test;

public class TeamFormTest {

	@Test
	public void test_getter_setter() {
		TeamForm form = new TeamForm();
		form.setId(123);
		form.setName("name");
		form.setDescription("description");
		assertEquals(Integer.valueOf(123), form.getId());
		assertEquals("name", form.getName());
		assertEquals("description", form.getDescription());
	}

}