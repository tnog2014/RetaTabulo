package tabulo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoginUserTest {

	@Test
	public void test_getter_setter() {
		LoginUser form = new LoginUser();
		form.setUserName("userName");

		form.setPassword("password");

		form.setNickname("nickname");

		form.setId(123);
		form.setLevel("level");
		form.setValid("valid");

		assertEquals("userName", form.getUserName());
		assertEquals("password", form.getPassword());

		assertEquals("nickname", form.getNickname());
		assertEquals(Integer.valueOf(123), form.getId());

		assertEquals("level", form.getLevel());
		assertEquals("valid", form.getValid());
	}

	@Test
	public void test_toString() {
		LoginUser form = new LoginUser();
		form.setUserName("userName");
		form.setPassword("password");
		form.setNickname("nickname");
		form.setId(123);
		form.setLevel("level");
		form.setValid("valid");

		assertEquals("123/userName/level", form.toString());
	}

}