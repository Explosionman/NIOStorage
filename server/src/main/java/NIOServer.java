import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class NIOServer implements Runnable {
    private ServerSocketChannel channel;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(8182);
    private int clientID = 1;

    public NIOServer() throws IOException {
        channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(8189));
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("server started");
            while (channel.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        String clientName = "Client №" + clientID;
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ, clientName);
                        channel.write(ByteBuffer.wrap(("Hello! " + clientName).getBytes()));
                    }
                    if (key.isReadable()) {
                        Path path = Paths.get("./server/");
                        FileChannel fileChannel = FileChannel.open(path);
                        ByteBuffer buffer = ByteBuffer.allocate(8192);
                        while(fileChannel.read(buffer) > 0) {
                            buffer.flip();

                        }
                        fileChannel.close();
                        System.out.println("File Sent");
                        channel.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
