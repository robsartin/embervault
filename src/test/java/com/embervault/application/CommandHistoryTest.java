package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.application.port.in.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandHistoryTest {

    @Test
    @DisplayName("execute() calls command's execute method")
    void execute_shouldCallCommandExecute() {
        CommandHistory history = new CommandHistory();
        int[] counter = {0};
        Command command = new Command() {
            @Override
            public void execute() {
                counter[0]++;
            }

            @Override
            public void undo() {
                counter[0]--;
            }

            @Override
            public String description() {
                return "test";
            }
        };

        history.execute(command);

        assertEquals(1, counter[0]);
    }
}
