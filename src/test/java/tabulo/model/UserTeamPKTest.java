package tabulo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTeamPKTest {

	@Test
	public void test_getter_setter() {
		UserTeamPK form = new UserTeamPK();
		form.setUserId(123);
		form.setTeamId(234);
		assertEquals(Integer.valueOf(123), form.getUserId());
		assertEquals(Integer.valueOf(234), form.getTeamId());
	}

}