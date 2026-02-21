package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.noom.interview.fullstack.sleep.sleeplog.control.DuplicateSleepLogException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("unittest")
class SleepLogRepositoryTest {

    @Autowired
    private SleepLogRepository repository;

    @Test
    void save_persistsAndReturnsWithGeneratedFields() {
        var sleepLog = new SleepLog(
                null, 1L, LocalDate.of(2026, 2, 19),
                LocalDateTime.of(2026, 2, 18, 23, 30),
                LocalDateTime.of(2026, 2, 19, 7, 0),
                MorningFeeling.GOOD, null
        );

        var saved = repository.save(sleepLog);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.userId()).isEqualTo(1L);
        assertThat(saved.sleepDate()).isEqualTo(LocalDate.of(2026, 2, 19));
        assertThat(saved.bedTime()).isEqualTo(LocalDateTime.of(2026, 2, 18, 23, 30));
        assertThat(saved.wakeTime()).isEqualTo(LocalDateTime.of(2026, 2, 19, 7, 0));
        assertThat(saved.morningFeeling()).isEqualTo(MorningFeeling.GOOD);
        assertThat(saved.createdAt()).isNotNull();
    }

    @Test
    void save_duplicateUserAndDate_throwsDuplicateKeyException() {
        var date = LocalDate.of(2026, 3, 1);
        var sleepLog = new SleepLog(
                null, 2L, date,
                LocalDateTime.of(2026, 2, 28, 22, 0),
                LocalDateTime.of(2026, 3, 1, 6, 30),
                MorningFeeling.OK, null
        );

        repository.save(sleepLog);

        var duplicate = new SleepLog(
                null, 2L, date,
                LocalDateTime.of(2026, 2, 28, 23, 0),
                LocalDateTime.of(2026, 3, 1, 7, 0),
                MorningFeeling.BAD, null
        );

        assertThatThrownBy(() -> repository.save(duplicate))
                .isInstanceOf(DuplicateSleepLogException.class);
    }

    @Test
    void findByUserIdAndDate_existingLog_returnsLog() {
        LocalDate date = LocalDate.of(2026, 4, 1);
        SleepLog sleepLog = new SleepLog(
                null, 10L, date,
                LocalDateTime.of(2026, 3, 31, 23, 0),
                LocalDateTime.of(2026, 4, 1, 7, 0),
                MorningFeeling.GOOD, null
        );
        repository.save(sleepLog);

        Optional<SleepLog> found = repository.findByUserIdAndDate(10L, date);

        assertThat(found).isPresent();
        assertThat(found.get().userId()).isEqualTo(10L);
        assertThat(found.get().sleepDate()).isEqualTo(date);
    }

    @Test
    void findByUserIdAndDate_noLog_returnsEmpty() {
        Optional<SleepLog> found = repository.findByUserIdAndDate(999L, LocalDate.of(2026, 1, 1));

        assertThat(found).isEmpty();
    }
}
