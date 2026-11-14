# CrowdOptimizer

Client-side Fabric mod for Minecraft 1.21.11.

## What it changes

CrowdOptimizer samples the client's FPS once per second and uses three quality tiers with hysteresis:

- **High** and **Balanced**: preserve the entity-shadow setting captured when joining the world.
- **Economy**: disables entity shadows.

A tier change requires three consecutive samples. Different thresholds prevent rapid switching.

When leaving a world or server, the mod restores the captured entity-shadow setting.

## Particle safety

CrowdOptimizer 0.1.2 does **not** change the vanilla particle setting. Critical-hit particles and every other particle effect remain governed by the player's own Minecraft setting.

Selective particle optimization is a separate planned feature: critical-hit particles must be explicitly preserved before any other particle types are considered for filtering.

## What it deliberately does NOT change

- Entity render distance or entity visibility.
- Mob/player models, textures, skins, or resource packs.
- Mob or player animation ticking.
- Name tags, player nametags, chat, or scoreboard rendering.
- Entity AI, server tick rate, or network behavior.
- Particle types, particle frequency, or particle appearance.

The mod does not hide mobs or players. This conservative version only changes entity shadows, so the FPS improvement may be modest. It cannot reduce the cost of drawing dozens of animated entities.

## Build

GitHub Actions builds the mod on every push to `main`. Download the `CrowdOptimizer-0.1.2` artifact from the completed workflow run.
