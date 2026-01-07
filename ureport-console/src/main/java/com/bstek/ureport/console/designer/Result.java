package com.bstek.ureport.console.designer;

import lombok.Data;

@Data
class Result {
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
