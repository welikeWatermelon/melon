package com.melonme.block.exception;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;

public class BlockSelfException extends CustomException {

    public BlockSelfException() {
        super(ErrorCode.BLOCK_SELF);
    }
}
