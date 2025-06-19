package fr.anisekai.wireless.plannifier;

import fr.anisekai.wireless.api.plannifier.EventScheduler;
import fr.anisekai.wireless.api.plannifier.data.CalibrationResult;
import fr.anisekai.wireless.plannifier.data.*;
import fr.anisekai.wireless.api.plannifier.exceptions.DelayOverlapException;
import fr.anisekai.wireless.api.plannifier.exceptions.NotSchedulableException;
import fr.anisekai.wireless.api.plannifier.interfaces.ScheduleSpotData;
import fr.anisekai.wireless.api.plannifier.interfaces.Scheduler;
import fr.anisekai.wireless.api.plannifier.interfaces.SchedulerManager;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.Planifiable;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Consumer;

@DisplayName("SimpleScheduler")
@Tags({@Tag("unit-test"), @Tag("event-scheduler")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class EventSchedulerTests {

    private static class Manager implements SchedulerManager<TestWatchTarget, TestWatchParty, TestWatchParty> {

        private int id = 100; // Safe margin for pre-populated data in TestData class.

        private int id() {

            this.id++;
            return this.id;
        }

        @Override
        public TestWatchParty create(Planifiable<TestWatchTarget> planifiable) {

            return new TestWatchParty(
                    this.id(),
                    planifiable.getWatchTarget(),
                    planifiable.getFirstEpisode(),
                    planifiable.getStartingAt(),
                    planifiable.getEpisodeCount(),
                    planifiable.isSkipEnabled()
            );
        }

        @Override
        public TestWatchParty update(TestWatchParty entity, Consumer<TestWatchParty> updateHook) {

            return entity;
        }

        @Override
        public List<TestWatchParty> updateAll(List<TestWatchParty> entities, Consumer<TestWatchParty> updateHook) {

            return entities;
        }

        @Override
        public boolean delete(TestWatchParty entity) {

            entity.tagDeleted();
            return true;
        }

    }

    private Scheduler<TestWatchTarget, TestWatchParty, TestWatchParty> scheduler;
    private TestData                                                   data;

    @BeforeEach
    public void setup() {

        this.data      = new TestData();
        this.scheduler = new EventScheduler<>(new Manager(), this.data.dataBank());
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - No Conflicts")
    public void testSingleSchedulingNoConflict() {

        ZonedDateTime                     scheduleAt = TestData.BASE_DATETIME.plusDays(1);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target2, scheduleAt, 1);

        Assertions.assertTrue(
                this.scheduler.canSchedule(spot),
                "The event can't be scheduled."
        );

        TestWatchParty party = Assertions.assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error as occurred while scheduling the event."
        );

        Assertions.assertEquals(1, party.getFirstEpisode(), "First episode mismatch.");
        Assertions.assertEquals(1, party.getEpisodeCount(), "Episode count mismatch.");
        Assertions.assertEquals(scheduleAt, party.getStartingAt(), "Starting datetime mismatch.");
        Assertions.assertTrue(this.scheduler.getState().contains(party), "Dirty State.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - With Conflicts")
    public void testSingleSchedulingWithConflict() {

        ScheduleSpotData<TestWatchTarget> spot = new TestSpot(this.data.target2, TestData.BASE_DATETIME, 1);

        Assertions.assertFalse(
                this.scheduler.canSchedule(spot),
                "The event can be scheduled."
        );

        Assertions.assertThrows(
                NotSchedulableException.class,
                () -> this.scheduler.schedule(spot),
                "An error as occurred while scheduling the event."
        );
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling - Follow-up")
    public void testSingleSchedulingFollowUp() {

        ZonedDateTime                     scheduleAt = TestData.BASE_DATETIME.plusDays(1);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target1, scheduleAt, 1);

        Assertions.assertTrue(
                this.scheduler.canSchedule(spot),
                "The event can't be scheduled."
        );

        TestWatchParty party = Assertions.assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error as occurred while scheduling the event."
        );

        Assertions.assertEquals(3, party.getFirstEpisode(), "First episode mismatch.");
        Assertions.assertEquals(1, party.getEpisodeCount(), "Episode count mismatch.");
        Assertions.assertEquals(scheduleAt, party.getStartingAt(), "Starting datetime mismatch.");
        Assertions.assertTrue(this.scheduler.getState().contains(party), "Dirty State.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - To Left")
    public void testSingleSchedulingMergingToLeft() {

        ZonedDateTime                     leftTime   = this.data.partyB2.getStartingAt();
        ZonedDateTime                     scheduleAt = leftTime.plusMinutes(50);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target1, scheduleAt, 1);

        Assertions.assertTrue(
                this.scheduler.canSchedule(spot),
                "The event can't be scheduled."
        );

        TestWatchParty party = Assertions.assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error as occurred while scheduling the event."
        );

        Assertions.assertEquals(this.data.partyB2, party, "Event not merged");
        Assertions.assertEquals(5, party.getFirstEpisode(), "First episode mismatch");
        Assertions.assertEquals(3, party.getEpisodeCount(), "Episode count mismatch");
        Assertions.assertEquals(leftTime, party.getStartingAt(), "Starting datetime mismatch");
        Assertions.assertTrue(this.scheduler.getState().contains(party), "Dirty State.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - To Right")
    public void testSingleSchedulingMergingToRight() {

        ZonedDateTime                     scheduleAt = this.data.partyB1.getStartingAt().minusMinutes(25);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target1, scheduleAt, 1);

        Assertions.assertTrue(
                this.scheduler.canSchedule(spot),
                "The event can't be scheduled."
        );

        TestWatchParty party = Assertions.assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error as occurred while scheduling the event."
        );

        Assertions.assertEquals(this.data.partyB1, party, "Event not merged");
        Assertions.assertEquals(3, party.getFirstEpisode(), "First episode mismatch");
        Assertions.assertEquals(3, party.getEpisodeCount(), "Episode count mismatch");
        Assertions.assertEquals(scheduleAt, party.getStartingAt(), "Starting datetime mismatch");
        Assertions.assertTrue(this.scheduler.getState().contains(party), "Dirty State.");
    }

    @Test
    @DisplayName("Scheduler | Single Scheduling | Merging - Left & Right (Sandwich Merging)")
    public void testSingleSchedulingSandwichMerging() {

        ZonedDateTime                     leftTime   = this.data.partyB1.getStartingAt();
        ZonedDateTime                     scheduleAt = leftTime.plusMinutes(50);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target1, scheduleAt, 1);

        Assertions.assertTrue(
                this.scheduler.canSchedule(spot),
                "The event can't be scheduled."
        );

        TestWatchParty party = Assertions.assertDoesNotThrow(
                () -> this.scheduler.schedule(spot),
                "An error as occurred while scheduling the event."
        );

        // Ensuring global state
        Assertions.assertEquals(this.data.partyB1, party, "B1 is not merged");
        Assertions.assertTrue(this.scheduler.getState().contains(this.data.partyB1), "Dirty State (B1)");
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB2), "Dirty State (B2)");
        Assertions.assertFalse(this.data.partyB1.isDeleted(), "B1 tagged");
        Assertions.assertTrue(this.data.partyB2.isDeleted(), "B2 not tagged");

        // Ensuring data
        Assertions.assertEquals(3, party.getFirstEpisode(), "First episode mismatch");
        Assertions.assertEquals(5, party.getEpisodeCount(), "Episode count mismatch");
        Assertions.assertEquals(leftTime, party.getStartingAt(), "Starting datetime mismatch");
    }

    @Test
    @DisplayName("Scheduler | Delaying - Success")
    public void testDelayingSuccess() {

        ZonedDateTime time = this.data.partyA1.getStartingAt();
        List<TestWatchParty> parties = Assertions.assertDoesNotThrow(() -> this.scheduler.delay(
                TestData.BASE_DATETIME,
                Duration.ofMinutes(60),
                Duration.ofMinutes(60)
        ));

        Assertions.assertEquals(1, parties.size(), "Nothing has been delayed");
        Assertions.assertEquals(this.data.partyA1, parties.getFirst(), "Wrong item delayed");
        Assertions.assertEquals(time.plusHours(1), this.data.partyA1.getStartingAt(), "Delay duration not respected");
    }

    @Test
    @DisplayName("Scheduler | Delaying - Conflict")
    public void testDelayConflict() {

        Assertions.assertThrows(
                DelayOverlapException.class, () -> this.scheduler.delay(
                        TestData.BASE_DATETIME,
                        Duration.ofMinutes(60),
                        Duration.between(this.data.partyA1.getStartingAt(), this.data.partyB1.getStartingAt())
                )
        );
    }

    @Test
    @DisplayName("Scheduler | Calibration - After Scheduling")
    public void testCalibrationAfterScheduling() {

        // Shameless copy-paste from the first test, without the checks around
        ZonedDateTime                     scheduleAt = TestData.BASE_DATETIME.plusDays(1);
        ScheduleSpotData<TestWatchTarget> spot       = new TestSpot(this.data.target1, scheduleAt, 1);
        this.scheduler.schedule(spot);

        // Right now the state should be "dirty", as no auto-calibration is done.
        Assertions.assertEquals(3, this.data.partyB1.getFirstEpisode(), "State was not dirty before calibration");

        CalibrationResult res = Assertions.assertDoesNotThrow(() -> this.scheduler.calibrate());
        Assertions.assertEquals(2, res.updateCount(), "Unexpected count of updates");
        Assertions.assertEquals(0, res.deleteCount(), "Unexpected count of deletes");

        // Checking data
        Assertions.assertEquals(1, this.data.partyA1.getFirstEpisode());
        Assertions.assertEquals(4, this.data.partyB1.getFirstEpisode());
        Assertions.assertEquals(6, this.data.partyB2.getFirstEpisode());
    }

    @Test
    @DisplayName("Scheduler | Calibration - Raw")
    public void testCalibrationUpstreamRaw() {

        CalibrationResult res = Assertions.assertDoesNotThrow(() -> this.scheduler.calibrate());

        Assertions.assertEquals(0, res.updateCount(), "Unexpected count of updates");
        Assertions.assertEquals(0, res.deleteCount(), "Unexpected count of deletes");
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update Only")
    public void testCalibrationUpstreamUpdateOnly() {

        this.data.target1.setWatched(1);
        CalibrationResult res = Assertions.assertDoesNotThrow(() -> this.scheduler.calibrate());

        Assertions.assertEquals(3, res.updateCount(), "Unexpected count of updates");
        Assertions.assertEquals(0, res.deleteCount(), "Unexpected count of deletes");

        Assertions.assertEquals(2, this.data.partyA1.getFirstEpisode());
        Assertions.assertEquals(4, this.data.partyB1.getFirstEpisode());
        Assertions.assertEquals(6, this.data.partyB2.getFirstEpisode());
        Assertions.assertFalse(this.data.partyA1.isDeleted());
        Assertions.assertFalse(this.data.partyB1.isDeleted());
        Assertions.assertFalse(this.data.partyB2.isDeleted());
        Assertions.assertTrue(this.scheduler.getState().contains(this.data.partyA1));
        Assertions.assertTrue(this.scheduler.getState().contains(this.data.partyB1));
        Assertions.assertTrue(this.scheduler.getState().contains(this.data.partyB2));
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Delete Only")
    public void testCalibrationUpstreamDeleteOnly() {

        this.data.target1.setWatched(12);
        CalibrationResult res = Assertions.assertDoesNotThrow(() -> this.scheduler.calibrate());

        Assertions.assertEquals(0, res.updateCount(), "Unexpected count of updates");
        Assertions.assertEquals(3, res.deleteCount(), "Unexpected count of deletes");

        Assertions.assertTrue(this.data.partyA1.isDeleted());
        Assertions.assertTrue(this.data.partyB1.isDeleted());
        Assertions.assertTrue(this.data.partyB2.isDeleted());
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyA1));
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB1));
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB2));
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update + Delete")
    public void testCalibrationUpstreamUpdateDelete() {

        this.data.target1.setWatched(11);
        CalibrationResult res = Assertions.assertDoesNotThrow(() -> this.scheduler.calibrate());

        Assertions.assertEquals(1, res.updateCount(), "Unexpected count of updates");
        Assertions.assertEquals(2, res.deleteCount(), "Unexpected count of deletes");

        Assertions.assertEquals(12, this.data.partyA1.getFirstEpisode());

        Assertions.assertFalse(this.data.partyA1.isDeleted());
        Assertions.assertTrue(this.data.partyB1.isDeleted());
        Assertions.assertTrue(this.data.partyB2.isDeleted());
        Assertions.assertTrue(this.scheduler.getState().contains(this.data.partyA1));
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB1));
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB2));
    }

    @Test
    @DisplayName("Scheduler | Calibration - Upstream @ Update + Delete + Shrink")
    public void testCalibrationUpstreamUpdateDeleteShrink() {

        this.data.target1.setWatched(11);
        CalibrationResult res = Assertions.assertDoesNotThrow(() -> this.scheduler.calibrate());

        Assertions.assertEquals(1, res.updateCount(), "Unexpected count of updates");
        Assertions.assertEquals(2, res.deleteCount(), "Unexpected count of deletes");

        // Checking ALL
        Assertions.assertEquals(12, this.data.partyA1.getFirstEpisode());
        Assertions.assertEquals(1, this.data.partyA1.getEpisodeCount());

        Assertions.assertFalse(this.data.partyA1.isDeleted());
        Assertions.assertTrue(this.data.partyB1.isDeleted());
        Assertions.assertTrue(this.data.partyB2.isDeleted());
        Assertions.assertTrue(this.scheduler.getState().contains(this.data.partyA1));
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB1));
        Assertions.assertFalse(this.scheduler.getState().contains(this.data.partyB2));
    }

}
