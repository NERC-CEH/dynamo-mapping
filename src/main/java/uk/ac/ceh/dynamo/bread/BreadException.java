package uk.ac.ceh.dynamo.bread;

/**
 * An exception which is thrown when an exception has occurred which means that 
 * a slice of bread could not be baked
 * @author Christopher Johnson
 */
public class BreadException extends Exception {
    public BreadException(String mess) {
        super(mess);
    }
    
    public BreadException(String mess, Throwable cause) {
        super(mess, cause);
    }
}
