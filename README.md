**English** | [简体中文](README_ZH.md)

# Ashlands

Once you create a world with the **Ashlands** world type, the whole world becomes irradiated: the 3×3 chunks
around the spawn point (9 chunks in total) form the only safe zone. Crossing the safe zone boundary inflicts
an infinite-duration radiation effect, and radiation cannot be removed by any means.

## Core Mechanics

### World Type
- When creating a new world, choose **Ashlands** in the world type list.
- These mechanics only work in an Ashlands world; in a normal world the mod does not interfere at all.

### Safe Zone and Light Curtain
- The safe zone is the chunk the spawn point is in plus one ring around it — 3×3 = 9 chunks. The radius is
  configurable (`safeRadiusChunks`).
- A light curtain spanning the entire world height (from the world's minimum build height to its maximum)
  separates the safe zone from the outside.

### Purifier
- Right-click a purifier to turn **the single chunk the block is in** into a safe zone: radiation inside that
  chunk disappears and the light curtain redraws itself around it.
- It can only be used right against the outer edge of the safe zone: the chunk must share an edge
  (north, south, east or west) with an existing safe chunk. Otherwise right-clicking only shows
  "Can only be used right outside the safe zone". This lets the safe zone grow ring by ring instead of
  opening a safe chunk somewhere else in the world out of thin air.
- The purifier is **single-use**: after a successful purification the block disappears and drops nothing.
- The recipe is expensive: a Nether Star + 4 Netherite Ingots + 4 Echo Shards (`NEN / ESE / NEN`, where `S`
  is the Nether Star).
- Purified chunks are stored in the save data (`data/ashlands_purified_chunks.dat`) and remain valid after
  re-entering the world or restarting the server.

### Radiation
Crossing the curtain immediately grants the **Radiation** effect with an infinite duration. The effect gains
one level every 10 minutes of infection, up to level 3:

- **Level 1**: constantly drains hunger and saturation, and deals half a heart of damage every 60 seconds.
- **Level 2**: deals 1 heart of damage every 30 seconds and drains a large amount of hunger and saturation.
- **Level 3**: instantly empties hunger and saturation, after which food can no longer restore them; deals
  2 hearts of damage every 10 seconds.

All of these values can be tuned per level in the `[radiationLevel1]` / `[radiationLevel2]` / `[radiationLevel3]`
sections of the config file.

### Radiation Atmosphere
In an Ashlands world the sky is always tinted by radiation — inside the safe zone as well:

- **Sky**: a dark-green gradient from the horizon to the zenith. The vanilla sun, moon, stars and clouds are
  not rendered at all, so the sky looks the same inside and outside the safe zone.
- **Radiation fog**: the fog line follows "how far you can look along your view direction before leaving the
  safe zone". Looking out through the curtain, the fog starts right at the curtain and the wasteland beyond it
  is grey-green; looking back into the safe zone, the fog line is pushed out of your view and nothing inside
  is fogged. Once you step outside the curtain the fog line closes in further, leaving only a few dozen blocks
  of visibility.
- Fog starts fading in 0.5 blocks past the curtain and is fully active after about 2.5 blocks, so the boundary
  never turns into a sudden wall of fog.
- The safe zone size follows both the config and the purifier: chunks added by purifiers count as safe zone too,
  and the fog moves out of the way accordingly.
- **Rain and snow**: while raining or snowing, both the rain streaks and the snowflakes are tinted the
  radiation yellow-green, inside the safe zone and outside the curtain alike. Rain uses its own shader — the
  vanilla rain texture is dark blue, so multiplying it by a colour only ever makes it darker. Instead the
  colour is assigned directly and only the silhouette of the vanilla rain texture is reused. The vanilla
  particle shader is handed back as soon as the rain has been drawn, so other mods are unaffected.
- **Radiation rain**: standing in rain or snow continuously applies level 1 Poison, and each check also has a
  5% chance to inflict level 1 Radiation directly (inside the safe zone too).
- **Water outside the safe zone**: soaking in water outside the curtain also applies continuous Poison.

Notes:

- Radiation cannot be removed with milk, honey, a totem or any other method.
- If the effect is forcibly cleared, the mod re-applies it at the next check.
- Radiation only affects players: non-player entities never receive it, and if one somehow does (for example
  via a command or another mod) it is removed immediately.

### Dead Forests
- Leaves (`#minecraft:leaves`) in **every chunk** outside the safe zone are removed, leaving only bare trunks.
  The only living parts of the world are the 9 chunks around the spawn point (plus any chunks lit up by
  purifiers).

### Strengthened Monsters
- Hostile mobs spawned in an Ashlands world are strengthened: +100% health, +50% attack damage, +15% movement
  speed and +4 armor.
- Hostile mobs do not spawn inside the safe zone: natural spawns, spawners, structure spawns and patrols are
  all blocked.

### Damage Type
- Radiation damage uses its own damage type `ashlands:radiation`, which ignores armor and enchantments.

## Configuration

Config file: `config/ashlands-common.toml`

- `safeRadiusChunks`: safe zone radius in chunks, default `1`. `0` = only the spawn chunk, `1` = 3×3 = 9 chunks,
  `2` = 5×5 … up to `16` (33×33). The light curtain position, the radiation boundary and the leaf-preserved area
  all follow this value.
- `ticksPerRadiationLevel`: game ticks required for radiation to gain one level, default `12000`
  (20 ticks = 1 second, 12000 ticks = 10 minutes). Maximum level is 3.
- `strengthenMonsters`: whether to strengthen monsters in Ashlands worlds, default `true`.
- `stripTreesOutsideSafeZone`: whether to remove leaves and tree decorations in every chunk outside the safe
  zone, default `true`.
- `radiationDamage`: master switch for radiation damage, default `true` (when disabled, radiation only drains
  hunger and saturation).
- `noHostileSpawnsInSafeZone`: whether to prevent hostile mobs from spawning inside the safe zone, default `true`.

The per-level values live in the three sections below: `[radiationLevel1]` / `[radiationLevel2]` / `[radiationLevel3]`

- `damage`: damage per hit, where `2` points = 1 heart. Defaults: level 1 `1.0` (half a heart), level 2 `2.0`
  (1 heart), level 3 `4.0` (2 hearts).
- `damageIntervalTicks`: ticks between two damage hits. Defaults: level 1 `1200` (60 s), level 2 `600` (30 s),
  level 3 `200` (10 s).
- `foodDrain`: hunger points drained per hit. Defaults: level 1 `1`, level 2 `3`.
- `saturationDrain`: saturation drained per hit. Defaults: level 1 `1.0`, level 2 `6.0`.
- `foodIntervalTicks`: ticks between two hunger/saturation drains. Default `200` (10 s) for both levels.
- `locksFood` (level 3 only): whether hunger and saturation are forced to zero every tick, default `true`.

All intervals are expressed in game ticks (20 ticks = 1 second), so they match in-game durations exactly and
never produce repeating decimals.

Setting a value to `0` effectively disables that entry (for example `damage = 0.0` means that level deals no
damage, and `foodDrain = 0` means no hunger drain).

Changes take effect after re-entering the world (or restarting the server). At startup the log prints a line like
`Ashlands safe zone: radius N chunk(s) = (2N+1)x(2N+1) chunks, centered on chunk (x, z)`, which you can use to
confirm the config took effect. Leaves already removed in generated chunks do not come back when the safe zone
is enlarged.
