# Online-Multiplayer-Checkers
Real-time online multiplayer checkers in Java &amp; JavaFX. Full rule enforcement — mandatory jumps, multi-jump chains &amp; king promotion · TCP socket networking · Multithreaded move-validation · Animated JavaFX UI · SQLite game history · Replay &amp; analysis mode · 10×10 International Draughts support.
# ♟️ CheckersOnline — Real-Time Multiplayer Checkers

> **A full-featured real-time online multiplayer checkers game built in Java — with full rule enforcement, animated JavaFX UI, multithreaded networking, SQLite game history, and a replay & analysis mode.**

---

## 📸 Overview

**CheckersOnline** is a cross-platform two-player checkers game played over a TCP socket connection. Every move is validated server-side on a dedicated thread — enforcing mandatory jumps, multi-jump chains, and king promotion — without ever blocking the UI. Game history is stored in SQLite and players can replay any past game with optimal jump-sequence analysis.

---

## ⚡ Core Features

| Module | Details |
|---|---|
| 🌐 **Networking** | TCP socket pair — move coordinates + board-state confirmation packets |
| 🧵 **Threads** | Move-validation thread; dedicated network-listener thread |
| 🎨 **JavaFX UI** | Animated piece movement · jump-chain highlight · captured-piece counter |
| 🗄️ **SQLite Database** | Stores `WinLossRecord`, `AverageGameLength`, `ReplayMoves` |
| ♟️ **Full Rule Engine** | Mandatory jumps · multi-jump chains · king promotion |
| 🔁 **Replay & Analysis** | Review any past game · highlights optimal jump sequences |
| 🌍 **Extras** | International draughts variant (10×10) · optional hint engine |

---

## 🚀 Getting Started

### Prerequisites

- Java **17+**
- JavaFX SDK — [Download here](https://openjfx.io/)
- SQLite JDBC driver — included in `lib/`

### Run the Server

```bash
javac --module-path $PATH_TO_FX --add-modules javafx.controls -cp lib/* src/*.java
java --module-path $PATH_TO_FX --add-modules javafx.controls -cp lib/*:src CheckersServer
```

### Run the Client (both players)

```bash
java --module-path $PATH_TO_FX --add-modules javafx.controls -cp lib/*:src CheckersClient
```

> Both players launch the client independently. Player 1 hosts; Player 2 connects via IP.

---

## 🗺️ How It Works

### 1 — Connection Setup
Player 1 starts the server; Player 2 connects via TCP socket. A dedicated **network-listener thread** on each client handles all incoming packets non-blocking, keeping the JavaFX UI fully responsive.

### 2 — Move Flow
```
Player clicks piece → Client sends move coords packet
→ Server move-validation thread checks rules
→ Server sends board-state confirmation packet
→ Both clients animate the move via JavaFX Timeline
```

### 3 — Rule Enforcement
The validation thread enforces:
- **Mandatory jumps** — player must capture if a jump is available
- **Multi-jump chains** — piece continues jumping until no capture is available
- **King promotion** — piece reaching the back row becomes a king
- **Illegal move rejection** — invalid moves are refused without UI stall

### 4 — Game Storage
On game end, SQLite records:

```sql
WinLossRecord     -- Player win/loss history
AverageGameLength -- Average moves per completed game
ReplayMoves       -- Full move-by-move sequence for replay
```

### 5 — Replay & Analysis
Load any past game from the database → step through moves → highlighted optimal jump sequences shown at each state.

---

## 🎨 JavaFX UI Details

- **Animated piece movement** — smooth `TranslateTransition` on every move
- **Jump-chain highlight** — golden glow on valid multi-jump paths
- **Captured-piece counter** — live sidebar showing captures per player
- **King promotion effect** — crown icon animates onto promoted pieces
- **10×10 board variant** — full International Draughts support

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17+ |
| **GUI & Animation** | JavaFX (FXML + CSS + Timeline) |
| **Networking** | `java.net.Socket` / `ServerSocket` — TCP |
| **Concurrency** | `Thread` — validation thread + listener thread |
| **Database** | SQLite via JDBC |
| **Build** | Javac / Maven (optional) |

---

## 📁 Project Structure

```
checkers-online/
│
├── src/
│   ├── CheckersServer.java       # TCP server + move-validation thread
│   ├── CheckersClient.java       # JavaFX client + network-listener thread
│   ├── Board.java                # Board model + rule engine
│   ├── Piece.java                # Piece model (man / king)
│   ├── MoveValidator.java        # Mandatory jump, multi-jump, king logic
│   ├── GameDatabase.java         # SQLite JDBC — save/load game records
│   ├── ReplayController.java     # Replay & analysis mode
│   └── ui/
│       ├── BoardView.fxml        # JavaFX board layout
│       └── styles.css            # Board & piece styling
│
├── lib/
│   └── sqlite-jdbc-*.jar         # SQLite driver
│
└── README.md
```

---

## 📌 Roadmap

- [ ] **Spectator Mode** — watch live games as observer
- [ ] **ELO Rating System** — competitive rank tracking
- [ ] **Lobby / Matchmaking** — auto-pair players by rating
- [ ] **AI Opponent** — Minimax + Alpha-Beta pruning bot
- [ ] **Chat System** — in-game text messaging
- [ ] **Tournament Bracket** — multi-player elimination mode
- [ ] **Mobile Port** — Android client via JavaFX Mobile
- [ ] **Undo Request** — player can request take-back (opponent must accept)

---

## ⚙️ Game Rules Enforced

| Rule | Behavior |
|---|---|
| **Mandatory Jump** | Player must capture if any jump is available |
| **Multi-Jump Chain** | Capturing piece continues until no capture possible |
| **King Promotion** | Piece reaching opponent's back row becomes a king |
| **King Movement** | Kings move & jump both forward and backward |
| **Draw Condition** | No capture or king move in 40 moves → draw |

---

## 🤝 Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit: `git commit -m "Add: your feature"`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

MIT License — free to use, modify, and distribute.

---

## 👨‍💻 Author

Built with ❤️ in Java & JavaFX.  
If this project helped you, consider giving it a ⭐ on GitHub!
