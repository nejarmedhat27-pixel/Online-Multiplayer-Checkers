package org.example.onlinemultiplayercheckers.network;

import org.example.onlinemultiplayercheckers.model.Move;
import java.io.*;
import java.net.*;

public class NetworkManager {
    public static final int DEFAULT_PORT = 55201;
    public static final String PROTOCOL_MOVE = "MOVE:";
    public static final String PROTOCOL_CONFIRM = "BOARD_CONFIRM:";
    public static final String PROTOCOL_CHAT = "CHAT:";
    public static final String PROTOCOL_RESIGN = "RESIGN";
    public static final String PROTOCOL_DRAW_OFFER = "DRAW_OFFER";
    public static final String PROTOCOL_DRAW_RESPONSE = "DRAW_RESPONSE:";
    public static final String PROTOCOL_REMATCH = "REMATCH";
    public static final String PROTOCOL_PING = "PING";
    public static final String PROTOCOL_PONG = "PONG";

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isHost;
    private volatile boolean connected = false;
    private NetworkListener listener;

    public interface NetworkListener {
        void onMoveReceived(Move move);
        void onBoardConfirm(boolean ok);
        void onChatReceived(String message);
        void onOpponentResigned();
        void onDrawOffered();
        void onDrawResponse(boolean accepted);
        void onRematchOffered();
        void onConnectionLost();
        void onConnected();
    }

    public NetworkManager(NetworkListener listener) {
        this.listener = listener;
    }

    public void startServer(int port) throws IOException {
        isHost = true;
        serverSocket = new ServerSocket(port);
        new Thread(() -> {
            try {
                socket = serverSocket.accept();
                setupStreams();
                connected = true;
                listener.onConnected();
                listenLoop();
            } catch (IOException e) {
                if (!connected) listener.onConnectionLost();
            }
        }, "server-accept-thread").start();
    }

    public void connectToServer(String host, int port) throws IOException {
        isHost = false;
        socket = new Socket(host, port);
        setupStreams();
        connected = true;
        listener.onConnected();
        new Thread(this::listenLoop, "network-listener-thread").start();
    }

    private void setupStreams() throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    private void listenLoop() {
        try {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                dispatch(line.trim());
            }
        } catch (IOException e) {
            if (connected) listener.onConnectionLost();
        } finally {
            connected = false;
        }
    }

    private void dispatch(String packet) {
        try {
            if (packet.startsWith(PROTOCOL_MOVE)) {
                listener.onMoveReceived(Move.deserialize(packet.substring(PROTOCOL_MOVE.length())));
            } else if (packet.startsWith(PROTOCOL_CONFIRM)) {
                listener.onBoardConfirm("ok".equals(packet.substring(PROTOCOL_CONFIRM.length())));
            } else if (packet.startsWith(PROTOCOL_CHAT)) {
                listener.onChatReceived(packet.substring(PROTOCOL_CHAT.length()));
            } else if (PROTOCOL_RESIGN.equals(packet)) {
                listener.onOpponentResigned();
            } else if (PROTOCOL_DRAW_OFFER.equals(packet)) {
                listener.onDrawOffered();
            } else if (packet.startsWith(PROTOCOL_DRAW_RESPONSE)) {
                listener.onDrawResponse("accept".equals(packet.substring(PROTOCOL_DRAW_RESPONSE.length())));
            } else if (PROTOCOL_REMATCH.equals(packet)) {
                listener.onRematchOffered();
            } else if (PROTOCOL_PING.equals(packet)) {
                send(PROTOCOL_PONG);
            }
        } catch (Exception e) {
            System.err.println("Error dispatching: " + e.getMessage());
        }
    }

    public void sendMove(Move move) { send(PROTOCOL_MOVE + move.serialize()); }
    public void sendBoardConfirm(boolean ok) { send(PROTOCOL_CONFIRM + (ok ? "ok" : "mismatch")); }
    public void sendChat(String message) { send(PROTOCOL_CHAT + message); }
    public void sendResign() { send(PROTOCOL_RESIGN); }
    public void sendDrawOffer() { send(PROTOCOL_DRAW_OFFER); }
    public void sendDrawResponse(boolean accept) { send(PROTOCOL_DRAW_RESPONSE + (accept ? "accept" : "decline")); }
    public void sendRematch() { send(PROTOCOL_REMATCH); }

    private synchronized void send(String packet) {
        if (writer != null && connected) {
            writer.println(packet);
        }
    }

    public boolean isConnected() { return connected; }
    public boolean isHost() { return isHost; }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) { }
    }
}