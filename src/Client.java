import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {

        SocketChannel channel = null;
        String server = "localhost";
        int serverPort = 12345;
        JTextArea respondArea = new JTextArea();

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.connect(new InetSocketAddress(server, serverPort));

            System.out.print("Client: connecting to the server ...");

            while (!channel.finishConnect()) {
            }

        } catch (UnknownHostException exc) {
            System.err.println("Unknown host " + server);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        System.out.println("\nClient: connected to the server ...");

        Charset charset = Charset.forName("ISO-8859-2");
        Scanner scanner = new Scanner(System.in);

        int bufferSize = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(bufferSize);
        CharBuffer cbuf = null;

        System.out.println("Client: sending - New client");

        channel.write(charset.encode("New client\n"));

        final SocketChannel finalChannel = channel;
        
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
                    String topic = textSub.getText();
                    try {
                        finalChannel.write(charset.encode("Subscribe: " + topic + "\n"));
                        System.out.println("Client: writing \"Subscribe: " + topic + "\\n\"");
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
                        finalChannel.write(charset.encode("Unsubscribe: " + topic + "\n"));
                        System.out.println("Client: writing \"Unsubscribe: " + topic + "\\n\"");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            mainPanel.add(unsub);

            mainPanel.add(new JLabel("From server: "));

            respondArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(respondArea);
            mainPanel.add(scrollPane);

            jFrame.add(mainPanel);
            jFrame.setVisible(true);
        });

        while (true) {
            inBuf.clear();
            int readBytes = channel.read(inBuf);

            if (readBytes > 0) {
                System.out.println("something received from server");
                inBuf.flip();
                cbuf = charset.decode(inBuf);
                String fromServer = cbuf.toString();

                System.out.println("Client: server just replied ... \n" + fromServer);
                respondArea.setText(fromServer + "\n");
                cbuf.clear();

                if (fromServer.equals("Bye")) break;
            }

            if (System.in.available() > 0) {
                String input = scanner.nextLine();
                cbuf = CharBuffer.wrap(input + "\n");
                ByteBuffer outBuf = charset.encode(cbuf);
                channel.write(outBuf);
                System.out.println("Client: writing " + input);
            }
        }

        scanner.close();
    }
}