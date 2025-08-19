package com.minhyung.schedule.testsupport;

import com.minhyung.schedule.security.principal.UserPrincipal;

public final class TestPrincipalBuilder {
    private Long sub = 1L;
    private boolean verified = true;

    private TestPrincipalBuilder() {}

    public static TestPrincipalBuilder principal() {
        return new TestPrincipalBuilder();
    }

    public TestPrincipalBuilder withSub(Long sub) {
        this.sub = sub;
        return this;
    }

    public TestPrincipalBuilder withVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public UserPrincipal build() {
        return new UserPrincipal(sub, verified);
    }
}
