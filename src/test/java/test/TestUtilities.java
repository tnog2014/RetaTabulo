package test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestUtilities {

	public static void setField(Object target, String fieldName, Object valueToSet)
			throws Exception {
		Field f = target.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(target, valueToSet);
	}

	public static Object invoke(Object target, String methodName, Object[] args) throws Throwable {
		Class[] classes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			classes[i] = args[i].getClass();
		}
		Method m = target.getClass().getDeclaredMethod(methodName, classes);
		m.setAccessible(true);

		Object ret = null;
		try {
			ret = m.invoke(target, args);
		} catch (InvocationTargetException ite) {
			throw ite.getTargetException();
		}
		return ret;
	}
}
