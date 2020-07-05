package tabulo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class DescriptionTest {

	@Test
	public void test_getter_setter() {
		Description form = new Description();
		form.setBoardId(234);
		form.setId(123);
		form.setX(1.23);
		form.setY(2.45);
		form.setRaw("raw");
		form.setHtml("html");
		assertEquals(Integer.valueOf(234), form.getBoardId());
		assertEquals(Integer.valueOf(123), form.getId());
		assertEquals(Double.valueOf(1.23), form.getX());
		assertEquals(Double.valueOf(2.45), form.getY());
		assertEquals("raw", form.getRaw());
		assertEquals("html", form.getHtml());
	}

}