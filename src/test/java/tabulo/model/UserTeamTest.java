package tabulo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTeamTest {

	@Test
	public void test_getter_setter() {
		UserTeam form = new UserTeam();
		form.setAuth("auth");
		form.setUserId(123);
		form.setTeamId(234);
		assertEquals(Integer.valueOf(123), form.getUserId());
		assertEquals(Integer.valueOf(234), form.getTeamId());
		assertEquals("auth", form.getAuth());
	}

}