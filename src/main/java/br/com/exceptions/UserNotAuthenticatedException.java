package br.com.exceptions;

public class UserNotAuthenticatedException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	public UserNotAuthenticatedException(String msg) {
        super(msg);
    }

}
