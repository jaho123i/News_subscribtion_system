package zad1;


import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
    HashMap<SocketAddress, HashSet<String>> clientsTopics = new HashMap<SocketAddress, HashSet<String>>();
    Selector sele;

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

        sele = Selector.open();

        serverSCh.register(sele, SelectionKey.OP_ACCEPT);

        System.out.println("Serwer: czekam ... ");

        while (true) {
            sele.select();

            Set<SelectionKey> keys = sele.selectedKeys();

            Iterator<SelectionKey> iter = keys.iterator();

            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                iter.remove();

                if (key.isAcceptable()) {
                    System.out.println("Serwer: ktoś się połączył ..., akceptuję go ... ");
                    SocketChannel sCh = serverSCh.accept();

                    sCh.configureBlocking(false);

                    sCh.register(sele, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

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

        System.out.print("Serwer: czytam komunikat od klienta ... ");

        reqString.setLength(0);
        bbuf.clear();

        try {
            readLoop:
            // Pokazuje gdzie ma wychodzić break
            while (true) {
                int n = sc.read(bbuf);
                if (n > 0) {
                    bbuf.flip();
                    CharBuffer cbuf = charset.decode(bbuf);
                    while (cbuf.hasRemaining()) {
                        char c = cbuf.get();
                        System.out.println(c);
                        if (c == '\r' || c == '\n') break readLoop; // <-
                        else {
                            //System.out.println(c);
                            reqString.append(c);
                        }
                    }
                }
            }

            String cmd = reqString.toString();
            System.out.println(reqString);

            if (cmd.startsWith("New client")) {
                clientsTopics.put(sc.getLocalAddress(), new HashSet<String>());
                System.out.println();
                System.out.println(sc.getLocalAddress());
                sc.write(charset.encode(CharBuffer.wrap("Client added")));
            }
            else if (cmd.equals("Show topics")) {
                String msg = "Ok, choose topic: ";
                for (String topic:topics) {
                    msg += topic + ", ";
                }
                sc.write(charset.encode(CharBuffer.wrap(msg)));
            }
            else if (cmd.equals("Hello")) {
                sc.write(charset.encode(CharBuffer.wrap("Hello")));
            }
            else if (cmd.startsWith("Subscribe ")) {
                if (topics.contains(cmd.split(" ")[1])) {
                    clientsTopics.get(sc.getLocalAddress()).add(cmd.split(" ")[1]);
                    sc.write(charset.encode(CharBuffer.wrap("Ok, added")));
                    System.out.println("I sent: \"Ok, added\"");
                }
                sc.write(charset.encode(CharBuffer.wrap("No such topic")));
                System.out.println("I sent: \"No such topic\"");
            }
            else if (cmd.startsWith("Unsubscribe ")) {
                if (clientsTopics.get(sc.getLocalAddress()).remove(cmd.split(" ")[1])) {
                    sc.write(charset.encode(CharBuffer.wrap("Ok, deleted")));
                    System.out.println("I sent: \"Ok, deleted\"");
                }
                sc.write(charset.encode(CharBuffer.wrap("No such topic subscribed")));
                System.out.println("I sent: \"No such topic subscribed\"");
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
                Set<SelectionKey> keys = sele.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                sc.write(charset.encode(CharBuffer.wrap("News sent")));
                System.out.println("I sent: \"News sent\"");
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (clientsTopics.get(key).contains(cmd.split(" ")[2])) {
                        SocketChannel socketCh = (SocketChannel) key.channel();
                        socketCh.write(charset.encode(CharBuffer.wrap(cmd)));
                        System.out.println("I sent news: \""+cmd.split("")[3]+"...\"");
                    }
                }
            }
            else if (cmd.equals("Bye")) {
                sc.write(charset.encode(CharBuffer.wrap("Bye")));
                System.out.println("Serwer: mówię \"Bye\" do klienta ...\n\n");

                sc.close();
                sc.socket().close();
            }
            else
                sc.write(charset.encode(CharBuffer.wrap("Nie rozumiem: "+reqString)));

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
