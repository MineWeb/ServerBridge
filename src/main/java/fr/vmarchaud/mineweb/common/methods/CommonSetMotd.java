package fr.vmarchaud.mineweb.common.methods;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;

@MethodHandler(inputs = 1, types = { String.class })
public class CommonSetMotd implements IMethod {

	@Override
	public Object execute(ICore instance, Object... inputs) {
		String motd = (String) inputs[0];
		instance.config().setMotd(motd);
		instance.config().save(instance);
		return true;
	}

}
