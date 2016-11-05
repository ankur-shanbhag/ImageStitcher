package neu.nctracer.utils;

import neu.nctracer.exception.ReflectionUtilsException;

/**
 * Utility to load classes and create instances reflectively
 * 
 * @author Ankur Shanbhag
 *
 */
public class ReflectionUtils {

    /**
     * The methods creates an instance of the class specified by className
     * param. The className should be of type param.<br>
     * For example: To instantiate class <code>foo</code> which is of type
     * <code>bar</code> call - <br>
     * <code>xyz.bar instance = ReflectionUtils.instantiate("foo", bar.class)</code>
     * 
     * @param className
     *            - fully qualifies className of the class to be instantiated
     * @param type
     *            - Type of className
     * @return - instance of className reference by type
     * @throws ReflectionUtilsException
     *             - If instantiation fails or class cannot be found
     */
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

    /**
     * Returns a class descriptor for the specified class name, referenced by
     * type
     * 
     * @param className
     * @param type
     * @return
     * @throws ReflectionUtilsException
     */
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
