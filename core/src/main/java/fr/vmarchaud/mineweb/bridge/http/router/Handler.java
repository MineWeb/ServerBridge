package fr.vmarchaud.mineweb.bridge.http.router;

public interface Handler<R, T> {
    R handle(T input) throws Exception;
}
