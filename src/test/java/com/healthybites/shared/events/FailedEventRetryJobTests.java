package com.healthybites.shared.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;

class FailedEventRetryJobTests {

    private final FailedEventPublications failed = mock(FailedEventPublications.class);

    @Test
    void resubmitsFailuresThatAreOldEnoughAndHaveAttemptsLeft() {
        new FailedEventRetryJob(failed).resubmit();

        ArgumentCaptor<ResubmissionOptions> options = ArgumentCaptor.forClass(ResubmissionOptions.class);
        verify(failed).resubmit(options.capture());
        assertThat(options.getValue().getMinAge()).isEqualTo(FailedEventRetryJob.MIN_AGE);
        assertThat(options.getValue().getFilter())
                .accepts(publicationWithAttempts(1), publicationWithAttempts(FailedEventRetryJob.MAX_ATTEMPTS - 1))
                .rejects(publicationWithAttempts(FailedEventRetryJob.MAX_ATTEMPTS));
    }

    private static EventPublication publicationWithAttempts(int attempts) {
        EventPublication publication = mock(EventPublication.class);
        when(publication.getCompletionAttempts()).thenReturn(attempts);
        return publication;
    }
}
