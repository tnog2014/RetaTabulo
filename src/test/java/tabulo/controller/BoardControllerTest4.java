package tabulo.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import tabulo.ApplicationException;
import tabulo.ControllerUtil;
import tabulo.CustomUser;
import tabulo.MessageUtil;
import tabulo.constant.ErrorCode;
import tabulo.model.Description;
import tabulo.model.WebSocketMessage;
import tabulo.service.DescriptionService;
import test.TestUtilities;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class BoardControllerTest4 {

	@Mocked("getLoginUser")
	ControllerUtil moc;

	@Autowired
	private MessageUtil messageUtil;

	@Mocked
	private DescriptionService ds;

	// -----------------------------
	// processResize
	// -----------------------------

	@Test
	public void test_processResize() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("1");
		String data = "{\"width\": 100.0, \"height\": 200.0}";
		wsm.setData(data);
		Object[] args = new Object[]{wsm};

		Description d = new Description();
		Optional<Description> optional = Optional.of(d);
		new NonStrictExpectations() {
			{
				Integer id = Integer.valueOf("1");
				ds.findById(id);
				result = optional;
			}
		};
		TestUtilities.setField(target, "descService", ds);
		TestUtilities.invoke(target, "processResize", args);
		assertEquals(Double.valueOf(100.0), (Double) d.getWidth());
		assertEquals(Double.valueOf(200.0), (Double) d.getHeight());
	}

	@Test
	public void test_processResize_found_is_null() throws Throwable {

		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("1");
		String data = "{\"width\": 100.0, \"height\": 200.0}";
		wsm.setData(data);

		BoardController target = new BoardController();
		Object[] args = new Object[]{wsm};
		Description d = new Description();
		Optional<Description> optional = Optional.empty();
		new NonStrictExpectations() {
			{
				Integer id = Integer.valueOf("1");
				ds.findById(id);
				result = optional;
			}
		};

		TestUtilities.setField(target, "messageUtil", messageUtil);
		TestUtilities.setField(target, "descService", ds);

		TestUtilities.invoke(target, "processResize", args);
		// TODO: Needs to ensure that the error log message was written.
	}

	@Test
	public void test_processResize_id_not_defined() throws Throwable {
		BoardController target = new BoardController();

		WebSocketMessage wsm = new WebSocketMessage();

		Object[] args = new Object[]{wsm};

		try {
			TestUtilities.invoke(target, "processResize", args);
			fail("Expected exception not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());

			String message = messageUtil.decode(ErrorCode.ERR_NO_ID_FOR_MOVING_DESCRPTION);
		}

	}

	@Test
	public void test_processResize_bad_json() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		String data = "BAD JSON";
		wsm.setId("1");
		wsm.setData(data);

		Object[] args = new Object[]{wsm};

		try {
			TestUtilities.invoke(target, "processResize", args);
			fail("Expected exception not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_FAILURE_JSON_TO_MAP, data),
					messageUtil.decode(e.getCode(), e.getParameters()));

		}

	}

	// -----------------------------
	// processMove
	// -----------------------------

	@Test
	public void test_processMove() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("1");
		String data = "{\"x\": 100.0, \"y\": 200.0}";
		wsm.setData(data);
		Object[] args = new Object[]{wsm};

		Description d = new Description();
		Optional<Description> optional = Optional.of(d);
		new NonStrictExpectations() {
			{
				Integer id = Integer.valueOf("1");
				ds.findById(id);
				result = optional;
			}
		};
		TestUtilities.setField(target, "descService", ds);
		TestUtilities.invoke(target, "processMove", args);
		assertEquals(Double.valueOf(100.0), (Double) d.getX());
		assertEquals(Double.valueOf(200.0), (Double) d.getY());
	}

	@Test
	public void test_processMove_found_is_null() throws Throwable {

		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("1");
		String data = "{\"x\": 100.0, \"y\": 200.0}";
		wsm.setData(data);

		BoardController target = new BoardController();
		Object[] args = new Object[]{wsm};
		Description d = new Description();
		Optional<Description> optional = Optional.empty();
		new NonStrictExpectations() {
			{
				Integer id = Integer.valueOf("1");
				ds.findById(id);
				result = optional;
			}
		};
		TestUtilities.setField(target, "messageUtil", messageUtil);
		TestUtilities.setField(target, "descService", ds);

		TestUtilities.invoke(target, "processMove", args);
		// TODO: Needs to ensure that the error log message was written.
	}

	@Test
	public void test_processMove_id_not_defined() throws Throwable {
		BoardController target = new BoardController();

		WebSocketMessage wsm = new WebSocketMessage();

		Object[] args = new Object[]{wsm};

		try {
			TestUtilities.invoke(target, "processMove", args);
			fail("Expected exception not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			String message = messageUtil.decode(ErrorCode.ERR_NO_ID_FOR_MOVING_DESCRPTION);
		}

	}

	@Test
	public void test_processMove_bad_json() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		String data = "BAD JSON";
		wsm.setId("1");
		wsm.setData(data);

		Object[] args = new Object[]{wsm};

		try {
			TestUtilities.invoke(target, "processMove", args);
			fail("Expected exception not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_FAILURE_JSON_TO_MAP, data),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}

	}

	// -----------------------------
	// processUpdate
	// -----------------------------

	@Test
	public void test_processUpdate() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("1");
		String data = "{\"raw\": \"RAW2\", \"html\": \"HTML2\"}";
		wsm.setData(data);

		CustomUser user = createCustomUser();
		Object[] args = new Object[]{wsm, user};

		Description d = new Description();
		Optional<Description> optional = Optional.of(d);
		new NonStrictExpectations() {
			{
				Integer id = Integer.valueOf("1");
				ds.findById(id);
				result = optional;
			}
		};
		TestUtilities.setField(target, "descService", ds);
		TestUtilities.invoke(target, "processUpdate", args);
		assertEquals("RAW2", d.getRaw());
		assertEquals("HTML2", d.getHtml());
	}

	@Test
	public void test_processUpdate_found_is_null() throws Throwable {

		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("1");
		String data = "{\"raw\": \"RAW\", \"html\": \"HTML\"}";
		wsm.setData(data);

		BoardController target = new BoardController();
		CustomUser user = createCustomUser();

		Object[] args = new Object[]{wsm, user};
		Description d = new Description();
		Optional<Description> optional = Optional.empty();
		new NonStrictExpectations() {
			{
				Integer id = Integer.valueOf("1");
				ds.findById(id);
				result = optional;
			}
		};

		TestUtilities.setField(target, "messageUtil", messageUtil);
		TestUtilities.setField(target, "descService", ds);

		TestUtilities.invoke(target, "processUpdate", args);
		// TODO: Needs to ensure that the error log message was written.
	}

	CustomUser createCustomUser() {
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		CustomUser user = new CustomUser("username", "password", grantList);
		return user;
	}

	@Test
	public void test_processUpdate_id_not_defined() throws Throwable {
		BoardController target = new BoardController();

		WebSocketMessage wsm = new WebSocketMessage();
		CustomUser user = createCustomUser();

		Object[] args = new Object[]{wsm, user};

		try {
			TestUtilities.invoke(target, "processUpdate", args);
			fail("Expected exception not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			String message = messageUtil.decode(ErrorCode.ERR_NO_ID_FOR_UPDATING_DESCRPTION);
			//			assertEquals(, e.getMessage());
		}

	}

	@Test
	public void test_processUpdate_bad_json() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		String data = "BAD JSON";
		wsm.setId("1");
		wsm.setData(data);

		CustomUser user = createCustomUser();
		Object[] args = new Object[]{wsm, user};

		try {
			TestUtilities.invoke(target, "processUpdate", args);
			fail("Expected exception not thrown.");
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_FAILURE_JSON_TO_MAP, data),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}

	}

	// -----------------------------
	// processCreate
	// -----------------------------

	@Test
	public void test_processCreate() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setBoardId(234);
		//wsm.setId("1");
		String data = "{"
				+ "\"x\": 10.0, \"y\": 20.0,"
				+ "\"width\": 100.0, \"height\": 200.0,"
				+ "\"raw\": \"RAW\", \"html\": \"HTML\""
				+ "}";
		wsm.setData(data);
		CustomUser user = createCustomUser();

		Object[] args = new Object[]{wsm, user};

		final List<Description> list = new ArrayList<Description>();
		new MockUp<DescriptionService>() {

			@Mock
			public Description save(Description d) {
				list.add(d);
				d.setId(123);
				return d;
			}

		};

		TestUtilities.setField(target, "descService", ds);
		TestUtilities.invoke(target, "processCreate", args);
		Description d = list.get(0);
		assertEquals("123", wsm.getId());
		assertEquals(Integer.valueOf(234), d.getBoardId());
		assertEquals(Double.valueOf(10.0), d.getX());
		assertEquals(Double.valueOf(20.0), d.getY());
		assertEquals(Double.valueOf(100.0), d.getWidth());
		assertEquals(Double.valueOf(200.0), d.getHeight());
		assertEquals("RAW", d.getRaw());
		assertEquals("HTML", d.getHtml());
	}

	// -----------------------------
	// processRemove
	// -----------------------------

	@Test
	public void test_processRemove() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setBoardId(234);
		wsm.setId("123");
		Object[] args = new Object[]{wsm};

		List<Integer> value = new ArrayList<Integer>();
		new MockUp<DescriptionService>() {

			@Mock
			public void deleteById(Integer d) {
				value.add(d);
			}

		};

		TestUtilities.setField(target, "descService", ds);
		TestUtilities.invoke(target, "processRemove", args);

		assertEquals(Integer.valueOf(123), value.get(0));
	}

	@Test
	public void test_processRemove_no_id() throws Throwable {
		BoardController target = new BoardController();
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setBoardId(234);
		Object[] args = new Object[]{wsm};

		TestUtilities.setField(target, "descService", ds);
		try {
			TestUtilities.invoke(target, "processRemove", args);
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_NO_ID_FOR_DELETING_DESCRPTION),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}
	}

}