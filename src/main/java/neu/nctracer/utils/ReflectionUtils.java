package neu.nctracer.utils;

import neu.nctracer.exception.ReflectionUtilsException;

/**
 * Utility to load classes and create instances reflectively
 * 
 * @author Ankur Shanbhag
 *
 */
public class ReflectionUtils {

    public static <T> T instantiate(String className,
                                    Class<T> type) throws ReflectionUtilsException {
        try {
            T newInstance = type.cast(Class.forName(className).newInstance());
            return newInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReflectionUtilsException(e);
        } catch (ClassNotFoundException e) {
            throw new ReflectionUtilsException("Specified classname ["
                                               + className
                                               + "]not found on the job classpath",
                                               e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String className,
                                        Class<T> type) throws ReflectionUtilsException {
        try {
            Class<?> classHandle = Class.forName(className);
            return (Class<T>) classHandle;
        } catch (ClassNotFoundException e) {
            throw new ReflectionUtilsException("Specified classname ["
                                               + className
                                               + "]not found on the job classpath",
                                               e);
        }
    }

    private ReflectionUtils() {
    }
}
