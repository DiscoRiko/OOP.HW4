package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExceptionMismatchError;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

import java.lang.reflect.*;
import java.util.*;

public class OOPUnitCore {
    public static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new OOPAssertionFailure(expected, actual);
        }
    }

    public static void fail() {
        throw new OOPAssertionFailure();
    }

    public static OOPTestSummary runClass(Class<?> testClass) {
        Object instance = runClassStart(testClass);
        // OOPTest activation
        return new OOPTestSummary(test_activation(testClass, instance, ""));
    }

    public static OOPTestSummary runClass(Class<?> testClass, String tag) {
        Object instance = runClassStart(testClass);
        // OOPTest activation
        return new OOPTestSummary(test_activation(testClass, instance, tag));
    }

    static Object runClassStart(Class<?> testClass) {
        if (testClass == null)
            throw new IllegalArgumentException();

        if (testClass.getAnnotationsByType(OOPTestClass.class).length == 0)
            throw new IllegalArgumentException();

        // Construct new instance
        Object instance = null;
        try {
            Constructor<?> con = testClass.getDeclaredConstructor();
            con.setAccessible(true);
            instance = con.newInstance();
        } catch (Exception ignored) {}


        // OOPSetup activation
        setup_activation(testClass, instance);
        return instance;
    }

    static void setup_activation(Class<?> testClass, Object instance) {
        Stack<Method> setup_methods_stack = new Stack<>();
        try {
            for(Class<?> current_class = testClass; !current_class.equals(Class.forName("java.lang.Object")); current_class = current_class.getSuperclass()) {
                for (Method method : current_class.getDeclaredMethods()) {
                    if (method.getAnnotationsByType(OOPSetup.class).length != 0)
                        setup_methods_stack.push(method);
                }
            }

            while(!setup_methods_stack.empty()) {
                Method method = setup_methods_stack.pop();
                method.setAccessible(true);
                method.invoke(instance);
            }
        } catch (Exception ignored) {}
    }

    static Map<String, OOPResult> test_activation(Class<?> testClass, Object instance, String tag) {
        boolean test_class_unordered = testClass.getAnnotationsByType(OOPTestClass.class)[0].value() == OOPTestClass.OOPTestClassType.UNORDERED;
        TreeMap<Integer, Method> ordered_methods_map = new TreeMap<>();
        Map<String, OOPResult> result_map = new HashMap<>();

        try {
            for (Class<?> current_class = testClass; !current_class.equals(Class.forName("java.lang.Object")); current_class = current_class.getSuperclass()) {
                if (current_class.getAnnotationsByType(OOPTestClass.class).length != 0)
                    for (Method method : current_class.getDeclaredMethods()) {
                        if (method.getAnnotationsByType(OOPTest.class).length != 0)
                            if (tag.equals("") || method.getAnnotationsByType(OOPTest.class)[0].tag().equals(tag)) {
                                if (test_class_unordered) {
                                    result_map.put(method.getName(), run_before_test_after(testClass, instance, method));
                                }
                                else {
                                    boolean current_class_unordered = current_class.getAnnotationsByType(OOPTestClass.class)[0].value() == OOPTestClass.OOPTestClassType.UNORDERED;
                                    if (current_class_unordered) {
                                        result_map.put(method.getName(), run_before_test_after(testClass, instance, method));
                                    }
                                    else {
                                        ordered_methods_map.put(method.getAnnotationsByType(OOPTest.class)[0].order(), method);
                                    }
                                }
                            }
                    }
            }
        } catch (Exception ignored) {}


        for(Method method: ordered_methods_map.values()) {
            result_map.put(method.getName(), run_before_test_after(testClass, instance, method));
        }
        return result_map;
    }

    static String before_activation(Class<?> testClass, Object instance, Method test_method) {
        Stack<Method> before_methods_stack = new Stack<>();

        try {
            for(Class<?> current_class = testClass; !current_class.equals(Class.forName("java.lang.Object")); current_class = current_class.getSuperclass()) {
                for (Method method : current_class.getDeclaredMethods()) {
                    if (method.getAnnotationsByType(OOPBefore.class).length != 0)
                        if (Arrays.asList(method.getAnnotationsByType(OOPBefore.class)[0].value()).contains(test_method.getName()))
                            before_methods_stack.push(method);
                }
            }
        } catch (Exception ignored) {}

        while(!before_methods_stack.empty()) {
            Object backup = make_backup(testClass, instance);
            try {
                before_methods_stack.pop().invoke(instance);
            } catch (Exception e) {
                for (Field field : testClass.getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        field.set(instance, field.get(backup));
                    } catch (Exception ignored) {}

                }
                return e.getCause().getClass().getName();
            }
        }
        return null;
    }

    static String after_activation(Class<?> testClass, Object instance, Method test_method) {
        Object backup;
        try {
            for(Class<?> current_class = testClass; !current_class.equals(Class.forName("java.lang.Object")); current_class = current_class.getSuperclass()) {
                for (Method method : current_class.getDeclaredMethods()) {
                    if (method.getAnnotationsByType(OOPAfter.class).length != 0)
                        if (Arrays.asList(method.getAnnotationsByType(OOPAfter.class)[0].value()).contains(test_method.getName())) {
                            backup = make_backup(testClass, instance);
                            try {
                                method.invoke(instance);
                            } catch (Exception e) {
                                for (Field field : testClass.getDeclaredFields()) {
                                    field.setAccessible(true);
                                    field.set(instance, field.get(backup));
                                }
                                return e.getCause().getClass().getName();
                            }
                        }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    static Object make_backup(Class<?> testClass, Object instance) {
        Object backup = null;
        try {
            Constructor<?> con = testClass.getDeclaredConstructor();
            con.setAccessible(true);
            backup = con.newInstance();
        } catch (Exception ignored) {}


        for (Field field : testClass.getDeclaredFields()) {
            Class<?> field_class = null;
            Object field_instance = null;
            try {
                field.setAccessible(true);
                field_instance = field.get(instance);
                field_class = field_instance.getClass();
            } catch (Exception ignored) {}

            if (field_instance instanceof Cloneable) {
                try {
                    field.set(backup, field_class.getMethod("clone").invoke(field_instance));
                } catch (Exception ignored) {}
            }
            else {
                try {
                    try {
                        Constructor<?> field_con = field_class.getConstructor(field_class);
                        field_con.setAccessible(true);
                        field.set(backup, field_con.newInstance(field_instance));
                    } catch (NoSuchMethodException e) {
                        field.set(backup, field_instance);
                    }
                } catch (Exception ignored) {}
            }
        }
        return backup;
    }

    static OOPExpectedException get_expected_exception(Class<?> testClass, Object instance) {
        try {
            for (Field field : testClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getAnnotationsByType(OOPExceptionRule.class).length != 0)
                    return (OOPExpectedException)field.get(instance);
            }
        } catch (Exception ignored) {}
        return null;
    }

    static OOPResult run_before_test_after(Class<?> testClass, Object instance, Method method) {
        OOPResult result = null;
        String exception_class_name = null;
        // OOPBefore activation
        exception_class_name = before_activation(testClass, instance, method);
        if (exception_class_name != null)
            return new OOPResultImp(OOPResult.OOPTestResult.ERROR, exception_class_name);
        //OOPTest activation
        OOPExpectedException expected_exception = get_expected_exception(testClass, instance);
        try {
            method.invoke(instance);
            if(expected_exception != null && expected_exception.getExpectedException() != null) {
                result = new OOPResultImp(OOPResult.OOPTestResult.ERROR, expected_exception.getClass().getName());
            }
            else {
                result = new OOPResultImp(OOPResult.OOPTestResult.SUCCESS, null);
            }
        } catch (Exception wrapped_e) {
            Throwable e = wrapped_e.getCause();
            if (e instanceof OOPAssertionFailure) { // TODO - check if we can expect OOPAssertionFailure
                result = new OOPResultImp(OOPResult.OOPTestResult.FAILURE, e.getMessage());
            } else if (e instanceof Exception) {
                if(expected_exception == null || expected_exception.getExpectedException() == null) {
                    result = new OOPResultImp(OOPResult.OOPTestResult.ERROR, e.getClass().getName());
                }
                else if(expected_exception.assertExpected((Exception)e)) {
                    result = new OOPResultImp(OOPResult.OOPTestResult.SUCCESS, null);
                } else {
                    OOPExceptionMismatchError eme = new OOPExceptionMismatchError(expected_exception.getExpectedException(), ((Exception)e).getClass());
                    result = new OOPResultImp(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH, eme.getMessage());
                }
            }

            /*try {
                  throw wrapped_e.getCause();
            } catch (OOPAssertionFailure e) { // TODO - check if we can expect OOPAssertionFailure
                result = new OOPResultImp(OOPResult.OOPTestResult.FAILURE, e.getMessage());
            } catch (Exception e) {
                if(expected_exception == null || expected_exception.getExpectedException() == null) {
                    result = new OOPResultImp(OOPResult.OOPTestResult.ERROR, e.getClass().getName());
                }
                else if(expected_exception.assertExpected(e)) {
                    result = new OOPResultImp(OOPResult.OOPTestResult.SUCCESS, null);
                } else {
                    OOPExceptionMismatchError eme = new OOPExceptionMismatchError(expected_exception.getExpectedException(), e.getClass());
                    result = new OOPResultImp(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH, eme.getMessage());
                }
            }*/
        }
        // OOPAfter activation
        exception_class_name = after_activation(testClass, instance, method);
        if (exception_class_name != null)
            return new OOPResultImp(OOPResult.OOPTestResult.ERROR, exception_class_name);

        // Reset OOPExpectedException Field if exists
        if(expected_exception != null) {
            expected_exception = OOPExpectedExceptionImpl.none();
        }

        return result;
    }
}
