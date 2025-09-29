<div align="center">

# Aether (Client for Abyss)

[![Plugin Version](https://img.shields.io/badge/Alpha-v0.1-red.svg?style=for-the-badge&color=76bad9)](https://github.com/rootacite/Aether)

_🚀This is the client of the multimedia server Abyss, which can also be extended to other purposes🚀_

<img src="aether_clip.png" width="25%" alt="Logo">
</div>

## 🎯 Target

The ultimate goal of this software project is to enable anyone to easily build a smooth media library that they can fully manage and control, 
contribute to with trusted individuals, and securely access from any location without worrying about unauthorized use of their data by third parties. 
Undoubtedly, this is a distant goal, but in any case, 
I hope this project can make a modest contribution to the advancement of cybersecurity and the protection of user privacy.

## Key Features

- **Media Management**: Organize and serve images, videos, and live streams with structured directory support.
- **User Authentication**: Challenge-response authentication using **Ed25519** signatures. No private keys are ever transmitted.
- **Role-Based Access Control (RBAC)**: Hierarchical user privileges with configurable permissions for resources.
- **Secure Proxy**: Built-in HTTP/S proxy with end-to-end encrypted tunneling using **X25519** key exchange and **ChaCha20-Poly1305** encryption.
- **Resource-Level Permissions**: Fine-grained control over files and directories using a SQLite-based attribute system.
- **Task System**: Support for background tasks such as media uploads and processing with chunk-based integrity validation.
- **RESTful API**: Fully documented API endpoints for media access, user management, and task control.

## Technology Stack

- **Backend**: ASP.NET Core 9, MVC, Dependency Injection
- **Database**: SQLite with async ORM support
- **Cryptography**: NSec.Cryptography (Ed25519, X25519), ChaCha20Poly1305
- **Media Handling**: Range requests, MIME type detection, chunked uploads
- **Security**: Rate limiting, IP binding, token expiration, secure headers

## Development background

- Operating System: Voidraw OS v1.1 (based on Ubuntu) or any compatible Linux distribution.
- IDE: Android Studio 2025.1
- Language: Kotlin
- Framework: Jetpack Compose

## TODO List

### High Priority
- [x] Fix tablet full-screen mode bug
- [x] Hide private key after user input
- [x] Optimize API call logic, do not create crashes
- [x] Fix the issue of freezing when entering the client without configuring the private key
- [x] Replace Android robot icon with custom design
- [x] Configure server baseURL in client settings
- [ ] Implement proper access control for directory queries

### Medium Priority
- [x] Increase minHeight for video playback
- [x] Add top bar with title and back button in full-screen mode
- [x] Optimize data transfer system
- [x] Improve manga/comic page display

### Future
- [ ] (Prospective) Implement search functionality