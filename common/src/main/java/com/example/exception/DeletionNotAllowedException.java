package com.example.exception;

import com.thoughtworks.xstream.core.BaseException;

public class DeletionNotAllowedException extends BaseException {
    public DeletionNotAllowedException(String msg) {
        super(msg);
    }
}
