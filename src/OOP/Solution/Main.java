package OOP.Solution;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Stack;
import java.util.TreeMap;

public class Main {
    @OOPBefore("Dude")
    int pow(int x, int y) {
        return x*y;
    }

    static Object make_backup(Class<?> testClass, Object instance) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> con = testClass.getDeclaredConstructor();
        Object backup = con.newInstance();

        for (Field field : testClass.getDeclaredFields()) {
            System.out.println(field);
            Object field_instance = field.get(instance);
            Class<?> field_class = field_instance.getClass();
            if (field_instance instanceof Cloneable)
                field.set(backup, field_class.getMethod("clone").invoke(field_instance));
            else {
                try {
                    Constructor<?> field_con = field_class.getConstructor(field_class);
                    field.set(backup, field_con.newInstance(field_instance));
                } catch (NoSuchMethodException e) {
                    System.out.println("Nothing at all");
                    field.set(backup, field_instance);
                }
            }
        }

        return backup;
    }

    public static void main(String []args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> con = A3.class.getDeclaredConstructor();
        Object instance = con.newInstance();
        Object backup = make_backup(A3.class, instance);
        System.out.println(instance);
        System.out.println(backup);
        for (Field field : A3.class.getDeclaredFields()) {
            field.set(instance, field.get(backup));
        }
        System.out.println(instance);
        System.out.println(backup);
    }
}
