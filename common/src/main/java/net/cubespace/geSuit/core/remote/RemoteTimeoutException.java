package net.cubespace.geSuit.core.remote;

/**
 * This exception is thrown if a remote call times out. This can happen if the network is unavailable, or the remote has no execution side.
 */
public class RemoteTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 4807675328919028609L;

    public RemoteTimeoutException() {
        super();
    }
    
    public RemoteTimeoutException(String message) {
        super(message);
    }
    
    public RemoteTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RemoteTimeoutException(Throwable cause) {
        super(cause);
    }
}
