package kg.apc.jmeter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Map;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * @author undera
 */
public class PerfMonWorker {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private int tcpPort = 4444;
    private int udpPort = 4444;
    private int exitCode = -1;
    private boolean isFinished = true;
    private final Selector selector;
    private Map<SelectableChannel, Object> connections;

    public PerfMonWorker() throws IOException {
        this.selector = Selector.open();
    }

    public void setTCPPort(int parseInt) {
        tcpPort = parseInt;
    }

    public void setUDPPort(int parseInt) {
        udpPort = parseInt;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void processCommands() throws IOException {
        if (isFinished)
            throw new IOException("Worker finished");
        
        if (!selector.isOpen())
            throw new IOException("Selector is closed");
            
        log.debug("Selecting");
        this.selector.select();
        log.debug("Selected");

        // wakeup to work on selected keys
        Iterator keys = this.selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = (SelectionKey) keys.next();

            // this is necessary to prevent the same key from coming up 
            // again the next time around.
            keys.remove();

            if (!key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                this.accept(key);
            } else if (key.isReadable()) {
                this.read(key);
            } else if (key.isWritable()) {
                this.write(key);
            }
        }
    }

    public int getExitCode() {
        return exitCode;
    }

    public void startAcceptingCommands() throws IOException {
        isFinished=false;
        if (tcpPort > 0) {
            log.debug("Binding TCP to "+tcpPort);
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);

            serverChannel.socket().bind(new InetSocketAddress(tcpPort));
            serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        }

        if (udpPort > 0) {
            log.debug("Binding UDP to "+udpPort);
            DatagramChannel udp = DatagramChannel.open();
            udp.configureBlocking(false);
            udp.register(selector, SelectionKey.OP_READ);
            udp.connect(new InetSocketAddress(udpPort));
        }
    }

    private void accept(SelectionKey key) {
        SelectableChannel channel = key.channel();
        connections.put(channel, null);
    }

    private void read(SelectionKey key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void write(SelectionKey key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}