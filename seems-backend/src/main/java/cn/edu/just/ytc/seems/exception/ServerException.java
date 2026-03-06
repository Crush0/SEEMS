package cn.edu.just.ytc.seems.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerException extends RuntimeException{
    private int code = 500;
    public ServerException(){
        super();
    }
    public ServerException(int code, String message){
        super(message);
        this.code = code;
    }
    public ServerException(String message){
        super(message);
    }
    public ServerException(String message, Throwable cause){
        super(message, cause);
    }
}
