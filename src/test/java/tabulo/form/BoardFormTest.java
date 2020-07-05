package tabulo.form;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoardFormTest {

	@Test
	public void test_getter_setter() {
		BoardForm form = new BoardForm();
		form.setId(123);
		form.setName("name");
		form.setDescription("description");
		form.setTeamId(234);
		form.setTeamName("teamName");
		assertEquals(Integer.valueOf(123), form.getId());
		assertEquals("name", form.getName());
		assertEquals("description", form.getDescription());
		assertEquals(Integer.valueOf(234), form.getTeamId());
		assertEquals("teamName", form.getTeamName());
	}

}