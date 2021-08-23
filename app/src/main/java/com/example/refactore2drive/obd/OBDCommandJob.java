package com.example.refactore2drive.obd;

import com.github.pires.obd.commands.ObdCommand;

import java.io.Serializable;

public class OBDCommandJob {
    private Long _id;
    private ObdCommand _command;
    private ObdCommandJobState _state;

    /**
     * Default ctor.
     *
     * @param command the ObCommand to encapsulate.
     */
    public OBDCommandJob(ObdCommand command) {
        _command = command;
        _state = ObdCommandJobState.NEW;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    public ObdCommand getCommand() {
        return _command;
    }

    /**
     * @return job current state.
     */
    public ObdCommandJobState getState() {
        return _state;
    }

    /**
     * Sets a new job state.
     *
     * @param state the new job state.
     */
    public void setState(ObdCommandJobState state) {
        _state = state;
    }

    /**
     * The state of the command.
     */
    public enum ObdCommandJobState {
        NEW,
        RUNNING,
        FINISHED,
        EXECUTION_ERROR,
        BROKEN_PIPE,
        QUEUE_ERROR,
        NOT_SUPPORTED
    }
}
