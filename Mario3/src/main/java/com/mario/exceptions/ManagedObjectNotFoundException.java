package com.mario.exceptions;

public class ManagedObjectNotFoundException extends RuntimeException {

	public ManagedObjectNotFoundException(String string) {
		super(string);
	}

	private static final long serialVersionUID = -6406519701076398078L;

}
