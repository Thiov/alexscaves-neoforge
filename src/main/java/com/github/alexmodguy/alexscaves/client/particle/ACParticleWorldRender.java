package com.github.alexmodguy.alexscaves.client.particle;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Registry of live {@link RenderInWorldParticle}s. {@code LevelRendererMixin} iterates this set each frame and
 * draws each particle's geometry through the {@code SubmitNodeBufferSource} bridge — the 26.1 replacement for
 * the removed custom-particle render path.
 */
public final class ACParticleWorldRender {
    private static final Set<RenderInWorldParticle> ACTIVE = Collections.newSetFromMap(new IdentityHashMap<>());

    private ACParticleWorldRender() {
    }

    public static void add(RenderInWorldParticle particle) {
        ACTIVE.add(particle);
    }

    public static void remove(RenderInWorldParticle particle) {
        ACTIVE.remove(particle);
    }

    public static Collection<RenderInWorldParticle> active() {
        return ACTIVE;
    }
}
