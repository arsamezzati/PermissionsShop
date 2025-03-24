# 🛒 PermissionsShop

A Minecraft plugin that allows players to purchase permissions, commands, and features using their in-game currency.

![License](https://img.shields.io/badge/license-MIT-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.16%2B-brightgreen)
![Bukkit](https://img.shields.io/badge/API-Bukkit-yellow)
![Vault](https://img.shields.io/badge/API-Vault-orange)
![PaperMC](https://img.shields.io/badge/API-PaperMC-pink)

## 📋 Table of Contents
- [Overview](#-overview)
- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Configuration](#-configuration)
    - [Main Configuration](#main-configuration)
    - [Shop Configuration](#shop-configuration)
    - [Messages Configuration](#messages-configuration)
- [Usage](#-usage)
    - [Player Commands](#player-commands)
    - [Admin Commands](#admin-commands)
- [Purchase Types](#-purchase-types)
- [Support](#-support)
- [License](#-license)

## 🔍 Overview

PermissionsShop is a dynamic plugin designed to create an in-game economy around permissions and commands. Server administrators can set up a shop where players can purchase temporary permissions, limited-use commands, permanent permissions, and more using their in-game currency.

## ✨ Features

- **Multiple Purchase Types**:
    - Timed Permissions - grant players permissions for a specific duration
    - Limited Commands - allow players to use specific commands a limited number of times
    - Permanent Permissions - give players permissions that never expire
    - One-Time Commands - execute commands once for players upon purchase
    - Home Slots - increase the number of homes a player can have

- **Economy Integration**:
    - Full Vault support for various economy plugins
    - Customizable prices for each item

- **Permission System**:
    - LuckPerms integration for reliable permission management
    - Fallback system for servers without LuckPerms

- **Database Storage**:
    - SQLite storage for tracking purchases
    - MySQL support planned for future versions

- **Admin Tools**:
    - Give or revoke purchased items to/from players
    - Check player purchase history
    - Reload configurations

## 📋 Requirements

- **Minecraft Server**: Bukkit/Spigot/Paper 1.16 or higher
- **Java**: Java 11 or higher (Java 21 recommended)
- **Dependencies**:
    - [Vault](https://www.spigotmc.org/resources/vault.34315/) - For economy support
    - An economy plugin that hooks into Vault (e.g., EssentialsX, CMI)
    - [LuckPerms](https://luckperms.net/) - Recommended for permission management

## 💾 Installation

1. Download the latest version of PermissionsShop
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using the configuration files in the `plugins/PermissionsShop` folder

## ⚙️ Configuration

### Main Configuration
Located at `config.yml`:

```yaml
# Check interval for timed permissions (in seconds)
check_interval: 30

# Storage settings
storage:
  # Storage type (sqlite or mysql)
  type: sqlite

  # MySQL settings (if using mysql)
  mysql:
    host: localhost
    port: 3306
    database: permissionshop
    username: root
    password: password
    options: '?useSSL=false&autoReconnect=true'
```

### Shop Configuration
Located at `shop.yml`:

```yaml
items:
  # Timed flight permission example
  fly:
    name: "Flight"
    description: "Allows you to fly for a limited time"
    price: 1000.0
    type: TIMED_PERMISSION
    duration: 3600  # 1 hour in seconds
    uses: 0  # Not used for timed permissions
    permission: "essentials.fly"
    command: ""  # Not used for timed permissions
    display_in_shop: true

  # Other item examples are included in the default configuration
```

### Messages Configuration
Located at `messages.yml`:

You can customize all plugin messages, including colors and formatting.

## 🎮 Usage

### Player Commands

- `/permshop` (aliases: `/pshop`) - View the permissions shop
- `/psbuy <itemId>` (aliases: `/permsbuy`) - Buy an item from the shop

### Admin Commands

- `/psadmin reload` - Reload plugin configurations
- `/psadmin give <player> <itemId>` - Give a player an item for free
- `/psadmin revoke <player> <itemId>` - Revoke an item from a player
- `/psadmin list <player>` - List a player's purchases

## 🛍️ Purchase Types

PermissionsShop supports five different purchase types:

1. **TIMED_PERMISSION**
    - Grants a permission for a limited time
    - Requires: permission, duration

2. **LIMITED_COMMAND**
    - Allows using a command a limited number of times
    - Requires: command, uses

3. **PERMANENT_PERMISSION**
    - Grants a permission permanently
    - Requires: permission

4. **ONE_TIME_COMMAND**
    - Executes a command once for the player
    - Requires: command


## 🆘 Support

If you encounter any issues or have questions about PermissionsShop:

- Create an issue on GitHub
- Join our [Discord server](https://discord.gg/zA4MG5whmp)
- Contact me directly on Discord @stanical
## 🔮 Updates Plan
- Adding an optional command to activate a custom [CommandsPanel](https://www.spigotmc.org/resources/commandpanels.67788/) menu for ease of use
- Adding a default increase max sethome feature
- Some cleanup
## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

© 2025 FusionsLab. All rights reserved.
