package com.bstek.ureport.console.designer.dto;

import lombok.Data;

@Data
public class Result {
    private String message;
    private boolean success;


    public static Result ok(String message){
    	Result result=new Result();
    	result.setSuccess(true);
    	result.setMessage(message);
    	return result;
    }

    public static Result err(String message){
    	Result result=new Result();
    	result.setSuccess(false);
    	result.setMessage(message);
    	return result;
    }
}
