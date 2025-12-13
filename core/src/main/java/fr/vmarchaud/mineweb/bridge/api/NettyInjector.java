package fr.vmarchaud.mineweb.bridge.api;

public interface NettyInjector {
    void inject() throws Exception;
    void close();
}
