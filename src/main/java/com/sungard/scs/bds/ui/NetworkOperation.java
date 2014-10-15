package com.sungard.scs.bds.ui;

public interface NetworkOperation<T> {

	T execute(String url, String username, String password);

}
