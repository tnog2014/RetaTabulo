package tabulo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoardTest {

	@Test
	public void test_getter_setter() {
		Board form = new Board();
		form.setId(123);
		form.setName("name");
		form.setDescription("description");
		Team team = new Team();
		form.setTeam(team);

		assertEquals(Integer.valueOf(123), form.getId());
		assertEquals("name", form.getName());
		assertEquals("description", form.getDescription());
		assertEquals(team, form.getTeam());
	}

	@Test
	public void test_toString() {
		Board form = new Board();
		form.setId(123);
		form.setName("name");
		form.setDescription("description");
		Team team = new Team();
		form.setTeam(team);
		assertEquals("name/123", form.toString());
	}
}