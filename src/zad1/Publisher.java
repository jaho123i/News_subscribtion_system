package zad1;

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

            System.out.print("Klient: łączę się z serwerem ...");

            while (!channel.finishConnect()) {

            }

        } catch(UnknownHostException exc) {
            System.err.println("Uknown host " + server);
            // ...
        } catch(Exception exc) {
            exc.printStackTrace();
            // ...
        }

        System.out.println("\nKlient: jestem połączony z serwerem ...");

        Charset charset  = Charset.forName("ISO-8859-2");
        Scanner scanner = new Scanner(System.in);

        // Alokowanie bufora bajtowego
        // allocateDirect pozwala na wykorzystanie mechanizmów sprzętowych
        // do przyspieszenia operacji we/wy
        // Uwaga: taki bufor powinien być alokowany jednokrotnie
        // i wielokrotnie wykorzystywany w operacjach we/wy
        int rozmiar_bufora = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(rozmiar_bufora);
        CharBuffer cbuf = null;


        System.out.println("Klient: wysyłam - Hello");

        channel.write(charset.encode("Hello\n"));

        while (true) {

            inBuf.clear();
            int readBytes = channel.read(inBuf);

            if (readBytes == 0) {

                continue;

            }
            else if (readBytes == -1) {

                continue;
            }
            else {
                //System.out.println("coś jest od serwera");
                inBuf.flip();

                cbuf = charset.decode(inBuf);

                String odSerwera = cbuf.toString();

                System.out.println("Klient: serwer właśnie odpisał ... " + odSerwera);
                cbuf.clear();

                if (odSerwera.equals("Bye")) break;
            }

            String input = scanner.nextLine();
            cbuf = CharBuffer.wrap(input + "\n");
            ByteBuffer outBuf = charset.encode(cbuf);
            channel.write(outBuf);

            System.out.println("Klient: piszę " + input);
        }

        scanner.close();

    }
}

