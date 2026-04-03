package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WindowSetupContextTest {

    @Test
    void shouldExposeSharedServicesComponents() {
        SharedServices services = SharedServices.create();

        WindowSetupContext ctx = new WindowSetupContext(
                services, new WindowManager());

        assertEquals(services.project(), ctx.project());
        assertEquals(services.noteService(), ctx.noteService());
        assertEquals(services.linkService(), ctx.linkService());
        assertEquals(services.stampService(), ctx.stampService());
        assertEquals(services.schemaRegistry(), ctx.schemaRegistry());
        assertNotNull(ctx.windowManager());
    }

    @Test
    void shouldRejectNullServices() {
        assertThrows(NullPointerException.class,
                () -> new WindowSetupContext(null, new WindowManager()));
    }

    @Test
    void shouldRejectNullWindowManager() {
        assertThrows(NullPointerException.class,
                () -> new WindowSetupContext(
                        SharedServices.create(), null));
    }
}
