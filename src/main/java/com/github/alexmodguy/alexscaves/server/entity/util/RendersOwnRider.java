package com.github.alexmodguy.alexscaves.server.entity.util;

/**
 * Marker for mounts that draw their perched passenger themselves via a rider render layer
 * (Atlatitan back, Subterranodon grip, Candicorn saddle). The client suppresses the normal
 * world-pass render of such a passenger so it is drawn ONCE at the perch instead of also
 * floating at its raw positionRider anchor.
 */
public interface RendersOwnRider {
}
