package tabulo.controller;

import static org.junit.Assert.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import tabulo.ApplicationException;
import tabulo.CustomUser;
import tabulo.JsonConverter;
import tabulo.MessageUtil;
import tabulo.PropertyAccessor;
import tabulo.constant.ErrorCode;
import tabulo.model.BoardRegistrationMessage;
import tabulo.model.BoardUser;
import tabulo.model.WebSocketMessage;
import tabulo.service.BoardUserService;
import tabulo.service.DescriptionService;
import test.TestUtilities;

// TODO: ユーザーIDを直接指定している個所をgetUserIdOfに変更する。
@RunWith(SpringRunner.class)
@SpringBootTest
public class BoardControllerTest2 {

	@Mocked
	private DescriptionService ds;

	@Autowired
	private MessageUtil messageUtil;

	@Mocked({
			"checkBoardAvailability(Principal,Integer)",
			"processCreate(WebSocketMessage,CustomUser)",
			"processUpdate(WebSocketMessage,CustomUser)",
			"processMove(WebSocketMessage)",
			"processRemove(WebSocketMessage)",
			"processResize(WebSocketMessage)",
			"checkBoardAvailability(Principal,Integer)"
	})
	private BoardController target2;

	@Mocked
	SimpMessagingTemplate simpMessagingTemplate;

	@Mocked
	Principal principal;

	@Test
	public void test_processMessage_create() throws Exception {
		execTest("create");
	}

	@Test
	public void test_processMessage_update() throws Exception {
		execTest("update");
	}

	@Test
	public void test_processMessage_move() throws Exception {
		execTest("move");
	}

	@Test
	public void test_processMessage_remove() throws Exception {
		execTest("remove");
	}

	@Test
	public void test_processMessage_resize() throws Exception {
		execTest("resize");
	}

