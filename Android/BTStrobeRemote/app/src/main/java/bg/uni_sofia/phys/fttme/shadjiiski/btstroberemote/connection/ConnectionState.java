package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection;

/**
 * Created by shadjiiski on 28.03.15.
 */
public enum ConnectionState {
    DISCONNECTED{
        @Override
        public String toString() {
            return "disconnected";
        }
    },
    CONNECTING{
        @Override
        public String toString() {
            return "connecting";
        }
    },
    CONNECTED{
        @Override
        public String toString() {
            return "connected";
        }
    }
}
