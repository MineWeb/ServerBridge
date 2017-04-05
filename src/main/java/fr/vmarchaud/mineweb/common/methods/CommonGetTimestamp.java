package fr.vmarchaud.mineweb.common.methods;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;

@MethodHandler
public class CommonGetTimestamp implements IMethod{

	@Override
	public Object execute(ICore instance, Object... inputs) {
		return System.currentTimeMillis();
	}

}
