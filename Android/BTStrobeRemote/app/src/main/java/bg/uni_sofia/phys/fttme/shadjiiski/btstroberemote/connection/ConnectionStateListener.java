package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection;

/**
 * Created by shadjiiski on 28.03.15.
 */
public interface ConnectionStateListener {
    public void connectionStateChanged(BTConnectionHandler source, ConnectionState oldState, ConnectionState newState);
}
