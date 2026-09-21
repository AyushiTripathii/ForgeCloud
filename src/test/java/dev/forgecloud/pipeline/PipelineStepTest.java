package dev.forgecloud.pipeline;

import dev.forgecloud.project.Project;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class PipelineStepTest {

    private PipelineStep newStep() {
        Project project = new Project(
            "Test project",
            "https://github.com/example/demo",
            "example/demo",
            "main"
        );

        PipelineRun pipeline = new PipelineRun(
            project,
            null,
            "main",
            TriggerType.MANUAL,
            null
        );

        return new PipelineStep(pipeline, "Run tests", 1);
    }

    @Test
    void pendingStepCanStart() {
        PipelineStep step = newStep();
        Instant createdAt = step.getCreatedAt();
        Instant before = Instant.now();

        step.start();

        Instant after = Instant.now();

        assertEquals(PipelineStepStatus.RUNNING, step.getStatus());
        assertBetween(step.getStartedAt(), before, after);
        assertEquals(createdAt, step.getCreatedAt());
        assertNull(step.getFinishedAt());
        assertNull(step.getExitCode());
    }

    @Test
    void runningStepCanSucceed() {
        PipelineStep step = newStep();
        step.start();

        Instant startedAt = step.getStartedAt();
        Instant before = Instant.now();

        step.succeed();

        Instant after = Instant.now();

        assertEquals(PipelineStepStatus.SUCCESS, step.getStatus());
        assertEquals(Integer.valueOf(0), step.getExitCode());
        assertEquals(startedAt, step.getStartedAt());
        assertBetween(step.getFinishedAt(), before, after);
    }

    @Test
    void runningStepCanFail() {
        PipelineStep step = newStep();
        step.start();

        Instant startedAt = step.getStartedAt();
        Instant before = Instant.now();

        step.fail(2);

        Instant after = Instant.now();

        assertEquals(PipelineStepStatus.FAILED, step.getStatus());
        assertEquals(Integer.valueOf(2), step.getExitCode());
        assertEquals(startedAt, step.getStartedAt());
        assertBetween(step.getFinishedAt(), before, after);
    }

    @Test
    void pendingStepCanBeSkipped() {
        PipelineStep step = newStep();
        Instant before = Instant.now();

        step.skip();

        Instant after = Instant.now();

        assertEquals(PipelineStepStatus.SKIPPED, step.getStatus());
        assertBetween(step.getFinishedAt(), before, after);
        assertNull(step.getStartedAt());
        assertNull(step.getExitCode());
    }

    @Test
    void pendingStepCannotFinish() {
        PipelineStep step = newStep();

        assertRejectedWithoutChanges(step, step::succeed);
        assertRejectedWithoutChanges(step, () -> step.fail(1));
    }

    @Test
    void runningStepCannotStartAgainOrBeSkipped() {
        PipelineStep step = newStep();
        step.start();

        assertRejectedWithoutChanges(step, step::start);
        assertRejectedWithoutChanges(step, step::skip);
    }

    @Test
    void zeroFailureCodeIsRejectedWithoutChanges() {
        PipelineStep step = newStep();
        step.start();

        Instant createdAt = step.getCreatedAt();
        Instant startedAt = step.getStartedAt();

        assertThrows(
            IllegalArgumentException.class,
            () -> step.fail(0)
        );

        assertEquals(PipelineStepStatus.RUNNING, step.getStatus());
        assertEquals(createdAt, step.getCreatedAt());
        assertEquals(startedAt, step.getStartedAt());
        assertNull(step.getFinishedAt());
        assertNull(step.getExitCode());
    }

    @ParameterizedTest
    @EnumSource(
        value = PipelineStepStatus.class,
        names = {"SUCCESS", "FAILED", "SKIPPED"}
    )
    void terminalStepRejectsEveryTransition(
        PipelineStepStatus terminalStatus
    ) {
        PipelineStep step = newStep();

        switch (terminalStatus) {
            case SUCCESS -> {
                step.start();
                step.succeed();
            }
            case FAILED -> {
                step.start();
                step.fail(1);
            }
            case SKIPPED -> step.skip();
            default -> throw new IllegalArgumentException(
                "Unexpected status"
            );
        }

        assertRejectedWithoutChanges(step, step::start);
        assertRejectedWithoutChanges(step, step::succeed);
        assertRejectedWithoutChanges(step, () -> step.fail(1));
        assertRejectedWithoutChanges(step, step::skip);
    }

    private void assertRejectedWithoutChanges(
        PipelineStep step,
        Runnable operation
    ) {
        PipelineStepStatus status = step.getStatus();
        Instant createdAt = step.getCreatedAt();
        Instant startedAt = step.getStartedAt();
        Instant finishedAt = step.getFinishedAt();
        Integer exitCode = step.getExitCode();

        assertThrows(
            IllegalStateException.class,
            operation::run
        );

        assertEquals(status, step.getStatus());
        assertEquals(createdAt, step.getCreatedAt());
        assertEquals(startedAt, step.getStartedAt());
        assertEquals(finishedAt, step.getFinishedAt());
        assertEquals(exitCode, step.getExitCode());
    }

    private void assertBetween(
        Instant actual,
        Instant before,
        Instant after
    ) {
        assertNotNull(actual);
        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }
}