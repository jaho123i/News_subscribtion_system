import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;

public class Server {

    ArrayList<String> topics = new ArrayList<>();
    HashMap<SocketChannel, HashSet<String>> clientsTopics = new HashMap<>();
    Selector selector;

    public static void main(String[] args) throws IOException, InterruptedException {
        new Server();
    }

    Server() throws IOException {
        topics.add("Sport");
        topics.add("Politics");
        topics.add("Funny");

        String host = "localhost";
        int port = 12345;
        ServerSocketChannel serverSCh = ServerSocketChannel.open();
        serverSCh.socket().bind(new InetSocketAddress(host, port));

        serverSCh.configureBlocking(false);
        selector = Selector.open();
        serverSCh.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server: waiting ... ");

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {
                    System.out.println("Server: someone connected ..., accepting ... ");
                    SocketChannel sCh = serverSCh.accept();
                    sCh.configureBlocking(false);
                    sCh.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    continue;
                }

                if (key.isReadable()) {
                    SocketChannel sCh = (SocketChannel) key.channel();
                    serviceRequest(sCh);
                    continue;
                }

                if (key.isWritable()) {
                    continue;
                }
            }
        }
    }

    private static Charset charset = Charset.forName("ISO-8859-2");
    private static final int BSIZE = 1024;
    private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);
    private StringBuffer reqString = new StringBuffer();

    private void serviceRequest(SocketChannel sc) {
        if (!sc.isOpen()) return;

        System.out.print("Server: reading message from client ... ");

        reqString.setLength(0);
        bbuf.clear();

        try {
            readLoop:
            while (true) {
                int n = sc.read(bbuf);
                if (n == -1) {
                    sc.close();
                    sc.socket().close();
                    return;
                }
                if (n == 0) break;

                bbuf.flip();
                CharBuffer cbuf = charset.decode(bbuf);
                while (cbuf.hasRemaining()) {
                    char c = cbuf.get();
                    if (c == '\r' || c == '\n') break readLoop;
                    else {
                        reqString.append(c);
                    }
                }
                bbuf.clear();
            }

            if (reqString.length() == 0) return;

            String cmd = reqString.toString();
            System.out.println(reqString);

            if (cmd.startsWith("New client")) {
                clientsTopics.put(sc, new HashSet<>());
                System.out.println();
                System.out.println(sc.getRemoteAddress());
                sc.write(charset.encode(CharBuffer.wrap("Client added")));
            } 
            else if (cmd.equals("Show topics")) {
                String msg = "Ok, choose topic: ";
                for (String topic : topics) {
                    msg += topic + ", ";
                }
                sc.write(charset.encode(CharBuffer.wrap(msg)));
            } 
            else if (cmd.equals("Hello")) {
                sc.write(charset.encode(CharBuffer.wrap("Hello")));
            } 
            else if (cmd.startsWith("Subscribe ")) {
                String topic = cmd.split(" ")[1];
                if (topics.contains(topic)) {
                    clientsTopics.get(sc).add(topic);
                    sc.write(charset.encode(CharBuffer.wrap("Ok, added")));
                    System.out.println("I sent: \"Ok, added\"");
                } else {
                    sc.write(charset.encode(CharBuffer.wrap("No such topic")));
                    System.out.println("I sent: \"No such topic\"");
                }
            } 
            else if (cmd.startsWith("Unsubscribe ")) {
                String topic = cmd.split(" ")[1];
                if (clientsTopics.get(sc).remove(topic)) {
                    sc.write(charset.encode(CharBuffer.wrap("Ok, deleted")));
                    System.out.println("I sent: \"Ok, deleted\"");
                } else {
                    sc.write(charset.encode(CharBuffer.wrap("No such topic subscribed")));
                    System.out.println("I sent: \"No such topic subscribed\"");
                }
            } 
            else if (cmd.startsWith("Add topic ")) {
                topics.add(cmd.split(" ")[2]);
                sc.write(charset.encode(CharBuffer.wrap("Ok, added (or in base)")));
                System.out.println("I sent: \"Ok, added (or in base)\"");
            } 
            else if (cmd.startsWith("Remove topic ")) {
                topics.remove(cmd.split(" ")[2]);
                sc.write(charset.encode(CharBuffer.wrap("Ok, removed (or not in base)")));
                System.out.println("I sent: \"Ok, removed (or not in base)\"");
            } 
            else if (cmd.startsWith("News to ")) {
                String targetTopic = cmd.split(" ")[2];
                sc.write(charset.encode(CharBuffer.wrap("News sent")));
                System.out.println("I sent: \"News sent\"");
                
                for (SelectionKey key : selector.keys()) {
                    if (key.isValid() && key.channel() instanceof SocketChannel) {
                        SocketChannel socketCh = (SocketChannel) key.channel();
                        HashSet<String> clientTopics = clientsTopics.get(socketCh);
                        if (clientTopics != null && clientTopics.contains(targetTopic)) {
                            socketCh.write(charset.encode(CharBuffer.wrap(cmd)));
                            System.out.println("I sent news: \"" + cmd + "\"");
                        }
                    }
                }
            } 
            else if (cmd.equals("Bye")) {
                sc.write(charset.encode(CharBuffer.wrap("Bye")));
                System.out.println("Server: saying \"Bye\" to client ...\n\n");
                sc.close();
                sc.socket().close();
            } 
            else {
                sc.write(charset.encode(CharBuffer.wrap("I don't understand: " + reqString)));
            }

        } catch (Exception exc) {
            exc.printStackTrace();
            try {
                sc.close();
                sc.socket().close();
            } catch (Exception e) {
            }
        }
    }
}