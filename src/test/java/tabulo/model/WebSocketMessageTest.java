package tabulo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class WebSocketMessageTest {

	@Test
	public void test_getter_setter() {
		WebSocketMessage form = new WebSocketMessage();
		form.setData("data");
		form.setId("id");
		form.setType("type");
		form.setBoardId(123);
		assertEquals(123, form.getBoardId());
		assertEquals("data", form.getData());
		assertEquals("type", form.getType());
		assertEquals("id", form.getId());
	}

	@Test
	public void test_toString() {
		WebSocketMessage form = new WebSocketMessage();
		form.setData("data");
		form.setId("id");
		form.setType("type");
		form.setBoardId(123);
		assertEquals("boardId:[123],id:[id],type:[type],data:[data]", form.toString());
	}

}