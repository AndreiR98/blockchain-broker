package uk.co.roteala.messanging;

import uk.co.roteala.common.messenger.Assembler;
import uk.co.roteala.common.messenger.AssemblerSupplier;
import uk.co.roteala.common.messenger.ClientMessage;

public class AssemblerMessenger implements AssemblerSupplier<ClientMessage> {
    @Override
    public Assembler<ClientMessage> get() {
        return new CustomAssembler();
    }

    private class CustomAssembler implements Assembler<ClientMessage> {
        //
    }
}
