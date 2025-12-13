package fr.vmarchaud.mineweb.bridge.api;

public interface IMethod {
    Object execute(ICore core, Object... inputs) throws Exception;
}
