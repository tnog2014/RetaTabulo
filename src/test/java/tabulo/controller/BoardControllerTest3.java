package tabulo.controller;

import static org.junit.Assert.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import tabulo.ApplicationException;
import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BoardControllerTest3 {

	@Mocked
	AuthorizationUtil authUtil;

	BoardController target2 = new BoardController();

	@Autowired
	private MessageUtil messageUtil;// = new MessageUtil();

	@Test
	public void test_checkBoardAvailability_true() throws Exception {

		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		CustomUser cu = new CustomUser("username", "password", list);
		Principal principal = new UsernamePasswordAuthenticationToken(cu, null, list);

		new NonStrictExpectations() {
			{
				authUtil.isBoardAvailable(cu, 123, false);
				result = true;
			}
		};
		Deencapsulation.setField(target2, "authUtil", authUtil);
		target2.checkBoardAvailability(principal, 123);
	}

	@Test
	public void test_checkBoardAvailability_unexpected_principal() throws Exception {

		Principal principal = new Principal() {

			@Override
			public String getName() {
				return "dummy";
			}

		};
		Deencapsulation.setField(target2,  "messageUtil", messageUtil);
		try {
			target2.checkBoardAvailability(principal, 123);
			fail("Expected exception is not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(ErrorCode.ERR_UNEXPECTED_PRINCIPAL, e.getCode());
		}
	}

	@Test
	public void test_checkBoardAvailability_unexpected_user() throws Exception {

		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		String cu = "dummy";
		Principal principal = new UsernamePasswordAuthenticationToken(cu, null, list);

		Deencapsulation.setField(target2,  "messageUtil", messageUtil);
		try {
			target2.checkBoardAvailability(principal, 123);
			fail("Expected exception is not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_UNEXPECTED_USER_OBJ, String.class),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}
	}

	@Test
	public void test_checkBoardAvailability_no_boardId() throws Exception {

		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		CustomUser cu = new CustomUser("username", "password", list);
		Principal principal = new UsernamePasswordAuthenticationToken(cu, null, list);

		new NonStrictExpectations() {
			{
				authUtil.isBoardAvailable(cu, 123, false);
				result = true;
			}
		};
		Deencapsulation.setField(target2,  "messageUtil", messageUtil);
		Deencapsulation.setField(target2, "authUtil", authUtil);
		try {
			target2.checkBoardAvailability(principal, null);
			fail("Expected exception is not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_BOARD_ID_WAS_NULL),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}
	}

	@Test
	public void test_checkBoardAvailability_not_authenticated() throws Exception {

		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		CustomUser cu = new CustomUser("username", "password", list);
		Principal principal = new UsernamePasswordAuthenticationToken(cu, null, list);

		new NonStrictExpectations() {
			{
				authUtil.isBoardAvailable(cu, 123, false);
				result = false;
			}
		};
		Deencapsulation.setField(target2,  "messageUtil", messageUtil);
		Deencapsulation.setField(target2, "authUtil", authUtil);
		try {
			target2.checkBoardAvailability(principal, 123);
			fail("Expected exception is not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_CANNOT_ACCESS_BOARD, 123),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}
	}
}