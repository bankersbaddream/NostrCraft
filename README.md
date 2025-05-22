# NostrCraft

A Minecraft plugin that broadcasts server events to the Nostr decentralized social network protocol. Stay connected with your Minecraft community through the power of Nostr!

## Features

NostrCraft publishes the following server events to your configured Nostr relay:

- 🚀 **Server Start/Stop** - Know when your server comes online or goes offline
- 👋 **Player Join/Leave** - Track when players connect and disconnect
- 💬 **Chat Messages** - Share all player conversations with the Nostr network

## Requirements

- Minecraft Server running Paper 1.21.5+ (or compatible fork)
- Java 21+
- A Nostr keypair (private/public key)
- Access to a Nostr relay

## Installation

1. Download the latest release from the [Releases](../../releases) page
2. Place the `NostrCraft-1.0.jar` file in your server's `plugins/` directory
3. Start your server to generate the default configuration
4. Stop your server and configure your Nostr keys (see Configuration section)
5. Restart your server

## Configuration

Edit `plugins/NostrCraft/config.yml`:

```yaml
# NostrCraft Configuration
private-key: "your-64-character-hex-private-key"
public-key: "your-64-character-hex-public-key" 
relay: "wss://relay.damus.io"
```

### Generating Nostr Keys

You can generate Nostr keys using various tools:
- [Nostr Key Generator](https://nostr-keygen.com/)
- [Alby Browser Extension](https://getalby.com/)
- [Damus](https://damus.io/) mobile app
- Command line tools like `nak` or `nostril`

**Important**: Keep your private key secure and never share it publicly!

### Supported Relays

NostrCraft works with any standard Nostr relay. Popular options include:
- `wss://relay.damus.io`
- `wss://nos.lol`
- `wss://relay.nostr.band`
- `wss://nostr.wine`

## Building from Source

### Prerequisites
- Java 21+
- Gradle 8.14+

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/yourusername/nostrcraft.git
cd nostrcraft
```

2. Generate Gradle wrapper (if missing):
```bash
gradle wrapper
```

3. Build the plugin:
```bash
./gradlew build
```

The compiled JAR will be located in `build/libs/NostrCraft-1.0.jar`.

## Permissions

- `nostrcraft.command` - Allows usage of NostrCraft commands (default: op)

## Commands

- `/nostr` - Base command for NostrCraft plugin
- `/nostr info` - Display plugin information and connection status
- `/nostr send <message>` - Send a custom message to Nostr

## Event Format

All events published to Nostr follow this format:

### Server Events
```json
{
  "content": "Minecraft server started",
  "tags": [["e", "server_start"]]
}
```

### Player Events
```json
{
  "content": "Player Steve joined the server",
  "tags": [["p", "Steve"], ["e", "join"]]
}
```

### Chat Messages
```json
{
  "content": "[Steve]: Hello everyone!",
  "tags": [["p", "Steve"], ["e", "chat"]]
}
```

## Troubleshooting

### Common Issues

**Plugin fails to load:**
- Ensure you're running Paper 1.21.5+ or compatible
- Check that Java 21+ is installed
- Verify the JAR file is in the correct `plugins/` directory

**Can't connect to relay:**
- Check that the relay URL is correct and accessible
- Verify your internet connection allows WebSocket connections
- Try a different relay if the current one is down

**Events not appearing on Nostr:**
- Verify your private and public keys are correct 64-character hex strings
- Ensure the keys match (public key should be derived from private key)
- Check server logs for error messages

### Viewing Logs

Check your server console or `logs/latest.log` for NostrCraft messages:
- `INFO` messages show successful connections and events
- `SEVERE` messages indicate errors that need attention
- `FINE` messages show detailed debugging information

## Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Nostr Resources

- [Nostr Protocol Specification](https://github.com/nostr-protocol/nips)
- [Nostr Clients](https://www.nostr.net/)
- [Awesome Nostr](https://github.com/aljazceru/awesome-nostr)

## Support

For support, please:
1. Check the [Issues](../../issues) page for existing solutions
2. Create a new issue with detailed information about your problem
3. Join the discussion in our Nostr community

---

Built with ❤️ for the Minecraft and Nostr communities
