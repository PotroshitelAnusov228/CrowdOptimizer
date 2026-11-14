# CrowdOptimizer

Client-side Fabric mod for Minecraft 1.21.11.

## What it changes

CrowdOptimizer samples the client's FPS once per second and uses three quality tiers with hysteresis:

- **High**: restores the particle and entity-shadow settings captured when joining the world.
- **Balanced**: keeps the user's shadow setting and reduces particles from All to Decreased (does not make an already-reduced setting worse).
- **Economy**: sets particles to Minimal and disables entity shadows.

A tier change requires three consecutive samples. Different thresholds prevent rapid switching.

When leaving a world or server, the mod restores the captured settings.

## What it deliberately does NOT change

- Entity render distance or entity visibility.
- Mob/player models, textures, skins, or resource packs.
- Mob or player animation ticking.
- Name tags, player nametags, chat, or scoreboard rendering.
- Entity AI, server tick rate, or network behavior.

The mod does not hide mobs or players. Its safe first version only adjusts particle density and entity shadows. This is intentionally conservative: it protects visual correctness, but may provide only a modest FPS improvement in a dense mob farm. It cannot reduce the cost of drawing dozens of animated entities.

## Build

GitHub Actions builds the mod on every push to `main`. Download the `CrowdOptimizer-0.1.1` artifact from the completed workflow run.
