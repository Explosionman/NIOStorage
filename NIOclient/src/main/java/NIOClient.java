import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class NIOClient {
    private static boolean isActive = true;
    private static RandomAccessFile rf;
    static final String HOST = "localhost";
    static final int PORT = 8189;
    static String name;

    public static void main(String[] args) throws IOException {
        //создаём канал
        SocketChannel channel = SocketChannel.open();
        // делаем его не блокирующим
        channel.configureBlocking(false);
        //делаем заготовку для канала, пустой селектор
        Selector selector = Selector.open();
        //сдруживаем селектор и канал, просим ввести имя пользователя
        Scanner sc = new Scanner(System.in);
        channel.register(selector, SelectionKey.OP_CONNECT, sc.nextLine());
        //подключаем канал к сети
        channel.connect(new InetSocketAddress(HOST, PORT));

        while (isActive) {
            selector.select();
            System.out.println("Введите номер желаемого действия: ");
            System.out.println("1 - скачать файл с сервера");
            System.out.println("2 - загрузить файл на сервер");
            System.out.println("0 - выйти");
            String command = sc.nextLine();
            switch (command) {
                case ("1"):
                    System.out.println("Вы выбрали вариант: скачать файл");
                    break;
                case ("2"):
                    System.out.println("Вы выбрали вариант:  отправить файл");
                    System.out.println("Укажите путь к файлу для передачи");
                    String path = sc.nextLine();
                    sendFileOnServer(channel, path);

                case ("0"):
                    System.out.println("Завершаем работу");
                    channel.close();
                    System.exit(1);
            }
        }
    }

    public static void sendFileOnServer(SocketChannel channel, String path) throws IOException {
        Path pathToFile = Paths.get(path);
        FileChannel fileChannel = FileChannel.open(pathToFile, StandardOpenOption.WRITE);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }
        fileChannel.close();
        System.out.println("Файл доставлен");
        channel.close();
    }
}
