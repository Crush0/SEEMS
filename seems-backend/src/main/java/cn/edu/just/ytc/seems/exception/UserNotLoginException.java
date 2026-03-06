package cn.edu.just.ytc.seems.exception;

public class UserNotLoginException extends ServerException {
    public UserNotLoginException() {
        super("User not login");
        setCode(40100);
    }
    public UserNotLoginException(String message) {
        super(message);
        setCode(40100);
    }
    public UserNotLoginException(String message, Throwable cause) {
        super(message, cause);
        setCode(40100);
    }
}
