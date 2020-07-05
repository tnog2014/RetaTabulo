package tabulo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

public class CustomUserTest {

	@Test
	public void test_コンストラクタ_userIdなし() {
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		GrantedAuthority ga = new GrantedAuthority() {

			@Override
			public String getAuthority() {
				return "ADMIN";
			}
		};
		list.add(ga);
		CustomUser user = new CustomUser("username", "password", list);
		user.setEnabled(true);
		user.setUserId(123);

		assertEquals("username", user.getUsername());
		assertEquals("password", user.getPassword());
		assertEquals(Integer.valueOf(123), user.getUserId());
		Collection<GrantedAuthority> actual = user.getAuthorities();
		assertEquals(true, user.isEnabled());
		assertEquals(1, user.getAuthorities().size());
		assertEquals("ADMIN", user.getAuthorities().iterator().next().getAuthority());
	}
	@Test
	public void test_紺数トラクタ_userIdあり() {
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		GrantedAuthority ga = new GrantedAuthority() {

			@Override
			public String getAuthority() {
				return "ADMIN";
			}
		};
		list.add(ga);
		CustomUser user = new CustomUser("username", "password", list, 123);
		user.setEnabled(true);

		assertEquals("username", user.getUsername());
		assertEquals("password", user.getPassword());
		assertEquals(Integer.valueOf(123), user.getUserId());
		Collection<GrantedAuthority> actual = user.getAuthorities();
		assertEquals(true, user.isEnabled());
		assertEquals(1, user.getAuthorities().size());
		assertEquals("ADMIN", user.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	public void test_toString() {
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		GrantedAuthority ga = new GrantedAuthority() {

			@Override
			public String getAuthority() {
				return "ADMIN";
			}
		};
		list.add(ga);
		CustomUser user = new CustomUser("username", "password", list);
		user.setEnabled(true);
		user.setUserId(123);

		assertEquals("username/123/true", user.toString());

	}

}
