package zad1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {

        String text = "";
        SocketChannel channel = null;
        String server = "localhost";
        int serverPort = 12345;
        JTextArea respondArea = new JTextArea();


        try {
            channel = SocketChannel.open();

            channel.configureBlocking(false);

            Selector sele = Selector.open();

            //channel.register(sele, SelectionKey.OP_ACCEPT);

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


        System.out.println("Klient: wysyłam - New client");

        channel.write(charset.encode("New client\n"));

        SocketChannel finalChannel = channel;
        SocketChannel finalChannel1 = channel;
        SwingUtilities.invokeLater(() -> {
            JFrame jFrame = new JFrame("Client");
            jFrame.setSize(400, 300);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(4, 2, 10, 10));

            JTextField textSub = new JTextField();
            mainPanel.add(textSub);

            JButton sub = new JButton("<- Subscribe");
            sub.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    inBuf.clear();
                    String topic = textSub.getText();
                    try {
                        finalChannel.write(charset.encode("Subscribe: "+topic+"\n"));
                        System.out.println("Klient: piszę \"Subscribe: "+topic+"\\n\"");
                        inBuf.flip();
                        finalChannel1.read(inBuf);

                        CharBuffer cb = charset.decode(inBuf);

                        String odSerwera = cb.toString();

                        System.out.println("Klient: serwer właśnie odpisał ... \n" + odSerwera);
                        respondArea.setText(odSerwera+"\n");

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            mainPanel.add(sub);

            JTextField textUnsub = new JTextField();
            mainPanel.add(textUnsub);

            JButton unsub = new JButton("<- Unsubscribe");
            unsub.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String topic = textUnsub.getText();
                    try {
                        //text += "Unsubscribe: "+topic+"\n";
                        finalChannel.write(charset.encode("Unsubscribe: "+topic+"\n"));
                        System.out.println("Klient: piszę \"Unsubscribe: "+topic+"\\n\"");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            mainPanel.add(unsub);

            mainPanel.add(new JLabel("Od serwera: "));

            respondArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(respondArea);
            mainPanel.add(scrollPane);

            jFrame.add(mainPanel);
            jFrame.setVisible(true);
        });


        while (true) {
            String prefix = "";
            inBuf.clear();
            int readBytes = channel.read(inBuf);

            if (readBytes == 0) {
                continue;
            }
            else if (readBytes == -1) {
                continue;
            }

            else {
                System.out.println("coś jest od serwera");
                inBuf.flip();

                cbuf = charset.decode(inBuf);

                String odSerwera = cbuf.toString();

                System.out.println("Klient: serwer właśnie odpisał ... \n" + odSerwera);
                respondArea.setText(odSerwera+"\n");
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

