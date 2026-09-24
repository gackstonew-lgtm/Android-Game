package com.gackstone.chase.camera;

/**
 * Enumeration of all supported camera viewpoints in Chase.
 *
 * <ul>
 *   <li>{@link #CHASE} – Classic third-person trailing camera (default).
 *   <li>{@link #HOOD}  – Low-slung hood-mounted camera just above the bonnet.
 *   <li>{@link #COCKPIT} – First-person interior camera with dashboard visible.
 * </ul>
 */
public enum CameraMode {
    CHASE,
    HOOD,
    COCKPIT
}
