package kz.dissertation.caranalytics.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " with id " + id + " was not found");
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(resourceName + " with identifier " + identifier + " was not found");
    }
}
