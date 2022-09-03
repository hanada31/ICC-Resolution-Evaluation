package com.fsck.k9.mail.store.imap;


import java.util.Collections;

import com.fsck.k9.mail.Flag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class UidSearchCommandBuilderTest {

    @Test
    public void build_withFullTextSearch() {
        String command = new UidSearchCommandBuilder()
                .performFullTextSearch(true)
                .requiredFlags(Collections.singleton(Flag.FLAGGED))
                .forbiddenFlags(Collections.singleton(Flag.DELETED))
                .queryString("query")
                .build();

        assertEquals("UID SEARCH TEXT \"query\" FLAGGED NOT DELETED", command);
    }

    @Test
    public void build_withoutFullTextSearch() {
        String command = new UidSearchCommandBuilder()
                .performFullTextSearch(false)
                .requiredFlags(null)
                .forbiddenFlags(Collections.singleton(Flag.DELETED))
                .queryString("query")
                .build();

        assertEquals("UID SEARCH OR SUBJECT \"query\" FROM \"query\" NOT DELETED", command);
    }
}
