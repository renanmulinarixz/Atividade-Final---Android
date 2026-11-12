package br.com.pokespawn;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class EncounterTest {
    @Test public void expiresAtExactlyFifteenSeconds() {
        Encounter encounter = new Encounter(); encounter.show(25, 1000);
        assertEquals(15, encounter.seconds(1000));
        assertTrue(encounter.canCapture(15999));
        assertFalse(encounter.canCapture(16000));
        assertTrue(encounter.expired(16000));
        assertEquals(0, encounter.seconds(16000));
    }
    @Test public void rapidRepeatedClicksOnlyCaptureOnce() {
        Encounter encounter = new Encounter(); encounter.show(252, 0);
        assertTrue(encounter.capture(100));
        assertFalse(encounter.capture(101));
        assertFalse(encounter.capture(2000));
        assertTrue(encounter.captured());
    }
    @Test public void newEncounterCanCaptureSameSpeciesAgain() {
        Encounter encounter = new Encounter(); encounter.show(25, 0); encounter.capture(100);
        encounter.show(25, 15000);
        assertTrue(encounter.capture(15100));
    }
    @Test public void restoringAfterRotationDoesNotDuplicateCapture() {
        Encounter restored = new Encounter(); restored.restore(25, 15000, true);
        assertEquals(25, restored.id()); assertFalse(restored.capture(4000));
        assertEquals(11, restored.seconds(4000));
    }
    @Test public void regionDrawsNeverRepeatAndStayInsideRegion() {
        Random random = new Random(42);
        for (int region = 0; region < Regions.NAMES.length; region++) {
            int previous = Regions.first(region);
            for (int attempt = 0; attempt < 5000; attempt++) {
                int id = Encounter.nextId(random, region, previous);
                assertTrue(id >= Regions.first(region)); assertTrue(id <= Regions.last(region));
                assertNotEquals(previous, id); previous = id;
            }
        }
    }
    @Test public void regionRangesCoverWholeNationalDexWithoutGaps() {
        int sum = 0;
        for (int region = 1; region < Regions.NAMES.length; region++) {
            sum += Regions.size(region);
            assertEquals(region, Regions.of(Regions.first(region)));
            assertEquals(region, Regions.of(Regions.last(region)));
            if (region > 1) assertEquals(Regions.last(region - 1) + 1, Regions.first(region));
        }
        assertEquals(1025, sum);
    }
}
