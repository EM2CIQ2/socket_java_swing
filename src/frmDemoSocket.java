import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener

public class frmDemoSocket {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JTextField txtPortServer;
    private JTextField txtIPServer;
    private JButton btnListen;
    private JTextField txtPesanDiterimaServer;
    private JTextField txtPortClient;
    private JTextField txtAlamatIPTujuan;
    private JTextField txtPesanDikirimKeServer;
    private JButton btnKirim;

    public frmDemoSocket(){
        btnListen.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try {
                    new Thread(() -> {
                        try {
                            EchoServer(txtIPServer.getText(), Integer.parseInt(txtPortServer.getText()));
                        } catch (IOException ioException){
                            ioException.printStackTrace();
                        }
                    }).start();
                } catch (Exception ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });

        btnKirim.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try {
                    AtomicInteger messageWritten = new AtomicInteger(0);
                    AtomicInteger messageRead = new AtomicInteger(0);

                    EchoClient(txtAlamatIPTujuan.getText(), Integer.parseInt(txtPortClient.getText()), txtPesanDikirimKeServer.getText(), messageWritten, messageRead);
                } catch (Exception ex){
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame gui = new JFrame("Socket Java");
        gui.setContentPane(new frmDemoSocket().panel1);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.pack();
        gui.setVisible(true);
    }

    // Socket Client Helper
    public void EchoClient(String host, int port, final String message, final AtomicInteger messageWritten, final AtomicInteger messageRead) throws IOException {
        AsynchronousSocketChannel sockChannel = AsynchronousSocketChannel.open();

        sockChannel.connect(new InetSocketAddress(host, port), sockChannel, new CompletionHandler<Void, AsynchronousSocketChannel >(){
            @Override
            public void completed(Void result, AsynchronousSocketChannel channel){
                startRead(channel,messageRead);
                startWrite(channel,message,messageWritten);
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel){
                System.out.println("Fail to connect to server");
            }
        });
    }

    private void startRead(final AsynchronousSocketChannel sockChannel, final AtomicInteger messageRead){
        final ByteBuffer buf = ByteBuffer.allocate(2048);

        sockChannel.read(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>(){
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel){
                messageRead.getAndIncrement();
                System.out.println("Read message:"+new String(buf.array()));
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel){
                System.out.println("Fail to read message from server");
            }
        });
    }

    private void startWrite(final AsynchronousSocketChannel sockChannel, final String message, final AtomicInteger messageWritten){
        ByteBuffer buf = ByteBuffer.allocate(2048);
        buf.put(message.getBytes());
        buf.flip();
        messageWritten.getAndIncrement();
        sockChannel.write(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>(){
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel){
                // PASS
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel){
                System.out.println("Fail to write message to server");
            }
        });
    }
}
