import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Publisher {

    public static void main(String[] args) throws IOException {

        SocketChannel channel = null;
        String server = "localhost";
        int serverPort = 12345;

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(server, serverPort));

            System.out.print("Publisher: connecting to the server ...");

            while (!channel.finishConnect()) {
            }

        } catch (UnknownHostException exc) {
            System.err.println("Unknown host " + server);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        System.out.println("\nPublisher: connected to the server ...");

        Charset charset = Charset.forName("ISO-8859-2");
        Scanner scanner = new Scanner(System.in);

        int bufferSize = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(bufferSize);
        CharBuffer cbuf = null;

        System.out.println("Publisher: sending - Hello");

        channel.write(charset.encode("Hello\n"));

        while (true) {
            inBuf.clear();
            int readBytes = channel.read(inBuf);

            if (readBytes > 0) {
                inBuf.flip();
                cbuf = charset.decode(inBuf);
                String fromServer = cbuf.toString();

                System.out.println("Publisher: server just replied ... " + fromServer);
                cbuf.clear();

                if (fromServer.equals("Bye")) break;
            }

            if (System.in.available() > 0) {
                String input = scanner.nextLine();
                cbuf = CharBuffer.wrap(input + "\n");
                ByteBuffer outBuf = charset.encode(cbuf);
                channel.write(outBuf);
                System.out.println("Publisher: writing " + input);
            }
        }

        scanner.close();
    }
}