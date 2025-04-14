# StreakManager

A Minecraft plugin that manages player streaks with configurable rewards. Keep your players engaged with daily rewards and milestone achievements!

## Features

- Track player login streaks
- Configurable rewards for streak milestones
- Customizable messages
- PlaceholderAPI support
- Automatic data saving and backup system
- Streak management commands for administrators

## Requirements

- Paper/Spigot 1.20.4+
- Java 8 or higher
- PlaceholderAPI (optional, for placeholders)

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/StreakManager/config.yml`

## Configuration

The plugin uses a main configuration file (`config.yml`) for customizing messages, rewards, and settings.

### Messages

All messages in the plugin are customizable. Color codes using `&` are supported.

```yaml
messages:
  no-permission: "&cYou don't have permission to use this command!"
  player-not-found: "&cPlayer not found!"
  invalid-amount: "&cInvalid amount! Must be a number."
  config-reloaded: "&aConfiguration reloaded!"
  streak:
    added: "&aAdded %amount% streak to %player%!"
    received: "&aYour streak has been increased by %amount%!"
    reset: "&cYour streak has been reset!"
    reset-admin: "&aReset streak for %player%!"
    set: "&aSet streak for %player% to %amount%!"
    set-received: "&aYour streak has been set to %amount%!"
    milestone: "&aYou've reached a streak of %amount%!"
```

### Rewards

Configure rewards for different streak milestones. Use `%player%` as a placeholder for the player's name.

```yaml
rewards:
  5:
    - 'give %player% diamond 1'
    - 'broadcast %player% has reached a 5-day streak!'
  10:
    - 'give %player% diamond 5'
    - 'broadcast %player% has reached a 10-day streak! Amazing!'
```

### Settings

```yaml
settings:
  # Auto-save interval in minutes
  save-interval: 5
  # Save player data when they quit
  save-on-quit: true
  # Create backup before saving
  backup-before-save: true
  # Maximum number of backup files to keep
  max-backups: 5
```

## Commands

### Player Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/streak` | streakmanager.use | View your current streak |

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/addstreak <player> [amount]` | streakmanager.add | Add to a player's streak |
| `/streakadmin reload` | streakmanager.admin | Reload the configuration |
| `/streakadmin reset <player>` | streakmanager.admin | Reset a player's streak |
| `/streakadmin set <player> <amount>` | streakmanager.admin | Set a player's streak |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| streakmanager.use | Allows using basic streak commands | true |
| streakmanager.add | Allows adding to player streaks | op |
| streakmanager.admin | Allows access to admin commands | op |

## PlaceholderAPI Integration

The following placeholders are available when PlaceholderAPI is installed:

| Placeholder | Description |
|-------------|-------------|
| %streak_count% | Shows the player's current streak count |

## Data Storage

Player data is stored in `plugins/StreakManager/data.yml`. The plugin includes an automatic backup system that:

- Creates backups before saving data
- Maintains a configurable number of backup files
- Automatically cleans up old backups
- Saves data on player quit (configurable)

Backups are stored in `plugins/StreakManager/backups/` with timestamps in the filename.

## Building

To build the plugin from source:

1. Clone the repository
2. Run `mvn clean package`
3. Find the built jar in the `target` folder

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you encounter any issues or have questions:

1. Check the configuration guide above
2. Verify your server meets the requirements
3. Create an issue on the GitHub repository

## Author

Mario Enache