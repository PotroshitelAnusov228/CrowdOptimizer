# CrowdOptimizer

A client-side Fabric mod for Minecraft 1.21.11. It monitors FPS and gradually adjusts a small set of visual settings when performance drops.

## Automatic quality tiers

- **High**: restores your original entity distance, entity shadows and particle setting.
- **Balanced**: caps entity distance scaling at 0.85; keeps your original shadows and particles.
- **Economy**: caps entity distance scaling at 0.65, disables entity shadows and switches particles to Minimal.

The mod samples FPS once per second and requires three consecutive samples before changing tiers. Separate up/down thresholds prevent rapid switching.

When you leave a world or server, the mod restores the settings captured when you joined. It does not change mob AI, server tick rate, or entity behavior. It does not intentionally remove nearby mobs, and it does not modify resource packs or mob textures.

## Build

GitHub Actions builds the mod on every push to main. Download the CrowdOptimizer-0.1.0 artifact from the completed workflow run.

## Notes

This is an initial test build. The settings it adjusts may provide only a modest FPS improvement in dense mob farms; entity-distance scaling mainly affects entities farther from the camera, while particle and shadow savings depend on what is visible.
