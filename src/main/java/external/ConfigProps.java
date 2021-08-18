package external;

/**
 * this interface is designed for developers to implement its only one method to get all properties
 * in xml files, the classes which implement this interface must be marked with annotation {@code ConfigInject}
 * containing the path of the xml file
 * @param <T> the param which to be returned
 */
@FunctionalInterface
public interface ConfigProps<T> {

    T getPropertyCollection();

}
