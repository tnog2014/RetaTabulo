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
public class UserFormValidationTest {

	private Validator validator;

	private UserForm userForm = new UserForm();

	private BindingResult bindingResult = new BindException(userForm, "UserForm");

	@Before
	public void before() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();

		userForm.setId(1);
		userForm.setUserName("1234567890123456");
		userForm.setChangePassword(false);
		userForm.setOldPassword("12345678901234567890");
		userForm.setPassword("1234567890123456789A");
		userForm.setCnfPassword("1234567890123456789A");
		userForm.setNickname("12345678901234567890");
		userForm.setValid("1");
		userForm.setLevel("1");
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

	public void assertData(Object[][] expectations) {
		Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm);
//		Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm,
//				ValidGroup1.class, ValidGroup2.class, ValidGroup3.class);
		assertEquals(expectations.length, violations.size());
		List<ConstraintViolation<UserForm>> list = TestUtils.convertToListSortedByMessage(
				violations);
		for (int i = 0; i < list.size(); i++) {
			ConstraintViolation<UserForm> violation = list.get(i);
			Object[] expected = expectations[i];
			assertEquals("element[" + i + "]", expected[0], violation.getPropertyPath().toString());
			assertEquals("element[" + i + "]", expected[1], violation.getInvalidValue());
			assertEquals("element[" + i + "]", expected[2], violation.getMessage());
		}
	}

	@Test
	public void test_ユーザー名がnull() {
		userForm.setUserName(null);
		String[][] expectations = {
				{"userName", null, ValidationMessageKey.VALIDATION_REQUIRED}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名が下限より短い() {
		userForm.setUserName("123");
		String[][] expectations = {
				{"userName", "123",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名が上限より長い() {
		userForm.setUserName("1234567890123456X");
		String[][] expectations = {
				{"userName", "1234567890123456X",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名に全角文字が含まれる_あ() {
		userForm.setUserName("userあ");
		String[][] expectations = {
				{"userName", "userあ", ValidationMessageKey.VALIDATION_USERNAME_VALID}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名に半角スペースが含まれる_先頭() {
		userForm.setUserName(" usertest");
		String[][] expectations = {
				{"userName", " usertest", ValidationMessageKey.VALIDATION_USERNAME_VALID}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名に半角スペースが含まれる_内部() {
		userForm.setUserName("user test");
		String[][] expectations = {
				{"userName", "user test", ValidationMessageKey.VALIDATION_USERNAME_VALID}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名に半角スペースが含まれる_末尾() {
		userForm.setUserName("usertest ");
		String[][] expectations = {
				{"userName", "usertest ", ValidationMessageKey.VALIDATION_USERNAME_VALID}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名に半角スラッシュが含まれる() {
		userForm.setUserName("user/test");
		String[][] expectations = {
				{"userName", "user/test", ValidationMessageKey.VALIDATION_USERNAME_VALID}
		};
		assertData(expectations);
	}

	@Test
	public void test_ユーザー名がすべての利用可能文字からなる() {
		userForm.setUserName("azAZ09_.-");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	private static final String[] VALID_PASSWORDS = {
			"!\"#$%&'()*+,./",
			"0123456789:;<=>?@",
			"ABCDEFGHIJKLM",
			"NOPQRSTUVWXYZ",
			"\\[]^_`{|}~-",
			"abcdefghijklm",
			"nopqrstuvwxyz"
	};

	@Test
	public void test_パスワードが最小文字数未満() {
		userForm.setPassword("1234567");
		String[][] expectations = {
				{"password", "1234567",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワードが最小文字数() {
		userForm.setPassword("12345678");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワードが最大文字数() {
		userForm.setPassword("12345678901234567890");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワードが最大文字数より多い() {
		userForm.setPassword("12345678901234567890A");
		String[][] expectations = {
				{"password", "12345678901234567890A",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワードが利用不可文字_全角() {
		userForm.setPassword("1234567あ");
		String[][] expectations = {
				{"password", "1234567あ", ValidationMessageKey.VALIDATION_ONLY_ASCII}
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワードが利用可能文字() {

		for (String data : VALID_PASSWORDS) {
			userForm.setPassword(data);
			String[][] expectations = {
			};
			assertData(expectations);
		}
	}

	// --------------------------------
	// Old password
	// --------------------------------

	@Test
	public void test_現在のパスワードが最小文字数未満() {
		userForm.setOldPassword("1234567");
		String[][] expectations = {
				{"oldPassword", "1234567",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_現在のパスワードが最小文字数() {
		userForm.setOldPassword("12345678");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_現在のパスワードが最大文字数() {
		userForm.setOldPassword("12345678901234567890");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_現在のパスワードが最大文字数より多い() {
		userForm.setOldPassword("12345678901234567890A");
		String[][] expectations = {
				{"oldPassword", "12345678901234567890A",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_現在のパスワードが利用不可文字_全角() {
		userForm.setOldPassword("1234567あ");
		String[][] expectations = {
				{"oldPassword", "1234567あ", ValidationMessageKey.VALIDATION_ONLY_ASCII}
		};
		assertData(expectations);
	}

	@Test
	public void test_現在のパスワードが利用可能文字() {
		for (String data : VALID_PASSWORDS) {
			userForm.setOldPassword(data);
			String[][] expectations = {
			};
			assertData(expectations);
		}
	}

	// --------------------------------
	// password (confirm)
	// --------------------------------

	@Test
	public void test_確認パスワードが最小文字数未満() {
		userForm.setCnfPassword("1234567");
		String[][] expectations = {
				{"cnfPassword", "1234567",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_確認パスワードが最小文字数() {
		userForm.setCnfPassword("12345678");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_確認パスワードが最大文字数() {
		userForm.setCnfPassword("12345678901234567890");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_確認パスワードが最大文字数より多い() {
		userForm.setCnfPassword("12345678901234567890A");
		String[][] expectations = {
				{"cnfPassword", "12345678901234567890A", ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	@Test
	public void test_確認パスワードが利用不可文字_全角() {
		userForm.setCnfPassword("1234567あ");
		String[][] expectations = {
				{"cnfPassword", "1234567あ", ValidationMessageKey.VALIDATION_ONLY_ASCII}
		};
		assertData(expectations);
	}

	@Test
	public void test_確認パスワードが利用可能文字() {
		for (String data : VALID_PASSWORDS) {
			userForm.setCnfPassword(data);
			String[][] expectations = {
			};
			assertData(expectations);
		}
	}

	// --------------------------------
	// passwords match
	// --------------------------------

	@Test
	public void test_パスワード一致() {
		userForm.setChangePassword(true);
		userForm.setPassword("12345678");
		userForm.setCnfPassword("12345678");
		String[][] expectations = {
				//{"cnfPassword", "1234567", "8 ～ 20文字で入力してください"}
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワード不一致() {
		userForm.setChangePassword(true);
		userForm.setPassword("12345678");
		userForm.setCnfPassword("12345678A");
		Object[][] expectations = {
				{"cnfPasswordMatch", false, ValidationMessageKey.VALIDATION_PASSWORDS_NOT_MATCH}
		};
		assertData(expectations);
	}

	@Test
	public void test_パスワード不一致チェックしない() {
		userForm.setChangePassword(false);
		userForm.setPassword("12345678");
		userForm.setCnfPassword("12345678A");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	// --------------------------------
	// nickname
	// --------------------------------

	@Test
	public void test_ニックネームが最小文字数未満() {
		userForm.setNickname("");
		String[][] expectations = {
				{"nickname", "",  ValidationMessageKey.VALIDATION_RANGE},
				{"nickname", "", ValidationMessageKey.VALIDATION_REQUIRED}
		};
		assertData(expectations);
	}

	@Test
	public void test_ニックネームが最小文字数() {
		userForm.setNickname("1");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_ニックネームが最大文字数() {
		userForm.setNickname("12345678901234567890");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_ニックネームが最大文字数より多い() {
		userForm.setNickname("12345678901234567890A");
		String[][] expectations = {
				{"nickname", "12345678901234567890A",  ValidationMessageKey.VALIDATION_RANGE}
		};
		assertData(expectations);
	}

	// -----------------------------------
	// level
	// -----------------------------------

	@Test
	public void test_レベル_0() {
		userForm.setLevel("0");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_レベル_1() {
		userForm.setLevel("1");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_レベル_A() {
		userForm.setLevel("A");
		String[][] expectations = {
				{"level", "A", ValidationMessageKey.VALIDATION_ONLY_0_1}
		};
		assertData(expectations);
	}

	@Test
	public void test_レベル_空() {
		userForm.setLevel("");
		String[][] expectations = {
				{"level", "", ValidationMessageKey.VALIDATION_ONLY_0_1}
		};
		assertData(expectations);
	}

	// -----------------------------------
	// valid
	// -----------------------------------

	@Test
	public void test_有効性_0() {
		userForm.setValid("0");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_有効性_1() {
		userForm.setValid("1");
		String[][] expectations = {
		};
		assertData(expectations);
	}

	@Test
	public void test_有効性_A() {
		userForm.setValid("A");
		String[][] expectations = {
				{"valid", "A", ValidationMessageKey.VALIDATION_ONLY_0_1}
		};
		assertData(expectations);
	}

	@Test
	public void test_有効性_空() {
		userForm.setValid("");
		String[][] expectations = {
				{"valid", "", ValidationMessageKey.VALIDATION_ONLY_0_1}
		};
		assertData(expectations);
	}
}