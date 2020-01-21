package com.github.drinkjava2.cglib.core;

import com.github.drinkjava2.asm.Opcodes;

final class AsmApi {

    /**
     * Returns the latest stable ASM API value in {@link Opcodes}.
     */
    static int value() {
        return Opcodes.ASM7;
    }

    private AsmApi() {
    }
}
