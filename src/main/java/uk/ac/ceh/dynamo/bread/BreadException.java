package uk.ac.ceh.dynamo.bread;
/**
 *
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
