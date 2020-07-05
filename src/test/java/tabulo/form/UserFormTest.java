package tabulo.form;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserFormTest {

	@Test
	public void test_ユーザー名() {
		UserForm form = new UserForm();
		form.setUserName("userName");

		form.setChangePassword(true);

		form.setOldPassword("oldPassword");
		form.setPassword("password");
		form.setCnfPassword("cnfPassword");

		form.setNickname("nickname");

		form.setId(123);
		form.setLevel("level");
		form.setValid("valid");
		form.setFrom("from");

		form.setCurrentAdminTeams("currentAdminTeams");
		form.setCurrentAssignedTeams("currentAssignedTeams");

		form.setAssignedTeams("assignedTeams");
		form.setUnassignedTeams("unassignedTeams");
		form.setAssignedAdminTeams("assignedAdminTeams");
		form.setUnassignedAdminTeams("unassignedAdminTeams");

		assertEquals("userName", form.getUserName());
		assertEquals(true, form.isChangePassword());
		assertEquals("oldPassword", form.getOldPassword());
		assertEquals("password", form.getPassword());
		assertEquals("cnfPassword", form.getCnfPassword());

		assertEquals("nickname", form.getNickname());
		assertEquals(Integer.valueOf(123), form.getId());

		assertEquals("level", form.getLevel());
		assertEquals("valid", form.getValid());
		assertEquals("from", form.getFrom());

		assertEquals("currentAdminTeams", form.getCurrentAdminTeams());
		assertEquals("currentAssignedTeams", form.getCurrentAssignedTeams());

		assertEquals("assignedTeams", form.getAssignedTeams());
		assertEquals("unassignedTeams", form.getUnassignedTeams());
		assertEquals("assignedAdminTeams", form.getAssignedAdminTeams());
		assertEquals("unassignedAdminTeams", form.getUnassignedAdminTeams());
	}

	@Test
	public void test_パスワード_変更なし() {
		UserForm form = new UserForm();
		form.setChangePassword(false);
		form.setPassword("password");
		form.setCnfPassword("password");
		assertTrue(form.isCnfPasswordMatch());
	}

	@Test
	public void test_パスワード一致() {
		UserForm form = new UserForm();
		form.setChangePassword(true);
		form.setPassword("password");
		form.setCnfPassword("password");
		assertTrue(form.isCnfPasswordMatch());
	}

	@Test
	public void test_パスワード不一致() {
		UserForm form = new UserForm();
		form.setChangePassword(true);
		form.setPassword("password");
		form.setCnfPassword("passwordNG");
		assertFalse(form.isCnfPasswordMatch());
	}
	@Test
	public void test_パスワードなし_不一致() {
		UserForm form = new UserForm();
		form.setChangePassword(true);
		form.setPassword(null);
		form.setCnfPassword("password");
		assertFalse(form.isCnfPasswordMatch());
	}

}