	@Test
	public void test_processMessage_others() throws Exception {
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("12");
		wsm.setBoardId(123);
		wsm.setType("other");
		//		StringBuffer sb = new StringBuffer();

		try {
			target2.processMessage(principal, wsm);
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_UNEXPECTED_MESSAGE_TYPE, "other"),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}
	}

	public void execTest(String type) throws Exception {
		WebSocketMessage wsm = new WebSocketMessage();
		wsm.setId("12");
		wsm.setBoardId(123);
		wsm.setType(type);
		StringBuffer sb = new StringBuffer();

		new MockUp<BoardController>() {
			@Mock
			private void processCreate(final WebSocketMessage message, CustomUser user) {
				sb.append("create");
			}

			@Mock
			private void processMove(final WebSocketMessage message) {
				sb.append("move");
			}

			@Mock
			private void processResize(final WebSocketMessage message) {
				sb.append("resize");
			}

			@Mock
			private void processRemove(final WebSocketMessage message) {
				sb.append("remove");
			}

			@Mock
			private void processUpdate(final WebSocketMessage message, CustomUser user) {
				sb.append("update");
			}

		};
		new Expectations() {
			{
				simpMessagingTemplate.convertAndSend("/topic/board/123", wsm);
			}
		};
		TestUtilities.setField(target2, "simpMessagingTemplate", simpMessagingTemplate);
		target2.processMessage(principal, wsm);
		System.out.println(sb.toString());
		assertEquals(type, sb.toString());
	}

	@Mocked
	SimpMessageHeaderAccessor simpMessageHeaderAccessor;

	@Mocked
	PropertyAccessor propertyAccessor;

	@Mocked
	BoardUserService boardUserService;

	@Test
	public void test_userRegistration_open_insert() throws Exception {

		TestUtilities.setField(target2, "jsonConverter", new JsonConverter<WebSocketMessage>());

		new MockUp<BoardUserService>() {

			@Mock
			public void deleteNotUpdatedRows(Date d) {
				System.out.println("deleteNotUpdatedRows");
			}

			@Mock
			public Optional<BoardUser> findByUserIdSubId(Integer userId, String hash) {
				System.out.println("userId:" + userId);
				System.out.println("hash:" + hash);
				BoardUser bu = new BoardUser();
				//return Optional.of(bu);
				return Optional.empty();
			}

			@Mock
			public List<BoardUser> findByBoardId(Integer boardId) {
				System.out.println("★findByBoardId:" + boardId);
				BoardUser bu1 = new BoardUser();
				bu1.setUserName("name2");

				BoardUser bu2 = new BoardUser();
				bu2.setUserName("name1");

				List<BoardUser> ret = new ArrayList<BoardUser>();
				ret.add(bu1);
				ret.add(bu2);
				return ret;
			}

		};
		new NonStrictExpectations() {
			{
				simpMessageHeaderAccessor.getSessionId();
				result = "SESSION_ID";
				propertyAccessor.getThresholdPeriodInSeconds();
				result = 180;
			}
		};
		TestUtilities.setField(target2, "simpMessagingTemplate", simpMessagingTemplate);
		TestUtilities.setField(target2, "boardUserService", boardUserService);
		Deencapsulation.setField(target2, "propertyAccessor", propertyAccessor);

		BoardRegistrationMessage brm = new BoardRegistrationMessage();
		brm.setBoardId(123);
		brm.setUserId(234);
		brm.setType("open");
		target2.userRegistration(simpMessageHeaderAccessor, brm, principal);
	}

	@Test
	public void test_userRegistration_no_boardId() throws Exception {

		new NonStrictExpectations() {
			{
				simpMessageHeaderAccessor.getSessionId();
				result = "SESSION_ID";
			}
		};
		BoardRegistrationMessage brm = new BoardRegistrationMessage();
		brm.setUserId(234);
		brm.setType("open");
		try {
			target2.userRegistration(simpMessageHeaderAccessor, brm, principal);
		} catch (ApplicationException e) {
			assertEquals(ApplicationException.class, e.getClass());
			assertEquals(messageUtil.decode(ErrorCode.ERR_NO_BOARD_ID_FOR_REGISTRATION),
					messageUtil.decode(e.getCode(), e.getParameters()));
		}
	}

	@Test
	public void test_userRegistration_open_update() throws Exception {

		TestUtilities.setField(target2, "jsonConverter", new JsonConverter<WebSocketMessage>());

		new MockUp<BoardUserService>() {

			@Mock
			public void deleteNotUpdatedRows(Date d) {
				System.out.println("deleteNotUpdatedRows");
			}

			@Mock
			public Optional<BoardUser> findByUserIdSubId(Integer userId, String hash) {
				System.out.println("userId:" + userId);
				System.out.println("hash:" + hash);
				BoardUser bu = new BoardUser();
				return Optional.of(bu);
			}

		};
		new NonStrictExpectations() {
			{
				simpMessageHeaderAccessor.getSessionId();
				result = "SESSION_ID";
				propertyAccessor.getThresholdPeriodInSeconds();
				result = 180;
			}
		};
		TestUtilities.setField(target2, "simpMessagingTemplate", simpMessagingTemplate);
		TestUtilities.setField(target2, "boardUserService", boardUserService);
		Deencapsulation.setField(target2, "propertyAccessor", propertyAccessor);

		BoardRegistrationMessage brm = new BoardRegistrationMessage();
		brm.setBoardId(123);
		brm.setUserId(234);
		brm.setType("open");
		target2.userRegistration(simpMessageHeaderAccessor, brm, principal);
	}

	@Test
	public void test_userRegistration_close() throws Exception {

		TestUtilities.setField(target2, "jsonConverter", new JsonConverter<WebSocketMessage>());

		new MockUp<BoardUserService>() {

			@Mock
			public void deleteNotUpdatedRows(Date d) {
				System.out.println("deleteNotUpdatedRows");
			}

			@Mock
			public Optional<BoardUser> findByUserIdSubId(Integer userId, String hash) {
				System.out.println("userId:" + userId);
				System.out.println("hash:" + hash);
				BoardUser bu = new BoardUser();
				return Optional.of(bu);
			}

		};
		new NonStrictExpectations() {
			{
				simpMessageHeaderAccessor.getSessionId();
				result = "SESSION_ID";
				propertyAccessor.getThresholdPeriodInSeconds();
				result = 180;
			}
		};
		TestUtilities.setField(target2, "simpMessagingTemplate", simpMessagingTemplate);
		TestUtilities.setField(target2, "boardUserService", boardUserService);
		Deencapsulation.setField(target2, "propertyAccessor", propertyAccessor);

		BoardRegistrationMessage brm = new BoardRegistrationMessage();
		brm.setBoardId(123);
		brm.setUserId(234);
		brm.setType("close");
		target2.userRegistration(simpMessageHeaderAccessor, brm, principal);
	}

}