package com.mobile.application.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StockOverException extends RuntimeException 
{

	private static final long serialVersionUID = 1L;

	public StockOverException(String message) 
	{
		super(message);
	}

}