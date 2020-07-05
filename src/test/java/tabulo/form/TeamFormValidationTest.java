package tabulo.form;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import tabulo.constant.ValidationMessageKey;
import tabulo.controller.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TeamFormValidationTest {

	private static final String STRING_100 = "12345678901234567890"
			+ "12345678901234567890"
			+ "12345678901234567890"
			+ "12345678901234567890"
			+ "12345678901234567890";

	private Validator validator;

	private TeamForm targetForm = new TeamForm();

	private BindingResult bindingResult = new BindException(targetForm, "TeamForm");

	@Before
	public void before() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();

		targetForm.setId(1);
		targetForm.setName("123456789012345678901234567890");
		targetForm.setDescription(STRING_100);
	}

	/**
	 * エラーなし
	 */
	@Test
	public void noError() {
		String[][] expectations = {
		};
		assertData(expectations);
	}

	public void assertData(final Object[][] expectations) {
		Set<ConstraintViolation<TeamForm>> violations = validator.validate(targetForm);
//				ValidGroup1.class, ValidGroup2.class, ValidGroup3.class);
		assertEquals(expectations.length, violations.size());
		List<ConstraintViolation<TeamForm>> list = TestUtils.convertToListSortedByMessage(
				violations);
		for (int i = 0; i < list.size(); i++) {
			ConstraintViolation<TeamForm> violation = list.get(i);
			Object[] expected = expectations[i];
			assertEquals("element[" + i + "]", expected[0], violation.getPropertyPath().toString());
			assertEquals("element[" + i + "]", expected[1], violation.getInvalidValue());
			assertEquals("element[" + i + "]", expected[2], violation.getMessage());
		}
	}

	@Test
	public void test_チーム名がnull() {
		targetForm.setName(null);
		String[][] expectations = {
				{"name", null, ValidationMessageKey.VALIDATION_REQUIRED}
		};
		assertData(expectations);
	}

	@Test
	public void test_チーム名が空文字() {
		targetForm.setName("");
		String[][] expectations = {
				{"name", "", ValidationMessageKey.VALIDATION_RANGE},
				{"name", "", ValidationMessageKey.VALIDATION_REQUIRED}
		};
		assertData(expectations);
	}

	@Test
	public void test_チーム名が下限より短い() {
		targetForm.setName("12");
		String[][] expectations = {
				{"name", "12", ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_チーム名が下限() {
		targetForm.setName("123");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_チーム名が上限() {
		targetForm.setName("123456789012345678901234567890");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_チーム名が上限より長い() {
		targetForm.setName("123456789012345678901234567890X");
		String[][] expectations = {
				{"name", "123456789012345678901234567890X",
					ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	// -------------------------
	// 説明
	// -------------------------

	@Test
	public void test_説明がnull() {
		targetForm.setDescription(null);
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_説明が空文字() {
		targetForm.setDescription("");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_説明が上限() {
		targetForm.setDescription(STRING_100);
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_説明が上限より長い() {
		targetForm.setDescription(STRING_100 + "X");
		String[][] expectations = {
				{"description", STRING_100 + "X",
					ValidationMessageKey.VALIDATION_RANGE_MAX}
		};
		assertData(expectations);
	}

}